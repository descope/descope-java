package com.descope.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOptions {
  private boolean addToLoginIds;
  private boolean onMergeUseExisting;
  private String providerId;
  private String templateId;
}