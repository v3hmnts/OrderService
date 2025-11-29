package orderService.mapper;

import orderService.dto.OrderItemDto;
import orderService.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface OrderItemMapper {

    OrderItem toEntity(OrderItemDto orderItem);

    @Mapping(target = "order", ignore = true)
    OrderItemDto toDto(OrderItem orderItem);
}
