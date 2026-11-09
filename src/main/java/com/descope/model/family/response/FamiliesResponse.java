package com.descope.model.family.response;

import com.descope.model.family.Family;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamiliesResponse {
  List<Family> families;
}
