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
public class WellKnownValueDelta {
  /** Values advertised in addition to the base values. Up to 100 values. */
  private List<String> extra;
  /** Values removed from the base values. Up to 100 values. */
  private List<String> excluded;
}
