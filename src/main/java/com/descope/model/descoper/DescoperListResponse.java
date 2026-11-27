package com.descope.model.descoper;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Descopers in the company and their total. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DescoperListResponse {
  private List<Descoper> descopers;
  private int total;
}
