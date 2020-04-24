/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sap.iot.ain.rules.uaa;

public interface ClientCredentialsTokenProvider {

    OAuthToken refresh(OAuthToken token);

    OAuthToken getBearerToken(String tenantSubdomain);
}
