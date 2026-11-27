package com.descope.model.descoper;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A descoper: a member of the company console with access to projects. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Descoper {
  private String id;
  @JsonProperty("loginIDs")
  private List<String> loginIds;
  private DescoperAttributes attributes;
  private DescoperRBAC rbac;
  /** One of the {@link DescoperStatus} values. */
  private String status;
}
