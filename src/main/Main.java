package main;

import main.application.subscription.external.PaymentRepository;
import main.application.subscription.external.PlanRepository;
import main.application.subscription.external.SubscriptionRepository;
import main.application.user.external.UserRepository;
import main.domain.payment.Payment;
import main.domain.payment.PaymentStatus;
import main.domain.plan.Plan;
import main.domain.plan.PlanType;
import main.domain.subscription.Subscription;
import main.infrastructure.InMemoryPaymentRepository;
import main.infrastructure.InMemoryPlanRepository;
import main.infrastructure.InMemorySubscriptionRepository;
import main.infrastructure.InMemoryUserRepository;
import main.infrastructure.SubscriptionServiceImpl;
import main.infrastructure.UserServiceImpl;
import main.application.subscription.internal.SubscriptionService;
import main.application.user.internal.UserService;
import main.application.subscription.dto.SubscriptionStatusDto;
import main.application.subscription.dto.PlanInfoDto;
import main.application.user.dto.request.UserSignUpRequestDto;
import main.application.user.dto.request.UserLogInRequestDto;
import main.application.user.dto.response.UserResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static UserRepository userRepository;
    private static PlanRepository planRepository;
    private static SubscriptionRepository subscriptionRepository;
    private static PaymentRepository paymentRepository;
    private static UserService userService;
    private static SubscriptionService subscriptionService;
    private static Scanner scanner;

    public static void main(String[] args) {
        initialize();
        runMenu();
    }

    private static void initialize() {
        userRepository = new InMemoryUserRepository();
        planRepository = new InMemoryPlanRepository();
        subscriptionRepository = new InMemorySubscriptionRepository();
        paymentRepository = new InMemoryPaymentRepository();
        userService = new UserServiceImpl(userRepository, subscriptionRepository, planRepository, paymentRepository);
        subscriptionService = new SubscriptionServiceImpl(subscriptionRepository, planRepository, paymentRepository);
        scanner = new Scanner(System.in);
        
        // 초기 플랜 데이터 생성
        initializePlans();
    }

    private static void initializePlans() {
        planRepository.save(new Plan(1L, PlanType.FREE, 0L, 30L, LocalDateTime.now()));
        planRepository.save(new Plan(2L, PlanType.PRO, 9900L, 30L, LocalDateTime.now()));
        planRepository.save(new Plan(3L, PlanType.BUSINESS, 29900L, 30L, LocalDateTime.now()));
    }

    private static void runMenu() {
        while (true) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    signUp();
                    break;
                case "2":
                    logIn();
                    break;
                case "3":
                    viewPlans();
                    break;
                case "4":
                    runDemo();
                    break;
                case "5":
                    System.out.println("프로그램을 종료합니다.");
                    scanner.close();
                    return;
                default:
                    System.out.println("잘못된 입력입니다.\n");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n===== SaaS 구독 및 결제 시스템 =====");
        System.out.println("1. 회원가입");
        System.out.println("2. 로그인");
        System.out.println("3. 구독 플랜 조회");
        System.out.println("4. 데모 실행");
        System.out.println("5. 종료");
        System.out.print("선택: ");
    }

    private static void signUp() {
        System.out.print("이메일 입력: ");
        String email = scanner.nextLine().trim();
        System.out.print("비밀번호 입력: ");
        String password = scanner.nextLine().trim();
        System.out.print("이름 입력: ");
        String name = scanner.nextLine().trim();

        try {
            UserSignUpRequestDto request = new UserSignUpRequestDto(name, email, password);
            UserResponseDto response = userService.signUp(request);
            System.out.println("\n✓ 회원가입 성공!");
            System.out.println("ID: " + response.getId());
            System.out.println("이메일: " + response.getEmail());
            System.out.println("이름: " + response.getName());
        } catch (Exception e) {
            System.out.println("\n✗ 회원가입 실패: " + e.getMessage());
        }
    }

    private static void logIn() {
        System.out.print("이메일 입력: ");
        String email = scanner.nextLine().trim();
        System.out.print("비밀번호 입력: ");
        String password = scanner.nextLine().trim();

        try {
            UserLogInRequestDto request = new UserLogInRequestDto(email, password);
            UserResponseDto response = userService.logIn(request);
            System.out.println("\n✓ 로그인 성공!");
            System.out.println("ID: " + response.getId());
            System.out.println("이메일: " + response.getEmail());
            System.out.println("이름: " + response.getName());
            
            showSubscriptionMenu(response.getId());
        } catch (Exception e) {
            System.out.println("\n✗ 로그인 실패: " + e.getMessage());
        }
    }

    private static void showSubscriptionMenu(Long userId) {
        while (true) {
            System.out.println("\n===== 구독 메뉴 (ID: " + userId + ") =====");
            System.out.println("1. 구독 상태 확인");
            System.out.println("2. 플랜 목록 보기");
            System.out.println("3. 플랜 구독");
            System.out.println("4. 플랜 변경");
            System.out.println("5. 이전 메뉴로");
            System.out.print("선택: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    checkSubscriptionStatus(userId);
                    break;
                case "2":
                    viewPlansForSubscription();
                    break;
                case "3":
                    subscribeToPlan(userId);
                    break;
                case "4":
                    changePlan(userId);
                    break;
                case "5":
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
    }

    private static void checkSubscriptionStatus(Long userId) {
        try {
            SubscriptionStatusDto status = subscriptionService.getSubscriptionStatus(userId);
            System.out.println("\n=== 구독 상태 ===");
            System.out.println("플랜: " + status.getCurrentPlan().getValue());
            System.out.println("활성화: " + (status.isActive() ? "O" : "X"));
            System.out.println("종료 날짜: " + status.getEndDate());
        } catch (Exception e) {
            System.out.println("\n✗ 구독 상태 조회 실패: " + e.getMessage());
        }
    }

    private static void viewPlans() {
        List<PlanInfoDto> plans = subscriptionService.getPlanList();
        System.out.println("\n===== 구독 플랜 =====");
        for (PlanInfoDto plan : plans) {
            System.out.println(plan.getPlanType().getValue() + " - 가격: " + plan.getCharge() + "원, 기간: " + plan.getPeriod() + "일");
        }
    }

    private static void viewPlansForSubscription() {
        List<PlanInfoDto> plans = subscriptionService.getPlanList();
        System.out.println("\n===== 구독 플랜 =====");
        for (PlanInfoDto plan : plans) {
            System.out.println(plan.getPlanType().getValue() + " - 가격: " + plan.getCharge() + "원, 기간: " + plan.getPeriod() + "일");
        }
    }

    private static void subscribeToPlan(Long userId) {
        viewPlansForSubscription();
        System.out.print("구독할 플랜 선택 (FREE/PRO/BUSINESS): ");
        String planType = scanner.nextLine().trim().toUpperCase();

        try {
            Plan plan = planRepository.findByPlanType(PlanType.valueOf(planType));
            Payment payment = new Payment(System.currentTimeMillis(), PaymentStatus.PAID, plan.getCharge(), LocalDateTime.now());
            Payment savedPayment = paymentRepository.save(payment);
            Subscription subscription = new Subscription(System.currentTimeMillis(), plan, savedPayment);
            subscription.activateSubscription();
            subscriptionRepository.save(subscription);

            System.out.println("\n✓ 플랜 구독 성공!");
            System.out.println("플랜: " + planType);
            System.out.println("금액: " + plan.getCharge() + "원");
        } catch (IllegalArgumentException e) {
            System.out.println("\n✗ 잘못된 플랜입니다.");
        } catch (Exception e) {
            System.out.println("\n✗ 구독 실패: " + e.getMessage());
        }
    }

    private static void changePlan(Long userId) {
        try {
            SubscriptionStatusDto currentStatus = subscriptionService.getSubscriptionStatus(userId);
            System.out.println("\n현재 플랜: " + currentStatus.getCurrentPlan().getValue());
            
            viewPlansForSubscription();
            System.out.print("변경할 플랜 선택 (FREE/PRO/BUSINESS): ");
            String newPlanType = scanner.nextLine().trim().toUpperCase();
            
            Plan newPlan = planRepository.findByPlanType(PlanType.valueOf(newPlanType));
            SubscriptionStatusDto result = subscriptionService.changePlan(userId, newPlan.getId());
            
            System.out.println("\n✓ 플랜 변경 성공!");
            System.out.println("새로운 플랜: " + result.getCurrentPlan().getValue());
        } catch (Exception e) {
            System.out.println("\n✗ 플랜 변경 실패: " + e.getMessage());
        }
    }

    private static void runDemo() {
        System.out.println("\n===== 데모 실행 =====\n");
        
        // 1. 회원가입
        System.out.println("1. 사용자 'john@example.com' 회원가입");
        UserSignUpRequestDto signUpRequest = new UserSignUpRequestDto("John Doe", "john@example.com", "password123");
        UserResponseDto signUpResponse = userService.signUp(signUpRequest);
        Long userId = signUpResponse.getId();
        System.out.println("✓ 회원가입 완료: " + signUpResponse.getName() + " (ID: " + userId + ")\n");
        
        // 2. 플랜 목록 조회
        System.out.println("2. 구독 가능한 플랜 목록 조회");
        List<PlanInfoDto> plans = subscriptionService.getPlanList();
        for (PlanInfoDto plan : plans) {
            System.out.println("  - " + plan.getPlanType().getValue() + ": " + plan.getCharge() + "원 / " + plan.getPeriod() + "일");
        }
        System.out.println();
        
        // 3. PRO 플랜 구독
        System.out.println("3. PRO 플랜 구독 (9,900원)");
        Plan proPlan = planRepository.findByPlanType(PlanType.PRO);
        Payment payment = new Payment(1L, PaymentStatus.PAID, proPlan.getCharge(), LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);
        Subscription subscription = new Subscription(1L, proPlan, savedPayment);
        subscription.activateSubscription();
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        if (subscriptionRepository instanceof InMemorySubscriptionRepository) {
            ((InMemorySubscriptionRepository) subscriptionRepository).linkUserToSubscription(userId, savedSubscription.getId());
        }
        System.out.println("✓ PRO 플랜 구독 완료\n");
        
        // 4. 구독 상태 확인
        System.out.println("4. 구독 상태 확인");
        SubscriptionStatusDto status = subscriptionService.getSubscriptionStatus(userId);
        System.out.println("  - 플랜: " + status.getCurrentPlan().getValue());
        System.out.println("  - 활성화: " + (status.isActive() ? "O" : "X"));
        System.out.println("  - 종료 날짜: " + status.getEndDate());
        System.out.println();
        
        // 5. 플랜 변경
        System.out.println("5. BUSINESS 플랜으로 변경 (29,900원)");
        Plan businessPlan = planRepository.findByPlanType(PlanType.BUSINESS);
        SubscriptionStatusDto updatedStatus = subscriptionService.changePlan(userId, businessPlan.getId());
        System.out.println("✓ 플랜 변경 완료");
        System.out.println("  - 새로운 플랜: " + updatedStatus.getCurrentPlan().getValue());
        System.out.println();
        
        // 6. FREE 플랜으로 변경 (취소)
        System.out.println("6. FREE 플랜으로 변경 (구독 취소)");
        Plan freePlan = planRepository.findByPlanType(PlanType.FREE);
        SubscriptionStatusDto canceledStatus = subscriptionService.changePlan(userId, freePlan.getId());
        System.out.println("✓ 플랜 변경 완료");
        System.out.println("  - 새로운 플랜: " + canceledStatus.getCurrentPlan().getValue());
        System.out.println();
        
        System.out.println("===== 데모 완료 =====\n");
    }
}
