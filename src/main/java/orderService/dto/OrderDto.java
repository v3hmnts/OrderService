package orderService.dto;

import lombok.*;
import orderService.entity.enums.OrderStatus;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderDto{
    private Long id;
    private UserDto user;
    private OrderStatus orderStatus;
    private Double totalPrice;
    private Boolean deleted;
    private List<OrderItemDto> orderItems;
}
