package com.descope.model.auth;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyTenant {
  private String id;
  private String name;
  private Map<String, Object> customAttributes;
}
