package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.descope.model.jwt.MgmtSignUpUser;
import com.descope.model.user.User;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "jwt-signup", description = "Sign up and receive verified JWT")
public class JwtSignUp extends HelpBase implements Callable<Integer> {

  @Option(names = {"-l", "--login-id"}, description = "Login ID", required = true)
  String loginId;

  @Option(names = {"--email"}, description = "Email address")
  String email;

  @Option(names = {"--phone"}, description = "Phone number")
  String phone;

  @Option(names = {"--name"}, description = "User's full name")
  String name;

  @Option(names = {"--verified-email"}, description = "Mark email as verified (true/false)")
  boolean verifiedEmail;

  @Option(names = {"--verified-phone"}, description = "Mark phone as verified (true/false)")
  boolean verifiedPhone;

  @Option(names = {"--sso-app-id"}, description = "SSO App ID")
  String ssoAppId;

  @Option(names = {"--custom-claims"}, description = "Custom claims (JSON format)")
  String customClaimsJson;

  @Override
  public Integer call() {
    try {
      var client = new DescopeClient(); // Assume Client is properly initialized

      // Create User object if any user-related field is set
      User user = null;
      if (email != null || phone != null || name != null) {
        user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setName(name);
      }

      // Parse custom claims JSON if provided
      Map<String, Object> customClaims = null;
      if (customClaimsJson != null && !customClaimsJson.isEmpty()) {
        customClaims = parseJsonToMap(customClaimsJson);
      }

      // Build the MgmtSignUpUser object
      var signUpUser = new MgmtSignUpUser();
      signUpUser.setUser(user);
      signUpUser.setVerifiedEmail(verifiedEmail);
      signUpUser.setVerifiedPhone(verifiedPhone);
      signUpUser.setSsoAppId(ssoAppId);
      signUpUser.setCustomClaims(customClaims);

      var authInfo = client.getManagementServices().getJwtService().signUp(loginId, signUpUser);

      System.out.println("Signup successful!");
      System.out.println("Session JWT: " + authInfo.getToken().getJwt());
      System.out.println("Refresh JWT: " + authInfo.getRefreshToken().getJwt());
      System.out.println("User ID: " + authInfo.getUser().getUserId());

      return 0; // Success
    } catch (DescopeException e) {
      System.err.println("Error during sign-up: " + e.getMessage());
      return 1; // Error
    }
  }

  private Map<String, Object> parseJsonToMap(String json) {
    try {
      return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
    } catch (Exception e) {
      System.err.println("Invalid custom claims JSON format: " + json);
      return null;
    }
  }
}