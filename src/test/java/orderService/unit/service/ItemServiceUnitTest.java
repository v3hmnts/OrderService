package orderService.unit.service;

import orderService.dto.ItemCreateRequestDto;
import orderService.dto.ItemDto;
import orderService.dto.ItemUpdateDto;
import orderService.dto.PageDto;
import orderService.entity.Item;
import orderService.exception.ItemNotFoundException;
import orderService.mapper.ItemMapper;
import orderService.repository.ItemRepository;
import orderService.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("[unit] ItemService")
class ItemServiceUnitTest {


    private final Long ITEM_ID = 1L;
    private final String ITEM_NAME = "Test Item";
    private final BigDecimal ITEM_PRICE = new BigDecimal("19.99");
    private final Boolean DELETED_FLAG = false;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;
    @InjectMocks
    private ItemServiceImpl itemService;
    private Item item;
    private ItemDto itemDto;
    private ItemCreateRequestDto createRequestDto;
    private ItemUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        item = new Item(ITEM_ID, ITEM_NAME, ITEM_PRICE, null, DELETED_FLAG);
        itemDto = new ItemDto(ITEM_ID, ITEM_NAME, ITEM_PRICE, DELETED_FLAG);
        createRequestDto = new ItemCreateRequestDto(ITEM_NAME, ITEM_PRICE);
        updateDto = new ItemUpdateDto("Updated Item", new BigDecimal("29.99"), false);
    }

    @Nested
    @DisplayName("Create Item Tests")
    class CreateItem {

        @Test
        @DisplayName("Should create item successfully")
        void shouldCreateItemSuccessfully() {
            // Arrange
            when(itemMapper.toEntity(createRequestDto)).thenReturn(item);
            when(itemRepository.save(item)).thenReturn(item);
            when(itemMapper.toDto(item)).thenReturn(itemDto);

            // Act
            ItemDto result = itemService.createItem(createRequestDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(ITEM_ID);
            assertThat(result.name()).isEqualTo(ITEM_NAME);
            assertThat(result.price()).isEqualTo(ITEM_PRICE);

            verify(itemMapper, times(1)).toEntity(createRequestDto);
            verify(itemRepository, times(1)).save(item);
            verify(itemMapper, times(1)).toDto(item);
        }

        @Test
        @DisplayName("Should map entity from DTO when creating item")
        void shouldMapEntityFromDtoWhenCreatingItem() {
            // Arrange
            when(itemMapper.toEntity(createRequestDto)).thenReturn(item);
            when(itemRepository.save(item)).thenReturn(item);
            when(itemMapper.toDto(item)).thenReturn(itemDto);

            // Act
            itemService.createItem(createRequestDto);

            // Assert
            verify(itemMapper, times(1)).toEntity(createRequestDto);
        }

        @Test
        @DisplayName("Should save entity to repository")
        void shouldSaveEntityToRepository() {
            // Arrange
            when(itemMapper.toEntity(createRequestDto)).thenReturn(item);
            when(itemRepository.save(item)).thenReturn(item);
            when(itemMapper.toDto(item)).thenReturn(itemDto);

            // Act
            itemService.createItem(createRequestDto);

            // Assert
            verify(itemRepository, times(1)).save(item);
        }
    }

    @Nested
    @DisplayName("Find Item By ID Tests")
    class FindItemById {

        @Test
        @DisplayName("Should find item by ID successfully")
        void shouldFindItemByIdSuccessfully() {
            // Arrange
            when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
            when(itemMapper.toDto(item)).thenReturn(itemDto);

            // Act
            ItemDto result = itemService.findById(ITEM_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(ITEM_ID);
            assertThat(result.name()).isEqualTo(ITEM_NAME);

            verify(itemRepository, times(1)).findById(ITEM_ID);
            verify(itemMapper, times(1)).toDto(item);
        }

        @Test
        @DisplayName("Should throw ItemNotFoundException when item not found")
        void shouldThrowItemNotFoundExceptionWhenItemNotFound() {
            // Arrange
            when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> itemService.findById(ITEM_ID))
                    .isInstanceOf(ItemNotFoundException.class)
                    .hasMessageContaining(ITEM_ID.toString());

            verify(itemRepository, times(1)).findById(ITEM_ID);
            verify(itemMapper, never()).toDto(any());
        }

        @Test
        @DisplayName("Should map entity to DTO when found")
        void shouldMapEntityToDtoWhenFound() {
            // Arrange
            when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
            when(itemMapper.toDto(item)).thenReturn(itemDto);

            // Act
            itemService.findById(ITEM_ID);

            // Assert
            verify(itemMapper, times(1)).toDto(item);
        }
    }

    @Nested
    @DisplayName("Update Item Tests")
    class UpdateItem {

        @Test
        @DisplayName("Should update item successfully")
        void shouldUpdateItemSuccessfully() {
            // Arrange
            Item updatedItem = new Item(ITEM_ID, "Updated Item", new BigDecimal("29.99"), null, false);

            ItemDto updatedItemDto = new ItemDto(ITEM_ID, "Updated Item", new BigDecimal("29.99"), false);

            when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
            when(itemRepository.save(item)).thenReturn(updatedItem);
            when(itemMapper.toDto(updatedItem)).thenReturn(updatedItemDto);

            // Act
            ItemDto result = itemService.updateItemById(ITEM_ID, updateDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Updated Item");
            assertThat(result.price()).isEqualTo(new BigDecimal("29.99"));

            verify(itemRepository, times(1)).findById(ITEM_ID);
            verify(itemMapper, times(1)).updateFromDto(updateDto, item);
            verify(itemRepository, times(1)).save(item);
            verify(itemMapper, times(1)).toDto(updatedItem);
        }

        @Test
        @DisplayName("Should throw ItemNotFoundException when updating non-existent item")
        void shouldThrowItemNotFoundExceptionWhenUpdatingNonExistentItem() {
            // Arrange
            when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> itemService.updateItemById(ITEM_ID, updateDto))
                    .isInstanceOf(ItemNotFoundException.class)
                    .hasMessageContaining(ITEM_ID.toString());

            verify(itemRepository, times(1)).findById(ITEM_ID);
            verify(itemMapper, never()).updateFromDto(any(), any());
            verify(itemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update entity from DTO")
        void shouldUpdateEntityFromDto() {
            // Arrange
            when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
            when(itemRepository.save(item)).thenReturn(item);
            when(itemMapper.toDto(item)).thenReturn(itemDto);

            // Act
            itemService.updateItemById(ITEM_ID, updateDto);

            // Assert
            verify(itemMapper, times(1)).updateFromDto(updateDto, item);
        }

        @Test
        @DisplayName("Should save updated entity")
        void shouldSaveUpdatedEntity() {
            // Arrange
            when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
            when(itemRepository.save(item)).thenReturn(item);
            when(itemMapper.toDto(item)).thenReturn(itemDto);

            // Act
            itemService.updateItemById(ITEM_ID, updateDto);

            // Assert
            verify(itemRepository, times(1)).save(item);
        }
    }

    @Nested
    @DisplayName("Delete Item Tests")
    class DeleteItem {

        @Test
        @DisplayName("Should delete item successfully")
        void shouldDeleteItemSuccessfully() {
            // Arrange
            doNothing().when(itemRepository).deleteById(ITEM_ID);

            // Act
            itemService.deleteItemById(ITEM_ID);

            // Assert
            verify(itemRepository, times(1)).deleteById(ITEM_ID);
        }

        @Test
        @DisplayName("Should call repository delete method once")
        void shouldCallRepositoryDeleteMethodOnce() {
            // Arrange
            doNothing().when(itemRepository).deleteById(ITEM_ID);

            // Act
            itemService.deleteItemById(ITEM_ID);

            // Assert
            verify(itemRepository, times(1)).deleteById(ITEM_ID);
        }
    }

    @Nested
    @DisplayName("Find All Items Tests")
    class FindAllItems {

        private Pageable pageable;
        private Page<Item> itemPage;
        private Page<ItemDto> itemDtoPage;
        private PageDto<ItemDto> expectedPageDto;

        @BeforeEach
        void setUp() {
            pageable = mock(Pageable.class);
            List<Item> items = List.of(item);
            List<ItemDto> itemDtos = List.of(itemDto);

            itemPage = new PageImpl<>(items, pageable, items.size());
            itemDtoPage = new PageImpl<>(itemDtos, pageable, itemDtos.size());

            expectedPageDto = new PageDto<>(
                    itemDtos,
                    0,
                    10,
                    items.size(),
                    1
            );
        }

        @Test
        @DisplayName("Should find all items with pagination successfully")
        void shouldFindAllItemsWithPaginationSuccessfully() {
            // Arrange
            when(itemRepository.findAll(pageable)).thenReturn(itemPage);
            when(itemMapper.toDto(item)).thenReturn(itemDto);
            when(itemMapper.toPageDto(itemDtoPage)).thenReturn(expectedPageDto);

            // Act
            PageDto<ItemDto> result = itemService.findAll(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(ITEM_ID);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(itemRepository, times(1)).findAll(pageable);
            verify(itemMapper, times(1)).toPageDto(any(Page.class));
        }

        @Test
        @DisplayName("Should return empty page when no items exist")
        void shouldReturnEmptyPageWhenNoItemsExist() {
            // Arrange
            Page<Item> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            Page<ItemDto> emptyDtoPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            PageDto<ItemDto> emptyPageDto = new PageDto<>(
                    Collections.emptyList(),
                    0,
                    10,
                    0,
                    0
            );

            when(itemRepository.findAll(pageable)).thenReturn(emptyPage);
            when(itemMapper.toPageDto(emptyDtoPage)).thenReturn(emptyPageDto);

            // Act
            PageDto<ItemDto> result = itemService.findAll(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should use provided pageable parameters")
        void shouldUseProvidedPageableParameters() {
            // Arrange
            when(itemRepository.findAll(pageable)).thenReturn(itemPage);
            when(itemMapper.toDto(item)).thenReturn(itemDto);
            when(itemMapper.toPageDto(itemDtoPage)).thenReturn(expectedPageDto);

            // Act
            itemService.findAll(pageable);

            // Assert
            verify(itemRepository, times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("Should map page to DTO")
        void shouldMapPageToDto() {
            // Arrange
            when(itemRepository.findAll(pageable)).thenReturn(itemPage);
            when(itemMapper.toDto(item)).thenReturn(itemDto);
            when(itemMapper.toPageDto(itemDtoPage)).thenReturn(expectedPageDto);

            // Act
            itemService.findAll(pageable);

            // Assert
            verify(itemMapper, times(1)).toPageDto(any(Page.class));
        }
    }

}