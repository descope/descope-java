package com.descope.model.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDetails {
  private UserResponse user;
  private Boolean created;

  // Keeps the previous constructor binary compatible
  public UserResponseDetails(UserResponse user) {
    this.user = user;
  }
}
