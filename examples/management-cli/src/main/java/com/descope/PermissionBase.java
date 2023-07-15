package com.descope;

import picocli.CommandLine.Option;

public abstract class PermissionBase extends HelpBase {
  @Option(names = { "-n", "--name" }, required = true, description = "Permission name")
  String name;
}
