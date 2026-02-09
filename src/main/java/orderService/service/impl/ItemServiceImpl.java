package orderService.service.impl;

import orderService.dto.ItemCreateRequestDto;
import orderService.dto.ItemDto;
import orderService.dto.ItemUpdateDto;
import orderService.dto.PageDto;
import orderService.entity.Item;
import orderService.exception.ItemNotFoundException;
import orderService.mapper.ItemMapper;
import orderService.repository.ItemRepository;
import orderService.service.ItemService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemServiceImpl(ItemRepository itemRepository, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ItemDto createItem(ItemCreateRequestDto itemCreateRequestDto) {
        Item item = itemMapper.toEntity(itemCreateRequestDto);
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER'))")
    public ItemDto findById(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        return itemMapper.toDto(item);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ItemDto updateItemById(Long itemId, ItemUpdateDto itemUpdateDto) {
        Item itemToUpdate = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        itemMapper.updateFromDto(itemUpdateDto, itemToUpdate);
        return itemMapper.toDto(itemRepository.save(itemToUpdate));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteItemById(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER'))")
    public PageDto<ItemDto> findAll(Pageable pageable) {
        return itemMapper.toPageDto(itemRepository.findAll(pageable).map(itemMapper::toDto));
    }
}
