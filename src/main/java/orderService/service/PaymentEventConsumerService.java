package orderService.service;

import common.PaymentEvent;
import orderService.entity.Order;
import orderService.entity.enums.OrderStatus;
import orderService.exception.OrderNotFoundException;
import orderService.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentEventConsumerService {

    private OrderRepository orderRepository;

    public PaymentEventConsumerService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(
            topics = "CREATE_PAYMENT_EVENT",
            groupId = "order-service-group"
    )
    @Transactional
    public void handlePaymentEvent(@Payload PaymentEvent paymentEvent, Acknowledgment acknowledgment){
        System.out.println("RECEIVED EVENT");
        Order orderToEdit = orderRepository.findById(paymentEvent.getOrderId()).orElseThrow(()->new OrderNotFoundException(paymentEvent.getOrderId()));
        switch (paymentEvent.getStatus()){
            case FAILED -> {
                orderToEdit.setOrderStatus(OrderStatus.CANCELED);
                break;
            }
            case SUCCESS -> {
                orderToEdit.setOrderStatus(OrderStatus.PAYED);
                break;
            }
        }
        orderRepository.save(orderToEdit);
        acknowledgment.acknowledge();
    }
}
