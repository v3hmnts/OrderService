package orderService.mapper;

import orderService.dto.ItemDto;
import orderService.dto.OrderDto;
import orderService.dto.OrderItemDto;
import orderService.dto.PageDto;
import orderService.entity.Item;
import orderService.entity.Order;
import orderService.entity.OrderItem;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring",uses = {OrderItemMapper.class})
public interface OrderMapper {

    Order toEntity(OrderDto orderDto);
    OrderDto toDto(Order order);
    List<Order> toEntityList(List<OrderDto> dtoList);
    List<OrderDto> toDtoList(List<Order> orders);
    @Mapping(source = "number", target = "size")
    PageDto<OrderDto> toPageDto(Page<OrderDto> page);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateFromDto(OrderDto orderDto, @MappingTarget Order order);






}
