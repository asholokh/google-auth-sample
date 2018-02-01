package com.asholokh.twofa.googleauth.sample.rest;

import com.asholokh.twofa.googleauth.sample.service.TotpService;
import com.asholokh.twofa.googleauth.sample.service.User;
import com.asholokh.twofa.googleauth.sample.service.UserService;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value = "/authenticate/", method = RequestMethod.POST)
public class AuthenticationRestController {
    @Value("${2fa.enabled}")
    private boolean isTwoFaEnabled;
    @Autowired
    private UserService userService;
    @Autowired
    private TotpService totpService;

    @RequestMapping(value = "{login}/{password}", method = RequestMethod.POST)
    public AuthenticationStatus authenticate(@PathVariable String login, @PathVariable String password) {
        Optional<User> user = userService.findUser(login, password);

        if (!user.isPresent()) {
            return AuthenticationStatus.FAILED;
        }
        if (!isTwoFaEnabled) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(login, password);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return AuthenticationStatus.AUTHENTICATED;
        } else {
            SecurityContextHolder.getContext().setAuthentication(null);
            return AuthenticationStatus.REQUIRE_TOKEN_CHECK;
        }
    }

    @RequestMapping("token/{login}/{password}/{token}")
    public AuthenticationStatus tokenCheck(@PathVariable String login, @PathVariable String password, @PathVariable String token) {
        Optional<User> user = userService.findUser(login, password);

        if (!user.isPresent()) {
            return AuthenticationStatus.FAILED;
        }

        if (!totpService.verifyCode(token, user.get().getSecret())) {
            return AuthenticationStatus.FAILED;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.get().getLogin(), user.get().getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return AuthenticationStatus.AUTHENTICATED;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
