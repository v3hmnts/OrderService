package orderService.dto;

import java.math.BigDecimal;

public record ItemDto(
        Long id,
        String name,
        BigDecimal price,
        Boolean deleted
) {
}
