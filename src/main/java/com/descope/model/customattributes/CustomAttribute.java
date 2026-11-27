package com.descope.model.customattributes;

import java.util.List;
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
  private String displayName;
  private List<String> viewPermissions;
  private List<String> editPermissions;
  private Boolean editable;
  /** Optional default value: a string, number, boolean or list of strings matching the type. */
  private Object defaultValue;

  public CustomAttribute(String name, int type, List<CustomAttributeOption> options, String displayName) {
    this(name, type, options, displayName, null, null, null, null);
  }
}
