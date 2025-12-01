package orderService.dto;

import jakarta.validation.constraints.*;

public record OrderItemCreateRequestDto(
        @NotNull
        @Positive(message = "Item ID must be a positive number")
        Long itemId,

        @NotNull
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 9999, message = "Quantity must not exceed 9999")
        Integer quantity
) {
}
