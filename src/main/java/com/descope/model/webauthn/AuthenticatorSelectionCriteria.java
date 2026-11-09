package com.descope.model.webauthn;

import com.descope.enums.WebAuthnAuthenticatorAttachment;
import com.descope.enums.WebAuthnRequireResidentKey;
import com.descope.enums.WebAuthnResidentKey;
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
public class AuthenticatorSelectionCriteria {
  private WebAuthnAuthenticatorAttachment authenticatorAttachment;
  private WebAuthnRequireResidentKey requireResidentKey;
  private WebAuthnResidentKey residentKey;
  private WebAuthnUserVerification userVerification;
  @JsonProperty("extensionsJSON")
  private String extensionsJSON;
}
