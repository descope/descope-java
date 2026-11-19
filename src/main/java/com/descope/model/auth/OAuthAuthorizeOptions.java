package com.descope.model.auth;

import com.descope.enums.OAuthPrompt;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Optional query parameters for starting an OAuth authorization. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAuthorizeOptions {
  /** Prompt values to pass to the OAuth provider. */
  private List<OAuthPrompt> prompt;
  /** Login hint to pass to the OAuth provider. */
  private String loginHint;
  /** Email address the authentication was initiated with. */
  private String initiatedEmail;
}
