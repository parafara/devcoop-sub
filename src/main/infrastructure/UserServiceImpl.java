package main.infrastructure;

import main.application.user.internal.UserService;
import main.application.user.dto.request.UserLogInRequestDto;
import main.application.user.dto.request.UserSignUpRequestDto;
import main.application.user.dto.response.UserResponseDto;
import main.application.subscription.external.SubscriptionRepository;
import main.application.subscription.external.PlanRepository;
import main.application.subscription.external.PaymentRepository;
import main.application.user.external.UserRepository;
import main.application.user.exception.UserNotFoundException;
import main.domain.payment.Payment;
import main.domain.payment.PaymentStatus;
import main.domain.plan.Plan;
import main.domain.plan.PlanType;
import main.domain.subscription.Subscription;
import main.domain.user.User;

import java.time.LocalDateTime;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;

    public UserServiceImpl(UserRepository userRepository, SubscriptionRepository subscriptionRepository,
                         PlanRepository planRepository, PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public UserResponseDto signUp(UserSignUpRequestDto request) {
        // 중복 이메일 확인
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 기본 사용자 생성 (FREE 플랜)
        Plan freePlan = planRepository.findByPlanType(PlanType.FREE);
        Payment freePayment = new Payment(System.currentTimeMillis(), PaymentStatus.PAID, freePlan.getCharge(), LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(freePayment);
        Subscription subscription = new Subscription(System.currentTimeMillis(), freePlan, savedPayment);
        subscription.activateSubscription();
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        User user = new User(null, request.getName(), request.getEmail(), request.getPassword(), savedSubscription);
        User savedUser = userRepository.save(user);

        if (subscriptionRepository instanceof InMemorySubscriptionRepository) {
            ((InMemorySubscriptionRepository) subscriptionRepository).linkUserToSubscription(savedUser.getId(), savedSubscription.getId());
        }

        return new UserResponseDto(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getPassword());
    }

    @Override
    public UserResponseDto logIn(UserLogInRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new UserNotFoundException("이메일 또는 비밀번호가 틀렸습니다.");
        }

        return new UserResponseDto(user.getId(), user.getName(), user.getEmail(), user.getPassword());
    }
}
