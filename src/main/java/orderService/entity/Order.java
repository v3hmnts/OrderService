package orderService.entity;

import jakarta.persistence.*;
import orderService.entity.enums.OrderStatus;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus orderStatus;

    @Column(columnDefinition = "NUMERIC")
    private double totalPrice;

    private boolean deleted;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> orderItemList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Order that = (Order) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
