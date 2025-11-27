package orderService;

import jakarta.transaction.Transactional;
import orderService.entity.Item;
import orderService.entity.Order;
import orderService.entity.OrderItem;
import orderService.entity.enums.OrderStatus;
import orderService.repository.ItemRepository;
import orderService.repository.OrderItemRepository;
import orderService.repository.OrderRepository;
import orderService.specification.OrderSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@SpringBootApplication
@EnableJpaAuditing
public class OrderServiceApplication implements CommandLineRunner {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {




    }

    public Double getTotalPrice(List<OrderItem> list) {
        return list.stream()
                .map(orderItem -> orderItem.getItem().getPrice() * orderItem.getQuantity())
                .reduce(0d, Double::sum, Double::sum);
    }

	public void createSomeData(){
		Item item1 = new Item();
		item1.setName("Item1");
		item1.setPrice(10d);

		Item item2 = new Item();
		item2.setName("Item2");
		item2.setPrice(20d);

		Item item3 = new Item();
		item3.setName("Item3");
		item3.setPrice(30d);


		Order order = new Order();
		OrderItem orderItem1 = new OrderItem();
		orderItem1.setOrder(order);
		orderItem1.setItem(item1);
		orderItem1.setQuantity(2);

		OrderItem orderItem2 = new OrderItem();
		orderItem2.setOrder(order);
		orderItem2.setItem(item2);
		orderItem2.setQuantity(1);

		order.setUserId(1L);
		order.getOrderItemList().add(orderItem1);
		order.getOrderItemList().add(orderItem2);
		order.setTotalPrice(getTotalPrice(order.getOrderItemList()));
		order.setOrderStatus(OrderStatus.CONFIRMED);

		orderRepository.save(order);
	}

	public void deleteSomeData(){
		Item item = itemRepository.findById(1L).orElseThrow();
		System.out.println("Item name: "+item.getName());
		System.out.println("Item id: "+item.getId());
		Order order = orderRepository.findById(1L).orElseThrow();
		order.getOrderItemList().stream().filter(orderItem -> orderItem.getItem().equals(item)).forEach(orderItem -> System.out.println("Order has "+orderItem.getQuantity()));
		order.removeItem(item,3);
	}

	public void addSomeData(){
		Item item = itemRepository.findById(1L).orElseThrow();
		System.out.println("Item name: "+item.getName());
		System.out.println("Item id: "+item.getId());
		Order order = orderRepository.findById(1L).orElseThrow();
		order.getOrderItemList().stream().filter(orderItem -> orderItem.getItem().equals(item)).forEach(orderItem -> System.out.println("Order has "+orderItem.getQuantity()));
		order.addItem(item,1);
	}

	public void specificationTest(){
		Specification<Order> after = OrderSpecification.createdAfter(LocalDateTime.of(2025, 11, 27, 17, 47).toInstant(ZoneOffset.ofHours(0)));
		Specification<Order> before = OrderSpecification.createdBefore(LocalDateTime.of(2025, 11, 27, 17, 55).toInstant(ZoneOffset.ofHours(0)));
		Specification<Order> status = OrderSpecification.hasStatus(OrderStatus.FAILED);
		Page<Order> orders = orderRepository.findAll(before.and(after).and(status),Pageable.ofSize(2));
		System.out.println(orders);
	}
}
