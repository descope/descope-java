package com.descope.model.user.request;

import com.descope.enums.BatchUserPasswordAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUserPasswordHashed {
  BatchUserPasswordAlgorithm algorithm;
  byte[] hash;
  byte[] salt;
  int iterations;
}
