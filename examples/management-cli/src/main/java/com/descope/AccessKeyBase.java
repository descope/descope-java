package com.descope;

import picocli.CommandLine.Option;

public abstract class AccessKeyBase extends HelpBase {
  @Option(names = { "-i", "--keyid" }, required = true, description = "the key ID")
  String keyId;
}
