package com.descope.model.ssoapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOApplicationSecretMeta {
  private String id;
  private String name;
  private String status;
  /** Epoch seconds; 0 means it never expires. */
  private Long expireTime;
  /** Epoch seconds. */
  private Long createdTime;
}
