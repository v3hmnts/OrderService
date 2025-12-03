package orderService.mapper;

import orderService.dto.OrderDto;
import orderService.dto.PageDto;
import orderService.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    Order toEntity(OrderDto orderDto);

    OrderDto toDto(Order order);

    List<Order> toEntityList(List<OrderDto> dtoList);

    List<OrderDto> toDtoList(List<Order> orders);

    @Mapping(source = "number", target = "pageNumber")
    @Mapping(source = "size", target = "pageSize")
    PageDto<OrderDto> toPageDto(Page<OrderDto> page);

}
