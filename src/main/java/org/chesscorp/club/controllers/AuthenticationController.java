package org.chesscorp.club.controllers;

import org.chesscorp.club.dto.AuthenticationResult;
import org.chesscorp.club.exception.NotAuthenticatedException;
import org.chesscorp.club.model.Account;
import org.chesscorp.club.model.Player;
import org.chesscorp.club.model.Session;
import org.chesscorp.club.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @author Yannick Kirschhoffer alcibiade@alcibiade.org
 */
@RestController
@RequestMapping("/api/authentication")
public class AuthenticationController {
    public static final String AUTHENTICATION_TOKEN = "AUTH_TOKEN";
    private Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @Transactional
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public AuthenticationResult signup(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String displayName) {
        logger.debug("Signing up user {}", email);
        authenticationService.signup(email, password, displayName);

        String token = authenticationService.signin(email, password);

        Session session = authenticationService.getSession(token);
        Account account = session.getAccount();
        Player player = account.getPlayer();

        return new AuthenticationResult(token, account, player);
    }

    @Transactional
    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    public AuthenticationResult signin(
            @RequestParam String email,
            @RequestParam String password) {
        logger.debug("Authenticating user {}", email);

        String token = authenticationService.signin(email, password);

        Session session = authenticationService.getSession(token);
        Account account = session.getAccount();
        Player player = account.getPlayer();

        return new AuthenticationResult(token, account, player);
    }

    @Transactional
    @RequestMapping(value = "/getCredentials", method = RequestMethod.POST)
    public AuthenticationResult getCredentials(@CookieValue(value = AUTHENTICATION_TOKEN, required = false) String token) {
        if (token == null) {
            logger.debug("No token found while reading credentials.");
            throw new NotAuthenticatedException("No token found in request");
        } else {
            Session session = authenticationService.getSession(token);
            Account account = session.getAccount();
            Player player = account.getPlayer();
            logger.debug("Credentials found for {}", player);
            return new AuthenticationResult(token, account, player);
        }
    }

    @Transactional
    @RequestMapping(value = "/signout", method = RequestMethod.POST)
    public void signout(@CookieValue(value = AUTHENTICATION_TOKEN, required = false) String token) {
        if (token == null) {
            logger.debug("No token found on sign out.");
        } else {
            logger.debug("Signing out token {}", token);
            authenticationService.revoke(token);
        }
    }

}
