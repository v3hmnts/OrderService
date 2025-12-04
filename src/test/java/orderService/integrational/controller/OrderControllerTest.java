package orderService.integrational.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import orderService.TestcontainersConfig;
import orderService.dto.*;
import orderService.entity.enums.OrderStatus;
import orderService.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static orderService.util.TestJwtAuthenticationTokenSupplier.getJwtAuthenticationTokenWithAdminRole;
import static orderService.util.TestJwtAuthenticationTokenSupplier.getJwtAuthenticationTokenWithUserRole;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import({TestcontainersConfig.class})
@EnableWireMock({
        @ConfigureWireMock(
                name = "localhost",
                port = 8080)
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("[integration] OrderController")
public class OrderControllerTest {

    private final Long DEFAULT_USER_ID = 1L;
    private final String DEFAULT_USER_NAME = "Nam";
    private final String DEFAULT_USER_SURNAME = "Surn";
    private final Date DEFAULT_USER_BIRTH_DATE = Date.valueOf("1999-09-09");
    private final String DEFAULT_USER_EMAIL = "test@mail.com";
    private final Boolean DEFAULT_USER_DELETED = false;
    private final Long EXISTING_ORDER_ID = 1L;
    private final Long EXISTING_ORDER_USER_ID = DEFAULT_USER_ID;
    private final OrderStatus EXISTING_ORDER_STATUS = OrderStatus.CONFIRMED;
    private final BigDecimal EXISTING_ORDER_TOTAL_PRICE = new BigDecimal("1400");
    @Autowired
    protected PostgreSQLContainer<?> postgreSQLContainer;
    @Autowired
    protected MockMvcTester mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;

    private UserDto defaultUser;

    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void beforeEach() {

        defaultUser = new UserDto(
                DEFAULT_USER_ID,
                DEFAULT_USER_NAME,
                DEFAULT_USER_SURNAME,
                DEFAULT_USER_BIRTH_DATE,
                DEFAULT_USER_EMAIL,
                DEFAULT_USER_DELETED
        );
        stubFor(get(urlEqualTo("/api/v1/users/1")).willReturn(okJson(asJsonString(defaultUser))));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void createNewOrderRequestShouldReturnCreatedOrderDtoWithUserDto() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        OrderCreateRequestDto orderCreateRequestDto = new OrderCreateRequestDto().toBuilder()
                .userId(DEFAULT_USER_ID)
                .orderItemList(List.of(new OrderItemCreateRequestDto(1L, 5)))
                .build();


        // Act
        MvcTestResult result = mockMvc.post()
                .uri("/api/v1/orders")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(orderCreateRequestDto))
                .exchange();

        OrderDto orderDto = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);

        // Assert
        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertEquals(orderDto.getUser().id(), DEFAULT_USER_ID);
        assertEquals(orderDto.getUser().name(), DEFAULT_USER_NAME);
        assertEquals(orderDto.getUser().surname(), DEFAULT_USER_SURNAME);
        assertEquals(1, orderDto.getOrderItemList().size());
        assertEquals(orderCreateRequestDto.getOrderItemList().getFirst().quantity(), orderDto.getOrderItemList().getFirst().quantity());
        assertEquals(0, new BigDecimal("500").compareTo(orderDto.getTotalPrice()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void createNewOrderRequestWithNonExistentItemShouldReturnNotFound() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        final Long NON_EXISTENT_ITEM_ID = 999L;
        OrderCreateRequestDto orderCreateRequestDto = new OrderCreateRequestDto().toBuilder()
                .userId(DEFAULT_USER_ID)
                .orderItemList(List.of(new OrderItemCreateRequestDto(NON_EXISTENT_ITEM_ID, 5)))
                .build();

        // Act
        MvcTestResult result = mockMvc.post()
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(orderCreateRequestDto))
                .uri("/api/v1/orders")
                .exchange();

        // Assert
        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void createNewOrderRequestWithNotValidRequestDoShouldReturnBadRequest() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        OrderCreateRequestDto orderCreateRequestDto = new OrderCreateRequestDto().toBuilder()
                .orderItemList(List.of(new OrderItemCreateRequestDto(1L, 5)))
                .build();


        // Act
        MvcTestResult result = mockMvc.post()
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(orderCreateRequestDto))
                .uri("/api/v1/orders")
                .exchange();

        // Assert
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void updateOrderWithValidUpdateRequestShouldReturnUpdateOrderDto() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        OrderUpdateRequestDto orderUpdateRequestDto = new OrderUpdateRequestDto(
                "CANCELED",
                List.of(new OrderItemCreateRequestDto(1L, 2)),
                false
        );

        // Act
        MvcTestResult result = mockMvc.put()
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(orderUpdateRequestDto))
                .uri("/api/v1/orders/{orderId}", EXISTING_ORDER_ID)
                .exchange();

        OrderDto updatedOrderDto = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);

        // Assert
        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(OrderStatus.CANCELED).isEqualTo(updatedOrderDto.getOrderStatus());
        assertEquals(0, updatedOrderDto.getTotalPrice().compareTo(new BigDecimal("200")));
        assertEquals(1, updatedOrderDto.getOrderItemList().size());
    }

    @Test
    @WithMockUser()
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void updateOrderWithUserRoleShouldReturnForbidden() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithUserRole());
        OrderUpdateRequestDto orderUpdateRequestDto = new OrderUpdateRequestDto(
                "CANCELED",
                List.of(new OrderItemCreateRequestDto(1L, 2)),
                false
        );

        // Act
        MvcTestResult result = mockMvc.put()
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(orderUpdateRequestDto))
                .uri("/api/v1/orders/{orderId}", EXISTING_ORDER_ID)
                .exchange();

        // Assert
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void findOrderByExistingIdShouldReturnOrderDto() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        // Act
        MvcTestResult result = mockMvc.get()
                .uri("/api/v1/orders/{orderId}", EXISTING_ORDER_ID)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        OrderDto updatedOrderDto = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);

        // Assert
        assertThat(result).hasStatus(HttpStatus.OK);
        assertEquals(EXISTING_ORDER_ID, updatedOrderDto.getId());
        assertEquals(EXISTING_ORDER_STATUS, updatedOrderDto.getOrderStatus());
        assertEquals(0, updatedOrderDto.getTotalPrice().compareTo(EXISTING_ORDER_TOTAL_PRICE));
    }

    @Test
    @WithMockUser()
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void findOrderByIdWithUserRoleAndOwnedByUserShouldReturnOrderDto() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithUserRole());
        // Act
        MvcTestResult result = mockMvc.get()
                .uri("/api/v1/orders/{orderId}", EXISTING_ORDER_ID)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        OrderDto updatedOrderDto = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);

        // Assert
        assertThat(result).hasStatus(HttpStatus.OK);
        assertEquals(EXISTING_ORDER_ID, updatedOrderDto.getId());
        assertEquals(EXISTING_ORDER_STATUS, updatedOrderDto.getOrderStatus());
        assertEquals(0, updatedOrderDto.getTotalPrice().compareTo(EXISTING_ORDER_TOTAL_PRICE));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void findOrderByIdWithNonexistentIdShouldReturnNotFound() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        final Long NONEXISTEN_ORDER_ID = 999L;
        // Act
        MvcTestResult result = mockMvc.get()
                .uri("/api/v1/orders/{orderId}", NONEXISTEN_ORDER_ID)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        // Assert
        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void findAllWithAllDataShouldReturnOrderDtoWithAllData() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        // Act
        MvcTestResult result = mockMvc.get()
                .uri("/api/v1/orders")
                .param("createdBefore", String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0))))
                .param("createdAfter", String.valueOf(LocalDateTime.now().minusHours(4).toInstant(ZoneOffset.ofHours(0))))
                .param("orderStatus", "CONFIRMED")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        PageDto<OrderDto> list = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<PageDto<OrderDto>>() {
        });

        // Assert
        assertThat(result).hasStatus(HttpStatus.OK);
        assertEquals(2, list.getTotalElements());

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void findAllWithAllDataWithGivenStatusShouldReturnOrderDtoWithAllData() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        // Act
        MvcTestResult result = mockMvc.get()
                .uri("/api/v1/orders")
                .param("orderStatus", "CANCELED")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        PageDto<OrderDto> list = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<PageDto<OrderDto>>() {
        });

        // Assert
        assertThat(result).hasStatus(HttpStatus.OK);
        assertEquals(1, list.getTotalElements());

    }

    @Test
    @WithMockUser()
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void findAllWithAllDataWithUserRoleShouldReturnForbidden() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithUserRole());
        // Act
        MvcTestResult result = mockMvc.get()
                .uri("/api/v1/orders")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        PageDto<OrderDto> list = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<PageDto<OrderDto>>() {
        });

        // Assert
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void findAllByUserIdShouldReturnOrderDto() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        final BigDecimal TOTAL_PRICE_OF_ALL_ORDERS_OF_DEFAULT_USER = new BigDecimal("2100");

        // Act
        MvcTestResult result = mockMvc.get()
                .uri("/api/v1/orders/user/{userId}", DEFAULT_USER_ID)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        List<OrderDto> list = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<OrderDto>>() {
        });

        // Assert
        assertThat(result).hasStatus(HttpStatus.OK);
        assertEquals(3, list.size());
        assertEquals(0, TOTAL_PRICE_OF_ALL_ORDERS_OF_DEFAULT_USER.compareTo(calculateTotalPriceOfOrders(list)));

    }

    @Test
    @WithMockUser()
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void findAllByUserIdWithUserRoleShouldReturnOrderDto() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithUserRole());
        final BigDecimal TOTAL_PRICE_OF_ALL_ORDERS_OF_DEFAULT_USER = new BigDecimal("2100");

        // Act
        MvcTestResult result = mockMvc.get()
                .uri("/api/v1/orders/user/{userId}", DEFAULT_USER_ID)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        List<OrderDto> list = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<OrderDto>>() {
        });

        // Assert
        assertThat(result).hasStatus(HttpStatus.OK);
        assertEquals(3, list.size());
        assertEquals(0, TOTAL_PRICE_OF_ALL_ORDERS_OF_DEFAULT_USER.compareTo(calculateTotalPriceOfOrders(list)));

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void deleteOrderByIdShouldReturnNoContent() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        // Act
        MvcTestResult result = mockMvc.delete()
                .uri("/api/v1/orders/{orderId}", EXISTING_ORDER_ID)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();


        // Assert
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        assertTrue(orderRepository.findById(EXISTING_ORDER_ID).isEmpty());

    }

    @Test
    @WithMockUser()
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void deleteOrderByIdWithUserRoleShouldReturnForbidden() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithUserRole());
        // Act
        MvcTestResult result = mockMvc.delete()
                .uri("/api/v1/orders/{orderId}", EXISTING_ORDER_ID)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();


        // Assert
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);

    }


    BigDecimal calculateTotalPriceOfOrders(List<OrderDto> orderDtos) {
        return orderDtos.stream().map(OrderDto::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add, BigDecimal::add);
    }

    BigDecimal calculateTotalPriceOfOrdersOnPage(PageDto<OrderDto> orderDtos) {
        return orderDtos.getContent().stream().map(OrderDto::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add, BigDecimal::add);
    }


}
