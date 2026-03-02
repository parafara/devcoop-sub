package main.infrastructure;

import main.application.subscription.external.PlanRepository;
import main.domain.plan.Plan;
import main.domain.plan.PlanType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class InMemoryPlanRepository implements PlanRepository {
    private Map<Long, Plan> plans = new HashMap<>();
    private Map<PlanType, Plan> planTypeIndex = new HashMap<>();

    @Override
    public Plan save(Plan plan) {
        plans.put(plan.getId(), plan);
        planTypeIndex.put(plan.getPlanType(), plan);
        return plan;
    }

    @Override
    public Plan findById(Long planId) {
        return plans.get(planId);
    }

    @Override
    public Plan findByPlanType(PlanType planType) {
        return planTypeIndex.get(planType);
    }

    @Override
    public List<Plan> findAll() {
        return new ArrayList<>(plans.values());
    }
}
