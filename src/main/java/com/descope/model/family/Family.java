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
  String id;
  String name;
  Map<String, Object> customAttributes;
  Boolean disabled;
  String photo;
  Integer createdTime;
}
