package com.descope.model.flow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Screen {
  private String id;
  private String flowID;
  private Object inputs;
  private Object interactions;
  private Object htmlTemplate;
}
