package com.sap.iot.ain.rules.implementation;

import org.springframework.stereotype.Component;


@Component
public class RulesExecutorOld {
    /*

	private static final Logger logger = LoggerFactory.getLogger(RulesExecutorOld.class);

	@Autowired
	private RuleService service;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private RulesUtils rulesUtils;

	@Autowired
	private ActionHandler actionHandler;

	@Autowired
	private IndicatorHandler indicatorHandler;

	@Autowired
	private ClientSetup clientSetup;
	
	private String alertCreatedOn;
	
	private EmailData emailData;

	public RulesExecutorOld() {

	}

	public void execute(String ruleID, Instant fireTime, Instant previousFireTime) throws NamingException {
		String ruleRunID = DaoHelper.getUUID();
		String logMessages = "";
		String executionStatus = Constant.EXECUTIONSTATUS.FAILURE.getValue();
		// TODO: Logging of getRuleJSON needs to handle.
		// Problem is we are passing executable rule json object to persist method.
		// That signature needs to change. Then only we can adapt this.
		ExecutableRuleJsonOld ruleJson = getRuleJson(ruleID, fireTime);
		try {
			System.out.println("Rule id in execute method is " + ruleID);
			if (ruleJson.getTenantId() == null) {
				if (!logMessages.isEmpty()) {
					logMessages = logMessages + ", ";
				}
				logMessages = logMessages + "TenantID is null. Not able to fetch tenant id.";

			} else {
				executionStatus = evaluateRuleJson(ruleJson, fireTime, previousFireTime, logMessages);
			}

		} catch (Exception e) {
			if (!logMessages.isEmpty()) {
				logMessages = logMessages + ", ";
			}
			e.printStackTrace();
			logMessages = logMessages + "Exception during rule execution. " + e.getCause();
		} finally {
			service.persistRuleRun(ruleJson, executionStatus, ruleRunID, ruleJson.getClient());
			service.persistRuleExecutionLogs(ruleJson, logMessages, ruleRunID,
					ruleJson.getClient());
		}
	}

	private ExecutableRuleJsonOld getRuleJson(String ruleID, Instant fireTime) {
		ExecutableRuleJsonOld ruleJson = new ExecutableRuleJsonOld();
		RuleWithSteps ruleWithSteps = service.getRuleDetails(em, ruleID);

		ruleJson.setClient(ruleWithSteps.rule.getClient());
		ruleJson.setTenantId(ruleWithSteps.rule.getTenantSubDomain());
		ruleJson.setRuleId(ruleWithSteps.rule.getId());
		ruleJson.setName(ruleWithSteps.rule.getName());
		ruleJson.setPriority(ruleWithSteps.rule.getPriority());
		ruleJson.setIsEnabled(ruleWithSteps.rule.getIsEnabled());
		ruleJson.setDescription(ruleWithSteps.rule.getDescription());
		ruleJson.setRuleSubject(ruleWithSteps.ruleSubject);
		ruleJson.setRuleAggregations(ruleWithSteps.ruleAggregations);
		ruleJson.setRuleActions(ruleWithSteps.ruleActions);
		ruleJson.setRuleEvents(ruleWithSteps.ruleEvents);
		ruleJson.setRuleSteps(ruleWithSteps.ruleSteps);

		resolveRuleJSON(ruleJson, fireTime);

		return ruleJson;
	}

	private void resolveRuleJSON(ExecutableRuleJsonOld ruleJson, Instant fireTime) {
		getEquipmentsForRule(ruleJson, fireTime);
		rulesUtils.setExternalIdsForIndicator(ruleJson, ruleJson.getClient());
	}

	private void getEquipmentsForRule(ExecutableRuleJsonOld ruleJson, Instant fireTime) {
		String externalSysId = rulesUtils.getExternalSystemId(ruleJson.getClient(), "pdmsSysThing");
		List<Equipment> equipments = null;
		String subjectType = ruleJson.getRuleSubject().getSubjectType();
		if(subjectType.equals("0"))
			equipments = rulesUtils.getEquipmentListForARule(ruleJson.getRuleId(),
					externalSysId, ruleJson.getClient());
		else if(subjectType.equals("1")){
			equipments = rulesUtils.getEquipmentDetails(externalSysId,
					ruleJson.getRuleSubject().getSubjectID(), ruleJson.getClient());
		}
		ruleJson.getRuleSubject().setEquipments(equipments);
		ruleJson.getRuleSubject().setTimestamp(fireTime);
	}

	private String evaluateRuleJson(ExecutableRuleJsonOld ruleJson, Instant fireTime, Instant previousFireTime,
			String logMessages) throws NamingException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		int totalNoOfEquipmentsToBeExecuted = ruleJson.getRuleSubject().getEquipments().size();
		int noOfEquipmentsExecuted = 0;
		int totalNoOfStepsToBeExecuted = ruleJson.getRuleSteps().size();
		for (Equipment equipment : ruleJson.getRuleSubject().getEquipments()) {
			String expression = "(";
			int previousLevel = 1;
			int noOfStepsExecuted = 0;
			String subjectType = ruleJson.getRuleSubject().getSubjectType();
			String modelId = "";
			if(subjectType.equals("0")) {
				modelId = ruleJson.getRuleSubject().getSubjectID();
			} else if (subjectType.equals("1")) {
				modelId = equipment.getModelId();
			}
			for (RuleStepOld ruleStep : ruleJson.getRuleSteps()) {
				if (ruleStep.getIsStepOperator() == 1) {
					if (ruleStep.getLevel() > previousLevel) {
						expression = expression + "(";
					}
					if (ruleStep.getLevel() < previousLevel) {
						expression = expression + ")";
					}

					if (ruleStep.getOperator().equalsIgnoreCase(Constant.OR)) {
						expression = expression + "||";
					} else {
						expression = expression + "&&";
					}
				} else {
					int response = 0;
					String leftOperandValue = null;
					String rightOperandValue = null;
					String operator = ruleStep.getOperator();
					String dataType = ruleStep.getDataType();
					Range range = null;

					if (ruleStep.getLevel() > previousLevel) {
						expression = expression + "(";
					}
					if (ruleStep.getLevel() < previousLevel) {
						expression = expression + ")";
					}
					// Logic for left operand
					if (ruleStep.getField1Type() != null
							&& ruleStep.getField1Type().equalsIgnoreCase("NORMAL")) {
						if (ruleStep.getField1IsIndicator() != null
								&& ruleStep.getField1IsIndicator().equalsIgnoreCase("X")) {

							String pst = rulesUtils.getPSTName(ruleStep.getField1PST(), "pdmsSysPackage",
									ruleJson.getRuleSubject().getSubjectID(), ruleJson.getClient(),
									subjectType, ruleStep.getField1TemplateType().equals("0"));

							
							
							String startWithoutMilli =
									sdf.format(Date.from(previousFireTime));
							String endWithoutMilli = sdf.format(Date.from(fireTime));

							Map<String, String> indicatorMap = indicatorHandler.getIndicators(
									ruleJson.getClient(), pst, equipment.getEquipmentExternalId(),
									startWithoutMilli, endWithoutMilli, ruleJson.getTenantId());
							if (indicatorMap == null || indicatorMap.isEmpty()) {
								if (!logMessages.isEmpty()) {
									logMessages = logMessages + ", ";
								}
								logMessages = logMessages + "Failure reason for equipment ("
										+ equipment.getEquipmentName() + ") is: Indicator is null";
								break;
							}

							// pick your indicator value from here
							leftOperandValue = indicatorMap.get(ruleStep.getField1Ext() + "_MAX");
							if (leftOperandValue == null) {
								System.out.println("Map does not have the indicator ");
								if (!logMessages.isEmpty()) {
									logMessages = logMessages + ", ";
								}
								logMessages = logMessages + "Failure reason for equipment ("
										+ equipment.getEquipmentName()
										+ ") is: Indicator(Left operand value) value is null";
								break;
							}
						} else {
							// Logic for DB column will come here
							leftOperandValue = getAttributeValue(
									ruleJson.getRuleSubject().getSubjectID(), ruleStep.getField1(),
									ruleJson.getClient(), ruleStep.getDataType(), subjectType,
									ruleStep.getField1TemplateType(), ruleStep.getField1Template(),
									ruleStep.getField1PST());
							System.out.println("Left operand  is " + leftOperandValue);
							if (leftOperandValue == null) {
								if (!logMessages.isEmpty()) {
									logMessages = logMessages + ", ";
								}
								logMessages = logMessages + "Failure reason for equipment ("
										+ equipment.getEquipmentName()
										+ ") is: Attribute(Left operand value) value is null";
								break;
							}
						}
					}else if (ruleStep.getField1Type() != null
							&& ruleStep.getField1Type().equalsIgnoreCase("AGG")){
						Optional<RuleAggregationOld> optionalRuleAggregation = ruleJson.getRuleAggregations().stream().filter(aggregation -> aggregation.getName().equals(ruleStep.getField1())).findFirst();
						RuleAggregationOld ruleAggregation = optionalRuleAggregation.get();
						String pst = rulesUtils.getPSTName(ruleAggregation.getFieldPst(), "pdmsSysPackage", ruleJson.getRuleSubject().getSubjectID(), ruleJson.getClient(),
								subjectType, ruleAggregation.getFieldTemplateType().equals("1"));
						
						String startWithoutMilli = null;
						if(ruleAggregation.getTimeUnit().equalsIgnoreCase("S")){
							 startWithoutMilli = sdf.format(Date.from(fireTime.minus(Duration.ofSeconds(Integer.parseInt(ruleAggregation.getTimeFilter())))));
						}else if(ruleAggregation.getTimeUnit().equalsIgnoreCase("M")){
							 startWithoutMilli = sdf.format(Date.from(fireTime.minus(Duration.ofMinutes(Integer.parseInt(ruleAggregation.getTimeFilter())))));
						}else if(ruleAggregation.getTimeUnit().equalsIgnoreCase("H")){
							 startWithoutMilli = sdf.format(Date.from(fireTime.minus(Duration.ofHours(Integer.parseInt(ruleAggregation.getTimeFilter())))));
						}else if(ruleAggregation.getTimeUnit().equalsIgnoreCase("D")){
							 startWithoutMilli = sdf.format(Date.from(fireTime.minus(Duration.ofDays(Integer.parseInt(ruleAggregation.getTimeFilter())))));
						}
						String endWithoutMilli = sdf.format(Date.from(fireTime));

						Map<String, String> indicatorMap = indicatorHandler.getIndicators(
								ruleJson.getClient(), pst, equipment.getEquipmentExternalId(),
								startWithoutMilli, endWithoutMilli, ruleJson.getTenantId());
						if (indicatorMap == null || indicatorMap.isEmpty()) {
							if (!logMessages.isEmpty()) {
								logMessages = logMessages + ", ";
							}
							logMessages = logMessages + "Failure reason for equipment ("
									+ equipment.getEquipmentName() + ") is: Indicator for aggregation is null";
							break;
						}
						System.out.println("Map response is " + indicatorMap.toString());

						// pick your indicator value from here
						leftOperandValue = indicatorMap.get(ruleAggregation.getFieldExt() + "_" + ruleAggregation.getFunctionName());
						if (leftOperandValue == null) {
							System.out.println("Map does not have the indicator for aggregation ");
							if (!logMessages.isEmpty()) {
								logMessages = logMessages + ", ";
							}
							logMessages = logMessages + "Failure reason for equipment ("
									+ equipment.getEquipmentName()
									+ ") is: Indicator(Left operand value) value for aggregation is null";
							break;
						}

						System.out.println("LeftOperandValue (Indicator): " + leftOperandValue);
					}

					// logic for right operand
					if (ruleStep.getField2Type() != null
							&& ruleStep.getField2Type().equalsIgnoreCase("NORMAL")) {
						if (ruleStep.getField2IsIndicator() != null
								&& !ruleStep.getField2IsIndicator().equalsIgnoreCase("X")) {
							// Logic for DB column will come here
							rightOperandValue = getAttributeValue(
									ruleJson.getRuleSubject().getSubjectID(), ruleStep.getField2(),
									ruleJson.getClient(), ruleStep.getDataType(), subjectType,
									ruleStep.getField1TemplateType(), ruleStep.getField1Template(),
									ruleStep.getField1PST());
							System.out.println("right operand  is " + rightOperandValue);
							if (rightOperandValue == null) {
								if (!logMessages.isEmpty()) {
									logMessages = logMessages + ", ";
								}
								logMessages = logMessages + "Failure reason for equipment ("
										+ equipment.getEquipmentName()
										+ ") is: Attribute(Right operand value) value is null";
								break;
							}
						}
					} else if (ruleStep.getField2Type() != null
							&& ruleStep.getField2Type().equalsIgnoreCase("CONSTANT")) {
						rightOperandValue = ruleStep.getField2();
						if (rightOperandValue == null) {
							if (!logMessages.isEmpty()) {
								logMessages = logMessages + ", ";
							}
							logMessages = logMessages + "Failure reason for equipment ("
									+ equipment.getEquipmentName()
									+ ") is: Constant(Right operand value) value is null";
							break;
						}

					} else if (ruleStep.getField2Type() != null
							&& ruleStep.getField2Type().equalsIgnoreCase("THRESHOLD")) {
						rightOperandValue = ruleStep.getField2();
						String[] indicatorInstanceDetails = rightOperandValue.split(",");
						String indicatorInstanceId = indicatorInstanceDetails[0];
						String displayOrder = indicatorInstanceDetails[1];
						TypedQuery<IndicatorInstanceThresholdEntity> thresholdForIndicatorQuery =
								em.createQuery(
										"Select iite from  IndicatorInstanceThresholdEntity iite where iite.indicatorInstanceId=:indicatorInstanceId"
												+ " and iite.displayOrder=:displayOrder",
										IndicatorInstanceThresholdEntity.class);
						thresholdForIndicatorQuery.setParameter("indicatorInstanceId",
								indicatorInstanceId);
						thresholdForIndicatorQuery.setParameter("displayOrder",
								Integer.parseInt(displayOrder));
						List<IndicatorInstanceThresholdEntity> indicatorInstanceThresholdEntityList =
								thresholdForIndicatorQuery.getResultList();
						if (indicatorInstanceThresholdEntityList != null
								&& !indicatorInstanceThresholdEntityList.isEmpty()) {
							IndicatorInstanceThresholdEntity entity =
									indicatorInstanceThresholdEntityList.get(0);
							RangeEvaluator rangeEvaluator = new RangeEvaluator();
							range = rangeEvaluator.getRange(entity.getRangeFrom(),
									entity.getRangeTo());
							if (range == null) {
								if (!logMessages.isEmpty()) {
									logMessages = logMessages + ", ";
								}
								logMessages = logMessages + "Failure reason for equipment ("
										+ equipment.getEquipmentName()
										+ ") is: Threshold(Left operand value) value is null";
								break;
							}
						}

					}

					// Now create the respective expresssion evaluator
					if (dataType.equalsIgnoreCase(Constant.STRING)) {
						ExpressionEvaluator<String> expressionEvaluator =
								new ExpressionEvaluator<>();
						response = expressionEvaluator.compare(leftOperandValue, rightOperandValue);
					} else if ((dataType.equalsIgnoreCase(Constant.NUMBER))
							|| (dataType.equalsIgnoreCase(Constant.NUMERIC)) || (dataType.equalsIgnoreCase(Constant.NUMERICFLEXIBLE))) {
						if (ruleStep.getField2Type() != null
								&& ruleStep.getField2Type().equalsIgnoreCase("THRESHOLD")) {
							if (range.contains(Double.parseDouble(leftOperandValue))) {
								response = 1;
							} else {
								response = -1;
							}

						} else {
							ExpressionEvaluator<Double> expressionEvaluator =
									new ExpressionEvaluator<>();
							response = expressionEvaluator.compare(
									Double.parseDouble(leftOperandValue),
									Double.parseDouble(rightOperandValue));
						}
					} else if (dataType.equalsIgnoreCase(Constant.DATE)) {
						ExpressionEvaluator<Date> expressionEvaluator = new ExpressionEvaluator<>();
						response = expressionEvaluator.compare(new Date(leftOperandValue),
								new Date(rightOperandValue));
					}

					if (response > 0) {
						if (operator.equalsIgnoreCase(Constant.GREATER)
								|| operator.equalsIgnoreCase(Constant.GREATEROREQUAL)
								|| operator.equals(Constant.IN_RANGE)) {
							ruleStep.setExecutionStatus(true);

						} else {
							ruleStep.setExecutionStatus(false);
						}
					} else if (response == 0) {
						if (operator.equalsIgnoreCase(Constant.EQUAL)
								|| operator.equalsIgnoreCase(Constant.GREATEROREQUAL)
								|| operator.equalsIgnoreCase(Constant.LESSEROREQUAL)) {
							ruleStep.setExecutionStatus(true);

						} else {
							ruleStep.setExecutionStatus(false);
						}
					} else if (response < 0) {
						if (operator.equalsIgnoreCase(Constant.LESSER)
								|| operator.equalsIgnoreCase(Constant.LESSEROREQUAL)
								|| operator.equalsIgnoreCase(Constant.NOT_IN_RANGE)) {
							ruleStep.setExecutionStatus(true);

						} else {
							ruleStep.setExecutionStatus(false);
						}

					}
					expression = expression + ruleStep.getExecutionStatus();

				}
				previousLevel = ruleStep.getLevel();
				noOfStepsExecuted++;
			}
			if (totalNoOfStepsToBeExecuted == noOfStepsExecuted) {
				noOfEquipmentsExecuted++;
			}

			for (int index = previousLevel; index >= 1; index--) {
				expression = expression + ")";
			}

			System.out.println("Expression for rule: " + ruleJson.getRuleId() + " is " + expression);

			Boolean expRes;
			try {
				ExpressionParser parser = new SpelExpressionParser();
				Expression exp = parser.parseExpression(expression);
				expRes = exp.getValue(Boolean.class);
			} catch(Exception e) {
				expRes = false;
			}

			// Set rule action for equipment
			if (expRes) {

				List<RuleAction> ruleActions = ruleJson.getRuleActions();
				emailData = new EmailData();
				
				Optional<RuleAction> optionalAlertTypeAction = ruleActions.stream().filter(action -> action.getActionId().equalsIgnoreCase("alerttype")).findFirst();
				if(optionalAlertTypeAction.isPresent()) {
					RuleAction ruleAction = optionalAlertTypeAction.get();
					WSEvents events =
							getEvent(ruleAction, modelId, equipment.getEquipmentExternalId(), ruleJson.getTenantId(),
									emailData, fireTime, previousFireTime, subjectType);
					this.createEvent(ruleJson.getClient(), events, ruleJson.getTenantId());
				}
				
				Optional<RuleAction> optionalSendEmailAction = ruleActions.stream().filter(action -> action.getActionId().equalsIgnoreCase("sendemail")).findFirst();
				if(optionalSendEmailAction .isPresent()) {
					RuleAction ruleAction = optionalSendEmailAction.get(); 
					emailData.setEquipmentId(equipment.getEquipmentName());
					sendEmail(ruleAction, emailData);
				}
				
				Optional<RuleAction> optionalSendNotificationAction = ruleActions.stream().filter(action -> action.getActionId().equalsIgnoreCase("notification")).findFirst();
				if(optionalSendNotificationAction.isPresent()){
					RuleAction ruleAction = optionalSendNotificationAction.get();
					createNotification(ruleAction, emailData);
				}
			}
			System.out.println("Execution success");
		}
		String executionStatus;
		if (noOfEquipmentsExecuted == 0) {
			executionStatus = Constant.EXECUTIONSTATUS.FAILURE.getValue();
		} else if (noOfEquipmentsExecuted == totalNoOfEquipmentsToBeExecuted) {
			executionStatus = Constant.EXECUTIONSTATUS.SUCCESS.getValue();
		} else {
			executionStatus = Constant.EXECUTIONSTATUS.PARTIALLY_SUCCESS.getValue();
		}
		return executionStatus;

	}

	public WSEvents getEvent(RuleAction ruleAction, String modelId, String thingId,
			String tenantId, EmailData emailData, Instant fireTime, Instant previousfireTime, String subjectType) {
		WSEvents events = new WSEvents();
		try {
			JSONObject jsonObect = new JSONObject(ruleAction.getActionParams());

			String code = jsonObect.getString("errorCode");
			if (code == null || code.isEmpty()) {
				code = "None";
			}
			events.setCode(code);

			int severity = jsonObect.getInt("severity");
			events.setSeverity(severity);
			
			String severityDescription = jsonObect.getString("severityDescription");
			emailData.setAlertSeverity(severityDescription);

			String eventType = jsonObect.getString("externalID");
			events.setEventType(eventType);
			emailData.setAlertType(eventType);

			String status = jsonObect.getString("status");
			events.setStatus(status);

			String indicatorId = jsonObect.getString("indicatorID");
			String externalIndicatorId = rulesUtils.getExternalIDforIndicator(indicatorId, "pdmsSysPackage", modelId, ruleAction.getClient());
			emailData.setAlertProperty(externalIndicatorId);

			String indicatorGroupId = jsonObect.getString("indicatorGroupID");
			String externalIndicatorGroupId = rulesUtils.getNamedPSTId(indicatorGroupId, "pdmsSysPackage", modelId, ruleAction.getClient());

			String externalModelId = rulesUtils.getExternalIdModel(modelId, "pdmsSysPackage", ruleAction.getClient());
			emailData.setModelId(externalModelId);
				
			Map<String, Object> alert_details = new HashMap<>();
			alert_details.put("AlertSource", "Rule");
			alert_details.put("AlertDescription", ruleAction.getDescription());
			
			if(!Strings.isNullOrEmpty(externalModelId) && !Strings.isNullOrEmpty(externalIndicatorGroupId) && !Strings.isNullOrEmpty(externalIndicatorId)) {
				String thingProperty = externalModelId + "/" + externalIndicatorGroupId + "/" + externalIndicatorId;
				events.setThingProperty(thingProperty);
				SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				String startDateForThingData = sdfFormat.format(Date.from(previousfireTime));
				String endDateForThingData = sdfFormat.format(Date.from(fireTime));
				String pst = rulesUtils.getPSTName(indicatorGroupId, "pdmsSysPackage", modelId, ruleAction.getClient(), subjectType, true);
				Map<String, String> indicatorMap = 
						indicatorHandler.getIndicators(ruleAction.getClient(), pst , thingId,
								startDateForThingData, endDateForThingData, tenantId);
				if(indicatorMap != null) {
					alert_details.put(externalIndicatorId, indicatorMap.get(externalIndicatorId + "_MAX"));
				}
			}
			
			events.setAlert_details(alert_details);

			Date date = Date.from(fireTime);
			SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d yyyy hh:mm:ss 'GMT'Z (z)");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			alertCreatedOn = sdf.format(date);
			emailData.setAlertCreatedOn(alertCreatedOn);
			events.setBusinessTimeStamp(
					new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss'Z'").format(date));
			events.setType(tenantId.replaceAll("-", ".") + ".pdms.events:AlertsPST");
			events.setDescription(ruleAction.getDescription());
			events.setThingId(thingId);
			events.setSource("Rule");
		} catch (JSONException e) {
			logger.error("{}", e);
		}
		return events;
	}

	// Create event as a rule violation
	public void createEvent(String clientId, WSEvents events, String tenantId) {
		actionHandler.createEvent(clientId, events, tenantId);
	}

	public String getAttributeValue(String ruleSubjectID, String field, String clientId,
			String dataType, String subjectType, String field1TemplateType,
			String field1Template, String field1TemplatePST) {
		String value = "";

		List<AttributeValue> attributeValues =
				rulesUtils.getAttributes(clientId, ruleSubjectID, field, subjectType,
						field1TemplateType, field1Template, field1TemplatePST);
		if (attributeValues == null || attributeValues.isEmpty()) {
			return value;
		}
		AttributeValue attributeValue = attributeValues.get(0);

		if (dataType.equalsIgnoreCase("string") || dataType.equalsIgnoreCase("numeric")) {
			value = attributeValue.getStringValue();
		} else if (dataType.equalsIgnoreCase("date")) {
			if (attributeValue.getDateValue() != null) {
				value = attributeValue.getDateValue().toString();
			}
		}
		return value;
	}

	private void sendEmail(RuleAction ruleAction, EmailData emailData) {
		logger.debug("Sending email for rule: " + ruleAction.getRuleID());
		String emailSubject = EmailUtils.getEmailSubject(emailData);
		String emailBody = EmailUtils.getEmailBody(emailData);
		EmailActionParam emailActionParam = null;
		try
		{
		  emailActionParam = new ObjectMapper().readValue(ruleAction.getActionParams(), EmailActionParam.class);
		} catch(IOException e) {
		  logger.error("Exception while parsing email data from rule action: {} " + e.getMessage());	
		}
		
		List<EmailRecipient> emailRecipients = emailActionParam.getTo();
		
		List<String> recipients = new ArrayList<>();
		List<GetUsersForAnOrganization> allUsersForAnOrganization =
				rulesUtils.getUsersForAnOrganization(ruleAction.getClient());
		for (EmailRecipient emailRecipient : emailRecipients) {
			Optional<GetUsersForAnOrganization> optional = allUsersForAnOrganization.stream()
					.filter(user -> user.getPersonId().equals(emailRecipient.getPersonID()))
					.findFirst();
			if (optional.isPresent()) {
				recipients.add("email:" + optional.get().getEmailAddress());
			}
		}
		
		EmailUtils emailUtils = new EmailUtils();
		EmailAuthenticationInformation emailAuthenticationInformation = emailUtils.getEmailAuthDetailsFromSystemEnv();
		if(emailAuthenticationInformation == null) {
			logger.debug("Email ups service is null. Please check");
			return;
		}

		Email email = new Email();
		email.setReplyTo(emailAuthenticationInformation.getReplyTo());
		email.setSubject(emailSubject);

		Configuration configuration = new Configuration();
		configuration.setEmail(email);

		EmailNotification emailNotification = new EmailNotification();

		emailNotification.setRecipients(recipients);
		emailNotification.setContentTextType("text/html");
		emailNotification.setContentText(emailBody);
		emailNotification.setContentTextEncoding("NONE");
		emailNotification.setSender(emailAuthenticationInformation.getSender());
		emailNotification.setConfiguration(configuration);

		EmailRequestPayload emailRequestPayload = new EmailRequestPayload();
		emailRequestPayload.SAPnotification = emailNotification;

		try {
			String value = emailAuthenticationInformation.getEmailUrl();

			Entity<EmailRequestPayload> requestPaylod =
					Entity.entity(emailRequestPayload, MediaType.APPLICATION_JSON);
	        
			Response response = clientSetup.post(value, "basic " + Base64.encodeBase64String((emailAuthenticationInformation.getUsername() + ":" + new String(emailAuthenticationInformation.getPassword())).getBytes()),
					requestPaylod);
			logger.debug("Email Response: " + response.getStatus());
		} catch (Exception e) {
			logger.error("Email sending failing : {}", e);
		}

	}
	
	private void createNotification(RuleAction ruleAction, EmailData emailData){
		NotificationServices services = new NotificationServices();
		NotificationActionParam notificationActionParam = null;
		try {
			notificationActionParam = new ObjectMapper().readValue(ruleAction.getActionParams(), NotificationActionParam.class);
		} catch (IOException e) {
			System.out.println("Exception while parsing Notification data from rule actions "+e.getMessage());
		}
		List<String> notificationType = new ArrayList<>();
		notificationType.add("NEW");
		NotificationDescription description = new NotificationDescription();
		getNotificationDescription(description, emailData);
		NotificationPOST post = new NotificationPOST();
		post.setType(notificationActionParam.getType());
		post.setPriority(notificationActionParam.getPriority());
		post.setStatus(notificationType);
		post.setDescription(description);
		services.createInternalNotification(post);
	}
	
	private void getNotificationDescription(NotificationDescription description, EmailData emailData){
		String alertType = emailData.getAlertType();
		String modelId = emailData.getModelId();
		StringBuilder sb = new StringBuilder("The machine ");
		sb.append(modelId).append(" ").append(emailData.getEquipmentId()).append(" ").append("reported the following error:").append(System.getProperty("line.separator"));
		sb.append(alertType.substring(alertType.indexOf('_') + 1, alertType.length())).append(" (").append(emailData.getAlertSeverity()).append(" ").append(emailData.getAlertCreatedOn()).append(")").append(System.getProperty("line.separator"));
		sb.append("Measuring Point: ").append(emailData.getAlertProperty()).append(System.getProperty("line.separator"));
		description.setShortDescription(alertType);
		description.setLongDescription(sb.toString());
	}
*/
}
