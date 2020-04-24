package com.sap.iot.ain.rules.implementation;

import com.sap.iot.ain.reuse.AINConstants;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sap.iot.ain.rules.models.Rule;
import com.sap.iot.ain.rules.services.RuleService;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Component
@DisallowConcurrentExecution
public class SchedulerJob implements Job{

    @PersistenceContext
    EntityManager em;

    @Autowired
    RuleService service;

    @Autowired
    RuleExecutor re;

    private static final Logger logger = LoggerFactory.getLogger(RuleExecutor.class);

    @Override
    public void execute(JobExecutionContext jc) throws JobExecutionException {
        try {
            String correlationId = UUID.randomUUID().toString();
            MDC.put(AINConstants.CORRELATION_ID, correlationId);
            String executionId = jc.getFireTime().getTime()/1000 + jc.getJobDetail().getKey().getName().substring(0, 19);
            Rule rule = service.getRuleById(jc.getJobDetail().getKey().getName(), em);
            logger.debug("SchedulerJob : Job Execution context running for" + rule.getId() + "for Subdomain" + rule.getTenantSubDomain());
            if (rule != null && rule.getIsEnabled().equalsIgnoreCase("true")) {
                Instant fireTime = jc.getFireTime().toInstant();
                Instant previousFireTime = jc.getPreviousFireTime() != null ? jc.getPreviousFireTime().toInstant() : jc.getFireTime().toInstant();
                service.persistRuleRun(rule.getId(), executionId, rule.getClient(), fireTime);
                logger.debug("SchedulerJob : Job Execution context running for time instant" + rule.getId() + "firetime" + fireTime + "previoustime" + previousFireTime);
                re.execute(jc.getJobDetail().getKey().getName(), fireTime, previousFireTime, null, executionId, correlationId);
            }
        } catch (Exception ex) {
            logger.error("Error while processing rule : " + ex.getMessage());
        }

    }

}