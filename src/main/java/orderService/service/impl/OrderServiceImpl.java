package orderService.service.impl;

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
import orderService.service.OrderService;
import orderService.service.UserServiceClient;
import orderService.specification.OrderFilterRequest;
import orderService.specification.OrderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserServiceClient userServiceClient;

    public OrderServiceImpl(OrderMapper orderMapper, OrderRepository orderRepository, ItemRepository itemRepository, UserServiceClient userServiceClient) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.userServiceClient = userServiceClient;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @securityService.isResourceOwner('Order',#orderId,authentication))")
    public OrderDto findById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderDto orderDto = orderMapper.toDto(order);
        return addUserDtoToOrderDto(orderDto,order.getUserId());
    }

    @Transactional()
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @securityService.isResourceOwner('User',#orderCreateRequestDto.userId,authentication))")
    public OrderDto createOrder(OrderCreateRequestDto orderCreateRequestDto) {
        Order order = orderRepository.save(createNewOrder(orderCreateRequestDto));
        OrderDto orderDto = orderMapper.toDto(order);
        return addUserDtoToOrderDto(orderDto,order.getUserId());
    }

    private Order createNewOrder(OrderCreateRequestDto orderCreateRequestDto) {
        Order newOrder = new Order();
        newOrder.setUserId(orderCreateRequestDto.getUserId());
        newOrder.setOrderStatus(OrderStatus.PENDING);
        List<OrderItemCreateRequestDto> itemCreateRequestDtos = orderCreateRequestDto.getOrderItemList();
        for (OrderItemCreateRequestDto itemCreateRequestDto : itemCreateRequestDtos) {
            Item itemToAdd = itemRepository.findById(itemCreateRequestDto.itemId()).orElseThrow();
            newOrder.addItem(itemToAdd, itemCreateRequestDto.quantity());
        }
        newOrder.updateTotalPrice();
        return newOrder;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @securityService.isResourceOwner('User',#orderCreateRequestDto.userId,authentication))")
    public List<OrderDto> findAllByUserId(Long userId) {
        List<Order> userOrderList = orderRepository.findByUserId(userId);
        List<OrderDto> orderDtos = orderMapper.toDtoList(userOrderList);
        UserDto userDto = userServiceClient.findUserById(userId);
        orderDtos.forEach(orderDto -> orderDto.setUser(userDto));
        return orderDtos;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public PageDto<OrderDto> findAll(OrderFilterRequest orderFilterRequest, Pageable pageable) {
        Page<Order> dtoPage = orderRepository.findAll(orderFilterRequest.toSpecification(), pageable);
        return orderMapper.toPageDto(dtoPage.map(order -> {
            OrderDto orderDto = orderMapper.toDto(order);
            addUserDtoToOrderDto(orderDto,order.getUserId());
            return orderDto;
        }));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public PageDto<OrderDto> findAllWithAllData(OrderFilterRequest orderFilterRequest, Pageable pageable) {
        Page<Order> dtoPage = orderRepository.findAll(orderFilterRequest.toSpecification().and(OrderSpecification.withAllData()), pageable);
        return orderMapper.toPageDto(dtoPage.map(order -> {
            OrderDto orderDto = orderMapper.toDto(order);
            addUserDtoToOrderDto(orderDto,order.getUserId());
            return orderDto;
        }));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDto updateOrderById(Long orderId, OrderUpdateDto orderDto) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        handleOrderUpdate(order, orderDto);
        OrderDto saved = orderMapper.toDto(orderRepository.save(order));
        return addUserDtoToOrderDto(saved,order.getUserId());
    }

    private void handleOrderUpdate(Order order, OrderUpdateDto orderDto) {
        if (orderDto == null) {
            return;
        }
        if (orderDto.orderStatus() != null) {
            order.setOrderStatus(OrderStatus.valueOf(orderDto.orderStatus()));
        }
        if (orderDto.deleted() != null) {
            order.setDeleted(orderDto.deleted());
        }

        for (var i = 0; i < order.getOrderItemList().size(); i++) {
            OrderItem oldOrderItem = order.getOrderItemList().get(i);
            oldOrderItem.getItem().getOrderItemList().remove(oldOrderItem);
            oldOrderItem.setOrder(null);
            oldOrderItem.setItem(null);
            oldOrderItem.setQuantity(0);
        }
        ;
        order.getOrderItemList().clear();
        orderDto.orderItemList().forEach(orderItemDto -> {
            Item item = itemRepository.findById(orderItemDto.itemId()).orElseThrow(() -> new ItemNotFoundException(orderItemDto.itemId()));
            order.addItem(item, orderItemDto.quantity());
        });
        order.updateTotalPrice();
    }

    private OrderDto addUserDtoToOrderDto(OrderDto orderDto,Long userId){
        UserDto userDto = userServiceClient.findUserById(userId);
        orderDto.setUser(userDto);
        return orderDto;
    }
}
