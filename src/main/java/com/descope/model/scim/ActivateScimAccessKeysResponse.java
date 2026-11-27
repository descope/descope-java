package com.descope.model.scim;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivateScimAccessKeysResponse {
  private List<String> activatedKeyIds;
}
