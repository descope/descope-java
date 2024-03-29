package com.descope.model.mgmt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessKeyResponse {
  private AccessKeyResponseDetails key;
  private String cleartext;
}
