package com.asholokh.twofa.googleauth.sample.service;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

public class User {
    private String login;
    private String password;
    private String secret;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        this.secret = generateSecret();
    }

    private String generateSecret() {
        return RandomStringUtils.random(10, true, true);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getSecret() {
        return secret;
    }
}
