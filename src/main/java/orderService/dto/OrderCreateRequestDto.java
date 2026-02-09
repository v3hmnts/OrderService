package orderService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderCreateRequestDto {
    @NotNull
    private Long userId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<@Valid OrderItemCreateRequestDto> orderItemList;
}
