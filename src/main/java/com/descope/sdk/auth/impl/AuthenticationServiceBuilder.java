package com.descope.sdk.auth.impl;

import com.descope.model.auth.AuthenticationServices;
import com.descope.model.client.Client;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthenticationServiceBuilder {
  public static AuthenticationServices buildServices(Client client) {
    return AuthenticationServices.builder()
        .authService(new AuthenticationServiceImpl(client))
        .otpService(new OTPServiceImpl(client))
        .samlService(new SAMLServiceImpl(client))
        .ssoServiceProvider(new SSOServiceProviderImpl(client))
        .totpService(new TOTPServiceImpl(client))
        .oauthService(new OAuthServiceImpl(client))
        .passwordService(new PasswordServiceImpl(client))
        .magicLinkService(new MagicLinkServiceImpl(client))
        .enchantedLinkService(new EnchantedLinkServiceImpl(client))
        .webAuthnService(new WebAuthnServiceImpl(client))
        .build();
  }
}
