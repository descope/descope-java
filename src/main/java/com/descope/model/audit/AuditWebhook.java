package com.descope.model.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditWebhook {
  String name;
  String description;
  String url;
  AuditWebhookAuthentication authentication;
  String hmacSecret;
  Map<String, String> headers;
  boolean insecure;
  List<AuditWebhookFilter> filters;
}
