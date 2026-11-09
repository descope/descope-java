package com.descope.model.password;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordPolicy {

  private Integer minLength;
  private boolean lowercase;
  private boolean uppercase;
  private boolean number;
  private boolean nonAlphanumeric;
  private Boolean strengthScoreEnabled;
  private Integer strengthScore;

  public PasswordPolicy(Integer minLength, boolean lowercase, boolean uppercase, boolean number,
      boolean nonAlphanumeric) {
    this(minLength, lowercase, uppercase, number, nonAlphanumeric, null, null);
  }
}
