/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.iot.ain.rules.uaa;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;

public class UAAClientCredentialsTokenProvider implements ClientCredentialsTokenProvider {

    private final UAAServiceInfo uaaServiceInfo;
    private final RestTemplate template = new RestTemplate();
    private final Clock clock;

    public UAAClientCredentialsTokenProvider(UAAServiceInfo uaaServiceInfo) {
        this(uaaServiceInfo, Clock.systemUTC());
    }

    public UAAClientCredentialsTokenProvider(UAAServiceInfo uaaServiceInfo, Clock clock) {
        this.uaaServiceInfo = uaaServiceInfo;
        this.clock = clock;
    }

    public OAuthToken refresh(OAuthToken token) {
        if (token.needsRefresh(clock))
            return getBearerToken(token.getTenantSubdomain());
        else
            return token;
    }

    public OAuthToken getBearerToken(String tenantSubdomain) {
        URI tokenEndpointUri = uaaServiceInfo.getClientCredentialsFlowUri(tenantSubdomain);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        URI uri = UriComponentsBuilder.fromUri(tokenEndpointUri)
                .build().encode().toUri();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        map.add("response_type", "token");
        map.add("client_id", uaaServiceInfo.getClientId());
        map.add("client_secret", uaaServiceInfo.getClientSecret());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        OAuthTokenResponse tokenResponse = template.postForEntity(uri, request, OAuthTokenResponse.class).getBody();
        return new OAuthToken(tenantSubdomain, Instant.now(clock).plusSeconds(tokenResponse.getExpiresIn()), tokenResponse.getAccessToken());
    }
}