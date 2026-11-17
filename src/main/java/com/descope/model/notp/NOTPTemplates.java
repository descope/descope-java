package com.descope.model.notp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPTemplates {
  private String verifyTemplateId;
  private String successTemplateId;
  private String errorTemplateId;
}
