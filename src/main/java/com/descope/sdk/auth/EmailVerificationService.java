package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.emailverification.EmailVerificationStartResponse;

public interface EmailVerificationService {

  /**
   * Start email verification by getting a single-use nonce for the hidden email-verification-token input
   * of a sign-in page. The browser binds it into the email verification token it issues, so the token can
   * only be redeemed once and only for this project. Beta, available when the project enabled Email
   * Verification.
   *
   * @return {@link EmailVerificationStartResponse EmailVerificationStartResponse} with the nonce
   * @throws DescopeException - error upon failure
   */
  EmailVerificationStartResponse start() throws DescopeException;
}
