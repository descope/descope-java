package com.descope.model.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUserPasswordHashed {
  BatchUserPasswordBcrypt bcrypt;
  BatchUserPasswordFirebase firebase;
  BatchUserPasswordPbkdf2 pbkdf2;
  BatchUserPasswordDjango django;
  BatchUserPasswordPhpass phpass;
  BatchUserPasswordMd5 md5;
  BatchUserPasswordArgon2 argon2;
  BatchUserPasswordBuddyauth buddyauth;
}
