package com.descope.model.user.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for the response of listing a user's registered passkeys.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasskeysResponse {
  List<UserPasskey> passkeys;
}
