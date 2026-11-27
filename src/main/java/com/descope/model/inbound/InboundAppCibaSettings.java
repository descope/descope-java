package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** CIBA (Client Initiated Backchannel Authentication) settings. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppCibaSettings {
  public static final String NOTIFICATION_METHOD_EMAIL = "email";
  public static final String NOTIFICATION_METHOD_PUSH = "push";

  private Boolean enabled;
  private Integer expirationTime;
  /** One of the {@link InboundAppSessionSettings} {@code UNIT_*} values. */
  private String expirationTimeUnit;
  private String emailServiceProvider;
  private String emailServiceProviderFallback;
  private String emailServiceTemplateId;
  private String loginPageURL;
  /** One of the {@code NOTIFICATION_METHOD_*} values; email when empty. */
  private String notificationMethod;
  /** APN connector as "apn:&lt;connectorId&gt;". */
  private String pushAppleServiceProvider;
  /** FCM connector as "fcm:&lt;connectorId&gt;". */
  private String pushAndroidServiceProvider;
  /** With push, send the email when the push reaches no device. */
  private Boolean pushEmailFallback;
}
