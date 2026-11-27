package com.descope.model.user.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFamily {
  String familyId;
  List<String> roleNames;
  List<String> permissions;
  Map<String, Object> familyScopedAttributes;
}
