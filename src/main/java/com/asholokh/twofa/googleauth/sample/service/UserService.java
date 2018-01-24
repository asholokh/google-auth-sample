package com.asholokh.twofa.googleauth.sample.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final List<User> users = new ArrayList<>();

    public void register(String login, String password) {
        User user = new User(login, password);
        users.add(user);
    }

    public Optional<User> findUser(String login, String password) {
        return users.stream()
                .filter(user -> user.getLogin().equals(login) && user.getPassword().equals(password))
                .findFirst();
    }
}
