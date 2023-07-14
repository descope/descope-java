package com.descope;

import picocli.CommandLine.Option;

public abstract class TenantBase extends HelpBase {
  @Option(names = { "-i", "--tenantId" }, required = true, description = "Tenant ID")
  String tenantId;
}
