package com.sap.iot.ain.rules.cron;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CronFields {

	private Cron second;
	private Cron minute;
	private Cron hour;
	@JsonProperty("day")
	private Cron dayOfTheMonth;
	private Cron month;
	@JsonProperty("week")
	private Cron dayOfTheWeek;
	private Cron year;
	public Cron getSecond() {
		return second;
	}
	public void setSecond(Cron second) {
		this.second = second;
	}
	public Cron getMinute() {
		return minute;
	}
	public void setMinute(Cron minute) {
		this.minute = minute;
	}
	public Cron getHour() {
		return hour;
	}
	public void setHour(Cron hour) {
		this.hour = hour;
	}
	public Cron getDayOfTheMonth() {
		return dayOfTheMonth;
	}
	public void setDayOfTheMonth(Cron dayOfTheMonth) {
		this.dayOfTheMonth = dayOfTheMonth;
	}
	public Cron getMonth() {
		return month;
	}
	public void setMonth(Cron month) {
		this.month = month;
	}
	public Cron getDayOfTheWeek() {
		return dayOfTheWeek;
	}
	public void setDayOfTheWeek(Cron dayOfTheWeek) {
		this.dayOfTheWeek = dayOfTheWeek;
	}
	public Cron getYear() {
		return year;
	}
	public void setYear(Cron year) {
		this.year = year;
	}

	

}
