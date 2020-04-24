package com.sap.iot.ain.rules.inn365;

public class Email {

	// private String senderName;

	private String replyTo;

	// private String replyToName;
	//
	// private boolean clickEvent;
	//
	// private boolean openEvent;

	private String subject;


	/**
	 * @return the replyTo
	 */
	public String getReplyTo() {
		return replyTo;
	}

	/**
	 * @param replyTo the replyTo to set
	 */
	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}


	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}


}
