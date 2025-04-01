package com.descope.model.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUserPasswordPhpass {
  String hash; // the hash as base64 encoded string with . and / characters
  String salt; // the salt as base64 encoded string with . and / characters
  int iterations; // the iterations cost value (usually in the tens of thousands)
  String type; // the hash name (md5, sha512)
}
