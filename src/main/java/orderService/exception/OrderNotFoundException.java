package orderService.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
        super(String.format("Item with id %s not found",orderId));
    }
}
