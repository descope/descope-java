package com.descope.model.family.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A dependent to create in a family. When the login ID is empty the server derives it from the name;
 * email and phone are never used as the login ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDependentRequest {
  private String loginId;
  private String name;
  private String email;
  private String phone;
  private String givenName;
  private String middleName;
  private String familyName;
  private String picture;
  private Map<String, Object> customAttributes;
  /** Family scoped attributes (name to value) of the dependent in the family it is created in. */
  private Map<String, Object> familyScopedAttributes;
}
