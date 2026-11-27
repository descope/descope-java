package com.descope.model.scopeclaimmapping;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The tokens a single mapped claim is written to. An empty list means every token. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClaimTokenTargets {
  public static final String ACCESS_TOKEN = "accessToken";
  public static final String ID_TOKEN = "idToken";
  public static final String USER_INFO = "userInfo";

  /** Token names, see the constants in this class. */
  private List<String> tokens;
}
