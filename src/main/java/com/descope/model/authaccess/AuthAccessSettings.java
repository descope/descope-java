package com.descope.model.authaccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Auth access policy settings. Fields left null keep their stored values. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthAccessSettings {
  /** The verdict when no rule matches, one of the {@link AuthAccessAction} values. */
  private String defaultAction;
  /**
   * The full evaluation order as a list of rule IDs, for example a {@code List<String>}. When set,
   * it must list every rule exactly once.
   */
  private Object ruleIds;
}
