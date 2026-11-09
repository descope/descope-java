package com.descope.model.analytics;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticRecord {
  String projectId;
  String action;
  Instant created;
  String device;
  String method;
  String geo;
  String tenant;
  String referrer;
  Long cnt;
}
