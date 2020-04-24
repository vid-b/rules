package com.sap.iot.ain.rules.implementation;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sap.iot.ain.rules.models.EmailAuthenticationInformation;
import com.sap.iot.ain.rules.models.EmailData;

public class EmailUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailUtils.class);

	public static String getEmailBody(EmailData emailData) {
		
		String alertType = emailData.getAlertType();
		String modelId = emailData.getModelId();

		String url = formAlertUrl(emailData);

		StringBuilder sb = new StringBuilder("<html><body><b>The machine: </b>");
		sb.append(emailData.getEquipmentId()).append(" ").append("of").append("<b> Model:  </b>");
		sb.append(modelId).append(" ").append("reported the following").append("<br><br>").append("<b>Alert of type: </b>");
		sb.append("<a href=").append("\"").append(url).append("\"").append(">");
		sb.append(alertType).append("</a>").append("<br><br>").append("<b>Severity: </b>").append("\"").append(emailData.getAlertSeverity()).append("\"").append(" ").append("on").append(" ").append(emailData.getAlertCreatedOn()).append("<br><br>");
		if(emailData.getAlertProperty() != null) {
		sb.append("<b>Indicator</b>: ").append(emailData.getAlertProperty()).append("<br><br>");
		}
		sb.append("This message was created and sent automatically, please do not reply.</body></html>");
		return sb.toString();
	}
	
	
	


	private static String formAlertUrl(EmailData emailData) {
		// TODO Auto-generated method stub
		String VCAP_APPLICATION = System.getenv("VCAP_APPLICATION");
		JSONObject vcapApplication;
		String spaceName;
		String url;
		String alertUrl = null;

		try{
			if(VCAP_APPLICATION != null){
				vcapApplication = new JSONObject(VCAP_APPLICATION);
				spaceName = vcapApplication.getString("space_name");
				logger.debug("apace name is" + spaceName);
				url = vcapApplication.getJSONArray("uris").getString(0);
				logger.debug("urls before splitting is" + url);
				String[] splitUrl = url.split("\\.", 2);
				url = splitUrl[1];

				logger.debug("urls after splitting is" + url);

				if("sprint-end".equals(spaceName)) {

					alertUrl = "https://"+emailData.getSubDomain()+"-iam-flp-iam-test."+url+"/cp.portal/site#alert-display?AlertID="+emailData.getAlertId();
				}else if ("staging".equals(spaceName)) {
					alertUrl = "https://"+emailData.getSubDomain()+".iam-sb."+url+"/cp.portal/site#alert-display?AlertID="+emailData.getAlertId();
				}else if ("poc".equals(spaceName)) {
					alertUrl = "https://"+emailData.getSubDomain()+".iam-pr."+url+"/cp.portal/site#alert-display?AlertID="+emailData.getAlertId();	
				}else if("live".equals(spaceName)) {
					alertUrl = "https://"+emailData.getSubDomain()+".iam."+url+"/cp.portal/site#alert-display?AlertID="+emailData.getAlertId();	
				}

			}
			else
			{
				logger.debug("VCAP_APPLICATION environmental variable is unavailable");
			}

		}
		catch(Exception e)
		{
			logger.error("Error loading VCAP app: {}", e);
		}
		logger.debug("Url for alert in emails is " + alertUrl);
		return alertUrl;
	}

	public static String getEmailSubject(EmailData emailData) {
		String alertType = emailData.getAlertType();
		String modelId = emailData.getModelId();
		StringBuilder sb = new StringBuilder(alertType).append(" ").append(emailData.getAlertSeverity()).append(" ");
		sb.append(modelId).append(" ").append(emailData.getEquipmentId());
		return sb.toString();
	}

	public EmailAuthenticationInformation getEmailAuthDetailsFromSystemEnv() {
		EmailAuthenticationInformation emailAuthenticationInformation = null;
		String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
        JsonElement jelement = new JsonParser().parse(VCAP_SERVICES);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonArray jarray = jobject.getAsJsonArray("user-provided");
        if(jarray != null) {
             for(int i = 0; i < jarray.size(); i++) {
        	         jobject = jarray.get(i).getAsJsonObject();
        	         if(jobject.get("instance_name").getAsString().equals("ain-email")) {
        		           jobject = jobject.getAsJsonObject("credentials");
        		           try
        		           {
        			            emailAuthenticationInformation = new EmailAuthenticationInformation();
        			            emailAuthenticationInformation.setEmailUrl(jobject.get("emailUrl").getAsString());
        			            emailAuthenticationInformation.setPassword(jobject.get("password").getAsString().getBytes());
        			            emailAuthenticationInformation.setReplyTo(jobject.get("replyTo").getAsString());
        			            emailAuthenticationInformation.setSender(jobject.get("sender").getAsString());
        			            emailAuthenticationInformation.setUsername(jobject.get("username").getAsString());
        		           } catch(JsonSyntaxException e) {
        		                logger.error("Exception occured while fetching email user provided service");	
        		           }
        	         }
             }
        }
        return emailAuthenticationInformation;
    }
}
