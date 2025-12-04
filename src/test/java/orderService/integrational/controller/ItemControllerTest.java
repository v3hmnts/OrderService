package orderService.integrational.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import orderService.TestcontainersConfig;
import orderService.config.TestConfig;
import orderService.dto.*;
import orderService.repository.ItemRepository;
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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static orderService.util.TestJwtAuthenticationTokenSupplier.getJwtAuthenticationTokenWithAdminRole;
import static orderService.util.TestJwtAuthenticationTokenSupplier.getJwtAuthenticationTokenWithUserRole;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import({TestcontainersConfig.class, TestConfig.class})
@EnableWireMock({
        @ConfigureWireMock(
                name = "localhost",
                port = 8080)
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("[integration] ItemController")
public class ItemControllerTest {
    private final Long DEFAULT_USER_ID = 1L;
    private final String DEFAULT_USER_NAME = "Nam";
    private final String DEFAULT_USER_SURNAME = "Surn";
    private final Date DEFAULT_USER_BIRTH_DATE = Date.valueOf("1999-09-09");
    private final String DEFAULT_USER_EMAIL = "test@mail.com";
    private final Boolean DEFAULT_USER_DELETED = false;
    private final Long EXISTING_ITEM_ID = 1L;
    private final String EXISTING_ITEM_NAME = "Item1";
    private final BigDecimal EXISTING_ITEM_PRICE = new BigDecimal("100");
    private final Boolean EXISTING_ITEM_DELETED = false;

    @Autowired
    protected PostgreSQLContainer<?> postgreSQLContainer;
    @Autowired
    protected MockMvcTester mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected ItemRepository itemRepository;

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
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void createNewItemShouldReturnItemDto() throws UnsupportedEncodingException, JsonProcessingException {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        ItemCreateRequestDto itemCreateRequestDto = new ItemCreateRequestDto("SuperItem", new BigDecimal("299"));

        // Act
        MvcTestResult result = mockMvc.post()
                .uri("/api/v1/items")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(itemCreateRequestDto))
                .exchange();

        ItemDto itemDto = objectMapper.readValue(result.getResponse().getContentAsString(), ItemDto.class);

        // Assert
        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertEquals(itemCreateRequestDto.name(), itemDto.name());
        assertEquals(false, itemDto.deleted());
        assertEquals(0, itemDto.price().compareTo(itemCreateRequestDto.price()));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void createNewWithNullRequestDtoItemShouldReturnBadRequest() throws UnsupportedEncodingException, JsonProcessingException {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        ItemCreateRequestDto itemCreateRequestDto = new ItemCreateRequestDto(null, null);

        // Act
        MvcTestResult result = mockMvc.post()
                .uri("/api/v1/items")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(itemCreateRequestDto))
                .exchange();

        // Assert
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void findByIdShouldReturnItemDto() throws UnsupportedEncodingException, JsonProcessingException {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());

        // Act
        MvcTestResult result = mockMvc.get()
                .uri("/api/v1/items/{itemId}", EXISTING_ITEM_ID)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        ItemDto itemDto = objectMapper.readValue(result.getResponse().getContentAsString(), ItemDto.class);

        // Assert
        assertThat(result).hasStatus(HttpStatus.OK);
        assertEquals(EXISTING_ITEM_ID, itemDto.id());
        assertEquals(EXISTING_ITEM_NAME, itemDto.name());
        assertEquals(EXISTING_ITEM_DELETED, itemDto.deleted());
        assertEquals(0, itemDto.price().compareTo(EXISTING_ITEM_PRICE));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void findByIdWithNonexistentIdShouldReturnNotFound() throws UnsupportedEncodingException, JsonProcessingException {
        // Arrange
        final Long NONESITENT_ITEM_ID = 999L;
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());

        // Act
        MvcTestResult result = mockMvc.get()
                .uri("/api/v1/items/{itemId}", NONESITENT_ITEM_ID)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        // Assert
        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void findByAllShouldReturnPageItemDto() throws UnsupportedEncodingException, JsonProcessingException {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());

        // Act
        MvcTestResult result = mockMvc.get()
                .uri("/api/v1/items")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        PageDto<ItemDto> itemDtoPageDto = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<PageDto<ItemDto>>() {
        });

        // Assert
        assertThat(result).hasStatus(HttpStatus.OK);
        assertEquals(4, itemDtoPageDto.getTotalElements());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void updateItemByIdShouldReturnItemDto() throws UnsupportedEncodingException, JsonProcessingException {
        // Arrange
        final String ITEM_NEW_NAME = "NewName";
        final BigDecimal ITEM_NEW_PRICE = new BigDecimal("55");
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(ITEM_NEW_NAME, ITEM_NEW_PRICE, false);

        // Act
        MvcTestResult result = mockMvc.put()
                .uri("/api/v1/items/{itemId}", EXISTING_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(itemUpdateDto))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        ItemDto itemDto = objectMapper.readValue(result.getResponse().getContentAsString(), ItemDto.class);

        // Assert
        assertThat(result).hasStatus(HttpStatus.OK);
        assertEquals(EXISTING_ITEM_ID, itemDto.id());
        assertEquals(ITEM_NEW_NAME, itemDto.name());
        assertEquals(false, itemDto.deleted());
        assertEquals(0, ITEM_NEW_PRICE.compareTo(itemDto.price()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void updateItemByIdWithNotValidItemNameShouldReturnBadRequest() throws UnsupportedEncodingException, JsonProcessingException {
        // Arrange
        final String ITEM_NEW_NAME = null;
        final BigDecimal ITEM_NEW_PRICE = new BigDecimal("55");
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(ITEM_NEW_NAME, ITEM_NEW_PRICE, false);

        // Act
        MvcTestResult result = mockMvc.put()
                .uri("/api/v1/items/{itemId}", EXISTING_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(itemUpdateDto))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        // Assert
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void deleteByIdShouldReturnNoContent() throws UnsupportedEncodingException, JsonProcessingException {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithAdminRole());

        // Act
        MvcTestResult result = mockMvc.delete()
                .uri("/api/v1/items/{itemId}", EXISTING_ITEM_ID)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        // Assert
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        assertTrue(itemRepository.findById(EXISTING_ITEM_ID).isEmpty());
    }

    @Test
    @WithMockUser()
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void deleteByIdWithUserRoleShouldReturnForbidden() throws UnsupportedEncodingException, JsonProcessingException {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithUserRole());

        // Act
        MvcTestResult result = mockMvc.delete()
                .uri("/api/v1/items/{itemId}", EXISTING_ITEM_ID)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        // Assert
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser()
    @Sql(scripts = {"classpath:truncate.sql", "classpath:testdata.sql"})
    void updateItemByIdWithRoleUserShouldReturnForbidden() throws UnsupportedEncodingException, JsonProcessingException {
        // Arrange
        final String ITEM_NEW_NAME = "NewName";
        final BigDecimal ITEM_NEW_PRICE = new BigDecimal("55");
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationTokenWithUserRole());
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(ITEM_NEW_NAME, ITEM_NEW_PRICE, false);

        // Act
        MvcTestResult result = mockMvc.put()
                .uri("/api/v1/items/{itemId}", EXISTING_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(itemUpdateDto))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .exchange();

        // Assert
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

}
