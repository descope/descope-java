package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The tokens a mapped claim is written to. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppClaimTokenTargets {
  public static final String TOKEN_ACCESS_TOKEN = "accessToken";
  public static final String TOKEN_ID_TOKEN = "idToken";
  public static final String TOKEN_USER_INFO = "userInfo";

  /** The {@code TOKEN_*} values the claim is written to. */
  private String[] tokens;
}
