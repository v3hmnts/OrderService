package orderService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import orderService.entity.enums.OrderStatus;

import java.util.List;

public record OrderUpdateDto(

        @Pattern(regexp = "PENDING|CONFIRMED|PAYED|DELIVERED|CANCELED",
                message = "Invalid order status. Valid values: PENDING, CONFIRMED, PAYED, DELIVERED, CANCELED")
        String orderStatus,

        @Valid
        List<@Valid OrderItemCreateRequestDto> orderItemList,

        Boolean deleted
) {
}
