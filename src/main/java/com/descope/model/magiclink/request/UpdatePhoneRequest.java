package com.descope.model.magiclink.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePhoneRequest {

    private String phone;
    private String loginId;
    @JsonProperty("URI")
    private String uri;
    private boolean crossDevice;
}
