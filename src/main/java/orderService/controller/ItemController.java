package orderService.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import orderService.dto.ItemCreateRequestDto;
import orderService.dto.ItemDto;
import orderService.dto.ItemUpdateDto;
import orderService.dto.PageDto;
import orderService.service.ItemService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<ItemDto> createNewItem(@NotNull @Valid @RequestBody ItemCreateRequestDto itemCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(itemCreateRequestDto));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> findItemById(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.findById(itemId));
    }

    @GetMapping()
    public ResponseEntity<PageDto<ItemDto>> findAll(@PageableDefault(size = 2, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(itemService.findAll(pageable));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItemById(@PathVariable Long itemId, @NotNull @Valid @RequestBody ItemUpdateDto itemUpdateDto) {
        return ResponseEntity.ok(itemService.updateItemById(itemId, itemUpdateDto));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItemById(@PathVariable Long itemId) {
        itemService.deleteItemById(itemId);
        return ResponseEntity.noContent().build();
    }

}
