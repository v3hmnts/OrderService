package orderService.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderCreateRequestDto {
    private Long userId;
    private List<OrderItemCreateRequestDto> orderItemList;
}
