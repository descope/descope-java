package com.descope.model.jwt.response;

import com.descope.model.jwt.SigningKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SigningKeysResponse {
  private List<SigningKey> keys;    
}
