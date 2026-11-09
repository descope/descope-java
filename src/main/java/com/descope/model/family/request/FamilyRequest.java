package com.descope.model.family.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Family fields for create and update. On update, unset (null) fields are left unchanged. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyRequest {
  /** Family name, up to 254 characters. Required on create. */
  String name;
  /** Replaces all of the family's custom attributes on update, it is not merged. */
  Map<String, Object> customAttributes;
  String photo;
  Boolean disabled;
}
