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
  /** Start of the time period this record summarizes. */
  Instant created;
  String device;
  String method;
  String geo;
  String tenant;
  String referrer;
  /** Number of events in this record. */
  long count;
}
