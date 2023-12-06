package com.descope.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private String name;
  private String email;
  private String phone;
  private String givenName;
  private String middleName;
  private String familyName;

  public User(String name, String email, String phone) {
    this(name, email, phone, null, null, null);
  }
}
