package orderService.service;

import orderService.TestcontainersConfig;
import orderService.dto.OrderDto;
import orderService.dto.OrderItemDto;
import orderService.dto.PageDto;
import orderService.entity.Item;
import orderService.entity.Order;
import orderService.entity.enums.OrderStatus;
import orderService.repository.ItemRepository;
import orderService.repository.OrderItemRepository;
import orderService.repository.OrderRepository;
import orderService.service.impl.OrderServiceImpl;
import orderService.specification.OrderFilterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(TestcontainersConfig.class)
class OrderServiceTest {
    @Autowired
    protected PostgreSQLContainer<?> postgreSQLContainer;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderServiceImpl orderServiceImpl;

    @BeforeEach
    void beforeEach() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        orderItemRepository.deleteAll();
    }

    @Test
    void getOrderByIdShouldReturnOrder() {
        // Arrange
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(10d);

        Item item2 = new Item();
        item2.setName("Item2");
        item2.setPrice(20d);

        Order order = new Order();
        order.setUserId(1L);
        order.addItem(item1, 2);
        order.addItem(item2, 1);
        order.updatePrice();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);

        // Act
        OrderDto result = orderServiceImpl.findById(savedOrder.getId());

        // Assert

        assertThat(result.getId()).isEqualTo(savedOrder.getId());
        assertThat(result.getOrderStatus()).isEqualTo(savedOrder.getOrderStatus());
        assertThat(result.getDeleted()).isEqualTo(savedOrder.isDeleted());
        assertThat(result.getTotalPrice()).isEqualTo(savedOrder.getTotalPrice());
        assertThat(result.getOrderItemList().size()).isEqualTo(savedOrder.getOrderItemList().size());

    }

    @Test
    void deleteOrderByIdShouldReturnDeletedOrder() {
        // Arrange
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(10d);

        Item item2 = new Item();
        item2.setName("Item2");
        item2.setPrice(20d);

        Order order = new Order();
        order.setUserId(1L);
        order.addItem(item1, 2);
        order.addItem(item2, 1);
        order.updatePrice();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);

        // Act
        orderServiceImpl.deleteById(savedOrder.getId());

        OrderDto result = orderServiceImpl.findById(savedOrder.getId());
        // Assert

        assertThat(result.getId()).isEqualTo(savedOrder.getId());
        assertTrue(result.getDeleted());
        assertThat(result.getOrderStatus()).isEqualTo(savedOrder.getOrderStatus());
        assertThat(result.getTotalPrice()).isEqualTo(savedOrder.getTotalPrice());
        assertThat(result.getOrderItemList().size()).isEqualTo(savedOrder.getOrderItemList().size());

    }

    @Test
    void findAllFilteredAndPageableShouldReturnPageOrder() {
        // Arrange
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(10d);

        Order order = new Order();
        order.setUserId(1L);
        order.addItem(item1, 2);
        order.updatePrice();
        order.setOrderStatus(OrderStatus.FAILED);
        Order savedOrder = orderRepository.save(order);

        Item item2 = new Item();
        item2.setName("Item2");
        item2.setPrice(20d);

        Order order2 = new Order();
        order2.setUserId(1L);
        order2.addItem(item2, 1);
        order2.updatePrice();
        order2.setOrderStatus(OrderStatus.PAYED);
        Order savedOrder2 = orderRepository.save(order2);

        OrderFilterRequest filterRequest = new OrderFilterRequest().toBuilder()
                .createdBefore(LocalDateTime.now().plusHours(4).toInstant(ZoneOffset.ofHours(0)))
                .createdAfter(LocalDateTime.now().minusHours(4).toInstant(ZoneOffset.ofHours(0)))
                .orderStatus(OrderStatus.FAILED)
                .build();

        // Act
        PageDto<OrderDto> result = orderServiceImpl.findAll(filterRequest, Pageable.ofSize(10));

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(1L);

    }

    @Test
    void updateOrderByIdShouldReturnUpdatedOrderDto() {
        // Arrange
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(10d);

        Item item2 = new Item();
        item2.setName("Item2");
        item2.setPrice(20d);

        Order order = new Order();
        order.setUserId(1L);
        order.addItem(item1, 2);
        order.addItem(item2, 3);
        order.updatePrice();
        order.setOrderStatus(OrderStatus.FAILED);
        Order savedOrder = orderRepository.save(order);

        OrderDto updateOrderDto = orderServiceImpl.findById(savedOrder.getId());
        updateOrderDto.setOrderStatus(OrderStatus.CONFIRMED);
        updateOrderDto.setDeleted(true);
        OrderItemDto orderItemDto1 = updateOrderDto.getOrderItemList().get(0);
        OrderItemDto orderItemDto2 = updateOrderDto.getOrderItemList().get(1);
        OrderItemDto newOrderItemDto1 = new OrderItemDto(orderItemDto1.id(), orderItemDto1.itemDto(), 5, orderItemDto1.deleted());
        OrderItemDto newOrderItemDto2 = new OrderItemDto(orderItemDto2.id(), orderItemDto2.itemDto(), 10, orderItemDto2.deleted());
        updateOrderDto.getOrderItemList().set(0, newOrderItemDto1);
        updateOrderDto.getOrderItemList().set(1, newOrderItemDto2);

        // Act
        OrderDto result = orderServiceImpl.updateOrderById(savedOrder.getId(), updateOrderDto);

        // Assert
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(result.getTotalPrice()).isEqualTo(item1.getPrice()*newOrderItemDto1.quantity()+item2.getPrice()*newOrderItemDto2.quantity());
        assertTrue(result.getDeleted());

    }

    @Test
    void findByUserIdShouldReturnUserOrders() {
        // Arrange
        final Long userId=1L;

        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(10d);

        Item item2 = new Item();
        item2.setName("Item2");
        item2.setPrice(20d);

        Order order = new Order();
        order.setUserId(userId);
        order.addItem(item1, 2);
        order.updatePrice();
        order.setOrderStatus(OrderStatus.FAILED);
        Order savedOrder = orderRepository.save(order);

        Item item3 = new Item();
        item3.setName("Item3");
        item3.setPrice(40d);

        Order order2 = new Order();
        order2.setUserId(userId);
        order2.addItem(item2, 1);
        order2.updatePrice();
        order2.setOrderStatus(OrderStatus.PAYED);
        Order savedOrder2 = orderRepository.save(order2);

        Order order3 = new Order();
        order3.setUserId(userId);
        order3.addItem(item3, 10);
        order3.updatePrice();
        order3.setOrderStatus(OrderStatus.CONFIRMED);
        Order savedOrder3 = orderRepository.save(order3);

        // Act
        List<OrderDto> result = orderServiceImpl.findAllByUserId(userId);

        // Assert
        assertThat(result.stream().filter(orderDto -> !orderDto.getDeleted()).count()).isEqualTo(3);

    }

}