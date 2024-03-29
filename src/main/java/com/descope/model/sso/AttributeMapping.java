package com.descope.model.sso;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a SAML mapping between Descope and IDP user attributes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeMapping {
  private String name;
  private String givenName;
  private String middleName;
  private String familyName;
  private String picture;
  private String email;
  private String phoneNumber;
  private String group;
  private Map<String, String> customAttributes;
}
