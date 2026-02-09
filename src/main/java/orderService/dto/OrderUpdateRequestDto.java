package orderService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record OrderUpdateRequestDto(

        @Pattern(regexp = "PENDING|CONFIRMED|PAYED|DELIVERED|CANCELED",
                message = "Invalid order status. Valid values: PENDING, CONFIRMED, PAYED, DELIVERED, CANCELED")
        String orderStatus,

        @Valid
        List<@Valid OrderItemCreateRequestDto> orderItemList,

        Boolean deleted
) {
}
