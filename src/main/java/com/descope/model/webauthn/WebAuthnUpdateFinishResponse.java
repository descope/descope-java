package com.descope.model.webauthn;

import com.descope.model.jwt.response.JWTResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebAuthnUpdateFinishResponse {
  // Set only when the update started with mfa
  private JWTResponse jwt;
}
