package com.descope.model.user.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllUsersResponseDetails {
  private List<UserResponse> users;
  private int total;
}
