package com.descope.model.auth;

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
public class AssociatedFamily {
  private String familyId;
  private List<String> roleNames;
  private Map<String, Object> familyScopedAttributes;
}
