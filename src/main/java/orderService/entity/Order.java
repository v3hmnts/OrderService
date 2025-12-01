package orderService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import orderService.entity.enums.OrderStatus;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE orders SET deleted = true WHERE id=?")
public class Order extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus orderStatus;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "deleted")
    private boolean deleted;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL
    )
    private List<OrderItem> orderItemList = new ArrayList<>();


    public void addItem(Item item, Integer quantity) {
        OrderItem newOrderItem = new OrderItem(this, item, quantity);
        for(var orderItem : orderItemList){
            if(orderItem.getItem().equals(item)){
                orderItem.setQuantity(orderItem.getQuantity()+newOrderItem.getQuantity());
                return;
            }
        }
        this.orderItemList.add(newOrderItem);
        item.getOrderItemList().add(newOrderItem);
    }

    public void removeItem(Item item, Integer quantity) {
        for (var i = 0; i < orderItemList.size(); i++) {
            var orderItem = orderItemList.get(i);
            if (orderItem.getOrder().equals(this) && orderItem.getItem().equals(item)) {
                if (orderItem.getQuantity() > quantity) {
                    orderItem.setQuantity(orderItem.getQuantity() - quantity);
                    break;
                }else if(orderItem.getQuantity().equals(quantity)){
                    orderItemList.remove(orderItem);
                    orderItem.getItem().getOrderItemList().remove(orderItem);
                    orderItem.setOrder(null);
                    orderItem.setItem(null);
                    orderItem.setQuantity(0);
                    break;
                }else{
                    throw new RuntimeException("Can't delete more that in order");
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Order that = (Order) o;
        if(this.getId()==null || that.getId()==null){
            return false;
        }
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void updateTotalPrice(){
        this.totalPrice = this.orderItemList.stream()
                .map(orderItem -> orderItem.getItem().getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add, BigDecimal::add);
    }
}
