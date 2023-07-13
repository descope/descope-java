package com.descope.sdk;

import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtils {

  private static SdkInfo getSdkInfo() {
    String name = "java";
    var javaVersion = Runtime.version();

    // TODO - SHA
    return SdkInfo.builder()
        .name(name)
        .javaVersion(javaVersion.toString())
        .version(new SdkInfo().getClass().getPackage().getImplementationVersion())
        .build();
  }

  public static Client getClient() {
    return Client.builder().uri("https://api.descope.com").sdkInfo(getSdkInfo()).build();
  }

  public static String getRandomName(String prefix) {
    return prefix + UUID.randomUUID().toString();
  }

}
