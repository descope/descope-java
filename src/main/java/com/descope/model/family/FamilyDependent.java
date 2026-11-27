package com.descope.model.family;

import com.descope.model.user.response.UserResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** A user created as a family dependent, with its family memberships. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FamilyDependent extends UserResponse {
  private Boolean dependent;
  private List<UserFamily> userFamilies;
}
