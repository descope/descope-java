package com.descope.model.webauthn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasskeyOptions {
  public static final String ATTESTATION_NONE = "none";
  public static final String ATTESTATION_INDIRECT = "indirect";
  public static final String ATTESTATION_DIRECT = "direct";
  public static final String USER_VERIFICATION_PREFERRED = "preferred";
  public static final String USER_VERIFICATION_REQUIRED = "required";
  public static final String USER_VERIFICATION_DISCOURAGED = "discouraged";

  // Used on sign up and device update
  private PasskeyAuthenticatorSelection authenticatorSelection;
  // One of the ATTESTATION_* values, used on sign up and device update
  private String attestation;
  // One of the USER_VERIFICATION_* values, used on sign in
  private String userVerification;
  private String extensionsJSON;
}
