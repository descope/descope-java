package com.descope.model.magiclink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MagicLinkUpdateOptions {
  private String providerId;
  private String templateId;
  private String locale;
  private Boolean failOnConflict;
}
