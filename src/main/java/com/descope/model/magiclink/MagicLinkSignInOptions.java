package com.descope.model.magiclink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request options sent alongside {@link LoginOptions} by magic link sign-in and sign-up-or-in. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MagicLinkSignInOptions {
  private String providerId;
  private String templateId;
  private String ssoAppId;
  private Boolean useRecovery;
}
