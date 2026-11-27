package com.descope.model.notp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPSignInRequest {
  private String loginId;
  private NOTPLoginOptions loginOptions;
}
