package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** An inbound application secret, without its value. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppSecretMeta {
  private String id;
  private String name;
  /** "active", "disabled" or "expired". */
  private String status;
  /** Epoch seconds; 0 means the secret never expires. */
  private Integer expireTime;
  /** Epoch seconds. */
  private Integer createdTime;
}
