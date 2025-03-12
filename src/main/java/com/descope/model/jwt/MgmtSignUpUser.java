package com.descope.model.jwt;

import com.descope.model.user.User;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MgmtSignUpUser {
  private User user;
  boolean verifiedEmail;
  boolean verifiedPhone;
  String ssoAppId;
  Map<String, Object> customClaims;
}
