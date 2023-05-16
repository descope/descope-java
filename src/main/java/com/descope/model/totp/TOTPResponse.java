package com.descope.model.totp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TOTPResponse {
  private String provisioningURL;
  private String image;
  private String key;
}
