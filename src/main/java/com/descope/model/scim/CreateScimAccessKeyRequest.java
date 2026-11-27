package com.descope.model.scim;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateScimAccessKeyRequest {
  /** Required. */
  private String tenantId;
  /** The SSO configuration the key provisions into. Omit for the tenant's default configuration. */
  private String ssoId;
  private String name;
  private String description;
  /** Epoch seconds. Omit for a key that never expires. */
  @JsonSerialize(using = ToStringSerializer.class)
  private Long expireTime;
}
