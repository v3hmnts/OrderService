package orderService.service;

import orderService.dto.ItemCreateRequestDto;
import orderService.dto.ItemDto;
import orderService.dto.ItemUpdateDto;
import orderService.dto.PageDto;
import org.springframework.data.domain.Pageable;

public interface ItemService {

    public ItemDto createItem(ItemCreateRequestDto itemCreateRequestDto);

    public ItemDto findById(Long itemId);

    public ItemDto updateItemById(Long itemId, ItemUpdateDto itemUpdateDto);

    public void deleteItemById(Long itemId);

    public PageDto<ItemDto> findAll(Pageable pageable);

}
