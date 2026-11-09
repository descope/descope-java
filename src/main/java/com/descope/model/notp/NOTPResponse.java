package com.descope.model.notp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPResponse {
  /** Reference to the pending operation, used to poll for the session. */
  private String pendingRef;
  /** Link to the provider app, e.g. a wa.me link with the authentication message. */
  private String redirectUrl;
  /** QR code image. */
  private String image;
}
