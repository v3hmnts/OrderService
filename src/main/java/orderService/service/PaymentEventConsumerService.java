package orderService.service;

import common.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventConsumerService {

    @KafkaListener(
            topics = "CREATE_PAYMENT_EVENT",
            groupId = "order-service-group"
    )
    public void handlePaymentEvent(@Payload PaymentEvent paymentEvent, Acknowledgment acknowledgment){
        System.out.println("RECEIVED EVENT");
        System.out.println(paymentEvent);
        acknowledgment.acknowledge();
    }
}
