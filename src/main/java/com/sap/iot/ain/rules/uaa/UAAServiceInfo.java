/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.iot.ain.rules.uaa;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.springframework.cloud.service.BaseServiceInfo;
import org.springframework.cloud.service.ServiceInfo.ServiceLabel;
import org.springframework.cloud.service.ServiceInfo.ServiceProperty;

@ServiceLabel("xsuaa")
public class UAAServiceInfo extends BaseServiceInfo {

    protected final String clientid;
    protected final String clientsecret;
    protected final String identityzone;
    protected final String tenantid;
    protected final String tenantmode;
    protected final String uaadomain;
    protected final URL url;
    protected final String verificationkey;
    protected final String xsappname;
    private static final String TOKEN_PREFIX = "/oauth/token";

    public UAAServiceInfo(String id, String clientid, String clientsecret, String identityzone, String tenantid, String tenantmode, String uaadomain, URL url, String verificationkey, String xsappname) {
        super(id);
        this.clientid = clientid;
        this.clientsecret = clientsecret;
        this.identityzone = identityzone;
        this.tenantid = tenantid;
        this.tenantmode = tenantmode;
        this.uaadomain = uaadomain;
        this.url = url;
        this.verificationkey = verificationkey;
        this.xsappname = xsappname;
    }

    public String getClientId() {
        return clientid;
    }

    public String getIdentityZone() {
        return identityzone;
    }

    public String getTenantId() {
        return tenantid;
    }

    public String getTenantMode() {
        return tenantmode;
    }

    public String getUaaDomain() {
        return uaadomain;
    }

    public URL getUrl() {
        return url;
    }

    public String getVerificationKey() {
        return verificationkey;
    }

    public String getXsAppName() {
        return xsappname;
    }

    public String getClientSecret() {
        return clientsecret;
    }

    @ServiceProperty(category = "connection")
    public URI getClientCredentialsFlowUri(String tenantSubdomain) {
        String protocol = getUrl().getProtocol();
        String hostname = tenantSubdomain + "." + getUaaDomain();
        try {
            return new URL(protocol, hostname, TOKEN_PREFIX).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException("Unable to build client credentials token endpoint URI.", e);
        }

    }

}
