package com.descope.model.jwt.request;

import com.descope.model.user.User;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManagementSignUpRequest {
  private String loginId;
  private User user;
  private boolean emailVerified;
  private boolean phoneVerified;
  private String ssoAppId;
  private Map<String, Object> customClaims;
}