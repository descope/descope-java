package com.descope.model.mgmt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessKeySearchSort {
  /** Required. The field to sort by, up to 100 characters. */
  private String field;
  private Boolean desc;
}
