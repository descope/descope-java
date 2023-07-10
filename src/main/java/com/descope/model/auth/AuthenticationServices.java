package com.descope.model.auth;

import com.descope.sdk.auth.AuthenticationService;
import com.descope.sdk.auth.EnchantedLinkService;
import com.descope.sdk.auth.MagicLinkService;
import com.descope.sdk.auth.OAuthService;
import com.descope.sdk.auth.OTPService;
import com.descope.sdk.auth.PasswordService;
import com.descope.sdk.auth.SAMLService;
import com.descope.sdk.auth.TOTPService;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthenticationServices {
  AuthenticationService authService;
  OTPService otpService;
  SAMLService samlService;
  TOTPService totpService;
  OAuthService oauthService;
  PasswordService passwordService;
  MagicLinkService magicLinkService;
  EnchantedLinkService enchantedLinkService;
}
