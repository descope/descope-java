package com.descope.model.family.response;

import com.descope.model.family.Family;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyResponse {
  Family family;
}
