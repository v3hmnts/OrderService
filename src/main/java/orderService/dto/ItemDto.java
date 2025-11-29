package orderService.dto;

public record ItemDto(
        Long id,
        String name,
        Double price,
        Boolean deleted
) {
}
