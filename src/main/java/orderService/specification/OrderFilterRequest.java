package orderService.specification;

import jakarta.validation.constraints.Past;
import lombok.*;
import orderService.entity.Order;
import orderService.entity.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class OrderFilterRequest {

    @Past
    private Instant createdBefore;

    @Past
    private Instant createdAfter;

    private OrderStatus orderStatus;

    public Specification<Order> toSpecification() {
        List<Specification<Order>> specifications = new ArrayList<>();

        if (this.createdBefore != null) {
            specifications.add(OrderSpecification.createdBefore(this.createdBefore));
        }
        if (this.createdAfter != null) {
            specifications.add(OrderSpecification.createdAfter(this.createdAfter));
        }
        if (this.orderStatus != null) {
            specifications.add(OrderSpecification.hasStatus(this.orderStatus));
        }
        if (specifications.isEmpty()) {
            return (root, query, criteriaBuilder) -> {
                return criteriaBuilder.conjunction();
            };
        }

        return Specification.allOf(specifications);

    }

}
