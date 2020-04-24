package com.sap.iot.ain.rules.models;

public class EmailAuthenticationInformation {

	private String emailUrl;

	private String username;

	private byte[] password;

	private String replyTo;

	private String sender;

	public String getEmailUrl() {
		return emailUrl;
	}

	public void setEmailUrl(String emailUrl) {
		this.emailUrl = emailUrl;
	}


	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public byte[] getPassword() {
		return password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}



}
