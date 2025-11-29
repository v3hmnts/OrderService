package orderService.mapper;

import orderService.dto.ItemDto;
import orderService.entity.Item;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toDto(Item item);
    Item toEntity(ItemDto itemDto);
    List<ItemDto> toDtoList(List<Item> items);
    List<Item> toEntityList(List<ItemDto> itemDtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateFromDto(ItemDto itemDto, @MappingTarget Item item);

}
