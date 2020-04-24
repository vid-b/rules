package com.sap.iot.ain.rules.inn365;

import java.util.List;

public class EmailNotification {

	// private String channelPreferences;

	private List<String> recipients;

	private String contentTextType;

	private String contentText;

	private String contentTextEncoding;

	private String sender;

	private Configuration configuration;



	/**
	 * @return the recipients
	 */
	public List<String> getRecipients() {
		return recipients;
	}

	/**
	 * @param recipients the recipients to set
	 */
	public void setRecipients(List<String> recipients) {
		this.recipients = recipients;
	}

	/**
	 * @return the contentTextType
	 */
	public String getContentTextType() {
		return contentTextType;
	}

	/**
	 * @param contentTextType the contentTextType to set
	 */
	public void setContentTextType(String contentTextType) {
		this.contentTextType = contentTextType;
	}

	/**
	 * @return the contentText
	 */
	public String getContentText() {
		return contentText;
	}

	/**
	 * @param contentText the contentText to set
	 */
	public void setContentText(String contentText) {
		this.contentText = contentText;
	}

	/**
	 * @return the contentTextEncoding
	 */
	public String getContentTextEncoding() {
		return contentTextEncoding;
	}

	/**
	 * @param contentTextEncoding the contentTextEncoding to set
	 */
	public void setContentTextEncoding(String contentTextEncoding) {
		this.contentTextEncoding = contentTextEncoding;
	}

	/**
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * @param sender the sender to set
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * @return the configuration
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

}
