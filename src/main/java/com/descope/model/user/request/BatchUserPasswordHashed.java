package com.descope.model.user.request;

import com.descope.enums.Pbkdf2Type;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatchUserPasswordHashed {
  BatchUserPasswordHashedBcrypt bcrypt;
  BatchUserPasswordHashedPbkdf2 pbkdf2;

  public static BatchUserPasswordHashed bcrypt(byte[] hash) {
    BatchUserPasswordHashedBcrypt bcrypt = BatchUserPasswordHashedBcrypt.builder().hash(hash).build();
    return BatchUserPasswordHashed.builder().bcrypt(bcrypt).build();
  }

  public static BatchUserPasswordHashed pbkdf2(byte[] hash, byte[] salt, int iterations, Pbkdf2Type type) {
    BatchUserPasswordHashedPbkdf2 pbkdf2 = BatchUserPasswordHashedPbkdf2.builder().hash(hash).salt(salt).iterations(iterations).type(type).build();
    return BatchUserPasswordHashed.builder().pbkdf2(pbkdf2).build();
  }
}
