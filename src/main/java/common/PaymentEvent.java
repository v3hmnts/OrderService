package common;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentEvent {
    private String paymentId;
    private Long orderId;
    private BigDecimal paymentAmount;
    private Instant timestamp;
    private PaymentStatus status;
}
