package com.descope.model.magiclink;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaskedEmailRes implements Masked {

  // Masked email to which the message was sent
  private String maskedEmail;

  @Override
  public String getMasked() {
    return maskedEmail;
  }
}
