package com.descope.model.family.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A dependent (shadow profile) user to create in a family. All fields are optional. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDependentRequest {
  /** Derived from the name when missing. Email and phone are never used as the login ID. */
  String loginId;
  /** Up to 254 characters. */
  String name;
  String email;
  String phone;
  /** Up to 254 characters. */
  String givenName;
  /** Up to 254 characters. */
  String middleName;
  /** Up to 254 characters. */
  String familyName;
  String picture;
  Map<String, Object> customAttributes;
  /** Family-scoped attribute values, each an object. */
  Map<String, Object> familyScopedAttributes;
}
