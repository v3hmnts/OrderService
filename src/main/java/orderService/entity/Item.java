package orderService.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "items")
public class Item  extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "price", columnDefinition = "NUMERIC")
    private Double price;

    @OneToMany(
            mappedBy = "item",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> orderItemList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Item that = (Item) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
