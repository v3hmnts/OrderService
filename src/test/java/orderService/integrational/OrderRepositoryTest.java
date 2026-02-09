package orderService.integrational;


import jakarta.transaction.Transactional;
import orderService.TestcontainersConfig;
import orderService.entity.Item;
import orderService.entity.Order;
import orderService.entity.OrderItem;
import orderService.entity.enums.OrderStatus;
import orderService.repository.ItemRepository;
import orderService.repository.OrderItemRepository;
import orderService.repository.OrderRepository;
import orderService.specification.OrderSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(TestcontainersConfig.class)
@DisplayName("[integration] OrderRepository")
class OrderRepositoryTest {

    @Autowired
    protected PostgreSQLContainer<?> postgreSQLContainer;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    @AfterEach
    void afterEach() {

    }


    @BeforeEach
    void beforeEach() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        orderItemRepository.deleteAll();
    }

    @Test
    void addOrderShouldReturnOrder() {
        // Arrange
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(new BigDecimal("10"));

        Item item2 = new Item();
        item2.setName("Item2");
        item2.setPrice(new BigDecimal("20"));

        Order order = new Order();
        order.setUserId(1L);
        order.addItem(item1, 2);
        order.addItem(item2, 1);
        order.updateTotalPrice();
        order.setOrderStatus(OrderStatus.CONFIRMED);

        // Act
        Order result = orderRepository.save(order);

        // Assert

        assertThat(result.getOrderStatus()).isEqualTo(order.getOrderStatus());
        assertThat(result.getUserId()).isEqualTo(order.getUserId());
        assertThat(result.getTotalPrice()).isEqualTo(order.getTotalPrice());
        assertThat(result.getOrderItemList().size()).isEqualTo(order.getOrderItemList().size());
        assertThat(result.getOrderItemList().stream().map(OrderItem::getQuantity).mapToInt(Integer::intValue).sum()).isEqualTo(3);


    }

    @Test
    void findAllWithConfirmedStatusShouldReturnOrderListWithSizeEqualsTwo() {
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(new BigDecimal(10));

        Item item2 = new Item();
        item2.setName("Item2");
        item2.setPrice(new BigDecimal(20));

        Order order = new Order();
        order.setUserId(1L);
        order.addItem(item1, 2);
        order.updateTotalPrice();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);


        Order order2 = new Order();
        order2.setUserId(2L);
        order2.addItem(item2, 2);
        order2.updateTotalPrice();
        order2.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order2);

        Specification<Order> orderSpecification = OrderSpecification.hasStatus(OrderStatus.CONFIRMED);

        // Act
        List<Order> result = orderRepository.findAll(orderSpecification);

        // Assert
        assertThat(result.size()).isEqualTo(2);

    }

    @Test
    void findAllWithConfirmedStatusShouldReturnOrderListWithSizeEqualsOne() {
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(new BigDecimal(10));

        Item item2 = new Item();
        item2.setName("Item2");
        item2.setPrice(new BigDecimal(20));

        Order order = new Order();
        order.setUserId(1L);
        order.addItem(item1, 2);
        order.updateTotalPrice();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);


        Order order2 = new Order();
        order2.setUserId(2L);
        order2.addItem(item2, 2);
        order2.updateTotalPrice();
        order2.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order2);

        Specification<Order> orderSpecification = OrderSpecification.hasStatus(OrderStatus.CONFIRMED);

        // Act
        List<Order> result = orderRepository.findAll(orderSpecification);

        // Assert
        assertThat(result.size()).isEqualTo(1);

    }

    @Test
    @Transactional
    void addItemThatWasAlreadyInOrderShouldUpdateQuantity() {
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(new BigDecimal(10));
        itemRepository.save(item1);

        Order order = new Order();
        order.setUserId(1L);
        order.addItem(item1, 2);
        order.updateTotalPrice();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        order.addItem(item1, 10);
        order.updateTotalPrice();
        orderRepository.save(order);


        // Act
        Order result = orderRepository.findById(order.getId()).orElseThrow();

        // Assert
        assertThat(result.getOrderItemList().size()).isEqualTo(1);
        assertThat(result.getTotalPrice()).isEqualTo(new BigDecimal(120));
        assertThat(result.getOrderItemList().getFirst().getQuantity()).isEqualTo(12);

    }

    @Test
    @Transactional
    void removeItemFromOrderItemQunatityShouldDecrease() {
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(new BigDecimal(10));
        Item savedItem = itemRepository.save(item1);

        Order order = new Order();
        order.setUserId(1L);
        order.addItem(item1, 20);
        order.updateTotalPrice();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        order.removeItem(item1, 5);
        order.updateTotalPrice();


        // Act
        Order result = orderRepository.findById(order.getId()).orElseThrow();

        // Assert
        assertThat(result.getOrderItemList().size()).isEqualTo(1);
        assertThat(result.getTotalPrice()).isEqualTo(new BigDecimal(150));
        assertThat(result.getOrderItemList().getFirst().getQuantity()).isEqualTo(15);

    }

    @Test
    @Transactional
    void removeMoreItemQuantityThanItemQunatityInOrderShouldThrowException() {
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(new BigDecimal(10));
        Item savedItem = itemRepository.save(item1);

        Order order = new Order();
        order.setUserId(1L);
        order.addItem(item1, 1);
        order.updateTotalPrice();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);


        // Act & Assert
        assertThrows(RuntimeException.class, () -> order.removeItem(item1, 5));

    }

    @Test
    @Transactional
    void removeUnexistentItemFromOrderShouldThrowException() {
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setPrice(new BigDecimal(10));
        Item savedItem = itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Item2");
        item2.setPrice(new BigDecimal(10));
        Item savedItem2 = itemRepository.save(item2);

        Order order = new Order();
        order.setUserId(1L);
        order.addItem(item1, 1);
        order.updateTotalPrice();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        order.removeItem(savedItem2, 5);
        order.updateTotalPrice();

        // Act
        Order result = orderRepository.findById(order.getId()).orElseThrow();

        // Assert
        assertThat(result.getOrderItemList().size()).isEqualTo(1);
        assertThat(result.getTotalPrice()).isEqualTo(new BigDecimal(10));
        assertThat(result.getOrderItemList().getFirst().getQuantity()).isEqualTo(1);

    }

}
