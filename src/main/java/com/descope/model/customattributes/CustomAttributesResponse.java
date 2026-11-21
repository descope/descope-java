package com.descope.model.customattributes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class CustomAttributesResponse {
  private List<CustomAttribute> data;
  // The API sends total as int64
  private long total;

  public CustomAttributesResponse(List<CustomAttribute> data, int total) {
    this(data, (long) total);
  }

  /**
   * Returns the total, capped at {@link Integer#MAX_VALUE}.
   *
   * @return the total as an int
   * @see #getTotalLong()
   */
  public int getTotal() {
    return (int) Math.min(total, Integer.MAX_VALUE);
  }

  /**
   * Returns the total as sent by the API.
   *
   * @return the total as a long
   */
  @JsonIgnore
  public long getTotalLong() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  @JsonSetter("total")
  public void setTotalLong(long total) {
    this.total = total;
  }

  public static class CustomAttributesResponseBuilder {
    public CustomAttributesResponseBuilder total(int total) {
      this.total = total;
      return this;
    }

    public CustomAttributesResponseBuilder totalLong(long total) {
      this.total = total;
      return this;
    }
  }
}
