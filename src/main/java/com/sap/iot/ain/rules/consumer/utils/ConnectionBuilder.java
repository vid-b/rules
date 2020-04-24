package com.sap.iot.ain.rules.consumer.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sap.iot.ain.rules.utils.Constant;

public class ConnectionBuilder {

	private static Channel channel;
	private static Connection connection;

	private static final Logger logger = LoggerFactory.getLogger(ConnectionBuilder.class);

	private static class ConnectionBuilderHolder {
		private static final ConnectionBuilder instance = new ConnectionBuilder();
	}

	public static ConnectionBuilder getInstance() {
		return ConnectionBuilderHolder.instance;
	}

	public Channel buildMQConnection() throws KeyManagementException, NoSuchAlgorithmException,
			URISyntaxException, IOException, TimeoutException {

		JsonObject rabbitMQCredentials = EnvironmentValueReader.getRabbitMQCredentials();
		if (rabbitMQCredentials != null && !rabbitMQCredentials.isJsonNull()) {
			String uri = rabbitMQCredentials.get("uri").getAsString();
			ConnectionFactory factory = new ConnectionFactory();
			factory.setAutomaticRecoveryEnabled(true);
			
			factory.setUri(uri);
			connection = getMQConnection(factory);
			channel = getMqchannel(connection);
		}
		return channel;
	}

	private Connection getMQConnection(ConnectionFactory factory)
			throws IOException, TimeoutException {
		if (connection == null) {
			connection = factory.newConnection();
		}
		return connection;
	}

	private Channel getMqchannel(Connection connection) {

		if (channel == null) {
			try {

				channel = connection.createChannel();
				channel.exchangeDeclare(Constant.EXCHANGE_NAME, "direct", true);
				channel.queueDeclare(Constant.QUEUE_NAME, true, false, false, null);
				channel.queueBind(Constant.QUEUE_NAME, Constant.EXCHANGE_NAME, Constant.ROUTING_KEY);

			} catch (IOException e) {
				logger.error("Exception while creating channel");
			}
		}
		return channel;
	}

}
