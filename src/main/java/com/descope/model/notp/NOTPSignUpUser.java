package com.descope.model.notp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPSignUpUser {
  private String loginId;
  private String name;
  private String phone;
  private String email;
  private String givenName;
  private String middleName;
  private String familyName;
  private String recoveryEmail;
  private String recoveryPhone;
}
