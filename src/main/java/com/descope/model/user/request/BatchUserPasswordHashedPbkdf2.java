package com.descope.model.user.request;

import com.descope.enums.Pbkdf2Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BatchUserPasswordHashedPbkdf2 {
  byte[] hash;
  byte[] salt;
  int iterations;
  Pbkdf2Type type;
}
