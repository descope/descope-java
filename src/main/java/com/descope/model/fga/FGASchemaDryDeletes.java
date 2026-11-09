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
  // Tuples the dropped relations and types would delete, clamped to the server's cap
  private Integer deletedTupleCount;
  // True when the real count reached the cap, deletedTupleCount then holds the cap
  private boolean tupleCountCapped;

  public FGASchemaDryDeletes(boolean hasDeletes, List<String> relations, List<String> types) {
    this.hasDeletes = hasDeletes;
    this.relations = relations;
    this.types = types;
  }
}
