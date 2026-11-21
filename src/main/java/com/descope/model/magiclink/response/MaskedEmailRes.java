package com.descope.model.magiclink.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaskedEmailRes implements Masked {

  // Masked email to which the message was sent
  private String maskedEmail;
  private Integer verificationAttempts;

  public MaskedEmailRes(String maskedEmail) {
    this.maskedEmail = maskedEmail;
  }

  @Override
  public String getMasked() {
    return maskedEmail;
  }
}
