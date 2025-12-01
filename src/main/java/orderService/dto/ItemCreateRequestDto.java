package orderService.dto;

import java.math.BigDecimal;

public record ItemCreateRequestDto(
        String name,
        BigDecimal price
) {
}
