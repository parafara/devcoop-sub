package main.infrastructure;

import main.application.subscription.external.SubscriptionRepository;
import main.domain.subscription.Subscription;
import java.util.HashMap;
import java.util.Map;

public class InMemorySubscriptionRepository implements SubscriptionRepository {
    private Map<Long, Subscription> subscriptions = new HashMap<>();
    private Map<Long, Long> userSubscriptionIndex = new HashMap<>(); // userId -> subscriptionId
    private Long nextId = 1L;

    @Override
    public Subscription save(Subscription subscription) {
        if (subscription.getId() == null) {
            subscription = new Subscription(nextId++, subscription.getPlan(), subscription.getPayment(), subscription.getEndDate());
        }
        subscriptions.put(subscription.getId(), subscription);
        return subscription;
    }

    @Override
    public Subscription update(Subscription subscription) {
        subscriptions.put(subscription.getId(), subscription);
        return subscription;
    }

    @Override
    public Subscription findByUserId(Long userId) {
        Long subscriptionId = userSubscriptionIndex.get(userId);
        if (subscriptionId != null) {
            return subscriptions.get(subscriptionId);
        }
        return null;
    }

    public void linkUserToSubscription(Long userId, Long subscriptionId) {
        userSubscriptionIndex.put(userId, subscriptionId);
    }
}
