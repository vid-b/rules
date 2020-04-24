/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.iot.ain.rules.uaa;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

public class UAAServiceInfoCreator extends CloudFoundryServiceInfoCreator<UAAServiceInfo> {

    public UAAServiceInfoCreator() {
        super(new Tags("xsuaa"));
    }

    @Override
    public UAAServiceInfo createServiceInfo(Map<String, Object> serviceData) {
        String id = (String) serviceData.get("name");
        Map<String, Object> credentials = getCredentials(serviceData);

        String clientid = getStringFromCredentials(credentials, "clientid");
        String clientsecret = getStringFromCredentials(credentials, "clientsecret");
        String identityzone = getStringFromCredentials(credentials, "identityzone");
        String tenantid = getStringFromCredentials(credentials, "tenantid");
        String tenantmode = getStringFromCredentials(credentials, "tenantmode");
        String uaadomain = getStringFromCredentials(credentials, "uaadomain");
        String verificationkey = getStringFromCredentials(credentials, "verificationkey");
        String xsappname = getStringFromCredentials(credentials, "xsappname");
        try {
            URL url = new URL(getStringFromCredentials(credentials, "url"));
            return new UAAServiceInfo(id, clientid, clientsecret, identityzone, tenantid, tenantmode, uaadomain, url, verificationkey, xsappname);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to create UAAServiceInfo, 'url' is malformed.", e);
        }
    }
}
