package com.descope.model.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotSecret {
  private String id;
  private String name;
  private String type;
  private String value;
}
