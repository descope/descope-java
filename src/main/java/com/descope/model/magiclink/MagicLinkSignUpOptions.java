package com.descope.model.magiclink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request options sent alongside {@link SignUpOptions} by magic link sign-up. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MagicLinkSignUpOptions {
  /** The user's phone, for SMS and WhatsApp delivery only. */
  private String phone;
  private String providerId;
  private String templateId;
}
