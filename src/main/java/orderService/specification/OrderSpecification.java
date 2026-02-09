package orderService.specification;

import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import orderService.entity.Order;
import orderService.entity.OrderItem;
import orderService.entity.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class OrderSpecification {
    public static Specification<Order> createdAfter(Instant createdAfter) {
        return ((root, query, criteriaBuilder) -> {
            return createdAfter == null ? criteriaBuilder.conjunction() : criteriaBuilder.greaterThan(root.get("createdAt"), createdAfter);
        });
    }

    public static Specification<Order> createdBefore(Instant createdBefore) {
        return ((root, query, criteriaBuilder) -> {
            return createdBefore == null ? criteriaBuilder.conjunction() : criteriaBuilder.lessThan(root.get("createdAt"), createdBefore);
        });
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return ((root, query, criteriaBuilder) -> {
            return status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("orderStatus"), status);
        });
    }

    public static Specification<Order> withAllData() {
        return (root, query, criteriaBuilder) -> {
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                return null;
            }
            Fetch<Order, OrderItem> ordersJoin = root.fetch("orderItemList", JoinType.LEFT);
            ordersJoin.fetch("item", JoinType.LEFT);
            return null;
        };
    }
}
