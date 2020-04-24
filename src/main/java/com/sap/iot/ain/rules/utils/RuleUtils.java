package com.sap.iot.ain.rules.utils;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.NamingException;

import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.iot.ain.rules.uaa.UAAClientCredentialsTokenProvider;
import com.sap.iot.ain.rules.uaa.UAAServiceInfo;
@Component
public class RuleUtils {

   public String getJwtToken(String tenant) throws NamingException {
        String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
        JsonElement jelement = new JsonParser().parse(VCAP_SERVICES);
        JsonObject jobject = new JsonObject();
        JsonObject vcap = jelement.getAsJsonObject();
        JsonArray jarray = vcap.getAsJsonArray("xsuaa");
        int i = 0;
        for(i = 0; i< jarray.size() ; i++) {
            if (jarray.get(i).getAsJsonObject().get("plan").getAsString().equals("application")) {
                jobject = jarray.get(i).getAsJsonObject();
                
            }
        }
        //jobject = jarray.get(0).getAsJsonObject();

            jobject = jobject.getAsJsonObject("credentials");

        //jobject = jarray.get(0).getAsJsonObject();
        //jobject = jobject.getAsJsonObject("credentials");
        String clientID = jobject.get("clientid").getAsString();
        String httpsProtocol = "https://";
        String stringUrl = httpsProtocol + tenant + "." + jobject.get("uaadomain").getAsString();
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException mfue) {
            //slogger.error("Malformed Url " + mfue.getMessage());
        }
        UAAServiceInfo uaaServiceInfo = new UAAServiceInfo(tenant,
                jobject.get("clientid").getAsString(), jobject.get("clientsecret").getAsString(),
                jobject.get("identityzone").getAsString(), jobject.get("tenantid").getAsString(),
                jobject.get("tenantmode").getAsString(), jobject.get("uaadomain").getAsString(),
                url, jobject.get("verificationkey").getAsString(),
                jobject.get("xsappname").getAsString());
        UAAClientCredentialsTokenProvider tokenProvider
                = new UAAClientCredentialsTokenProvider(uaaServiceInfo);
        return tokenProvider.getBearerToken(tenant).getAccessToken();
    }

}
