/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.iot.ain.rules.uaa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthTokenResponse {

    public String getAccessToken() {
        return accessToken;
    }

    private final String accessToken;

    long getExpiresIn() {
        return expiresIn;
    }

    private final long expiresIn;

    public OAuthTokenResponse(@JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") long expiresIn,
            @JsonProperty("scope") String scope,
            @JsonProperty("ext_attr")
            @JsonDeserialize(as = HashMap.class) Map<String, String> extAttributes,
            @JsonProperty("jti") String jti) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }

}
