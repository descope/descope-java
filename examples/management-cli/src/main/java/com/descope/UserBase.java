package com.descope;

import picocli.CommandLine.Option;

public abstract class UserBase extends HelpBase {
  @Option(names = { "-i", "--loginId" }, required = true, description = "the login ID of the user")
  String loginId;
}
