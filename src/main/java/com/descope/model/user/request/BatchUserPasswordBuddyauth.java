package com.descope.model.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUserPasswordBuddyauth {
  String hash; // the BuddyAuth hash in plaintext format, for example "bcrypt+sha512$..."
}
