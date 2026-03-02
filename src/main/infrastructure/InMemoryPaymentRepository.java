package main.infrastructure;

import main.application.subscription.external.PaymentRepository;
import main.domain.payment.Payment;
import java.util.HashMap;
import java.util.Map;

public class InMemoryPaymentRepository implements PaymentRepository {
    private Map<Long, Payment> payments = new HashMap<>();

    @Override
    public Payment save(Payment payment) {
        payments.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public Payment findById(Long paymentId) {
        return payments.get(paymentId);
    }
}
