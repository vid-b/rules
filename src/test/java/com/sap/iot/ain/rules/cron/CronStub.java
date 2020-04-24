package com.sap.iot.ain.rules.cron;

public class CronStub {
	public static String CRON_HOUR_INVALID_ARGUMENT = "{\r\n" + 
			"		\"minute\":{ \"value\" : 15 , \"operation\":\"ON\"},\r\n" + 
			"		\"hour\":{ \"value\" : 25 , \"operation\":\"EVERY\"}\r\n" + 
			"}";
	public static String CRON_HOUR_ZERO_ARGUMENT = "{\r\n" + 
			"		\"minute\":{ \"value\" : 15 , \"operation\":\"ON\"},\r\n" + 
			"		\"hour\":{ \"value\" : 0 , \"operation\":\"EVERY\"}\r\n" + 
			"}";
	public static String CRON_RESPONSE_EVERY_HOUR = "{\"second\":{\"value\":0,\"operation\":\"ON\"},\"minute\":{\"value\":10,\"operation\":\"ON\"},\"hour\":{\"value\":1,\"operation\":\"EVERY\"}}";
	
	public static String CRON_RESPONSE_EVERY_DAY = "{\"second\":{\"value\":0,\"operation\":\"ON\"},\"minute\":{\"value\":10,\"operation\":\"ON\"},\"hour\":{\"value\":3,\"operation\":\"ON\"},\"day\":{\"value\":1,\"operation\":\"EVERY\"}}";
	
	public static String CRON_RESPONSE_EVERY_MINUTE ="{\"second\":{\"value\":0,\"operation\":\"ON\"},\"minute\":{\"value\":1,\"operation\":\"EVERY\"}}";
	
	public static String CRON_RESPONSE_EVERY_WEEK="{\"second\":{\"value\":0,\"operation\":\"ON\"},\"minute\":{\"value\":2,\"operation\":\"ON\"},\"hour\":{\"value\":3,\"operation\":\"ON\"},\"week\":{\"value\":1,\"operation\":\"EVERY\"}}";
	
	public static String CRON_RESPONSE_EVERY_YEAR ="{\"second\":{\"value\":0,\"operation\":\"ON\"},\"minute\":{\"value\":2,\"operation\":\"ON\"},\"hour\":{\"value\":3,\"operation\":\"ON\"},\"month\":{\"value\":6,\"operation\":\"ON\"},\"year\":{\"value\":1,\"operation\":\"EVERY\"},\"day\":{\"value\":4,\"operation\":\"ON\"}}";
	
	public static  String CRON_OLD_RESPONSE_EVERY_4_MINUTE ="{\"second\":{\"value\":0,\"operation\":\"ON\"},\"minute\":{\"value\":4,\"operation\":\"EVERY\"}}";
	
	public static String CRON_OLD_RESPONSE_EVERY_HOUR_58_MINUTE = "{\"second\":{\"value\":0,\"operation\":\"ON\"},\"minute\":{\"value\":58,\"operation\":\"ON\"},\"hour\":{\"value\":1,\"operation\":\"EVERY\"}}";
	
	public static String CRON_OLD_RESPONSE_EVERY_DAY_20_13 ="{\"second\":{\"value\":0,\"operation\":\"ON\"},\"minute\":{\"value\":13,\"operation\":\"ON\"},\"hour\":{\"value\":20,\"operation\":\"ON\"},\"day\":{\"value\":1,\"operation\":\"EVERY\"}}";
	
	public static String CRON_OLD_EVERY_MONTH_23RD_HOUR15_MINUTE14="{\"second\":{\"value\":0,\"operation\":\"ON\"},\"minute\":{\"value\":14,\"operation\":\"ON\"},\"hour\":{\"value\":15,\"operation\":\"ON\"},\"month\":{\"value\":1,\"operation\":\"EVERY\"},\"day\":{\"value\":23,\"operation\":\"ON\"}}";
	
	public static String CRON_OLD_EVRY_YEAR_8TH_MONTH ="{\"second\":{\"value\":0,\"operation\":\"ON\"},\"minute\":{\"value\":12,\"operation\":\"ON\"},\"hour\":{\"value\":15,\"operation\":\"ON\"},\"month\":{\"value\":8,\"operation\":\"ON\"},\"year\":{\"value\":1,\"operation\":\"EVERY\"},\"day\":{\"value\":23,\"operation\":\"ON\"}}";
	

}
