package com.descope.model.fga;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FGASchemaDryDeletes {
  private boolean hasDeletes;
  private List<String> relations;
  private List<String> types;
}
