package com.descope.model.user.request;

import com.descope.enums.DeliveryMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MagicLinkTestUserRequest {
  private TestUserRequest testUserRequest;
  private DeliveryMethod deliveryMethod;
  private URI uri;
}
