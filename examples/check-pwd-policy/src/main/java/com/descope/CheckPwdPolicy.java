package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.descope.model.passwordsettings.PasswordSettings;

public class CheckPwdPolicy {
  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: CheckPwdPolicy <loginId> <password>");
      System.exit(1);
    }
    try {
      var client = new DescopeClient();
      var userService = client.getManagementServices().getUserService();
      var pwdPolicyService = client.getManagementServices().getPasswordSettingsService();
      var res = userService.load(args[0]);
      var commonPasswordSettings = PasswordSettings.builder()
          .enabled(true)
          .minLength(Integer.MIN_VALUE)
          .expirationWeeks(Integer.MAX_VALUE)
          .reuseAmount(Integer.MIN_VALUE)
          .lockAttempts(Integer.MAX_VALUE)
          .build();
      if (res.getUser() != null && res.getUser().getUserTenants() != null) {
        for (var tenant : res.getUser().getUserTenants()) {
          var policy = pwdPolicyService.getSettings(tenant.getTenantId());
          if (policy != null && policy.getEnabled()) {
            if (policy.getMinLength() != null && policy.getMinLength() > commonPasswordSettings.getMinLength()) {
              commonPasswordSettings.setMinLength(policy.getMinLength());
            }
            if (policy.getLowercase() != null && policy.getLowercase()) {
              commonPasswordSettings.setLowercase(policy.getLowercase());
            }
            if (policy.getUppercase() != null && policy.getUppercase()) {
              commonPasswordSettings.setUppercase(policy.getUppercase());
            }
            if (policy.getNumber() != null && policy.getNumber()) {
              commonPasswordSettings.setNumber(policy.getNumber());
            }
            if (policy.getNonAlphanumeric() != null && policy.getNonAlphanumeric()) {
              commonPasswordSettings.setNonAlphanumeric(policy.getNonAlphanumeric());
            }
            if (policy.getExpiration() != null && policy.getExpiration()) {
              commonPasswordSettings.setExpiration(policy.getExpiration());
              if (policy.getExpirationWeeks() != null
                  && policy.getExpirationWeeks() < commonPasswordSettings.getExpirationWeeks()) {
                commonPasswordSettings.setExpirationWeeks(policy.getExpirationWeeks());
              }
            }
            if (policy.getReuse() != null && policy.getReuse()) {
              commonPasswordSettings.setReuse(policy.getReuse());
              if (policy.getReuseAmount() != null
                  && policy.getReuseAmount() > commonPasswordSettings.getReuseAmount()) {
                commonPasswordSettings.setReuseAmount(policy.getReuseAmount());
              }
            }
            if (policy.getLock() != null && policy.getLock()) {
              commonPasswordSettings.setLock(policy.getLock());
              if (policy.getLockAttempts() != null
                  && policy.getLockAttempts() < commonPasswordSettings.getLockAttempts()) {
                commonPasswordSettings.setLockAttempts(policy.getLockAttempts());
              }
            }
          }
        }
        if (args[1].length() < commonPasswordSettings.getMinLength()) {
          System.out.println("Password does not meet the minimum length requirement");
        }
        if (commonPasswordSettings.getLowercase() && !args[1].matches(".*[a-z].*")) {
          System.out.println("Password does not contain lowercase characters");
        }
        if (commonPasswordSettings.getUppercase() && !args[1].matches(".*[A-Z].*")) {
          System.out.println("Password does not contain uppercase characters");
        }
        if (commonPasswordSettings.getNumber() && !args[1].matches(".*[0-9].*")) {
          System.out.println("Password does not contain numeric characters");
        }
        if (commonPasswordSettings.getNonAlphanumeric() && args[1].matches("[a-zA-Z0-9]*")) {
          System.out.println("Password does not contain non-alphanumeric characters");
        }
      }
    } catch (DescopeException de) {
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
  }
}
