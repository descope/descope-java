package com.descope.model.sso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a OIDC mapping between Descope and IDP user attributes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OIDCAttributeMapping {
  private String loginId;
  private String name;
  private String givenName;
  private String middleName;
  private String familyName;
  private String email;
  private String verifiedEmail;
  private String username;
  private String phoneNumber;
  private String verifiedPhone;
  private String picture;
}
