package com.descope.model.otp;

import com.descope.model.magiclink.LoginOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationVerifyRequestBody {
  private String loginId;
  private String code;
  private LoginOptions loginOptions;
}
