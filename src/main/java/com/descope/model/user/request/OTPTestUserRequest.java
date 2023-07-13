package com.descope.model.user.request;

import com.descope.enums.DeliveryMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPTestUserRequest {
  private String loginId;
  private DeliveryMethod deliveryMethod;
}
