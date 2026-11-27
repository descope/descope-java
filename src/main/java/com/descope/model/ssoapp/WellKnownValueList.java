package com.descope.model.ssoapp;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WellKnownValueList {
  /** Up to 100 values. */
  private List<String> values;
}
