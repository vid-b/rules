package com.sap.iot.ain.rules.implementation;
import java.util.ArrayList;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

public class CFEnv {
	
	
       public static final JSONObject VCAP_SERVICES = loadServices();

		private static final Logger logger = LoggerFactory.getLogger(CFEnv.class);

		/**
		 * Loads VCAP Variables into memory in a json object
		 *
		 * @return
		 */
		private static JSONObject loadServices() {
			final String env = System.getenv("VCAP_SERVICES");
			JSONObject output = null;
			try {
				output = Objects.isNull(env) ? new JSONObject() : new JSONObject(env);
			} catch (JSONException e) {
				logger.error("Error reading VCAP variables");
			}
			return output;
		}

		private static JSONObject getIoTAEService() throws JSONException {
			JSONArray service;
			service = getService("iotae");
			service = service.length() == 0 ? getService("iotae-stakeholder") : service;
			return service.length() > 0 ? service.getJSONObject(0) : null;
		}

		private static JSONObject getXSUAAService() throws JSONException {
			JSONArray s = getService("xsuaa");
			String plan = s.getJSONObject(0).getString("plan");
			if ("broker".equals(plan)) {
				return s.getJSONObject(0);
			} else {
				return s.getJSONObject(1);
			}
		}

		public static JSONArray getService(String service) {
			JSONArray s = new JSONArray();
			try {
				s = VCAP_SERVICES.getJSONArray(service);
			} catch (JSONException e) {
				logger.debug("Error in fetching details from vcap services");
			}
			return s;
		}

		public static JSONObject getIoTAECredentials() throws JSONException {
			JSONObject service = getIoTAEService();
			if (service != null) {
				return service.getJSONObject("credentials").getJSONObject("uaa");
			}
			return null;
		}

		public static JSONObject getXSUAACredentials() throws JSONException {
			JSONObject service = getXSUAAService();
			if (service != null) {
				return service.getJSONObject("credentials");
			}
			return null;
		}

		public static String getIoTAEDomain() {
			try {
				return getIoTAECredentials().getString("uaadomain");
			} catch (JSONException e) {
				logger.debug("Error in fetching details from vcap services");
			}
			return null;
		}

		public static String getIoTAEClientID() {
			try {
				return getIoTAECredentials().getString("clientid");
			} catch (JSONException e) {
				logger.debug("Error in fetching details from vcap services");
			}
			return null;
		}

		public static String getIoTAEClientSecret() {
			try {
				return getIoTAECredentials().getString("clientsecret");
			} catch (JSONException e) {
				logger.debug("Error in fetching details from vcap services");
			}
			return null;
		}

		public static String getXSUAADomain() {
			try {
				return getXSUAACredentials().getString("uaadomain");
			} catch (JSONException e) {
				logger.debug("Error in fetching details from vcap services");
			}
			return null;
		}

		public static String getXSUAAClientID() {
			try {
				return getXSUAACredentials().getString("clientid");
			} catch (JSONException e) {
				logger.debug("Error in fetching details from vcap services");
			}
			return null;
		}

		public static String getXSUAAClientSecret() {
			try {
				return getXSUAACredentials().getString("clientsecret");
			} catch (JSONException e) {
				logger.debug("Error in fetching details from vcap services");
			}
			return null;
		}

		
	}


