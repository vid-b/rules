package com.sap.iot.ain.rules;

import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.sap.iot.ain.reuse.utils.EnvironmentUtils;
import com.sap.iot.ain.rules.implementation.CFEnv;
import com.sap.iot.ain.rules.implementation.SchedulerJob;
import com.sap.iot.ain.rules.models.RuleStartUp;
import com.sap.iot.ain.rules.scheduler.RuleSchedulerController;
import com.sap.iot.ain.rules.services.RuleService;

@Component
public class ApplicationRuleStartUp {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	JdbcTemplate jdbcTemplate;

	private Scheduler _scheduler;

	@Autowired
	private RuleService service;

	@PersistenceContext
	private EntityManager em;
	
	private static final Logger logger = LoggerFactory.getLogger(ApplicationRuleStartUp.class);

	@EventListener(ContextRefreshedEvent.class)
	public void contextRefreshedEvent() throws SchedulerException {
	
		if (EnvironmentUtils.isCF()) {
			final String env = System.getenv("ac_rule_run");
			String output = "false";
			output = Objects.isNull(env) ? "false" : env;
			logger.debug("ac_rule_run value is with new code" + output);
			if("true".equals(output)) {

				List<RuleStartUp> rules = service.getAllRuleJobsWithOrgCheck();
				getScheduler();

				if(rules!= null) {
					for (RuleStartUp oRuleJobTrigger : rules) {
						if (oRuleJobTrigger == null) {
							continue;
						}
						JobDetail jd = JobBuilder.newJob((Class) SchedulerJob.class)
								.withIdentity(oRuleJobTrigger.getId()).storeDurably(true).build();
						_scheduler.addJob(jd, true);
						Trigger trigger = TriggerBuilder.newTrigger()
								.withSchedule((ScheduleBuilder) CronScheduleBuilder
										.cronSchedule((String) oRuleJobTrigger.getCronexpression()))
								.withIdentity(oRuleJobTrigger.getId())
								.usingJobData("EndPointURL", "")
								.usingJobData("tenantId", oRuleJobTrigger.getTenantSubDomain())
								.forJob(oRuleJobTrigger.getId()).build();
						_scheduler.scheduleJob(trigger);
					}
				}
				/*if (System.getProperty("enableRule") == null) {
					
					List<Object[]> rules = service.getAllRuleJobs(em);
					getScheduler();
					for (Object[] oRuleJobTrigger : rules) {
						if (oRuleJobTrigger[2] == null) {
							continue;
						}
						JobDetail jd = JobBuilder.newJob((Class) SchedulerJob.class)
								.withIdentity(oRuleJobTrigger[0].toString()).storeDurably(true).build();
						_scheduler.addJob(jd, true);
						Trigger trigger = TriggerBuilder.newTrigger()
								.withSchedule((ScheduleBuilder) CronScheduleBuilder
										.cronSchedule((String) oRuleJobTrigger[1]))
								.withIdentity(oRuleJobTrigger[0].toString())
								.usingJobData("EndPointURL", "")
								.usingJobData("tenantId", oRuleJobTrigger[2].toString())
								.forJob(oRuleJobTrigger[0].toString()).build();
						_scheduler.scheduleJob(trigger);
					}
				}*/
			}
		}
	}

	private void getScheduler() throws SchedulerException {
		if (_scheduler == null) {
			_scheduler = RuleSchedulerController.getInstance().getScheduler();
			AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
			jobFactory.setApplicationContext(applicationContext);
			_scheduler.setJobFactory(jobFactory);
		}
	}
	
	

}
