package com.descope.model.user.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SortField {
  /**
   * Required. One of: id, modifiedTime, createdTime, externalId, displayName, tenant, phoneNumber,
   * email, status.
   */
  String field;
  Boolean desc;
}
