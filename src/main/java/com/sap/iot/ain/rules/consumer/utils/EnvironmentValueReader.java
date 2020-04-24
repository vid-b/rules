package com.sap.iot.ain.rules.consumer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.iot.ain.rules.utils.Constant;

public class EnvironmentValueReader {

	private static final Logger logger = LoggerFactory.getLogger(EnvironmentValueReader.class);

	public static JsonObject getRabbitMQCredentials() {
		JsonObject credentials = null;
		JsonArray rabbitmqcredentials = null;
		String vcapServices = System.getenv(Constant.VCAP_SERVICES);
		if (vcapServices != null && !vcapServices.trim().isEmpty()) {
			JsonElement jelement = new JsonParser().parse(vcapServices);
			JsonObject vcapServicesAsJSON = jelement.getAsJsonObject();

			JsonElement jsonRabbitMqElement = vcapServicesAsJSON.get(Constant.RABBITMQ);
			if (jsonRabbitMqElement != null) {
				rabbitmqcredentials = jsonRabbitMqElement.getAsJsonArray();
				for (JsonElement jsonNode : rabbitmqcredentials) {
					String name = jsonNode.getAsJsonObject().get("name").getAsString();
					if (name.equals(Constant.RABBITMQ_NAME)) {
						credentials = jsonNode.getAsJsonObject().get(Constant.CREDENTIALS).getAsJsonObject();
						logger.info("Fetched rabbit mq credentials");
					}
				}
			}
		}
		return credentials;
	}

}
