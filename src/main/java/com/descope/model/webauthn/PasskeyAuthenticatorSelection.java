package com.descope.model.webauthn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasskeyAuthenticatorSelection {
  public static final String AUTHENTICATOR_ATTACHMENT_ANY = "any";
  public static final String AUTHENTICATOR_ATTACHMENT_PLATFORM = "platform";
  public static final String AUTHENTICATOR_ATTACHMENT_CROSS_PLATFORM = "crossplatform";
  public static final String REQUIRE_RESIDENT_KEY_UNSPECIFIED = "unspecified";
  public static final String REQUIRE_RESIDENT_KEY_TRUE = "true";
  public static final String REQUIRE_RESIDENT_KEY_FALSE = "false";
  public static final String RESIDENT_KEY_DISCOURAGED = "discouraged";
  public static final String RESIDENT_KEY_PREFERRED = "preferred";
  public static final String RESIDENT_KEY_REQUIRED = "required";

  // One of the AUTHENTICATOR_ATTACHMENT_* values
  private String authenticatorAttachment;
  // One of the REQUIRE_RESIDENT_KEY_* values
  private String requireResidentKey;
  // One of the RESIDENT_KEY_* values
  private String residentKey;
  // One of the PasskeyOptions.USER_VERIFICATION_* values
  private String userVerification;
  private String extensionsJSON;
}
