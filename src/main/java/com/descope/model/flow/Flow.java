package com.descope.model.flow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("checkstyle:MemberName")
public class Flow {
  private String id;
  private String name;
  private String description;
  private Object dsl;
  private boolean disabled;
  private String eTag;
}
