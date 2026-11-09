package com.descope.model.family.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilySearchRequest {
  /** Family IDs, each up to 254 characters. */
  @JsonProperty("familyIds")
  List<String> ids;
  /** Family names, each up to 254 characters. */
  @JsonProperty("familyNames")
  List<String> names;
  /** Free text search, up to 254 characters. */
  String freeText;
  Map<String, Object> customAttributes;
  Integer page;
  Integer size;
}
