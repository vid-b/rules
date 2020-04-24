package com.sap.iot.ain.rules.inn365;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.dsc.ac.iotae.utils.ClientSetup;
import com.sap.iot.ain.security.Secure;

@Path("sapin")
@Component
public class EmailRequestService {

	private static final Logger logger = LoggerFactory.getLogger(EmailRequestService.class);

	@Autowired
	private ClientSetup clientSetup;

	@POST
	@Path("/mail")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secure(roles = {"EQUIPMENT_EDIT", "EQUIPMENT_DELETE"})

	public Response sendMail(SendMailPayload sendMailPayload)

	{

		// String contentText = "<html><a href='http://www.google.com'>hello</a></html>";
		String contentText = sendMailPayload.getContentText();

		// String[] recipients = new String[] {"email:deepshikha.mohanta@customer.com"};
		List<String> recipientsList = new ArrayList<String>();
		for (String recipient : sendMailPayload.getRecipientList()) {
			recipientsList.add("email:" + recipient);
		}

		Email email = new Email();
		email.setReplyTo("contact@customer.com");
		email.setSubject("TestIN365");

		Configuration configuration = new Configuration();
		configuration.setEmail(email);

		EmailNotification emailNotification = new EmailNotification();

		emailNotification.setRecipients(recipientsList);
		emailNotification.setContentTextType("text/html");
		emailNotification.setContentText(contentText);
		emailNotification.setContentTextEncoding("NONE");
		emailNotification.setSender("pilotuser@test.sapmobileservices.com");
		emailNotification.setConfiguration(configuration);

		EmailRequestPayload emailRequestPayload = new EmailRequestPayload();
		emailRequestPayload.SAPnotification = emailNotification;
		String payloadString = "";
		try {
			payloadString = new ObjectMapper().writeValueAsString(emailRequestPayload);

			String value =
					"https://multichannel-pp.sapmobileservices.com/email/caas_8_ema67079/notifications";

			logger.debug("email request payload");
			Entity<EmailRequestPayload> requestPaylod =
					Entity.entity(emailRequestPayload, MediaType.APPLICATION_JSON);
			logger.debug("request payload");
			Response response = clientSetup.post(value, "basic Y2Fhc184X2VtYTY3MDc5OkN0MDRjOWR4",
					requestPaylod);
			logger.debug("Response status from request");
			logger.debug("send sms");
//			sendSMS();

		} catch (IOException e) {
			logger.debug("Email sending failing ");
		}

		return Response.ok().build();
	}

	//TODO uncomment for sms integrations
//	    private void sendSMS() throws IOException {
//	        CloseableHttpClient client = HttpClients.createDefault();
//	        HttpPost httpPost = new HttpPost(
//	                "http://sms-pp.sapmobileservices.com/cmn/sap_intern03881/sap_intern03881.sms");
//	        httpPost.setHeader("Content-Type", "text/plain");
//	        httpPost.setHeader("Authorization", "Basic c2FwX2ludGVybjAzODgxOmhSYzJ4WUVR");
//
//	        String input = "Subject=GEA SMS Services for notifying Alerts...\n" + "List=+919663477633\n"
//	                + "Text=Testing SMS services for GEA on sat from cc bulletin!!!\n" + "MobileNotification=YES";
//	        
//	        StringEntity strentity = new StringEntity(input, ContentType.TEXT_PLAIN);
//	        Entity<StringEntity> entity = Entity.text(strentity);
//	        
//	        httpPost.setEntity(new StringEntity(input, ContentType.TEXT_PLAIN));
//	        
//	        // UsernamePasswordCredentials creds
//	        // = new UsernamePasswordCredentials("sap_intern03881", "hRc2xYEQ");
//	        // httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
//
//	        try {
//	            CloseableHttpResponse response = client.execute(httpPost);
//	            System.out.println("succeeds");
//	        } catch (Exception e) {
//	            System.out.println(e.getMessage());
//	        }
//
//	        client.close();
//		}

	protected String getAuth(String username, String password) {
		return "Basic " + Base64.encodeBase64String((username + ':' + password).getBytes());
	}

}
