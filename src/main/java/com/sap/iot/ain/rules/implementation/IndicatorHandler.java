package com.sap.iot.ain.rules.implementation;

import java.lang.reflect.Type;
import java.util.Map;

import javax.naming.NamingException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sap.iot.ain.externalidmapping.payload.SystemInfo;
import com.sap.dsc.ac.iotae.utils.ClientSetup;
import com.sap.dsc.ac.iotae.utils.IOTAeConstants;
import com.sap.iot.ain.rules.utils.RuleUtils;

@Component
public class IndicatorHandler {

	@Autowired
	private SystemsDAO systemsDao;

	@Autowired
	private ClientSetup clientSetup;

	private static final Logger logger = LoggerFactory.getLogger(IndicatorHandler.class);

	public Map<String, String> getIndicators(String clientID, String pst, String thingID,
			String startTimeStamp, String endTimeStamp, String tenantId) {
		try {
			RuleUtils utils = new RuleUtils();
			String jwt = utils.getJwtToken(tenantId);
			String authorization = "Bearer " + jwt;
			SystemInfo sysInfo = systemsDao.getSystemInfo(clientID).stream()
					.filter(s -> IOTAeConstants.SYSTEM_NAME_INDICATORS.equals(s.getSystemName()))
					.findFirst().orElse(null);

			String url =
					formIndicatorURL(sysInfo.getURL1(), pst, thingID, startTimeStamp, endTimeStamp);

			logger.debug("Final Indicator URL: " + url);
			Response responseCreate = clientSetup.get(url, authorization);

			if (responseCreate.getStatus() == 200) {
				logger.debug("Indicator value fetched");
				String indicators = responseCreate.readEntity(String.class);
				JsonElement jelement = new JsonParser().parse(indicators);
				JsonObject jobject = jelement.getAsJsonObject();
				jobject = jobject.getAsJsonObject("d");
				JsonArray jarray = jobject.getAsJsonArray("results");
				if (jarray == null || jarray.size() == 0) {
					return null;
				}
				if (jarray.get(0) != null) {
					jobject = jarray.get(0).getAsJsonObject();
					jobject.remove("__metadata");
					Type type = new TypeToken<Map<String, String>>() {}.getType();
					Map<String, String> indicatorMap =
							new Gson().fromJson(jobject.toString(), type);
					return indicatorMap;
				} else {
					logger.debug("Indicator data is not available!!!");
					return null;
				}
			} else {
				logger.debug("Indicator value fetch failed: " + responseCreate.getStatus());
				String output = responseCreate.readEntity(String.class);
				logger.debug("Reason for failure: " + output);
				return null;
			}
		} catch (NamingException e) {
			logger.error("Exception in Indicator Handler: " + e.getMessage());
			return null;
		} 
	}

	private static String formIndicatorURL(String baseUrl, String pst, String thingID,
			String startTimeStamp, String endTimeStamp) {
		String url = baseUrl + "/" + pst + "/" + "aggregates?$filter=id eq '" + thingID
				+ "' and time ge datetime'" + startTimeStamp + "' and time lt datetime'"
				+ endTimeStamp + "'&$format=json";
		url = url.replaceAll(" ", "%20");
		return url;
	}
}
