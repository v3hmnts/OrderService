package orderService.service;

import orderService.dto.OrderCreateRequestDto;
import orderService.dto.OrderDto;
import orderService.dto.PageDto;
import orderService.specification.OrderFilterRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    OrderDto findById(Long orderId);
    void deleteById(Long orderId);
    List<OrderDto> findAllByUserId(Long userId);
    OrderDto createOrder(OrderCreateRequestDto orderCreateRequestDto);
    PageDto<OrderDto> findAll(OrderFilterRequest orderFilterRequest, Pageable pageable);
    OrderDto updateOrderById(Long orderId,OrderDto orderDto);

}
