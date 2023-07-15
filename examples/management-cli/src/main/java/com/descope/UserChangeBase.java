package com.descope;

import picocli.CommandLine.Option;

public abstract class UserChangeBase extends UserBase {
  @Option(names = { "-n", "--name" }, description = "the user display name")
  String name;
  @Option(names = { "-e", "--email" }, description = "the user email", required = true)
  String email;
  @Option(names = { "-p", "--phone" }, description = "the user phone", required = true)
  String phone;
}
