package com.descope.model.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListRequest {
  private String name;
  private String description;
  /** One of the {@link ListType} values. */
  private String type;
  /** Any JSON value: a list of strings for texts and ips lists, an object for json lists. */
  private Object data;
}
