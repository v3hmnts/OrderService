package orderService.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ItemUpdateDto(
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    String name,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must be less than 1,000,000")
    @Digits(integer = 12, fraction = 2, message = "Price must have up to 12 integer digits and 2 decimal places")
    BigDecimal price,

    Boolean deleted
){}
