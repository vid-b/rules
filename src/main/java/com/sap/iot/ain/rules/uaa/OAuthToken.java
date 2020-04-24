/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.iot.ain.rules.uaa;

import java.time.Clock;
import java.time.Instant;

public class OAuthToken {

    private final String tenantSubdomain;
    private final Instant bestBefore;
    private final String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public OAuthToken(String tenantSubdomain, Instant bestBefore, String accessToken) {
        this.tenantSubdomain = tenantSubdomain;
        this.bestBefore = bestBefore;
        this.accessToken = accessToken;
    }

    boolean needsRefresh(Clock clock) {
        return clock.instant().isAfter(bestBefore);
    }

    public String getTenantSubdomain() {
        return tenantSubdomain;
    }

}
