package com.descope.model.descoper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescoperAttributes {
  // Up to 254 characters
  private String displayName;
  private String email;
  private String phone;
}
