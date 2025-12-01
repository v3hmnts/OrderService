package orderService.service.impl;

import orderService.dto.ItemCreateRequestDto;
import orderService.dto.ItemDto;
import orderService.dto.ItemUpdateDto;
import orderService.dto.PageDto;
import orderService.entity.Item;
import orderService.mapper.ItemMapper;
import orderService.repository.ItemRepository;
import orderService.service.ItemService;
import orderService.specification.ItemFilterRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemServiceImpl(ItemRepository itemRepository, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    @Override
    public ItemDto createItem(ItemCreateRequestDto itemCreateRequestDto) {
        Item item = new Item();
        item.setName(itemCreateRequestDto.name());
        item.setPrice(itemCreateRequestDto.price());
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto findById(Long itemId) {
        return null;
    }

    @Override
    public ItemDto updateItemById(Long itemId, ItemUpdateDto itemUpdateDto) {
        return null;
    }

    @Override
    public void deleteItemById(Long itemId) {

    }

    @Override
    public PageDto<ItemDto> findAll(ItemFilterRequest itemFilterRequest, Pageable pageable) {
        return null;
    }
}
