package com.descope.model.authz;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A typed parameter of a {@link Condition}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ConditionParam {
  /** Parameter name, up to 100 characters. */
  String name;
  /** Parameter type, up to 50 characters. */
  String type;
}
