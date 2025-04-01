package com.descope.model.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUserPasswordFirebase {
  byte[] hash; // the hash in raw bytes (base64 strings should be decoded first)
  byte[] salt; // the salt in raw bytes (base64 strings should be decoded first)
  byte[] saltSeparator; // the salt separator (usually 1 byte long)
  byte[] signerKey; // the signer key (base64 strings should be decoded first)
  int memory; // the memory cost value (usually between 12 to 17)
  int rounds; // the rounds cost value (usually between 6 to 10)
}
