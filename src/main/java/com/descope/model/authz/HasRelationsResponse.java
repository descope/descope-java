package com.descope.model.authz;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HasRelationsResponse {
  List<RelationQuery> relationQueries;
  List<Boolean> directRelations;

  // Keeps the pre-directRelations constructor for binary compatibility
  public HasRelationsResponse(List<RelationQuery> relationQueries) {
    this.relationQueries = relationQueries;
  }
}
