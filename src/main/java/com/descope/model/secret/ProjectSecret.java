package com.descope.model.secret;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSecret {
  private String id;
  private String name;
  private String description;
  /** Epoch seconds. */
  private Integer createdTime;
  /** Epoch seconds. */
  private Integer modifiedTime;
}
