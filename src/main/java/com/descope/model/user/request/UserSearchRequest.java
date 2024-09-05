package com.descope.model.user.request;

import com.descope.enums.UserStatus;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {
  List<String> tenantIds;
  List<String> roles;
  @Builder.Default
  Integer limit = 0;
  @Builder.Default
  Integer page = 0;
  Boolean withTestUser;
  Boolean testUsersOnly;
  Map<String, Object> customAttributes;
  List<UserStatus> statuses;
  List<String> emails;
  List<String> phones;
  List<String> loginIds;
  List<String> ssoAppIds;
}
