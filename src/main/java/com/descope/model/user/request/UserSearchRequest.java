package com.descope.model.user.request;

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
  Integer limit;
  Integer page;
  Boolean withTestUsers;
  Boolean testUsersOnly;
  Map<String, Object> customAttributes;
}
