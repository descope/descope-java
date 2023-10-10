package com.descope.model.authz;

import com.descope.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
@JsonInclude(Include.NON_NULL)
public class UserQuery {
  List<String> tenants;
  List<String> roles;
  String text;
  List<UserStatus> statuses;
  boolean ssoOnly;
  boolean withTestUser;
  Map<String, Object> customAttributes;
}
