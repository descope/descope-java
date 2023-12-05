package com.descope.model.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnchantedLinkTestUserResponse {
  private String pendingRef;
  private String link;
  private String loginId;
}
