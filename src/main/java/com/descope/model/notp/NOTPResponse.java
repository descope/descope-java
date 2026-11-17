package com.descope.model.notp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NOTPResponse {
  private String pendingRef;
  private String redirectUrl;
  private String image;
}
