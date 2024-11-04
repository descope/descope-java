package com.descope.model.user.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersBatchResponse {
  private List<UserResponse> createdUsers;
  private List<UserFailedResponse> failedUsers;
  private Map<String, String> additionalErrors;
}
