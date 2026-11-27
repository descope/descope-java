package com.descope.model.descoper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Descoper attributes. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DescoperAttributes {
  private String displayName;
  private String email;
  private String phone;
}
