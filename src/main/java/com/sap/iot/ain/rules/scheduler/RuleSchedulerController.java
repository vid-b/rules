package com.sap.iot.ain.rules.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class RuleSchedulerController {
	private static final Logger logger = LoggerFactory.getLogger(RuleSchedulerController.class);
	private Scheduler scheduler;

	public Scheduler getScheduler() {
		return scheduler;
	}

	private static class RuleSchedulerControllerHolder {

		private static final RuleSchedulerController instance = new RuleSchedulerController();
	}

	public static RuleSchedulerController getInstance() {
		return RuleSchedulerControllerHolder.instance;
	}

	public RuleSchedulerController() {
		try {
			scheduler = new StdSchedulerFactory("config/quartz.properties").getScheduler();
			
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

}
