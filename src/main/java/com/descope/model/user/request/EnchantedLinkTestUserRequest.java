package com.descope.model.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnchantedLinkTestUserRequest {
  private TestUserRequest testUserRequest;
  private URI uri;
}
