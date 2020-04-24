package com.sap.iot.ain.rules;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.sap.iot.ain.reuse.Strings;
import com.sap.iot.ain.reuse.utils.EnvironmentUtils;
import com.sap.iot.ain.rules.consumer.utils.ConnectionBuilder;
import com.sap.iot.ain.rules.utils.Constant;

@Component
public class MessageQueueConsumer {

	@Autowired
	private EventBasedRule eventBasedRule;

	private static final Logger logger = LoggerFactory.getLogger(MessageQueueConsumer.class);

	@EventListener(ContextRefreshedEvent.class)
	public void handleConsumerEvent() throws KeyManagementException, NoSuchAlgorithmException, SchedulerException, URISyntaxException, IOException, TimeoutException {
		if (EnvironmentUtils.isCF()) {
			try {
				messageQueueConsumer();
			}catch(Exception e) {
				e.printStackTrace();
				logger.error("Exception occured while reading the queue");
			}
		}
	}


	private void messageQueueConsumer() throws SchedulerException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {

		//Channel channel = null;
		Consumer consumer = null;

		// get the channel of the rabbit mq to take the message =========
		final Channel channel = ConnectionBuilder.getInstance().buildMQConnection();


		// consumer takes the message and work with that
		consumer = new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
					AMQP.BasicProperties properties, byte[] body) throws IOException {
				logger.error("Inside method handleDelivery");

				String inputMessage = new String(body, "UTF-8");
				System.out.println(" [x] Received '" + inputMessage + "'");
				
				try {
					if (!Strings.isNullOrEmpty(inputMessage)) {
						eventBasedRule.executeEventBasedRule(inputMessage);
					}
					channel.basicAck(envelope.getDeliveryTag(), false);
				}catch(Exception e) {
					logger.error("Inside handle delivery : Exception in event based rule ");}
			}


		};

		try {

			channel.basicConsume(Constant.QUEUE_NAME, false, consumer);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occured while consuming the queue");
		}

	}



}
