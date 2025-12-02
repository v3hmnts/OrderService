package orderService.controller;

import jakarta.validation.Valid;
import orderService.dto.OrderCreateRequestDto;
import orderService.dto.OrderDto;
import orderService.dto.OrderUpdateDto;
import orderService.dto.PageDto;
import orderService.service.OrderService;
import orderService.specification.OrderFilterRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDto> createNewOrder(@Valid @RequestBody OrderCreateRequestDto orderCreateRequestDto){
            return ResponseEntity.ok(orderService.createOrder(orderCreateRequestDto));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable Long orderId, @Valid @RequestBody OrderUpdateDto orderUpdateDto){
        return ResponseEntity.ok(orderService.updateOrderById(orderId,orderUpdateDto));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> findOrderById(@PathVariable Long orderId){
        return ResponseEntity.ok(orderService.findById(orderId));
    }

    @GetMapping()
    public ResponseEntity<PageDto<OrderDto>> findAllWithAllData(OrderFilterRequest orderFilterRequest, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseEntity.ok(orderService.findAllWithAllData(orderFilterRequest,pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> findAllByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(orderService.findAllByUserId(userId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteById(@PathVariable Long orderId){
        return ResponseEntity.noContent().build();
    }

}
