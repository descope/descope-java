package com.descope.model.family.response;

import com.descope.model.family.FamilyDependent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDependentResponse {
  private FamilyDependent user;
}
