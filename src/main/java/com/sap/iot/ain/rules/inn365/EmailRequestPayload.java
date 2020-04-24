package com.sap.iot.ain.rules.inn365;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailRequestPayload {

	public EmailNotification SAPnotification;
	
//	property has been made public because jackson was serializing the property to lower case

}
