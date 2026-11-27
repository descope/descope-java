package com.descope.model.family.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Search options for families. Leaving every option empty returns all families. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilySearchRequest {
  @JsonProperty("familyIds")
  List<String> ids;
  @JsonProperty("familyNames")
  List<String> names;
  @JsonProperty("freeText")
  String text;
  Map<String, Object> customAttributes;
  /** The page to return, starting at 0. */
  Integer page;
  /** The page size, up to 1000. 0 or null uses the server default. */
  Integer size;
}
