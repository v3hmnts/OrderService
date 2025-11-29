package orderService.dto;

public record OrderItemDto(
        Long id,
        ItemDto itemDto,
        Integer quantity,
        Boolean deleted
) {
}
