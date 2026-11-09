package com.descope.model.auth;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthStartOptions {
  /** OAuth prompt values: none, login, consent or select_account. */
  private List<String> prompt;
  /** Not supported by {@code updateUser}. */
  private Boolean rawResponse;
  /** Hint for the provider about the user's login identifier. */
  private String loginHint;
  /** Email that initiated the flow. */
  private String initiatedEmail;
  /** Additional query params to append to the returned authorize URL. */
  private Map<String, String> authParams;
}
