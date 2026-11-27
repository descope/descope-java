package com.descope.model.ssoapp;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WellKnownCustomization {
  private String id;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Long version;
  /** Up to 50 fields. */
  private List<String> hiddenFields;
  private Map<String, WellKnownValueList> removedValues;
  private WellKnownValueDelta scopesSupported;
  private WellKnownValueDelta claimsSupported;
  /** Applies the customization to the served documents. */
  private Boolean enabled;
}
