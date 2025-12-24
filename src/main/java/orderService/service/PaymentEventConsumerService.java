package orderService.service;

import common.PaymentEvent;
import orderService.entity.Order;
import orderService.entity.enums.OrderStatus;
import orderService.exception.OrderNotFoundException;
import orderService.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentEventConsumerService {

    private final Logger log = LoggerFactory.getLogger(PaymentEventConsumerService.class);
    private final OrderRepository orderRepository;

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
                log.info("Payment for order with id {} failed", orderToEdit.getId());
                orderToEdit.setOrderStatus(OrderStatus.CANCELED);
                break;
            }
            case SUCCESS -> {
                if((paymentEvent.getPaymentAmount().compareTo(orderToEdit.getTotalPrice()) == 0)){
                    log.info("Payment for order with id {} succeed", orderToEdit.getId());
                    orderToEdit.setOrderStatus(OrderStatus.PAYED);
                    break;
                }
                log.info("Payment for order with id {} succeed, but payment amount {} lower than totalPrice {}", orderToEdit.getId(),paymentEvent.getPaymentAmount(),orderToEdit.getTotalPrice());
                break;
            }
        }
        orderRepository.save(orderToEdit);
        acknowledgment.acknowledge();
    }
}
