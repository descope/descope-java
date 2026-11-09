package com.descope.model.webauthn;

import com.descope.enums.WebAuthnAttestation;
import com.descope.enums.WebAuthnUserVerification;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasskeyOptions {
  /** Sign up only. */
  private AuthenticatorSelectionCriteria authenticatorSelection;
  private WebAuthnAttestation attestation;
  /** Sign in only. */
  private WebAuthnUserVerification userVerification;
  @JsonProperty("extensionsJSON")
  private String extensionsJSON;
}
