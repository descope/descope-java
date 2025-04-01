package com.descope.model.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUserPasswordDjango {
  String hash; // the django hash in plaintext format, for example "pbkdf2_sha256$..."
}
