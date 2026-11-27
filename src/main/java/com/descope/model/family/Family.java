package com.descope.model.family;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Family {
  private String id;
  private String name;
  private Map<String, Object> customAttributes;
  private Boolean disabled;
  private String photo;
  private Integer createdTime;
}
