package main.infrastructure;

import main.application.subscription.internal.SubscriptionService;
import main.application.subscription.dto.ChargeDto;
import main.application.subscription.dto.PlanInfoDto;
import main.application.subscription.dto.SubscriptionStatusDto;
import main.application.subscription.external.PaymentRepository;
import main.application.subscription.external.PlanRepository;
import main.application.subscription.external.SubscriptionRepository;
import main.domain.payment.Payment;
import main.domain.payment.PaymentStatus;
import main.domain.plan.Plan;
import main.domain.subscription.Subscription;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository, PlanRepository planRepository,
                                  PaymentRepository paymentRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public SubscriptionStatusDto getSubscriptionStatus(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId);
        if (subscription == null) {
            throw new IllegalArgumentException("구독 정보를 찾을 수 없습니다.");
        }

        Plan plan = subscription.getPlan();
        return new SubscriptionStatusDto(
            plan.getPlanType(),
            subscription.isActive(),
            subscription.getEndDate(),
            plan.getCharge(),
            plan.getPeriod()
        );
    }

    @Override
    public SubscriptionStatusDto changePlan(Long userId, Long newPlanId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId);
        if (subscription == null) {
            throw new IllegalArgumentException("구독 정보를 찾을 수 없습니다.");
        }

        Plan newPlan = planRepository.findById(newPlanId);
        if (newPlan == null) {
            throw new IllegalArgumentException("플랜을 찾을 수 없습니다.");
        }

        subscription.changePlan(newPlan);
        
        // 새로운 결제 정보 생성
        Payment newPayment = new Payment(System.currentTimeMillis(), PaymentStatus.PAID, newPlan.getCharge(), LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(newPayment);
        subscription.updatePayment(savedPayment);
        subscription.activateSubscription();
        
        subscriptionRepository.update(subscription);

        Plan changedPlan = subscription.getPlan();
        return new SubscriptionStatusDto(
            changedPlan.getPlanType(),
            subscription.isActive(),
            subscription.getEndDate(),
            changedPlan.getCharge(),
            changedPlan.getPeriod()
        );
    }

    @Override
    public ChargeDto getCharge(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId);
        if (subscription == null) {
            throw new IllegalArgumentException("구독 정보를 찾을 수 없습니다.");
        }

        long charge = 0;
        boolean shouldCharge = false;
        String reason = "활성화된 구독";
        
        if (!subscription.isActive()) {
            charge = subscription.getPlan().getCharge();
            shouldCharge = true;
            reason = "구독 갱신 필요";
        }

        return new ChargeDto(charge, shouldCharge, reason);
    }

    @Override
    public List<PlanInfoDto> getPlanList() {
        return planRepository.findAll().stream()
            .map(plan -> new PlanInfoDto(plan.getId(), plan.getPlanType(), plan.getCharge(), plan.getPeriod()))
            .collect(Collectors.toList());
    }

    @Override
    public SubscriptionStatusDto payCharge(Long userId, Long paymentId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId);
        if (subscription == null) {
            throw new IllegalArgumentException("구독 정보를 찾을 수 없습니다.");
        }

        Payment payment = paymentRepository.findById(paymentId);
        if (payment == null || !payment.isPaid()) {
            throw new IllegalArgumentException("결제 정보가 유효하지 않습니다.");
        }

        subscription.updatePayment(payment);
        subscription.activateSubscription();
        subscriptionRepository.update(subscription);

        Plan finalPlan = subscription.getPlan();
        return new SubscriptionStatusDto(
            finalPlan.getPlanType(),
            subscription.isActive(),
            subscription.getEndDate(),
            finalPlan.getCharge(),
            finalPlan.getPeriod()
        );
    }
}
