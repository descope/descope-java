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
public class AuthenticationRequestBody {
  private String loginId;
  private LoginOptions loginOptions;
}
