package com.descope.sdk.auth.impl;

import com.descope.enums.AuthType;
import com.descope.model.auth.AuthParams;
import com.descope.model.client.Client;
import com.descope.sdk.auth.AuthenticationService;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthenticationServiceBuilder {
  public static AuthenticationService buildService(
      AuthType authType, Client client, AuthParams authParams) {
    switch (authType) {
      case OTP:
        return new OTPServiceImpl(client, authParams);
      case MAGIC_LINK:
        return new MagicLinkServiceImpl(client, authParams);
      case ENCHANTED_LINK:
        return new EnchantedLinkServiceImpl(client, authParams);
      case TOTP:
        return new TOTPServiceImpl(client, authParams);
      default:
        throw new UnsupportedOperationException("Unsupported AuthType");
    }
  }
}
