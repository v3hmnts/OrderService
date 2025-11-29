package orderService.mapper;

import orderService.dto.OrderDto;
import orderService.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toEntity(OrderDto orderDto);

    @Mapping(target = "userId", ignore = true)
    OrderDto toDto(Order order);
    List<Order> toEntityList(List<OrderDto> dtoList);
    List<OrderDto> toDtoList(List<Order> orders);

}
