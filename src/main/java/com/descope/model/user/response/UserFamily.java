package com.descope.model.user.response;

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
public class UserFamily {
  private String familyId;
  private List<String> roleNames;
  private List<String> permissions;
  private Map<String, Object> familyScopedAttributes;
}
