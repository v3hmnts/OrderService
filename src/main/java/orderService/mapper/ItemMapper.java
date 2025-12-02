package orderService.mapper;

import orderService.dto.ItemCreateRequestDto;
import orderService.dto.ItemDto;
import orderService.dto.ItemUpdateDto;
import orderService.dto.PageDto;
import orderService.entity.Item;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toDto(Item item);

    Item toEntity(ItemDto itemDto);

    List<ItemDto> toDtoList(List<Item> items);

    List<Item> toEntityList(List<ItemDto> itemDtos);

    @Mapping(target = "id", ignore = true)
    Item toEntity(ItemCreateRequestDto itemCreateRequestDto);

    @Mapping(source = "number",target = "pageNumber")
    @Mapping(source = "size",target = "pageSize")
    PageDto<ItemDto> toPageDto(Page<ItemDto> itemDtoPage);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateFromDto(ItemUpdateDto itemDto, @MappingTarget Item item);

}
