package com.descope.sdk.auth.impl;

import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationServices;
import com.descope.model.client.Client;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthenticationServiceBuilder {
  public static AuthenticationServices buildServices(Client client, AuthParams authParams) {
    return AuthenticationServices.builder()
        .otpService(new OTPServiceImpl(client, authParams))
        .magicLinkService(new MagicLinkServiceImpl(client, authParams))
        .enchantedLinkService(new EnchantedLinkServiceImpl(client, authParams))
        .totpService(new TOTPServiceImpl(client, authParams))
        .oAuthService(new OAuthServiceImpl(client, authParams))
        .samlService(new SAMLServiceImpl(client, authParams))
        .build();
  }
}
