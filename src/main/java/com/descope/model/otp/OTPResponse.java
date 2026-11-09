package com.descope.model.otp;

import com.descope.model.magiclink.response.Masked;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OTPResponse implements Masked {
  // Set when the code was sent by email
  private String maskedEmail;
  // Set when the code was sent to a phone
  private String maskedPhone;
  private Integer verificationAttempts;

  @Override
  @JsonIgnore
  public String getMasked() {
    return maskedEmail != null ? maskedEmail : maskedPhone;
  }
}
