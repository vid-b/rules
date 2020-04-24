/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.sap.iot.ain.rules.services;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.iot.ain.indicator.v2.entities.IndicatorConfigurationThresholdEntity;
import com.sap.iot.ain.reuse.DateHelper;
import com.sap.iot.ain.reuse.GenericRowMapper;
import com.sap.iot.ain.reuse.Strings;
import com.sap.iot.ain.reuse.utils.ObjectUtils;
import com.sap.iot.ain.rules.AutowiringSpringBeanJobFactory;
import com.sap.iot.ain.rules.cron.CronExpression;
import com.sap.iot.ain.rules.cron.Parser;
import com.sap.iot.ain.rules.cron.RuleCronParsingException;
import com.sap.iot.ain.rules.implementation.RulesUtils;
import com.sap.iot.ain.rules.implementation.SchedulerJob;
import com.sap.iot.ain.rules.models.ExecutableRuleJson;
import com.sap.iot.ain.rules.models.GetUsersForAnOrganization;
import com.sap.iot.ain.rules.models.Model;
import com.sap.iot.ain.rules.models.Rule;
import com.sap.iot.ain.rules.models.RuleAction;
import com.sap.iot.ain.rules.models.RuleAggregation;
import com.sap.iot.ain.rules.models.RuleDescription;
import com.sap.iot.ain.rules.models.RuleEquipmentMapping;
import com.sap.iot.ain.rules.models.RuleEquipmentMappingPOST;
import com.sap.iot.ain.rules.models.RuleErrorMessages;
import com.sap.iot.ain.rules.models.RuleEvent;
import com.sap.iot.ain.rules.models.RuleJobSchedule;
import com.sap.iot.ain.rules.models.RuleJobTriggers;
import com.sap.iot.ain.rules.models.RuleJobTriggersPK;
import com.sap.iot.ain.rules.models.RulePK;
import com.sap.iot.ain.rules.models.RuleStartUp;
import com.sap.iot.ain.rules.models.RuleStep;
import com.sap.iot.ain.rules.models.RuleStepOut;
import com.sap.iot.ain.rules.models.RuleSubject;
import com.sap.iot.ain.rules.models.RuleWithEvents;
import com.sap.iot.ain.rules.models.RuleWithSteps;
import com.sap.iot.ain.rules.models.ResponseData;
import com.sap.iot.ain.rules.scheduler.RuleSchedulerController;
import com.sap.iot.ain.security.AuthenticatedUserDetails;

@Component
public class RuleService {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	AuthenticatedUserDetails aud;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@PersistenceContext
	private EntityManager em;

	private Scheduler _scheduler;
	int _sequence = 0;

	List<RuleErrorMessages> errors = new ArrayList();
	List<RuleStepOut> _steps = new ArrayList();

	private static final Logger logger = LoggerFactory.getLogger(RuleService.class);

	private void getScheduler() throws SchedulerException {
		if (_scheduler == null) {
			_scheduler = RuleSchedulerController.getInstance().getScheduler();
			AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
			jobFactory.setApplicationContext(applicationContext);
			_scheduler.setJobFactory(jobFactory);
		}
	}

	public List<RuleWithEvents> getAllRules(EntityManager em) {
		try {
			Query q = em.createNativeQuery(
					"select rs.\"ID\", rs.\"Name\", rs.\"RuleType\", rs.\"Description\",rs.\"Priority\", "
							+ "rs.\"IsEnabled\",rev.\"EventID\","
							+ "rev.\"EventName\" from \"sap.ain.metaData::Rules.Rule\" rs JOIN"
							+ "\"sap.ain.metaData::Rules.RuleEvent\" rev ON (rs.\"ID\"=rev.\"RuleID\")",
							RuleWithEvents.class);
			q.setHint("javax.persistence.cache.storeMode", "REFRESH");
			List<RuleWithEvents> result = (List<RuleWithEvents>) q.getResultList();// new
			// ArrayList<RuleWithEvents>();
			for (RuleWithEvents rwe : result) {
				String eventId = rwe.getEventID();
				if (eventId.equals("schedule")) {
					rwe.setEventName("Scheduled Event");
				}
			}
			// return (List<RuleWithEvents>) q.getResultList();
			return result;
		} catch (NoResultException e) {
			return null;
		} finally {
		}

	}

	public Rule getRuleById(String id, EntityManager em) {
		try {
			Query q = em.createQuery("select a from Rule a where a.id = :id");
			q.setParameter("id", id);
			return (Rule) q.getSingleResult();
		} catch (Exception e) {
			return null;
		} finally {
		}
	}

	public Rule getRule(String id, EntityManager em) {
		TypedQuery<Rule> ruleQuery =
				em.createQuery("select r from Rule r WHERE r.id=:id", Rule.class);

		ruleQuery.setParameter("id", id);
		List<Rule> rules = ruleQuery.getResultList();
		if (rules == null || rules.isEmpty()) {
			return null;
		}
		return rules.get(0);
	}

	@Transactional
	public int changeRuleStatus(String id, int statuscode, EntityManager em) {
		try {
			Rule oRule = this.getRuleById(id, em);
			if (statuscode == 0) {
				oRule.setIsEnabled("false");
			} else {
				oRule.setIsEnabled("true");
			}
			this.updateRule(oRule, em);
			return 0;
		} catch (Exception ex) {
			return -1;
		}
	}

	@Transactional
	public void updateRule(Rule oRule, EntityManager em) {
		try {
			em.merge(oRule);
		} catch (Exception ex) {
			throw ex;
		} finally {
		}

	}

	@Transactional
	public Rule createRule(Rule oRule, EntityManager em, String tenantId) {
		try {

			// TODO: Remove sysout. This is what we set in the Rule tables as our tenant.
			// changes to be taken up only after confirmation on this!!!
			String ID = UUID.randomUUID().toString().replaceAll("-", "");
			oRule.setClient(aud.getUserDetails().getUserClientId());
			oRule.setId(ID);
			oRule.setTenantSubDomain(tenantId);
			// oRule.setRuleExpression(ruleExpression);
			em.persist(oRule);
		} catch (Exception ex) {
			throw ex;
		} finally {
		}
		return oRule;
	}

	public boolean isScheduledJob(String id, EntityManager em) {
		try {

			Query q = em.createQuery("select rev.eventID from RuleEvent rev WHERE rev.ruleID= :ruleID");
			q.setParameter("ruleID", id);

			String eventId = q.getSingleResult().toString();
			if (eventId.equals("schedule")) {
				return true;

			} else {
				return false;

			}
		} catch (NoResultException e) {
			return false;
		} finally {
		}
	}

