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
  private String recoveryEmail;
  private String recoveryPhone;

  public User(String name, String email, String phone) {
    this(name, email, phone, null, null, null);
  }

  public User(String name, String email, String phone, String givenName, String middleName,
      String familyName) {
    this(name, email, phone, givenName, middleName, familyName, null, null);
  }
}
