package com.descope.model.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BatchUserRequest extends UserRequest {
  String loginId;
  String password;
  BatchUserPasswordHashed hashedPassword;
}
