package com.descope.model.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUserPasswordArgon2 {
  byte[] hash; // the hash in raw bytes (base64 strings should be decoded first)
  byte[] salt; // the salt in raw bytes (base64 strings should be decoded first)
  int iterations; // the memory cost value (usually between 1 to 10)
  int memory; // the memory cost value in kilobytes (usually between 1,000 to 1,000,000)
  int threads; // the threads cost value (usually between 1 to 10)
}
