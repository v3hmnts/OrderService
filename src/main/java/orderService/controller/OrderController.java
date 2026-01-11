package orderService.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import orderService.dto.OrderCreateRequestDto;
import orderService.dto.OrderDto;
import orderService.dto.OrderUpdateRequestDto;
import orderService.dto.PageDto;
import orderService.service.OrderService;
import orderService.specification.OrderFilterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {

    private final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDto> createNewOrder(@NotNull @Valid @RequestBody OrderCreateRequestDto orderCreateRequestDto) {
        logger.info("POST request to /api/v1/orders endpoint received");
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderCreateRequestDto));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable Long orderId, @NotNull @Valid @RequestBody OrderUpdateRequestDto orderUpdateRequestDto) {
        logger.info("PUT request to /api/v1/orders/{} endpoint received",orderId);
        return ResponseEntity.ok(orderService.updateOrderById(orderId, orderUpdateRequestDto));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> findOrderById(@PathVariable Long orderId) {
        logger.info("GET request to /api/v1/orders/{} endpoint received",orderId);
        return ResponseEntity.ok(orderService.findById(orderId));
    }

    @GetMapping()
    public ResponseEntity<PageDto<OrderDto>> findAllWithAllData(@ModelAttribute @Valid OrderFilterRequest orderFilterRequest, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.info("GET request to /api/v1/orders endpoint received with OrderFilterRequest {}",orderFilterRequest);
        return ResponseEntity.ok(orderService.findAllWithAllData(orderFilterRequest, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> findAllByUserId(@PathVariable Long userId) {
        logger.info("GET request to /api/v1/orders/user/{} endpoint received",userId);
        return ResponseEntity.ok(orderService.findAllByUserId(userId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Long orderId) {
        logger.info("DELETE request to /api/v1/orders/{} endpoint received",orderId);
        orderService.deleteById(orderId);
        return ResponseEntity.noContent().build();
    }

}
