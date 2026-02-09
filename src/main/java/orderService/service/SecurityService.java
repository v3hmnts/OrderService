package orderService.service;

import orderService.entity.Order;
import orderService.exception.OrderNotFoundException;
import orderService.repository.ItemRepository;
import orderService.repository.OrderRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    private final OrderRepository orderRepository;

    public SecurityService(OrderRepository orderRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
    }

    public boolean isResourceOwner(String resourceName, Long resourceId, JwtAuthenticationToken token) {
        Long userIdFromToken = Long.valueOf(token.getToken().getClaimAsString("userId"));
        if ("User".equals(resourceName)) {
            return resourceId.equals(userIdFromToken);
        }
        if ("Order".equals(resourceName)) {
            Order order = orderRepository.findById(resourceId).orElseThrow(() -> new OrderNotFoundException(resourceId));
            return order.getUserId().equals(userIdFromToken);
        }
        return false;
    }
}
