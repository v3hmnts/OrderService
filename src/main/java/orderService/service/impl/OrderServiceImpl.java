package orderService.service.impl;

import orderService.dto.OrderCreateRequestDto;
import orderService.dto.OrderDto;
import orderService.dto.PageDto;
import orderService.entity.Item;
import orderService.entity.Order;
import orderService.entity.OrderItem;
import orderService.mapper.OrderMapper;
import orderService.repository.ItemRepository;
import orderService.repository.OrderRepository;
import orderService.service.OrderService;
import orderService.specification.OrderFilterRequest;
import orderService.specification.OrderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    public OrderServiceImpl(OrderMapper orderMapper, OrderRepository orderRepository, ItemRepository itemRepository) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow();
        return orderMapper.toDto(order);
    }

    @Transactional()
    public OrderDto createOrder(OrderCreateRequestDto orderCreateRequestDto){
        Order orderToSave = orderMapper.toEntity(orderDto);
        Order order = orderRepository.save(orderToSave);
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findAllByUserId(Long userId){
        List<Order> userOrderList = orderRepository.findByUserId(userId).orElseThrow();
        return orderMapper.toDtoList(userOrderList);
    }

    @Transactional
    public void deleteById(Long orderId){
        orderRepository.deleteById(orderId);
    }

    @Transactional(readOnly = true)
    public PageDto<OrderDto> findAll(OrderFilterRequest orderFilterRequest, Pageable pageable){
        Page<Order> dtoPage = orderRepository.findAll(orderFilterRequest.toSpecification().and(OrderSpecification.withAllData()),pageable);
        return orderMapper.toPageDto(dtoPage.map(orderMapper::toDto));
    }

    @Transactional
    public OrderDto updateOrderById(Long orderId, OrderDto orderDto){
        Order order = orderRepository.findById(orderId).orElseThrow();
        handleOrderUpdate(order,orderDto);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto deleteOrderById(Long orderId){
        orderRepository.deleteById(orderId);
        Order order = orderRepository.findById(orderId).orElseThrow();
        return orderMapper.toDto(orderRepository.save(order));
    }

    private void handleOrderUpdate(Order order,OrderDto orderDto){
        if ( orderDto == null ) {
            return;
        }
        if ( orderDto.getOrderStatus() != null ) {
            order.setOrderStatus( orderDto.getOrderStatus() );
        }
        if ( orderDto.getTotalPrice() != null ) {
            order.setTotalPrice( orderDto.getTotalPrice() );
        }
        if ( orderDto.getDeleted() != null ) {
            order.setDeleted( orderDto.getDeleted() );
        }

        for(var i=0;i<order.getOrderItemList().size();i++){
            OrderItem oldOrderItem = order.getOrderItemList().get(i);
            oldOrderItem.getItem().getOrderItemList().remove(oldOrderItem);
            oldOrderItem.setOrder(null);
            oldOrderItem.setItem(null);
            oldOrderItem.setQuantity(0);
        };
        order.getOrderItemList().clear();
        orderDto.getOrderItemList().forEach(orderItemDto -> {
            Item item = itemRepository.findById(orderItemDto.itemDto().id()).orElseThrow();
            order.addItem(item,orderItemDto.quantity());
        });
        order.updatePrice();
    }

//    private Order handleOrderCreation(OrderDto orderDto){
//        Order newOrder = new Order();
//        newOrder.setOrderStatus(OrderStatus.CONFIRMED);
//        newOrder.setUserId(orderDto);
//    }
}
