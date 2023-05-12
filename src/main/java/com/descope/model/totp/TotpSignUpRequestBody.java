package com.descope.model.totp;

import com.descope.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class TotpSignUpRequestBody {
  private String LoginID;
  private User user;
}
