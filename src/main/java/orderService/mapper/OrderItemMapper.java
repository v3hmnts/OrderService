package orderService.mapper;

import orderService.dto.OrderItemDto;
import orderService.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface OrderItemMapper {

    OrderItem toEntity(OrderItemDto orderItem);

    @Mapping(source = "item", target = "itemDto")
    OrderItemDto toDto(OrderItem orderItem);

    List<OrderItem> toEntityList(List<OrderItemDto> dtoList);

    List<OrderItemDto> toDtoList(List<OrderItem> orderItems);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(source = "itemDto", target = "item")
    void updateFromDto(OrderItemDto orderItemDto, @MappingTarget OrderItem orderItem);
}
