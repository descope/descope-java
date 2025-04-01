package com.descope.model.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUserPasswordMd5 {
  String hash; // the md5 hash in plaintext format, for example "68f724c9ad..."
}
