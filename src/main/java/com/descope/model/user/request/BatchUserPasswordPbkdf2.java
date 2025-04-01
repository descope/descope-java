package com.descope.model.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUserPasswordPbkdf2 {
  byte[] hash; // the hash in raw bytes (base64 strings should be decoded first)
  byte[] salt; // the salt in raw bytes (base64 strings should be decoded first)
  int iterations; // the iterations cost value (usually in the thousands)
  String type; // the hash name (sha1, sha256, sha512)
}
