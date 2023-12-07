package com.descope.model.user.request;

import com.descope.model.auth.AssociatedTenant;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
  String userId;
  String loginId;
  String email;
  Boolean verifiedEmail;
  String phone;
  Boolean verifiedPhone;
  String displayName;
  List<String> roleNames;
  List<AssociatedTenant> userTenants;
  Map<String, Object> customAttributes;
  String picture;
  Boolean invite;
  Boolean test;
  String inviteUrl;
  Boolean sendEmail;
  @JsonProperty("sendSMS")
  Boolean sendSMS;
  List<String> additionalLoginIds;
}
