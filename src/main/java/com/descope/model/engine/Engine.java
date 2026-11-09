package com.descope.model.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Engine {
  private String id;
  private String name;
  // Empty in load responses
  private String secret;
  // Epoch seconds
  private Integer createdTime;
}
