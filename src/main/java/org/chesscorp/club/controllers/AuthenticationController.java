package org.chesscorp.club.controllers;

import org.chesscorp.club.dto.AuthenticationRequest;
import org.chesscorp.club.dto.AuthenticationResult;
import org.chesscorp.club.dto.SubscriptionRequest;
import org.chesscorp.club.exception.NotAuthenticatedException;
import org.chesscorp.club.model.Player;
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
    private Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @Transactional
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public AuthenticationResult signup(@RequestBody SubscriptionRequest subscriptionRequest) {
        logger.debug("Signing up user {}", subscriptionRequest.getEmail());
        authenticationService.signup(
                subscriptionRequest.getEmail(),
                subscriptionRequest.getPassword(),
                subscriptionRequest.getDisplayName()
        );

        String token = authenticationService.signin(
                subscriptionRequest.getEmail(),
                subscriptionRequest.getPassword());

        Player player = authenticationService.getPlayer(token);

        return new AuthenticationResult(token, player);
    }

    @Transactional
    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    public AuthenticationResult signin(@RequestBody AuthenticationRequest authenticationRequest) {
        logger.debug("Authenticating user {}", authenticationRequest.getEmail());

        String token = authenticationService.signin(
                authenticationRequest.getEmail(),
                authenticationRequest.getPassword());

        Player player = authenticationService.getPlayer(token);

        return new AuthenticationResult(token, player);
    }

    @Transactional
    @RequestMapping(value = "/getCredentials", method = RequestMethod.POST)
    public AuthenticationResult getCredentials(@CookieValue(value = "AUTH_TOKEN", required = false) String token) {
        if (token == null) {
            logger.debug("No token found while reading credentials.");
            throw new NotAuthenticatedException("No token found in request");
        } else {
            Player player = authenticationService.getPlayer(token);
            logger.debug("Credentials found for {}", player);
            return new AuthenticationResult(token, player);
        }
    }

    @Transactional
    @RequestMapping(value = "/signout", method = RequestMethod.POST)
    public void signout(@CookieValue(value = "AUTH_TOKEN", required = false) String token) {
        if (token == null) {
            logger.debug("No token found on sign out.");
        } else {
            logger.debug("Signing out token {}", token);
            authenticationService.revoke(token);
        }
    }

}
