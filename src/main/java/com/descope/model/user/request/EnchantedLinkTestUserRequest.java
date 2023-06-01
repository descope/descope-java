package com.descope.model.user.request;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnchantedLinkTestUserRequest {
  private TestUserRequest testUserRequest;
  private URI uri;
}
