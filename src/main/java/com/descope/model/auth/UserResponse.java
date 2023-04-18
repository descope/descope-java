package com.descope.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
  private String userId;
  private List<String> loginIds;
  private Boolean verifiedEmail;
  private Boolean verifiedPhone;
  private List<String> roleNames;
  private List<AssociatedTenant> userTenants;
  private String status;
  private String picture;
  private Boolean test;
}
