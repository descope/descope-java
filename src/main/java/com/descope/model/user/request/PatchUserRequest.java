package com.descope.model.user.request;

import static com.descope.utils.CollectionUtils.addIfNotNull;

import com.descope.model.auth.AssociatedTenant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PatchUserRequest {
  String name;
  String givenName;
  String middleName;
  String familyName;
  String phone;
  String email;
  List<String> roleNames;
  List<AssociatedTenant> userTenants;
  Map<String, Object> customAttributes;
  String picture;
  Boolean verifiedEmail;
  Boolean verifiedPhone;
  List<String> ssoAppIds;

  public Map<String, Object> toMap() {
    Map<String, Object> m = new HashMap<>();
    addIfNotNull(m, "email", email);
    addIfNotNull(m, "verifiedEmail", verifiedEmail);
    addIfNotNull(m, "phone", phone);
    addIfNotNull(m, "verifiedPhone", verifiedPhone);
    addIfNotNull(m, "name", name);
    addIfNotNull(m, "givenName", givenName);
    addIfNotNull(m, "middleName", middleName);
    addIfNotNull(m, "familyName", familyName);
    addIfNotNull(m, "roleNames", roleNames);
    addIfNotNull(m, "userTenants", userTenants);
    addIfNotNull(m, "customAttributes", customAttributes);
    addIfNotNull(m, "picture", picture);
    addIfNotNull(m, "ssoAppIDs", ssoAppIds);
    return m;
  }
}