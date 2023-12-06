package com.descope.model.magiclink.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaskedPhoneRes implements Masked {

  // Masked phone to which the message was sent
  private String maskedPhone;

  @Override
  public String getMasked() {
    return maskedPhone;
  }
}
