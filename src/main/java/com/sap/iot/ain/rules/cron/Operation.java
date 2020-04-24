package com.sap.iot.ain.rules.cron;

public enum Operation {

	ALWAYS("always"), BETWEEN("between"), EVERY("every"), ON("on"), QUESTIONMARK("questionMark");

	private String value;

	Operation(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
