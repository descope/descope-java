package com.descope.model.project;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotSecrets {
  private List<SnapshotSecret> connectors;
  private List<SnapshotSecret> oauthProviders;
  private List<SnapshotSecret> outboundApps;
  private List<SnapshotSecret> externalAuth;
}
