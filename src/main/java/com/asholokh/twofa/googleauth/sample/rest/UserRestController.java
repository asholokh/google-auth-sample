package com.asholokh.twofa.googleauth.sample.rest;

import com.asholokh.twofa.googleauth.sample.service.TotpService;
import com.asholokh.twofa.googleauth.sample.service.User;
import com.asholokh.twofa.googleauth.sample.service.UserService;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import java.util.Optional;

@RestController
public class UserRestController {
    @Value("${2fa.enabled}")
    private boolean isTwoFaEnabled;
    @Autowired
    private UserService userService;
    @Autowired
    private TotpService totpService;
    @Autowired
    private AuthenticationManager authenticationManager;


    @RequestMapping(value = "/authenticate/{login}/{password}", method = RequestMethod.POST)
    public AuthenticationStatus authenticate(@PathVariable String login, @PathVariable String password, HttpServletRequest request) {
        Optional<User> user = userService.findUser(login, password);

        if (!user.isPresent()) {
            return AuthenticationStatus.FAILED;
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(login, password);
        authentication.setDetails(new WebAuthenticationDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        authenticationManager.authenticate(authentication);
        if (!isTwoFaEnabled) {
            SecurityContextHolder.getContext().getAuthentication().setAuthenticated(true);
            return AuthenticationStatus.AUTHENTICATED;
        } else {
            SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
            return AuthenticationStatus.REQUIRE_TOKEN_CHECK;
        }
    }

    @RequestMapping("/authenticate/token/{login}/{password}/{token}")
    public AuthenticationStatus tokenCheck(@PathVariable String login, @PathVariable String password, @PathVariable String token) {
        Optional<User> user = userService.findUser(login, password);

        if (!user.isPresent()) {
            return AuthenticationStatus.FAILED;
        }

        if (!isTwoFaEnabled) {
            return AuthenticationStatus.FAILED;
        }

        Authentication preAuthentication = SecurityContextHolder.getContext().getAuthentication();

        if (preAuthentication == null || preAuthentication.getPrincipal() == null) {
            return AuthenticationStatus.FAILED;
        }

        if (!preAuthentication.getPrincipal().equals(login)) {
            return AuthenticationStatus.FAILED;
        }

        if (!totpService.verifyCode(token, user.get().getSecret())) {
            return AuthenticationStatus.FAILED;
        }

        SecurityContextHolder.getContext().getAuthentication().setAuthenticated(true);

        return AuthenticationStatus.AUTHENTICATED;
    }

    @RequestMapping(value = "/register/{login}/{password}", method = RequestMethod.POST)
    public String register(@PathVariable String login, @PathVariable String password) {
        userService.register(login, password);
        String encodedSecret = new Base32().encodeToString(userService.findUser(login, password).get().getSecret().getBytes());

        return encodedSecret.replace("=", "");
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
