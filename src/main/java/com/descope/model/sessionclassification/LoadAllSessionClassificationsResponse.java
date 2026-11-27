package com.descope.model.sessionclassification;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadAllSessionClassificationsResponse {
  private List<SessionClassification> classifications;
}
