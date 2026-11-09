package com.descope.model.webauthn;

import com.descope.model.jwt.response.JWTResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebAuthnUpdateFinishResponse {
  /** Set only when the matching update start used mfa with a valid refresh token. */
  private JWTResponse jwt;
}
