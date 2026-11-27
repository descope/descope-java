package com.descope.model.notp;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The loginOptions sent by NOTP requests, holding only the fields the endpoints accept. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class NOTPLoginOptions {
  private boolean stepup;
  private boolean mfa;
  private Map<String, Object> customClaims;
  private Map<String, String> templateOptions;
  private boolean revokeOtherSessions;
  private String[] revokeOtherSessionsTypes;
  private String locale;
  private String templateId;
}
