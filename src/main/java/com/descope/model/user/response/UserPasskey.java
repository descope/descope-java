package com.descope.model.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a registered passkey (WebAuthn credential) for a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasskey {
  String id;
  String rpId;
  String kind;
  String displayName;
  Integer createdTime;
}
