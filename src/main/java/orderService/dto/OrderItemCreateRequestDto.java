package orderService.dto;

public record OrderItemCreateRequestDto(
        Long itemId,
        Integer quantity
) {
}
