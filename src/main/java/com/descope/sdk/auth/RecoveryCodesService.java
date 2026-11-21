package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.recoverycodes.RecoveryCodesResponse;

public interface RecoveryCodesService {

  /**
   * Generate a new set of recovery codes for an existing user.
   *
   * @param loginId      - User login ID
   * @param refreshToken - The user's refresh token
   * @return {@link RecoveryCodesResponse RecoveryCodesResponse} with the generated codes
   * @throws DescopeException - error upon failure
   */
  RecoveryCodesResponse generate(String loginId, String refreshToken) throws DescopeException;
}
