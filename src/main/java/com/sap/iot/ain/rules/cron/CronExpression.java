package com.sap.iot.ain.rules.cron;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;

public interface CronExpression {

	CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);

	public String getCronExpression(CronFields fiels) throws RuleCronParsingException;

	public CronFields getCronDescription(String fields) throws RuleCronParsingException;

	public String getSchedulerDescription(String fields);
}
