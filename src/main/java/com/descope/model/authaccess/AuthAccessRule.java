package com.descope.model.authaccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** An auth access policy rule, evaluated first-match in its evaluation order. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthAccessRule {
  /** Server-generated, rejected on create and required on update. */
  private String id;
  private String name;
  private String description;
  /** Defaults to true on create. */
  private Boolean enabled;
  /** One of the {@link AuthAccessAction} values. Required on create. */
  private String effect;
  /**
   * A list of {@code {key, operator, values}} objects, all of which must match. For example a
   * {@code List<Map<String, Object>>}.
   */
  private Object conditions;
  /** Epoch seconds. */
  private Integer createdTime;
  /** Epoch seconds. */
  private Integer modifiedTime;
  /** 1-based position in the evaluation order, set through reorder and the settings. */
  private Integer evaluationOrder;
  /** Epoch seconds after which the rule is no longer evaluated. 0 means never. */
  private Integer expireTime;
}
