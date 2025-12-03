package orderService.unit.service;

import orderService.dto.*;
import orderService.entity.Item;
import orderService.entity.Order;
import orderService.entity.OrderItem;
import orderService.entity.enums.OrderStatus;
import orderService.exception.ItemNotFoundException;
import orderService.exception.OrderNotFoundException;
import orderService.mapper.OrderMapper;
import orderService.repository.ItemRepository;
import orderService.repository.OrderRepository;
import orderService.service.UserServiceClient;
import orderService.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderDto orderDto;
    private Item item;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setPrice(new BigDecimal("99.99"));

        userDto = new UserDto(1L, "User", "Surname", Date.valueOf("1990-07-08"), "test@example.com", false);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setItem(item);
        orderItem.setQuantity(2);

        order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setOrderStatus(OrderStatus.PENDING);
        order.addItem(item, 2);
        order.updateTotalPrice();

        orderDto = OrderDto.builder()
                .id(1L)
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("199.98"))
                .deleted(false)
                .orderItemList(List.of(new OrderItemDto(1L, null, 2, false)))
                .build();
    }

    @Test
    void findByIdWhenOrderExistsShouldReturnOrderDto() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userServiceClient.findUserById(1L)).thenReturn(userDto);

        // Act
        OrderDto result = orderService.findById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUser()).isEqualTo(userDto);
        verify(orderRepository).findById(1L);
        verify(orderMapper).toDto(order);
        verify(userServiceClient).findUserById(1L);
    }

    @Test
    void findByIdWhenOrderDoesNotExistShouldThrowOrderNotFoundException() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.findById(1L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("1");

        verify(orderRepository).findById(1L);
        verify(orderMapper, never()).toDto(any());
        verify(userServiceClient, never()).findUserById(anyLong());
    }

    @Test
    void createOrderWhenValidRequestShouldCreateAndReturnOrderDto() {
        // Arrange
        OrderItemCreateRequestDto itemRequest = new OrderItemCreateRequestDto(1L, 2);
        OrderCreateRequestDto requestDto = OrderCreateRequestDto.builder()
                .userId(1L)
                .orderItemList(List.of(itemRequest))
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userServiceClient.findUserById(1L)).thenReturn(userDto);

        // Act
        OrderDto result = orderService.createOrder(requestDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUser()).isEqualTo(userDto);
        verify(itemRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toDto(order);
        verify(userServiceClient).findUserById(1L);
    }

    @Test
    void createOrderWhenItemDoesNotExistShouldThrowItemNotFoundException() {
        // Arrange
        OrderItemCreateRequestDto itemRequest = new OrderItemCreateRequestDto(999L, 2);
        OrderCreateRequestDto requestDto = OrderCreateRequestDto.builder()
                .userId(1L)
                .orderItemList(List.of(itemRequest))
                .build();

        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(requestDto))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("999");

        verify(itemRepository).findById(999L);
        verify(orderRepository, never()).save(any());
        verify(orderMapper, never()).toDto(any());
    }


    @Test
    void findAllByUserIdWhenUserHasOrdersShouldReturnOrderDtos() {
        // Arrange
        List<Order> orders = List.of(order);
        List<OrderDto> orderDtos = List.of(orderDto);

        when(orderRepository.findByUserId(1L)).thenReturn(orders);
        when(orderMapper.toDtoList(orders)).thenReturn(orderDtos);
        when(userServiceClient.findUserById(1L)).thenReturn(userDto);

        // Act
        List<OrderDto> result = orderService.findAllByUserId(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser()).isEqualTo(userDto);
        verify(orderRepository).findByUserId(1L);
        verify(orderMapper).toDtoList(orders);
        verify(userServiceClient).findUserById(1L);
    }

    @Test
    void findAllByUserIdWhenUserHasNoOrdersShouldReturnEmptyList() {
        // Arrange
        when(orderRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(orderMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(userServiceClient.findUserById(1L)).thenReturn(userDto);

        // Act
        List<OrderDto> result = orderService.findAllByUserId(1L);

        // Assert
        assertThat(result).isEmpty();
        verify(orderRepository).findByUserId(1L);
        verify(orderMapper).toDtoList(Collections.emptyList());
        verify(userServiceClient).findUserById(1L);
    }

    @Test
    void deleteByIdShouldCallRepositoryDelete() {
        // Act
        orderService.deleteById(1L);

        // Assert
        verify(orderRepository).deleteById(1L);
    }


    @Test
    void updateOrderByIdWhenOrderDoesNotExistShouldThrowOrderNotFoundException() {
        // Arrange
        OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                "CONFIRMED",
                null,
                null
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrderById(1L, updateRequest))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("1");

        verify(orderRepository).findById(1L);
        verify(itemRepository, never()).findById(anyLong());
        verify(orderMapper, never()).toDto(any());
    }

    @Test
    void updateOrderByIdWhenUpdatingWithNonExistentItemShouldThrowItemNotFoundException() {
        // Arrange
        OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                null,
                List.of(new OrderItemCreateRequestDto(999L, 3)),
                null
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrderById(1L, updateRequest))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("999");

        verify(orderRepository).findById(1L);
        verify(itemRepository).findById(999L);
        verify(orderMapper, never()).toDto(any());
    }

    @Test
    void updateOrderByIdWhenNullUpdateRequestShouldNotUpdateAnything() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userServiceClient.findUserById(1L)).thenReturn(userDto);

        // Act
        OrderDto result = orderService.updateOrderById(1L, null);

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).findById(1L);
        verify(itemRepository, never()).findById(anyLong());
        verify(orderMapper).toDto(order);
    }

    @Test
    void updateOrderByIdWhenEmptyItemListShouldClearOrderItems() {
        // Arrange
        OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                null,
                Collections.emptyList(),
                null
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userServiceClient.findUserById(1L)).thenReturn(userDto);

        // Act
        OrderDto result = orderService.updateOrderById(1L, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(order.getOrderItemList()).isEmpty();
        verify(orderRepository).findById(1L);
        verify(itemRepository, never()).findById(anyLong());
        verify(orderMapper).toDto(order);
    }

    @Test
    void updateOrderByIdWhenNullItemListShouldClearOrderItems() {
        // Arrange
        OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                null,
                null,
                null
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userServiceClient.findUserById(1L)).thenReturn(userDto);

        // Act
        OrderDto result = orderService.updateOrderById(1L, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(order.getOrderItemList()).isEmpty();
        verify(orderRepository).findById(1L);
        verify(itemRepository, never()).findById(anyLong());
        verify(orderMapper).toDto(order);
    }


    @Test
    void updateOrderByIdShouldRecalculateTotalPriceAfterUpdate() {
        // Arrange
        Item item2 = new Item();
        item2.setId(2L);
        item2.setPrice(new BigDecimal("50.00"));

        OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                null,
                List.of(
                        new OrderItemCreateRequestDto(1L, 1),
                        new OrderItemCreateRequestDto(2L, 2)
                ),
                null
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userServiceClient.findUserById(1L)).thenReturn(userDto);

        // Act
        OrderDto result = orderService.updateOrderById(1L, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(order.getTotalPrice()).isEqualTo(new BigDecimal("199.99")); // 99.99 + (50 * 2)
        verify(orderRepository).findById(1L);
        verify(itemRepository).findById(1L);
        verify(itemRepository).findById(2L);
    }

}