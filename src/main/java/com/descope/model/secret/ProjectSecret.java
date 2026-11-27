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
  private Long createdTime;
  /** Epoch seconds. */
  private Long modifiedTime;
}
