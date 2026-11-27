package com.descope.model.descoper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response holding a single descoper. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DescoperResponse {
  private Descoper descoper;
}
