package com.descope.model.auth;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeTenantsRequest {
  private boolean dct;
  private List<String> ids;
}
