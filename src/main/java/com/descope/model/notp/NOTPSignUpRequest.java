package com.descope.model.notp;

import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPSignUpRequest {
  private String loginId;
  private String phone;
  private User user;
  private SignUpOptions loginOptions;
}
