package com.descope.model.customattributes;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomAttribute {
  private String name;
  private int type;
  private List<CustomAttributeOption> options;
  /** Up to 254 characters. */
  private String displayName;
  /** A protobuf {@code Any} JSON object, with an {@code @type} key. */
  private Map<String, Object> defaultValue;
  /** Permission names, each up to 254 characters. */
  private List<String> viewPermissions;
  /** Permission names, each up to 254 characters. */
  private List<String> editPermissions;
  private Boolean editable;

  // Keeps the original public constructor for existing callers.
  public CustomAttribute(String name, int type, List<CustomAttributeOption> options, String displayName) {
    this(name, type, options, displayName, null, null, null, null);
  }
}
