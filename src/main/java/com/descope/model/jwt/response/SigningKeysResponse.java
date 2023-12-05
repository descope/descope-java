package com.descope.model.jwt.response;

import com.descope.model.jwt.SigningKey;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SigningKeysResponse {
  private List<SigningKey> keys;    
}
