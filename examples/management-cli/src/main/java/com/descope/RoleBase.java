package com.descope;

import picocli.CommandLine.Option;

public abstract class RoleBase extends HelpBase {
  @Option(names = { "-n", "--name" }, required = true, description = "Role name")
  String name;
}
