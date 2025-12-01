package orderService.service;

import orderService.dto.OrderCreateRequestDto;
import orderService.dto.OrderDto;
import orderService.dto.OrderUpdateDto;
import orderService.dto.PageDto;
import orderService.specification.OrderFilterRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    public OrderDto findById(Long orderId);
    public void deleteById(Long orderId);
    public List<OrderDto> findAllByUserId(Long userId);
    public OrderDto createOrder(OrderCreateRequestDto orderCreateRequestDto);
    public PageDto<OrderDto> findAll(OrderFilterRequest orderFilterRequest, Pageable pageable);
    public PageDto<OrderDto> findAllWithAllData(OrderFilterRequest orderFilterRequest, Pageable pageable);
    public OrderDto updateOrderById(Long orderId,OrderUpdateDto orderDto);

}
