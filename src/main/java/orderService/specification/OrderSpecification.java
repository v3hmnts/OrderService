package orderService.specification;

import orderService.entity.Order;
import orderService.entity.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class OrderSpecification {
    public static Specification<Order> createdAfter(Instant date) {
        return ((root, query, criteriaBuilder) -> {
            return date == null ? criteriaBuilder.conjunction() : criteriaBuilder.greaterThan(root.get("createdAt"), date);
        });
    }

    public static Specification<Order> createdBefore(Instant date) {
        return ((root, query, criteriaBuilder) -> {
            return date == null ? criteriaBuilder.conjunction() : criteriaBuilder.lessThan(root.get("createdAt"), date);
        });
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return ((root, query, criteriaBuilder) -> {
            return status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("orderStatus"), status);
        });
    }
}
