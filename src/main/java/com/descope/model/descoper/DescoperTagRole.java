package com.descope.model.descoper;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A role a descoper has in every project carrying one of the tags. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DescoperTagRole {
  private List<String> tags;
  /** One of the {@link DescoperRole} values (required). */
  private String role;
}
