package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.descope.model.magiclink.LoginOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "jwt-signin", description = "Sign in and receive verified JWT")
public class JwtSignIn extends HelpBase implements Callable<Integer> {

    @Option(names = { "-l", "--login-id" }, description = "Login ID", required = true)
    String loginId;

    @Option(names = { "-j", "--jwt" }, description = "JWT token for authentication", required = false)
    String jwt;

    @Override
    public Integer call() {
        try {
            LoginOptions loginOptions = new LoginOptions();
            loginOptions.setJwt(jwt); // Optional JWT input

            var client = new DescopeClient(); // Assume Client is properly initialized
            var authInfo = client.getManagementServices().getJwtService().signIn(loginId, loginOptions);

            System.out.println("Sign-in successful!");
            System.out.println("Session JWT: " + authInfo.getToken().getJwt());
            System.out.println("Refresh JWT: " + authInfo.getRefreshToken().getJwt());
            System.out.println("User ID: " + authInfo.getUser().getUserId());

            return 0; // Success
        } catch (DescopeException e) {
            System.err.println("Error during sign-in: " + e.getMessage());
            return 1; // Error
        }
    }
}