	@Transactional
	public void deleteJob(String id, EntityManager em) throws SchedulerException {
		try {
			Query q = em.createQuery("delete from RuleJobSchedule rjs where rjs.id=:JobID");
			q.setParameter("JobID", id);
			q.executeUpdate();
			getScheduler();
			_scheduler.deleteJob(new JobKey(id.toString()));

		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public void deleteJobTrigger(String id, EntityManager em) throws SchedulerException {
		try {
			Query q = em.createQuery("delete from RuleJobTriggers rjt where rjt.jobId=:TriggerID");
			q.setParameter("TriggerID", id);
			q.executeUpdate();
			_scheduler.unscheduleJob(new TriggerKey(id.toString()));

		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public void deleteRule(String id, EntityManager em) {
		try {
			RulePK rulePK = new RulePK();
			String inviteeClient = null;
			rulePK.setClient(aud.getUserDetails().getUserClientId());
			rulePK.setId(id);
			Rule oRule = em.find(Rule.class, rulePK);
			if(oRule != null) {
				em.remove(oRule);
			}else {
				if("1".equals(getAccountType(aud.getUserDetails().getUserClientId()))){
					Rule existingRule = getRuleById(id, em);
					inviteeClient = existingRule.getClient();
					if("BSC001".equals(isInviteeCheck(aud.getUserDetails().getUserClientId(),inviteeClient))){
						rulePK.setClient(inviteeClient);
						rulePK.setId(id);
						oRule = em.find(Rule.class, rulePK);
						em.remove(oRule);
					}
				}
			}


		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	private String isInviteeCheck(String premiumClientId, String inviteeClient) {
		String q = "select \"RelationshipType\"  from \"sap.ain.metaData::BusinessPartner.BusinessPartnerRelationship\" "
				+ "where \"BusinessPartner1\" = ? and  \"BusinessPartner2\"  = ? and \"IsMarkedForDeletion\" = 0";
		String premium = null;
		try {
			premium = jdbcTemplate.queryForObject(q, new Object[] { premiumClientId,inviteeClient }, String.class);
		} catch (DataAccessException ex) {
			logger.error("Error while retrieving invitee id for premium, {}", ex);

		}
		return premium;
	}
		
	

	@Transactional
	public void deleteRuleSteps(String id, EntityManager em) {
		try {
			// delete all existing rule steps
			Query qrydeleteRuleSteps = em.createQuery("delete from RuleStep rs where rs.ruleID=:id");
			qrydeleteRuleSteps.setParameter("id", id);
			qrydeleteRuleSteps.executeUpdate();

		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public void deleteRuleAggregations(String id, EntityManager em) {
		try {
			Query deletequery = em.createQuery("delete from RuleAggregation ra where ra.ruleID=:ruleId");
			deletequery.setParameter("ruleId", id);
			deletequery.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Transactional
	public void deleteRuleSubjects(String id, EntityManager em) {
		try {
			Query qrydelete = em.createQuery("delete from RuleSubject rs where rs.ruleID=:id");
			qrydelete.setParameter("id", id);
			qrydelete.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public void deleteRuleEvents(String id, EntityManager em) {
		try {
			Query qrydelete = em.createQuery("delete from RuleEvent rs where rs.ruleID=:id");
			qrydelete.setParameter("id", id);
			qrydelete.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public void deleteRuleActions(String id, EntityManager em) {
		try {
			Query qrydeleteRuleActions = em.createQuery("delete from RuleAction rs where rs.ruleID=:id");
			qrydeleteRuleActions.setParameter("id", id);
			qrydeleteRuleActions.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public void deleteRuleDescription(String id, EntityManager em) {
		try {
			Query qrydeleteRuleActions =
					em.createQuery("delete from RuleDescription rs where rs.ruleID=:id");
			qrydeleteRuleActions.setParameter("id", id);
			qrydeleteRuleActions.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}


	public List<RuleStep> getRuleStepsByRuleId(String id, EntityManager em) {
		try {
			Query q = em.createQuery(
					"select rs from RuleStep rs where rs.ruleID = :ruleId order by rs.sequenceNo ASC");
			q.setParameter("ruleId", id);
			return (List<RuleStep>) q.getResultList();
		} catch (NoResultException e) {
			e.printStackTrace();
			return null;
		} finally {
		}
	}

	@Transactional
	public void createRuleSteps(String id, List<RuleStep> oRuleSteps, EntityManager em,String client) {
		try {
			for (RuleStep oRuleStep : oRuleSteps) {
				String ID = UUID.randomUUID().toString().replaceAll("-", "");
				oRuleStep.setId(ID);
				if(client !=null) {
					oRuleStep.setClient(client);
				}
				else {
					oRuleStep.setClient(aud.getUserDetails().getUserClientId());
				}

				if (oRuleStep.getIsStepOperator() == 0) {
					if (oRuleStep.getField1().contains("${")) {
						oRuleStep.setField1Type("EXPRESSION");
					}
					if (oRuleStep.getField2().contains("${")) {
						oRuleStep.setField2Type("EXPRESSION");
					}
				}
				oRuleStep.setRuleID(id);

				em.persist(oRuleStep);
			}

		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public List<RuleAggregation> getRuleAggregationsByRuleId(String id, EntityManager em) {
		try {
			Query q = em.createQuery("select ra from RuleAggregation ra where ra.ruleID = :ruleId");
			q.setParameter("ruleId", id);
			List<RuleAggregation> ruleAggregation = (List<RuleAggregation>) q.getResultList();
			return ruleAggregation;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public RuleSubject getRuleSubjectsByRuleId(String id, EntityManager em) {
		try {
			Query q = em.createQuery("select rs from RuleSubject rs where rs.ruleID = :ruleId");
			q.setParameter("ruleId", id);
			List<RuleSubject> ruleSubjects = (List<RuleSubject>) q.getResultList();
			return ruleSubjects.get(0);
		} catch (NoResultException e) {
			e.printStackTrace();
			return null;
		} finally {
		}
	}

	public List<RuleDescription> getRuleDescription(String id, EntityManager em) {
		try {
			Query q = em.createQuery("select rs from RuleDescription rs where rs.ruleID = :ruleId");
			q.setParameter("ruleId", id);
			return (List<RuleDescription>) q.getResultList();

		} catch (NoResultException e) {
			e.printStackTrace();
			return null;
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		} finally {
		}
	}

	public List<RuleEvent> getRuleEventsByRuleId(String id, EntityManager em) {
		try {
			Query q = em.createQuery("select rs from RuleEvent rs where rs.ruleID = :ruleId");
			q.setParameter("ruleId", id);
			return (List<RuleEvent>) q.getResultList();
		} catch (NoResultException e) {
			e.printStackTrace();
			return null;
		} finally {
		}
	}

	public List<RuleAction> getRuleActionsByRuleId(String id, EntityManager em) {
		try {
			Query q = em.createQuery("select rs from RuleAction rs where rs.ruleID = :ruleId ");
			q.setParameter("ruleId", id);
			return (List<RuleAction>) q.getResultList();
		} catch (NoResultException e) {
			return null;
		} finally {
		}
	}

	public RuleJobSchedule findJob(String id, EntityManager em) {
		try {
			logger.debug("Inside service class  find job. Job ID is  " + id);
			Query q = em.createQuery("select rjs from RuleJobSchedule rjs where rjs.id=:JobName");
			q.setParameter("JobName", id);
			return (RuleJobSchedule) q.getSingleResult();
		} catch (Exception e) {
			logger.error("Exception thrown RuleJobSchedule.findJob: {}" + e.getMessage());
			return null;
		}
	}

	public RuleJobTriggers findJobTrigger(String id, EntityManager em) {
		try {
			RuleJobTriggersPK ruleJobTriggersPK = new RuleJobTriggersPK();
			ruleJobTriggersPK.setClient(aud.getUserDetails().getUserClientId());
			ruleJobTriggersPK.setId(id);
			return em.find(RuleJobTriggers.class, ruleJobTriggersPK);
		} finally {
		}
	}

	@Transactional
	public void updateRuleSteps(String id, List<RuleStep> ruleSteps, EntityManager em,String client) {
		deleteRuleSteps(id, em);

		createRuleSteps(id, ruleSteps, em , client);
	}

	@Transactional
	public void updateRuleAggregations(String id, List<RuleAggregation> ruleAggregations,
			EntityManager em,String client) {
		deleteRuleAggregations(id, em);
		createRuleAggregations(id, ruleAggregations, em, client);
	}

	@Transactional
	public void updateRuleDescriptions(String id, RuleDescription ruleDescription, EntityManager em,String client) {
		RuleDescription desc = new RuleDescription();
		// List<RuleDescription> tempRuleDescriptions = new ArrayList<RuleDescription>();

		desc = getRuleDescriptionForLoggedInLang(id, em, aud.getUserDetails().getLocale().getLanguage());
				
		if (desc != null) {

			ruleDescription.setLanguageISOCode(aud.getUserDetails().getLocale().getLanguage());
			ruleDescription.setClient(client);
			ruleDescription.setRuleID(id);
			updateRuleDescription(id, ruleDescription, em,client);
		} else {
			createRuleDescription(id, ruleDescription, em,client);

		}
	}

	@Transactional
	private void updateRuleDescription(String id, RuleDescription tempRuleDescription,
			EntityManager em, String client) {
		// TODO Auto-generated method stub
		try {

			Query q = em.createQuery(
					"update RuleDescription rd set rd.longDescription = :long where rd.ruleID = :ruleId and rd.client = :client and rd.languageISOCode = :lang");
			q.setParameter("ruleId", id);
			q.setParameter("client", client);
			q.setParameter("lang", aud.getUserDetails().getLocale().getLanguage());
			q.setParameter("long", tempRuleDescription.getLongDescription());
			em.merge(tempRuleDescription);


		} catch (NoResultException e) {

		} finally {
		}
	}

	@Transactional
	public void updateRuleSubject(String id, RuleSubject ruleSubject, EntityManager em, String client) {
		deleteRuleSubjects(id, em);
		createRuleSubject(id, ruleSubject, em,client);
	}

	@Transactional
	public void updateRuleEvents(String id, List<RuleEvent> ruleEvents, EntityManager em,String client) {
		deleteRuleEvents(id, em);
		createRuleEvents(id, ruleEvents, em,client);
	}

	@Transactional
	public void updateRuleActions(String id, List<RuleAction> ruleActions, EntityManager em,
			String clientId) {
		if (ruleActions == null || ruleActions.isEmpty()) {
			deleteActionsForARule(id, em, clientId);
			return;
		}
		for (RuleAction actions : ruleActions) {
			actions.setClient(clientId);
		}
		List<RuleAction> existingRuleActionsByRuleId = getAllRuleActionsByRuleId(id, em);
		if (!ObjectUtils.isListEmpty(existingRuleActionsByRuleId)) {
			String existingRuleActionAlertTypeId = null;
			String existingRuleActionSendEmailId = null;

			Optional<RuleAction> existingOptionalAlertTypeAction = existingRuleActionsByRuleId.stream()
					.filter(action -> action.getActionId().equalsIgnoreCase("alerttype")).findFirst();
			if (existingOptionalAlertTypeAction.isPresent()) {
				existingRuleActionAlertTypeId = existingOptionalAlertTypeAction.get().getId();
				Optional<RuleAction> optionalAlertTypeActionforUpdate = ruleActions.stream()
						.filter(action -> action.getActionId().equalsIgnoreCase("alerttype")).findFirst();
				if (optionalAlertTypeActionforUpdate.isPresent()) {
					optionalAlertTypeActionforUpdate.get().setId(existingRuleActionAlertTypeId);
				}
			}

			Optional<RuleAction> existingOptionalSendEmailAction = existingRuleActionsByRuleId.stream()
					.filter(action -> action.getActionId().equalsIgnoreCase("sendemail")).findFirst();
			if (existingOptionalSendEmailAction.isPresent()) {
				existingRuleActionSendEmailId = existingOptionalSendEmailAction.get().getId();
				Optional<RuleAction> optionalSendEmaiActionforUpdate = ruleActions.stream()
						.filter(action -> action.getActionId().equalsIgnoreCase("sendemail")).findFirst();
				if (optionalSendEmaiActionforUpdate.isPresent()) {
					optionalSendEmaiActionforUpdate.get().setId(existingRuleActionSendEmailId);
				}
			}
		}

		deleteActionsForARule(id, em, clientId);
		updateActionsForARule(id, ruleActions, em);
	}

	@Transactional
	public RuleJobSchedule updateJob(RuleJobSchedule oRuleJob, EntityManager em) {
		try {
			em.merge(oRuleJob);
		} catch (Exception ex) {
			throw ex;
		} finally {
		}
		return oRuleJob;
	}

	@Transactional
	public void unscheduleJobTrigger(String jobId, EntityManager em) throws SchedulerException {
		try {
			getScheduler();
			JobDetail jd = _scheduler.getJobDetail(new JobKey(jobId.toString()));
			if (jd != null) {
				_scheduler.unscheduleJob(new TriggerKey(jobId.toString()));
			}
		} catch (Exception ex) {
			logger.error("Error while unscheduling job.....");;
		}
	}

	@Transactional
	public RuleJobSchedule createJob(RuleJobSchedule oRuleJob, EntityManager em,String client)
			throws SchedulerException {
		try {
			if(client!=null) {
				oRuleJob.setClient(client);
			}else {
				oRuleJob.setClient(aud.getUserDetails().getUserClientId());
			}
			
			em.persist(oRuleJob);
			// em.flush();
			getScheduler();
			JobDetail jd = JobBuilder.newJob((Class) SchedulerJob.class)
					.withIdentity(oRuleJob.getId().toString()).storeDurably(true).build();
			_scheduler.addJob(jd, true);

		} catch (Exception ex) {
			throw ex;
		} finally {
		}
		return oRuleJob;
	}

	@Transactional
	public RuleJobTriggers createJobTrigger(RuleJobTriggers oRuleJobTrigger, EntityManager em,
			String tenantId,String client) throws SchedulerException {
		try {
			if(client !=null) {
				oRuleJobTrigger.setClient(client);
			}else {
			oRuleJobTrigger.setClient(aud.getUserDetails().getUserClientId());
			}
			em.persist(oRuleJobTrigger);
			// em.flush();

			Trigger trigger = TriggerBuilder.newTrigger()
					.withSchedule((ScheduleBuilder) CronScheduleBuilder
							.cronSchedule((String) oRuleJobTrigger.getCronexpression()))
					.withIdentity(oRuleJobTrigger.getId().toString()).usingJobData("EndPointURL", "")
					.usingJobData("tenantId", tenantId).forJob(oRuleJobTrigger.getJobId().toString()).build();
			getScheduler();
			_scheduler.scheduleJob(trigger);

		} catch (Exception ex) {
			throw ex;
		} finally {
			// if (em != null)
			// em.getTransaction().commit();
			// CloseEntityManager(em);
		}
		return oRuleJobTrigger;
	}

	@Transactional
	public void createRuleAggregations(String id, List<RuleAggregation> ruleAggregations,
			EntityManager em,String client) {
		try {
			for (RuleAggregation ruleAggregation : ruleAggregations) {

				String ID = UUID.randomUUID().toString().replaceAll("-", "");

				if(client!=null) {
					ruleAggregation.setClient(client);
				}
				else {
					ruleAggregation.setClient(aud.getUserDetails().getUserClientId());
				}
				ruleAggregation.setId(ID);
				ruleAggregation.setRuleID(id);
				em.persist(ruleAggregation);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Transactional
	public void createRuleSubject(String id, RuleSubject ruleSubjectType, EntityManager em,String client) {
		try {
			String ID = UUID.randomUUID().toString().replaceAll("-", "");
			ruleSubjectType.setId(ID);
			if(client!=null) {
				ruleSubjectType.setClient(client);
			}else {
				ruleSubjectType.setClient(aud.getUserDetails().getUserClientId());
			}
			ruleSubjectType.setRuleID(id);
			em.persist(ruleSubjectType);

		} catch (Exception ex) {
			throw ex;
		} finally {
		}

	}

	@Transactional
	public void createRuleEvents(String id, List<RuleEvent> ruleEvents, EntityManager em,String client) {
		try {
			for (RuleEvent oRuleEvent : ruleEvents) {
				String ID = UUID.randomUUID().toString().replaceAll("-", "");
				oRuleEvent.setId(ID);
				if(client!=null) {
					oRuleEvent.setClient(client);
				}else {
					oRuleEvent.setClient(aud.getUserDetails().getUserClientId());
				}
				
				oRuleEvent.setRuleID(id);
				em.persist(oRuleEvent);
			}

		} catch (Exception ex) {
			throw ex;
		} finally {
		}

	}

	public List<RuleAction> getAllRuleActionsByRuleId(String ruleId, EntityManager em
			) {
		try {
			Query q = em
					.createQuery("select a from RuleAction a where a.ruleID = :ruleId");
			q.setParameter("ruleId", ruleId);
			//q.setParameter("client", clientId);
			List<RuleAction> ruleActions = (List<RuleAction>) q.getResultList();
			return ruleActions;
		} catch (NoResultException e) {
			return null;
		} finally {
		}
	}

	@Transactional
	public void createRuleDescription(String ruleId, RuleDescription ruleDescription,
			EntityManager em,String client) {
		try {


			ruleDescription.setRuleID(ruleId);
			if(client != null) {
				ruleDescription.setClient(client);
			}
			else {
				ruleDescription.setClient(aud.getUserDetails().getUserClientId());
			}
			ruleDescription.setLanguageISOCode(aud.getUserDetails().getLocale().getLanguage());

			em.persist(ruleDescription);


		} catch (Exception ex) {
			throw ex;
		} finally {
		}

	}

	@Transactional
	public RuleDescription getRuleDescriptionForLoggedInLang(String ruleId, EntityManager em,
			String language) {
		Query query = null;
		RuleDescription ruledesc = null;
		try {

			query = em.createQuery(
					"select a from RuleDescription a where a.ruleID = :ruleId and a.languageISOCode=:language");
			query.setParameter("ruleId", ruleId);

			query.setParameter("language", language);
			List<RuleDescription> ruleDescriptions = (List<RuleDescription>) query.getResultList();
			if (!ruleDescriptions.isEmpty()) {
				ruledesc = ruleDescriptions.get(0);
			}

		} catch (NoResultException e) {
			ruledesc = null;
		} catch (Exception exception) {
			ruledesc = null;
		}
		return ruledesc;
	}

	@Transactional
	public void createRuleActions(String ruleid, List<RuleAction> oRuleActions, EntityManager em) {
		try {
			for (RuleAction oRuleAction : oRuleActions) {
				if ((oRuleAction.getId() == null) || (oRuleAction.getId().trim().length() == 0)) {
					String ID = UUID.randomUUID().toString().replaceAll("-", "");
					oRuleAction.setId(ID);
					oRuleAction.setClient(aud.getUserDetails().getUserClientId());
				}
				oRuleAction.setRuleID(ruleid);
				oRuleAction.setClient(aud.getUserDetails().getUserClientId());
				em.persist(oRuleAction);
			}

		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public void deleteRuleAction(String id, List<RuleAction> ruleActionList, EntityManager em) {
		try {

			for (RuleAction oRuleAction : ruleActionList) {
				RuleAction ruleActionJPA = em.find(RuleAction.class, oRuleAction.getId());
				if (ruleActionJPA != null) {
					em.remove(ruleActionJPA);
				}
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public void mergeRuleActions(String ruleid, List<RuleAction> oRuleActions, EntityManager em) {
		try {
			for (RuleAction oRuleAction : oRuleActions) {
				oRuleAction.setRuleID(ruleid);
				em.merge(oRuleAction);
			}

		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	public RuleWithSteps getRuleDetails(EntityManager em, String _ruleId) {
		// TODO Auto-generated method stub
		RuleWithSteps _ruleWithSteps = new RuleWithSteps();
		_ruleWithSteps.rule = getRuleById(_ruleId, em);
		_ruleWithSteps.ruleAggregations = getRuleAggregationsByRuleId(_ruleId, em);
		_ruleWithSteps.ruleSteps = getRuleStepsByRuleId(_ruleId, em);
		_ruleWithSteps.ruleEvents = getRuleEventsByRuleId(_ruleId, em);
		_ruleWithSteps.ruleSubject = getRuleSubjectsByRuleId(_ruleId, em);
		_ruleWithSteps.ruleActions = getRuleActionByRuleId(_ruleId, em);
		return _ruleWithSteps;
	}

	public List<RuleAction> getRuleActionByRuleId(String ruleId, EntityManager em) {
		List<Object[]> rs = null;
		try {
			Query q = em.createNativeQuery(
					"select a.\"ID\", a.\"Type\", a.\"RuleID\", a.\"Name\",a.\"MultipleEmails\", a.\"Description\", a.\"ActionParams\", a.\"ActionID\", b.\"ActionName\", b.\"ActionType\", a.\"Client\" from \"sap.ain.metaData::Rules.RuleAction\" a LEFT OUTER JOIN \"sap.ain.metaData::Rules.Action\" b on a.\"ActionID\" = b.\"ActionID\" where a.\"RuleID\" = ?");
			q.setParameter(1, ruleId);
			rs = q.getResultList();
			if (rs == null) {
				return new ArrayList<RuleAction>();
			} else {
				List<RuleAction> ruleActionList = new ArrayList<RuleAction>();
				RuleAction ruleAction;
				for (Object[] r : rs) {
					ruleAction = new RuleAction();
					ruleAction.setActionId((r[7] != null ? String.valueOf(r[7].toString()) : ""));
					ruleAction.setName((r[3] != null ? String.valueOf(r[3].toString()) : ""));
					ruleAction.setType((r[1] != null ? String.valueOf(r[1].toString()) : ""));
					ruleAction.setActionParams((r[6] != null ? String.valueOf(r[6].toString()) : ""));
					ruleAction.setDescription((r[5] != null ? String.valueOf(r[5].toString()) : ""));
					ruleAction.setId(String.valueOf(r[0].toString()));
					ruleAction.setMultipleEmails((r[4] != null ? String.valueOf(r[4].toString()) : ""));
					ruleAction.setName((r[3] != null ? String.valueOf(r[3].toString()) : ""));
					ruleAction.setRuleID(String.valueOf(r[2].toString()));
					ruleAction.setType(String.valueOf(r[1].toString()));
					ruleAction.setClient(String.valueOf(r[10].toString()));
					ruleActionList.add(ruleAction);
				}
				return ruleActionList;
			}
		} catch (Exception e) {
			logger.error("Error is: " + e.getMessage());
			return null;
		} finally {
		}
	}

	public Model getExternalIDsForaAModel(String modelId, EntityManager em) {
		String client = aud.getUserDetails().getUserClientId();
		String lang = aud.getUserDetails().getLocale().getLanguage();
		String scope = aud.getUserDetails().getScope();
		String bpid = aud.getUserDetails().getUserBpId();
		String queryForRulesSubject = "Select * from \"_SYS_BIC\".\"sap.ain.views.rules/Models\" "
				+ "(PLACEHOLDER.\"$$iv_client$$\" => ?, PLACEHOLDER.\"$$iv_scope$$\" => ?, PLACEHOLDER.\"$$iv_user_bp_id$$\" => ? ,PLACEHOLDER.\"$$iv_lang$$\" => ?)";
		List<Model> models = jdbcTemplate.query(queryForRulesSubject,
				new Object[] {client, scope, bpid, lang}, new Model());

		Optional<Model> optionalModel =
				models.stream().filter(model -> model.getModelId().equals(modelId)).findFirst();
		if (optionalModel.isPresent()) {
			return optionalModel.get();
		}
		return null;
	}

	public List<Object[]> getAllRuleJobs(EntityManager em) {
		try {
			TypedQuery<Object[]> query = em.createQuery(
					"select trigger.id, trigger.cronexpression, rule.tenantSubDomain from RuleJobTriggers trigger, Rule rule where rule.id=trigger.id",
					Object[].class);
			List<Object[]> results = query.getResultList();
			return results;
		} catch (NoResultException e) {
			logger.error("Exception in RuleService.getAllRuleJobs: {}" + e.getMessage());
			return null;
		} finally {
		}
	}


	public List<RuleStartUp> getAllRuleJobsWithOrgCheck() {
		try {

			List<RuleStartUp> rules = new ArrayList<RuleStartUp>();
			String query = "select \"trigger\".\"CronExpression\", \"Rule\".\"TenantSubDomain\",\"trigger\".\"ID\"\n" + 
					"from \"AIN_DEV\".\"sap.ain.metaData::Rules.RuleJobTriggers\" \"trigger\" join \"AIN_DEV\".\"sap.ain.metaData::Rules.Rule\" \"Rule\"\n" + 
					" on \"trigger\".\"ID\" = \"Rule\".\"ID\" where \"Rule\".\"Client\" in\n" + 
					"(select \"BP\".\"Client\" from \"sap.ain.metaData::BusinessPartner.OrgAdditionalDetails\" \"BP\" where \"BP\".\"isActive\" = 1)";





			rules = jdbcTemplate.query(query,(rs, rowNum) -> {
				RuleStartUp rule =
						new RuleStartUp();
				rule.setId(rs.getString("ID"));
				rule.setCronexpression(rs.getString("CronExpression"));
				rule.setTenantSubDomain(rs.getString("TenantSubDomain"));


				return rule;
			});








			return rules;
		} catch (NoResultException e) {
			logger.error("Exception in RuleService.getAllRuleJobs: {}" + e.getMessage());
			return null;
		} finally {
		}
	}

	public List<String> getAlertExternalID(String id) {
		List<String> externalIds = new ArrayList<>();
		String query = "select \"ExternalID\" from \"AIN_DEV\".\"sap.ain.metaData::AlertType.AlertTypeExternalIdTable\" where \"ID\" = ? and \"Client\" = ?";
		Object[] params = new Object[] {id,aud.getUserDetails().getUserClientId() };
		try {
			externalIds = jdbcTemplate.query(query, params, (rs, rowNum) -> rs.getString("ExternalID"));
		} catch (DataAccessException de) {
			logger.error("Exception while fetching externalId for the alert type GUID", de);
			throw de;
		}
		return externalIds;
	}

	public List<GetUsersForAnOrganization> getUsersForAnOrganization() {

		String getUsersForAnOrganizationQuery =
				"select * from \"_SYS_BIC\".\"sap.ain.views/GetUsersForAnOrganization\" ('PLACEHOLDER' = ( '$$iv_client$$', '"
						+ aud.getUserDetails().getUserClientId() + "'),\r\n"
						+ "'PLACEHOLDER' = ( '$$iv_lang$$', '" + aud.getUserDetails().getLocale().getLanguage()
						+ "' ))";

		List<GetUsersForAnOrganization> getUsersForAnOrganization = new ArrayList<>();
		try {
			getUsersForAnOrganization =
					(List<GetUsersForAnOrganization>) jdbcTemplate.query(getUsersForAnOrganizationQuery, null,
							null,
							new GenericRowMapper<GetUsersForAnOrganization>(GetUsersForAnOrganization.class));
		} catch (DataAccessException exception) {
			logger.error("Exception in RuleService.getUsersForAnOrganization: {}",
					exception.getMessage());
		}

		return getUsersForAnOrganization;
	}

	public List<GetUsersForAnOrganization> getUserOfAnOrganization(String personId) {

		String getUsersForAnOrganizationQuery =
				"select * from \"_SYS_BIC\".\"sap.ain.views/GetUsersForAnOrganization\" ('PLACEHOLDER' = ( '$$iv_client$$', '"
						+ aud.getUserDetails().getUserClientId() + "')," + "'PLACEHOLDER' = ( '$$iv_lang$$', '"
						+ aud.getUserDetails().getLocale().getLanguage() + "' ))" + " WHERE \"PersonID\" ='"
						+ personId + "';";

		List<GetUsersForAnOrganization> getUsersForAnOrganization = new ArrayList<>();
		try {
			getUsersForAnOrganization =
					(List<GetUsersForAnOrganization>) jdbcTemplate.query(getUsersForAnOrganizationQuery, null,
							null,
							new GenericRowMapper<GetUsersForAnOrganization>(GetUsersForAnOrganization.class));
		} catch (DataAccessException exception) {
			logger.error("Exception in RuleService.getUsersForAnOrganization: {}",
					exception.getMessage());
		}

		return getUsersForAnOrganization;
	}

	public List<IndicatorConfigurationThresholdEntity> getThresholdsForIndicator(String objectId,
			String categoryId, String pstId, String indicatorId) {

		try {
			List<IndicatorConfigurationThresholdEntity> indicatorThresholds = new ArrayList<>();
			Query query = em.createNativeQuery("select \n"
					+ "	threshold.\"ConfigID\" as \"IndicatorInstanceID\",\n"
					+ "	threshold.\"ColorCode\" as \"ColorCode\",\n"
					+ "	threshold.\"RangeFrom\" as \"RangeFrom\",\n"
					+ "	threshold.\"RangeTo\" as \"RangeTo\",\n"
					+ "	threshold.\"Description\" as \"Description\",\n"

          + "	threshold.\"DisplayOrder\" as \"DisplayOrder\",\n"
          + "	threshold.\"Primary\" as \"Primary\"\n"
          + "from \"AIN_DEV\".\"sap.ain.metaData::Object.IndicatorConfiguration\" as config \n"
          + "inner join \"AIN_DEV\".\"sap.ain.metaData::Object.IndicatorConfigurationThreshold\" as threshold\n"
          + "on \n" + "	config.\"ID\" = threshold.\"ConfigID\"\n" + "where \n"
          + "	config.\"ObjectID\" = ?\n" + "and config.\"CategoryID\" = ?\n"
          + "and config.\"PSTID\" = ?\n" + "and config.\"PropertyID\" = ?");
			query.setParameter(1, objectId);
			query.setParameter(2, categoryId);
			query.setParameter(3, pstId);
			query.setParameter(4, indicatorId);

			List<Object[]> rs = query.getResultList();

			if (rs == null) {
				return new ArrayList<IndicatorConfigurationThresholdEntity>();
			} else {
				for (Object[] r : rs) {
					IndicatorConfigurationThresholdEntity entity =
							new IndicatorConfigurationThresholdEntity();
					entity.setConfigId(r[0] != null ? String.valueOf(r[0].toString()) : "");
					entity.setColorCode(r[1] != null ? String.valueOf(r[1].toString()) : "");
					entity.setRangeFrom(r[2] != null ? r[2].toString() : "0");
					entity.setRangeTo(r[3] != null ? r[3].toString() : "0");
					entity.setDescription(r[4] != null ? String.valueOf(r[4].toString()) : "");
					entity.setDisplayOrder(r[5] != null ? Integer.parseInt(r[5].toString()) : 0);
					// entity.setIsPrimary(r[6] != null ? Boolean.parseBoolean(r[6].toString()) : false);
					indicatorThresholds.add(entity);
				}
			}

			return indicatorThresholds;

		} catch (Exception e) {
			logger.error("Error is: " + e.getMessage());
			return null;
		}

	}

	// public List<IndicatorConfigurationThresholdEntity> getThresholdsForIndicatorOld(String
	// objectId,
	// String categoryId, String pstId, String indicatorId) {
	// TypedQuery<IndicatorConfigurationEntity> thresholdForIndicatorQuery = em.createNamedQuery(
	// IndicatorConfigurationEntity.QUERY_BY_OBJ_CAT_AG_ATT, IndicatorConfigurationEntity.class);
	// thresholdForIndicatorQuery.setParameter(IndicatorConfigurationEntity.QUERY_PARAM_OBJECT_ID,
	// objectId);
	// thresholdForIndicatorQuery.setParameter(IndicatorConfigurationEntity.QUERY_PARAM_CAT,
	// categoryId);
	// thresholdForIndicatorQuery.setParameter(IndicatorInstanceEntity.QUERY_PARAM_AG, pstId);
	// thresholdForIndicatorQuery.setParameter(IndicatorInstanceEntity.QUERY_PARAM_ATT,
	// indicatorId);
	// thresholdForIndicatorQuery
	// .setParameter(IndicatorInstanceEntity.QUERY_PARAM_IS_MARKED_FOR_DELETION, "0");
	// List<IndicatorInstanceThreshold> indicatorInstanceEntityList
	// = thresholdForIndicatorQuery.getResultList();
	// List<IndicatorConfigurationThresholdEntity> indicatorThresholds = new ArrayList<>();
	// if (indicatorInstanceEntityList != null && !indicatorInstanceEntityList.isEmpty()) {
	// indicatorThresholds = indicatorInstanceEntityList.get(0).getThresholds();
	// }
	// return indicatorThresholds;
	//
	// }

	@Transactional
	public ResponseData createRuleEquipmentMapping(
			RuleEquipmentMappingPOST ruleEquipmentMappingPOST) {
		deleteEquipmentMappingForARule(ruleEquipmentMappingPOST);
		try {
			List<String> equipmentIds = ruleEquipmentMappingPOST.getEquipmentIds();
			ResponseData response = new ResponseData();
			if (equipmentIds == null) {
				response.setId(ruleEquipmentMappingPOST.getRuleId());
				response.setStatus(true);
				return response;
			}
			String insertQuery = RulesUtils.insertIntoEquipmentMappingQuery();
			jdbcTemplate.batchUpdate(insertQuery, new BatchPreparedStatementSetter() {

				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					String equipmentId = equipmentIds.get(i);
					ps.setString(1, aud.getUserDetails().getUserClientId());
					ps.setString(2, ruleEquipmentMappingPOST.getRuleId());
					ps.setString(3, equipmentId);
					ps.setString(4, ruleEquipmentMappingPOST.getModelId());
				}

				@Override
				public int getBatchSize() {
					return equipmentIds.size();
				}
			});

			response.setId(ruleEquipmentMappingPOST.getRuleId());
			response.setStatus(true);
			return response;
		} catch (Exception exception) {
			logger.error("Exception in RuleService.createRuleEquipmentMapping {}", exception);


			throw exception;
		}
	}

	@Transactional
	public ResponseData createRuleEquipmentMapping(String ruleId,
			RuleEquipmentMapping ruleEquipmentMapping) {

		try {
			List<String> equipmentIds = ruleEquipmentMapping.getEquipmentIds();
			ResponseData response = new ResponseData();
			if (equipmentIds == null) {
				response.setId(ruleId);
				response.setStatus(true);
				return response;
			}
			String insertQuery = RulesUtils.insertIntoEquipmentMappingQuery();
			jdbcTemplate.batchUpdate(insertQuery, new BatchPreparedStatementSetter() {

				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					String equipmentId = equipmentIds.get(i);
					ps.setString(1, aud.getUserDetails().getUserClientId());
					ps.setString(2, ruleId);
					ps.setString(3, equipmentId);
					ps.setString(4, ruleEquipmentMapping.getSubjectId());
				}

				@Override
				public int getBatchSize() {
					return equipmentIds.size();
				}
			});

			updateRuleHeaderTable(ruleId);
			response.setId(ruleId);
			response.setStatus(true);
			return response;
		} catch (Exception exception) {
			logger.error("Exception in RuleService.createRuleEquipmentMapping {}", exception);


			throw exception;
		}
	}

	private void updateRuleHeaderTable(String ruleId) {

		// TODO Auto-generated method stub
		String query = "Update \"sap.ain.metaData::Rules.Rule\" "
				+ "SET \"SystemAdministrativeData.LastChangedByUser.ID\" = ? , \"SystemAdministrativeData.LastChangeDateTime\" = ? "
				+ "WHERE \"ID\" = ? ";
		try {
			jdbcTemplate.update(query, new Object[] {aud.getUserDetails().getUserBpId(),
					DateHelper.getCurrentTimestamp(), ruleId});
		} catch (DataAccessException e) {
			logger.error("Exception in RuleService.createRuleEquipmentMapping.updateRuleHeaderTable {}",
					e);
		}
	}

	private void deleteEquipmentMappingForARule(RuleEquipmentMappingPOST ruleEquipmentMappingPOST) {
		try {
			String deleteQuery =
					"Delete from \"sap.ain.metaData::Rules.RuleEquipmentMapping\" where  \"RuleID\" = ? ";
			jdbcTemplate.update(deleteQuery, new Object[] {
					ruleEquipmentMappingPOST.getRuleId()});
		} catch (Exception exception) {
			logger.error("Exception in RuleService.deleteEquipmentMappingForARule: {}", exception);
			throw exception;
		}
	}

	public void unAssignEquipmentForARule(RuleEquipmentMappingPOST ruleEquipmentMappingPOST) {
		List<String> equipmentIds = ruleEquipmentMappingPOST.getEquipmentIds();
		if (ObjectUtils.isListEmpty(equipmentIds)) {
			return;
		}
		String deleteEquipmentMappingDataQuery =
				"delete from \"sap.ain.metaData::Rules.RuleEquipmentMapping\" where \"RuleID\" = ? and \"EquipmentID\" in ("
						+ ObjectUtils.getQuestionMark(equipmentIds.size()) + ");";
		ArrayList<String> idList = new ArrayList<>();
		idList.addAll(equipmentIds);
		idList.add(0, ruleEquipmentMappingPOST.getRuleId());
		//idList.add(0, aud.getUserDetails().getUserClientId());
		jdbcTemplate.update(deleteEquipmentMappingDataQuery, idList.toArray());
	}

	public ResponseData unAssignEquipmentForARule(String ruleId,
			RuleEquipmentMapping ruleEquipmentMapping) {
		ResponseData response = new ResponseData();
		List<String> equipmentIds = ruleEquipmentMapping.getEquipmentIds();
		if (ObjectUtils.isListEmpty(equipmentIds)) {
			response.setId(ruleId);
			response.setStatus(true);
			return response;
		}
		String deleteEquipmentMappingDataQuery =
				"delete from \"sap.ain.metaData::Rules.RuleEquipmentMapping\" where \"RuleID\" = ? and \"EquipmentID\" in ("
						+ ObjectUtils.getQuestionMark(equipmentIds.size()) + ");";
		ArrayList<String> idList = new ArrayList<>();
		idList.addAll(equipmentIds);
		idList.add(0, ruleId);
		//idList.add(0, aud.getUserDetails().getUserClientId());
		try {
			jdbcTemplate.update(deleteEquipmentMappingDataQuery, idList.toArray());

			updateRuleHeaderTable(ruleId);

			response.setId(ruleId);
			response.setStatus(true);
			return response;
		} catch (DataAccessException e) {
			response.setId(ruleId);
			response.setStatus(false);
			return response;
		}


	}

	public void persistRuleRun(String ruleId, String runId, String client, Instant fireTime) {
		String query = RulesUtils.insertIntoRuleRunsQuery();
		try {
			jdbcTemplate.update(query, new Object[] {client, runId, ruleId, "", fireTime, "", ""});
		} catch (DataAccessException de) {
			logger.error("Exception in RuleService.persistRuleRun: {}", de);
			throw de;
		}
	}

	public void updateRuleRun(ExecutableRuleJson ruleJson, String executionStatus, String runId,
			String client) {
		String query = RulesUtils.updateRuleRunsQuery();
		try {
			jdbcTemplate.update(query, new Object[] {executionStatus,
					ruleJson.getRuleSubject().getSubjectID(), client, runId});
		} catch (DataAccessException de) {
			logger.error("Exception in RuleService.persistRuleRun: {}", de);
		}
	}

	public void persistRuleExecutionLogs(ExecutableRuleJson ruleJson, String message, String runId,
			String client) {
		String query = RulesUtils.insertIntoRuleExecutionLogsQuery();
		try {
			jdbcTemplate.update(query,
					new Object[] {client, runId, ruleJson.getRuleEvents().get(0).getEventID(),
							ruleJson.getRuleId(), "", "", "", message, ruleJson.getRuleSubject().getSubjectID()});
		} catch (DataAccessException de) {
			logger.error("Exception in RuleService.persistRuleExecutionLogs: {}", de);
		}
	}

	@Transactional
	public void deleteRuleById(String id, EntityManager em)
			throws SchedulerException, NamingException {
		if (isScheduledJob(id, em)) {
			try {
				deleteJob(id, em);
				deleteJobTrigger(id, em);
			} catch (SchedulerException ex) {
				throw ex;
			}
		}
		deleteRule(id, em);
		deleteRuleSteps(id, em);
		deleteRuleAggregations(id, em);
		deleteRuleSubjects(id, em);
		deleteRuleEvents(id, em);
		deleteRuleActions(id, em);
		deleteRuleDescription(id, em);
	}

	/**
	 * This method will populate alertTypeId from actionParams
	 * 
	 * @param oRuleSteps
	 */
	@SuppressWarnings("unchecked")
	public void extractAlertTypeId(RuleWithSteps oRuleSteps) {
		String alertTypeActionId = "alerttype";
		List<RuleAction> ruleActions = oRuleSteps.getRuleActions();
		String jsonActionParams;
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);
		Map<String, Object> eventAttributes;
		List<String> externalIds = new ArrayList<String>();
		if (ruleActions != null) {
			for (RuleAction ruleAction : ruleActions) {
				if(!ruleAction.getActionId().equals(alertTypeActionId)) {
					continue;
				}
				try {
					if (ruleAction.getActionId().equals(alertTypeActionId)
							&& ruleAction.getAlertTypeId() != null
							&& ruleAction.getAlertTypeId().trim().length() != 0) {
						externalIds = getAlertExternalID(ruleAction.getAlertTypeId());
						ruleAction.setAlertTypeId(externalIds.get(0));
					}
					jsonActionParams = ruleAction.getActionParams();
					eventAttributes = mapper.readValue(jsonActionParams, HashMap.class);
					if (eventAttributes.get("alertID") != null) {
						externalIds = getAlertExternalID((String) eventAttributes.get("alertID"));
						ruleAction.setAlertTypeId(externalIds.get(0));
					}
				} catch (Exception ex) {
					logger.debug("Error updating alertTypeId, {}", ex);
				}
			}
		}
	}

	@Transactional
	public RuleWithSteps updateRuleWithSteps(String id, RuleWithSteps oRuleSteps, String tenantId)
			throws SchedulerException, NamingException {
		Rule existingRule = getRuleById(id, em);
		String cronExpression = null;
		existingRule.setIsEnabled(oRuleSteps.rule.getIsEnabled());
		existingRule.setDescription(oRuleSteps.rule.getDescription());
		existingRule.setRuleExpression(oRuleSteps.rule.getRuleExpression());

		oRuleSteps.rule = existingRule;
		updateRule(oRuleSteps.rule, em);
		if (oRuleSteps.ruleSteps != null)
			updateRuleSteps(id, oRuleSteps.ruleSteps, em,oRuleSteps.rule.getClient());
		if (oRuleSteps.ruleAggregations != null)
			updateRuleAggregations(id, oRuleSteps.ruleAggregations, em,oRuleSteps.rule.getClient());
		updateRuleSubject(id, oRuleSteps.ruleSubject, em,oRuleSteps.rule.getClient());
		if (oRuleSteps.ruleEvents != null)
			updateRuleEvents(id, oRuleSteps.ruleEvents, em,oRuleSteps.rule.getClient());
		if (oRuleSteps.ruleActions != null)
			updateRuleActions(id, oRuleSteps.ruleActions, em,oRuleSteps.rule.getClient());
		if (oRuleSteps.ruleDescription != null)
			updateRuleDescriptions(id, oRuleSteps.ruleDescription, em,oRuleSteps.rule.getClient());
		unscheduleJobTrigger(oRuleSteps.rule.getId(), em);

		deleteRuleJobs(id, em, oRuleSteps.rule.getClient());
		deleteRuleJobTriggers(id, em, oRuleSteps.rule.getClient());

		if (oRuleSteps.ruleEvents != null) {
			List<RuleEvent> ruleEvents = oRuleSteps.ruleEvents;
			CronExpression parser = new Parser();
			for (RuleEvent ruleEvent : ruleEvents) {
				if (ruleEvent.getEventID().equalsIgnoreCase("SCHEDULE")) {
					logger.debug("PQB------ updating JobSchedule");
					RuleJobSchedule js = new RuleJobSchedule();

					if (ruleEvent.getCron() != null) {
						try {
							cronExpression = parser.getCronExpression(ruleEvent.getCron());
							ruleEvent.setRunSchedule(cronExpression);
						} catch (RuleCronParsingException e) {
							logger.error(
									"Rule Service -updateRuleWithSteps-:::: Exception happned while parsing payload to cron expression");
						}

					}

					js.setId(oRuleSteps.rule.getId());
					js.setName(oRuleSteps.rule.getName());

					if (oRuleSteps.rule.getIsEnabled().toUpperCase().equals("TRUE")) {
						js.setStatus("Enabled");
					} else {
						js.setStatus("Disabled");
					}

					createJob(js, em,oRuleSteps.rule.getClient());

					RuleJobTriggers trigger = new RuleJobTriggers();
					trigger.setCronexpression(ruleEvent.getRunSchedule());
					trigger.setCronexpressionNotRun(ruleEvent.getNotRunSchedule());
					trigger.setId(oRuleSteps.rule.getId());
					trigger.setJobId(js.getId());
					trigger.setName(js.getName());

					createJobTrigger(trigger, em, tenantId,oRuleSteps.rule.getClient());
					logger.debug("PQB------ updated JobSchedule");

				}
			}

		}
		RuleWithSteps response = getRuleWithStepsById(id);
		return response;
	}

	@Transactional
	public void deleteRuleJobs(String id, EntityManager em, String client) {
		try {
			Query qrydelete =
					em.createQuery("delete from RuleJobSchedule rs where rs.id=:id and rs.client=:client");
			qrydelete.setParameter("id", id);
			qrydelete.setParameter("client", client);
			qrydelete.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public void deleteRuleJobTriggers(String id, EntityManager em, String client) {
		try {
			Query qrydelete =
					em.createQuery("delete from RuleJobTriggers rs where rs.id=:id and rs.client=:client");
			qrydelete.setParameter("id", id);
			qrydelete.setParameter("client", client);
			qrydelete.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public RuleWithSteps createRuleWithSteps(RuleWithSteps oRuleSteps, String tenantId)
			throws SchedulerException, NamingException {

		Rule orule = createRule(oRuleSteps.rule, em, tenantId);

		String cronExpression = null;


		logger.debug("Rule id generated is " + orule.getId());
		if (oRuleSteps.ruleSteps != null) {
			createRuleSteps(orule.getId(), oRuleSteps.ruleSteps, em,null);
		}

		if (oRuleSteps.ruleAggregations != null) {
			createRuleAggregations(orule.getId(), oRuleSteps.ruleAggregations, em,null);
		}

		if (oRuleSteps.ruleSubject != null) {
			createRuleSubject(orule.getId(), oRuleSteps.ruleSubject, em,null);
		}

		if (oRuleSteps.ruleDescription != null) {
			createRuleDescription(orule.getId(), oRuleSteps.ruleDescription, em,null);
		}

		if (oRuleSteps.ruleEvents != null) {
			CronExpression parser = new Parser();
			createRuleEvents(orule.getId(), oRuleSteps.ruleEvents, em,null);
			List<RuleEvent> ruleEvents = oRuleSteps.ruleEvents;
			for (RuleEvent ruleEvent : ruleEvents) {
				if (ruleEvent.getEventID().equalsIgnoreCase("SCHEDULE")) {
					logger.debug("PQB------ creating JobSchedule");
					RuleJobSchedule js = new RuleJobSchedule();

					// parse rule
					if (ruleEvent.getCron() != null) {
						try {
							cronExpression = parser.getCronExpression(ruleEvent.getCron());
							ruleEvent.setRunSchedule(cronExpression);
						} catch (RuleCronParsingException e) {
							logger.error(
									"Rule Service -createRuleWithSteps-:::: Exception happned while parsing payload to cron expression");
						}

					}

					js.setId(orule.getId());
					js.setName(orule.getName());

					if (orule.getIsEnabled().toUpperCase().equals("TRUE")) {
						js.setStatus("Enabled");
					} else {
						js.setStatus("Disabled");
					}

					createJob(js, em,null);

					RuleJobTriggers trigger = new RuleJobTriggers();
					trigger.setCronexpression(ruleEvent.getRunSchedule());
					trigger.setCronexpressionNotRun(ruleEvent.getNotRunSchedule());
					trigger.setId(orule.getId());
					trigger.setJobId(js.getId());
					trigger.setName(js.getName());

					createJobTrigger(trigger, em, tenantId,null);
					logger.debug("PQB------ started JobSchedule");

				}
			}
		}



		if (oRuleSteps.ruleActions != null) {
			createRuleActions(orule.getId(), oRuleSteps.ruleActions, em);
		}

		if (oRuleSteps.schedule != null) {
			RuleJobSchedule js = new RuleJobSchedule();

			js.setId(orule.getId());
			js.setName(orule.getName());
			if (orule.getIsEnabled().toUpperCase().equals("TRUE")) {
				js.setStatus("Enabled");
			} else {
				js.setStatus("Disabled");
			}

			createJob(js, em,null);

			RuleJobTriggers trigger = new RuleJobTriggers();
			trigger.setCronexpression(oRuleSteps.schedule);
			trigger.setId(orule.getId());
			trigger.setJobId(js.getId());
			trigger.setName(js.getName());

			createJobTrigger(trigger, em, tenantId,null);
		}

		// RuleWithSteps result = getRuleById(orule.getId(), em);
		RuleWithSteps response = getRuleWithStepsById(orule.getId());
		// List<RuleDescription> result = getRuleDescription(orule.getId(), em);



		return response;
	}



	@Transactional
	public void deleteActionsForARule(String id, EntityManager em, String clientId) {
		try {
			Query q = em.createQuery(
					"Delete from RuleAction ruleActionDelete where ruleActionDelete.ruleID = :ruleId and ruleActionDelete.client=:client");
			q.setParameter("ruleId", id);
			q.setParameter("client", clientId);
			q.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	@Transactional
	public void updateActionsForARule(String ruleid, List<RuleAction> oRuleActions,
			EntityManager em) {
		try {
			for (RuleAction oRuleAction : oRuleActions) {
				if (Strings.isNullOrEmpty(oRuleAction.getId())) {
					String id = UUID.randomUUID().toString().replaceAll("-", "");
					oRuleAction.setId(id);
				}
				oRuleAction.setRuleID(ruleid);
				em.persist(oRuleAction);
			}

		} catch (Exception ex) {
			throw ex;
		} finally {
		}
	}

	public void deleteRuleRunLogs(String startDate, String endDate) {
		String deleteRuleLogsQuery = "Delete from  \"AIN_DEV\".\"sap.ain.metaData::Rules.RuleRunLogs\" "
				+ "as \"RuleRunLogs\" " + " where \"RuleRunLogs\".\"Client\"= ? and "
				+ " \"RuleRunLogs\".\"ID\" IN (SELECT \"RuleRun\".\"ID\" from \"AIN_DEV\".\"sap.ain.metaData::Rules.RuleRun\" as \"RuleRun\" "
				+ " where \"RuleRun\".\"Client\"= ? and  \"RuleRun\".\"Timestamp\" BETWEEN ? AND ?)";
		jdbcTemplate.update(deleteRuleLogsQuery, new Object[] {aud.getUserDetails().getUserClientId(),
				aud.getUserDetails().getUserClientId(), startDate, endDate});
	}

	@Transactional
	public RuleDescription getRuleDescriptionById(String id, EntityManager em) {

		// TODO Auto-generated method stub
		RuleDescription desc = new RuleDescription();

		List<RuleDescription> desc_other = new ArrayList<RuleDescription>();
		desc = getRuleDescriptionForLoggedInLang(id, em, aud.getUserDetails().getLocale().getLanguage());
				
		if (desc == null) {
			desc =
					getRuleDescriptionForLoggedInLang(id, em, "en");
			if (desc == null) {
				desc_other = getRuleDescription(id, em);
				if ( desc_other == null || desc_other.isEmpty() ) {
					return null;
				} else {
					return desc_other.get(0);
				}
			} else {
				return desc;
			}
		} else {
			return desc;
		}


	}

	public RuleWithSteps getRuleWithStepsById(String id) {
		// TODO Auto-generated method stub
		RuleWithSteps rs = new RuleWithSteps();
		CronExpression expression = new Parser();
		Rule oRule = getRuleById(id, em);
		if (oRule != null) {
			rs.rule = oRule;
		}
		List<RuleStep> listRuleSteps = getRuleStepsByRuleId(id, em);
		if (listRuleSteps != null) {
			rs.ruleSteps = listRuleSteps;
		}
		List<RuleAggregation> listRuleAggegations = getRuleAggregationsByRuleId(id, em);
		if (listRuleAggegations != null) {
			rs.ruleAggregations = listRuleAggegations;
		}
		RuleSubject ruleSubject = getRuleSubjectsByRuleId(id, em);
		if (ruleSubject != null) {
			rs.ruleSubject = ruleSubject;
		}
		List<RuleEvent> listRuleEvents = getRuleEventsByRuleId(id, em);
		if (listRuleEvents != null) {
			listRuleEvents.forEach(item -> {
				if (item.getEventID().equals("schedule")) {
					try {
						item.setCron(expression.getCronDescription(item.getRunSchedule()));
						item.setCrondescription(expression.getSchedulerDescription(item.getRunSchedule()));
					} catch (RuleCronParsingException exception) {
						logger.error("Rule Service::getRuleWithStepsById ## Error while converting cron::cron to payload schedule type");

					}
				}
			});
			rs.ruleEvents = listRuleEvents;
		}
		List<RuleAction> listRuleActions = getRuleActionsByRuleId(id, em);
		if (listRuleActions != null) {
			rs.ruleActions = listRuleActions;
		}

		RuleDescription ruleDescription = getRuleDescriptionById(id, em);
		if (ruleDescription != null) {
			rs.ruleDescription = ruleDescription;
		}
		RuleJobSchedule job = findJob(id, em);
		if (job != null) {
			RuleJobTriggers jobTrigger = findJobTrigger(job.getId(), em);
			if (jobTrigger != null) {
				rs.schedule = jobTrigger.getCronexpression();
			}
		}
		rs.setSource(getOrganizationName(oRule.getClient()));
		if("1".equals(getAccountType(aud.getUserDetails().getUserClientId()))){
			if("BSC001".equals(isInviteeCheck(aud.getUserDetails().getUserClientId(),oRule.getClient()))) {
				rs.isInvitee =true;
			}else {
				rs.isInvitee = false;}
		}else if("3".equals(getAccountType(aud.getUserDetails().getUserClientId()))) {
			rs.isInvitee = false;
		}
			
		return rs;
	}




	private String getOrganizationName(String client) {
		// TODO Auto-generated method stub
		
		String  query  = "select CASE WHEN org.\"OrganizationName2\" IS NULL THEN org.\"OrganizationName1\" ELSE org.\"OrganizationName2\" \n" + 
				"END AS \"Source\" from \"AIN_DEV\".\"sap.ain.metaData::BusinessPartner.OrganizationName\" org where org.\"BusinessPartnerID\" = ?";
		String source = null;
		try {
			source = jdbcTemplate.queryForObject(query, new Object[] { client }, String.class);
		} catch (DataAccessException ex) {
			logger.error("Error while retrieving org name for client, {}", ex);

		}
		logger.debug("Inside getOrgName():Org name is" + source);
		return source;
		
	}

	private String getAccountType(String client) {

		String q = "select \"AccountType\"  from \"sap.ain.metaData::BusinessPartner.OrgAdditionalDetails\" "
				+ "where \"Client\" = ? and \"isActive\" = 1 ";
		String accountType = null;
		try {
			accountType = jdbcTemplate.queryForObject(q, new Object[] { client }, String.class);
		} catch (DataAccessException ex) {
			logger.error("Error while retrieving accoutn type for client, {}", ex);

		}
		logger.debug("Inside getAccountType():AccountType is" + accountType);
		return accountType;

	}

	private String getInviteeForPremium(String premiumClientId) {
		String q = "select \"BusinessPartner2\"  from \"sap.ain.metaData::BusinessPartner.BusinessPartnerRelationship\" "
				+ "where \"BusinessPartner1\" = ? and \"RelationshipType\" = 'BSC001'";
		String premium = null;
		try {
			premium = jdbcTemplate.queryForObject(q, new Object[] { premiumClientId }, String.class);
		} catch (DataAccessException ex) {
			logger.error("Error while retrieving invitee id for premium, {}", ex);

		}
		return premium;
	}


}
