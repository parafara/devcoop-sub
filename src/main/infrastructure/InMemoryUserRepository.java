package main.infrastructure;

import main.application.user.external.UserRepository;
import main.domain.user.User;
import java.util.HashMap;
import java.util.Map;

public class InMemoryUserRepository implements UserRepository {
    private Map<Long, User> users = new HashMap<>();
    private Map<String, User> emailIndex = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public User save(User user) {
        User newUser = new User(nextId++, user.getName(), user.getEmail(), user.getPassword(), user.getSubscription());
        users.put(newUser.getId(), newUser);
        emailIndex.put(newUser.getEmail(), newUser);
        return newUser;
    }

    @Override
    public User findByEmail(String email) {
        return emailIndex.get(email);
    }
}
