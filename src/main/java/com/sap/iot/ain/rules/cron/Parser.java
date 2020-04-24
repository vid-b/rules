package com.sap.iot.ain.rules.cron;

import static com.cronutils.model.field.expression.FieldExpressionFactory.always;
import static com.cronutils.model.field.expression.FieldExpressionFactory.every;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;
import static com.cronutils.model.field.expression.FieldExpressionFactory.questionMark;

import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cronutils.builder.CronBuilder;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.parser.CronParser;
import com.sap.iot.ain.rules.cron.CronFields;
import com.sap.iot.ain.rules.cron.Operation;
import com.sap.iot.ain.rules.services.RuleService;

/**
 * @author I323753
 * @see This class handles the encoding and the decoding of JSON and CRON sent from client
 * respectively. The implementation is based on cron-util and it follows a pattern like ALWAYS,
 * EVERY, ON and BETWEEN. User will be sending mostly EVERY and ON to specify the recurring
 * pattern. Always has every has different Meaning and need to be interpreted differently. In
 * cron – util every with unit value is Always( STAR/1 == STAR) In case of encoding user might
 * not pass every value, only specifies every, default 1 need to be considered. Currently, when
 * the user says every and one unit value, it's encoded as STAR/1 to make decoding easier and
 * error-free. Decoding logic is based on precedence year > month >week>day>hour>minute .
 * Decoding logic is also based on Relative cron expression if year , month and day does not
 * exist and hour exist , that equaly means every day .
 */
public class Parser implements CronExpression {

    private static final Logger logger = LoggerFactory.getLogger(RuleService.class);

    public Parser() {
    }

    /**
     * @param CronFields
     * @return String
     */
    public String getCronExpression(CronFields fiels) throws RuleCronParsingException {

        CronBuilder cronBuilder = CronBuilder.cron(cronDefinition);
        Cron cron = null;
        boolean isYearEvery = false, isMonthEvery = false, isDayEvery = false, isHourEvery = false,
                isMinuteEvery = false;
        Integer value = null;
        String[] crons = new String[7];
        try {
            // YEAR
            if (fiels.getYear() != null && fiels.getYear().getOperation() == Operation.ALWAYS) {
                cronBuilder = cronBuilder.withYear(always());
            } else if (fiels.getYear() != null && fiels.getYear().getOperation() == Operation.EVERY) {
                value = fiels.getYear().getValue();
                if (value == null || value == 0 || value == 1) {
                    value = 1;
                    isYearEvery = true;
                }
                cronBuilder = cronBuilder.withYear(every(value));

            } else if (fiels.getYear() != null && fiels.getYear().getOperation() == Operation.ON) {
                cronBuilder = cronBuilder.withYear(on(fiels.getYear().getValue()));
            } else {
                cronBuilder = cronBuilder.withYear(always());
            }

            // Day of the week
            // Every 3 days a week use case does not exist
            if (fiels.getDayOfTheWeek() != null
                    && fiels.getDayOfTheWeek().getOperation() == Operation.ALWAYS) {
                cronBuilder = cronBuilder.withDoW(always());
            } else if (fiels.getDayOfTheWeek() != null
                    && fiels.getDayOfTheWeek().getOperation() == Operation.EVERY) {
                cronBuilder = cronBuilder.withDoW(on(fiels.getDayOfTheWeek().getValue()));
            } else if (fiels.getDayOfTheWeek() != null
                    && fiels.getDayOfTheWeek().getOperation() == Operation.ON) {
                cronBuilder = cronBuilder.withDoW(on(fiels.getDayOfTheWeek().getValue()));
            } else if (fiels.getDayOfTheWeek() != null
                    && fiels.getDayOfTheWeek().getOperation() == Operation.QUESTIONMARK) {
                cronBuilder = cronBuilder.withDoW(questionMark());
            } else {
                cronBuilder = cronBuilder.withDoW(questionMark());
            }

            // Month ( Every month use case does not exist

            if (fiels.getMonth() != null && fiels.getMonth().getOperation() == Operation.ALWAYS) {
                cronBuilder = cronBuilder.withMonth(always());
            } else if (fiels.getMonth() != null && fiels.getMonth().getOperation() == Operation.EVERY) {
                value = fiels.getMonth().getValue();
                if (value == null || value == 0 || value == 1) {
                    value = 1;
                    isMonthEvery = true;
                }
                cronBuilder = cronBuilder.withMonth(every(value));

            } else if (fiels.getMonth() != null && fiels.getMonth().getOperation() == Operation.ON) {
                cronBuilder = cronBuilder.withMonth(on(fiels.getMonth().getValue()));
            } else {
                cronBuilder = cronBuilder.withMonth(always());
            }

            // Day of the month

            if (fiels.getDayOfTheMonth() != null
                    && fiels.getDayOfTheMonth().getOperation() == Operation.ALWAYS) {
                cronBuilder = cronBuilder.withDoM(always());
            } else if (fiels.getDayOfTheMonth() != null
                    && fiels.getDayOfTheMonth().getOperation() == Operation.EVERY) {
                value = fiels.getDayOfTheMonth().getValue();
                if (value == null || value == 0 || value == 1) {
                    value = 1;
                    isDayEvery = true;
                }
                cronBuilder = cronBuilder.withDoM(every(value));

            } else if (fiels.getDayOfTheMonth() != null
                    && fiels.getDayOfTheMonth().getOperation() == Operation.ON) {
                cronBuilder = cronBuilder.withDoM(on(fiels.getDayOfTheMonth().getValue()));
            } else if (fiels.getDayOfTheMonth() != null
                    && fiels.getDayOfTheMonth().getOperation() == Operation.QUESTIONMARK) {
                cronBuilder = cronBuilder.withDoM(questionMark());
            } else if (fiels.getDayOfTheWeek() != null
                    && fiels.getDayOfTheWeek().getOperation() != Operation.QUESTIONMARK) {

                cronBuilder = cronBuilder.withDoM(questionMark());

            } else {
                cronBuilder = cronBuilder.withDoM(always());
            }

            // Hour

            if (fiels.getHour() != null && fiels.getHour().getOperation() == Operation.ALWAYS) {
                cronBuilder = cronBuilder.withHour(always());
            } else if (fiels.getHour() != null && fiels.getHour().getOperation() == Operation.EVERY) {
                value = fiels.getHour().getValue();
                if (value == null || value == 0 || value == 1) {
                    value = 1;
                    isHourEvery = true;
                }
                cronBuilder = cronBuilder.withHour(every(value));

            } else if (fiels.getHour() != null && fiels.getHour().getOperation() == Operation.ON) {
                cronBuilder = cronBuilder.withHour(on(fiels.getHour().getValue()));
            } else {
                cronBuilder = cronBuilder.withHour(always());
            }

            // Minutes
            if (fiels.getMinute() != null && fiels.getMinute().getOperation() == Operation.ALWAYS) {
                cronBuilder = cronBuilder.withMinute(always());
            } else if (fiels.getMinute() != null && fiels.getMinute().getOperation() == Operation.EVERY) {
                value = fiels.getMinute().getValue();
                if (value == null || value == 0 || value == 1) {
                    value = 1;
                    isMinuteEvery = true;
                }
                cronBuilder = cronBuilder.withMinute(every(value));

            } else if (fiels.getMinute() != null && fiels.getMinute().getOperation() == Operation.ON) {
                cronBuilder = cronBuilder.withMinute(on(fiels.getMinute().getValue()));
            }

            // Seconds

            if (fiels.getSecond() != null && fiels.getSecond().getOperation() == Operation.ALWAYS) {
                cronBuilder = cronBuilder.withSecond(always());
            } else if (fiels.getSecond() != null && fiels.getSecond().getOperation() == Operation.EVERY) {
                cronBuilder = cronBuilder.withSecond(every(fiels.getSecond().getValue()));
            } else if (fiels.getSecond() != null && fiels.getSecond().getOperation() == Operation.ON) {
                cronBuilder = cronBuilder.withSecond(on(fiels.getSecond().getValue()));
            } else {
                cronBuilder = cronBuilder.withSecond(on(0));
            }

            cron = cronBuilder.instance();
            crons = cron.asString().split(" ");

            if (isYearEvery == true) {
                crons[6] = "*/1";
            }
            if (isMonthEvery == true) {
                crons[4] = "*/1";
            }
            if (isDayEvery == true) {
                crons[3] = "*/1";
            }
            if (isHourEvery == true) {
                crons[2] = "*/1";
            }
            if (isMinuteEvery == true) {
                crons[1] = "*/1";
            }
        } catch (NullPointerException exception) {
            logger.error(
                    "Parser::getCronExpression## Null pointer exception while converting payload to cron expression::");
            throw new RuleCronParsingException(
                    "Null Pounter exception while parsing rule", exception);
        } catch (ArrayIndexOutOfBoundsException exception) {
            logger.error(
                    "Parser::getCronExpression## ArrayIndexOutOfBoundsException  exception while converting payload to cron expression::");
            throw new RuleCronParsingException(
                    "ArrayIndexOutOfBoundsException exception while parsing rule",
                    exception);
        } catch (Exception exception) {
            logger.error(
                    "Parser::getCronExpression##  exception while converting payload to cron expression::");
            throw new RuleCronParsingException("Exception  while parsing rule",
                    exception);
        }

        return String.join(" ", crons);

    }

    // complex cron expression - 0 0/5 14,18,3-39,52 ? JAN,MAR,SEP MON-FRI 2002-2010

    /**
     * @param String
     * @return CronFields
     */
    public CronFields getCronDescription(String expression) throws RuleCronParsingException {
        if (expression == null || expression.length() == 0) {
            return null;
        }
        CronParser parser = new CronParser(cronDefinition);
        Cron quartzCron = parser.parse(expression);
        CronFields fields = new CronFields();
        boolean isEverySecond = false, isEveryMinute = false, isEveryHour = false, isEveryDay = false,
                isEveryMonth = false, isEveryWeek = false, isEveryYear = false;

        final Map<CronFieldName, CronField> expressions = quartzCron.retrieveFieldsAsMap();
        FieldExpression fieldExpression = null;

        String[] crons = expression.split(" ");

        for (int i = 0; i < crons.length; i++) {
            if (crons[i].matches(".\\\\*/1") || crons[i].matches("0/1")) {
                if (i == 0) {
                    isEverySecond = true;
                } else if (i == 1) {
                    isEveryMinute = true;
                } else if (i == 2) {
                    isEveryHour = true;
                } else if (i == 3) {
                    isEveryDay = true;
                } else if (i == 4) {
                    isEveryMonth = true;
                } else if (i == 5) {
                    isEveryWeek = true;
                } else if (i == 6) {
                    isEveryYear = true;
                }
            }
        }
        com.sap.iot.ain.rules.cron.Cron yearcron = new com.sap.iot.ain.rules.cron.Cron();

        try {
            if (expressions.containsKey(CronFieldName.YEAR)) {
                fieldExpression = expressions.get(CronFieldName.YEAR).getExpression();

                if (fieldExpression instanceof On) {
                    yearcron.setValue(((On) fieldExpression).getTime().getValue());
                    yearcron.setOperation(Operation.ON);
                    fields.setYear(yearcron);
                } else if (fieldExpression instanceof Every) {
                    yearcron.setValue(((Every) fieldExpression).getPeriod().getValue());
                    yearcron.setOperation(Operation.EVERY);
                    fields.setYear(yearcron);
                } else if (fieldExpression instanceof Always) {
                    yearcron.setOperation(Operation.ALWAYS);
                    if (isEveryYear) {
                        yearcron.setOperation(Operation.EVERY);
                        yearcron.setValue(1);
                    }
                    fields.setYear(yearcron);
                }
            } else {
                yearcron.setOperation(Operation.ALWAYS);
                fields.setYear(yearcron);
            }

            // Month

            if (expressions.containsKey(CronFieldName.MONTH)) {

                fieldExpression = expressions.get(CronFieldName.MONTH).getExpression();
                com.sap.iot.ain.rules.cron.Cron monthcron = new com.sap.iot.ain.rules.cron.Cron();
                if (fieldExpression instanceof On) {
                    monthcron.setValue(((On) fieldExpression).getTime().getValue());
                    monthcron.setOperation(Operation.ON);
                    fields.setMonth(monthcron);
                    // If Month has some value and Year says always then Month is relatively every
                    // month
                    if (fields.getYear().getOperation() == Operation.ALWAYS) {
                        fields.getYear().setOperation(Operation.EVERY);
                        fields.getYear().setValue(1);
                        isEveryYear = true;
                    }
                } else if (fieldExpression instanceof Every) {
                    monthcron.setValue(((Every) fieldExpression).getPeriod().getValue());
                    monthcron.setOperation(Operation.EVERY);
                    fields.setMonth(monthcron);
                    isEveryYear = true;
                } else if (fieldExpression instanceof Always) {
                    monthcron.setOperation(Operation.ALWAYS);
                    if (isEveryMonth) {
                        monthcron.setOperation(Operation.EVERY);
                        monthcron.setValue(1);
                    }
                    fields.setMonth(monthcron);
                }
            }

            // Day of the week
            if (expressions.containsKey(CronFieldName.DAY_OF_WEEK)) {

                fieldExpression = expressions.get(CronFieldName.DAY_OF_WEEK).getExpression();
                com.sap.iot.ain.rules.cron.Cron dowcron = new com.sap.iot.ain.rules.cron.Cron();
                if (fieldExpression instanceof On) {
                    dowcron.setValue(((On) fieldExpression).getTime().getValue());
                    dowcron.setOperation(Operation.ON); // Every SAT-MON OF A WEEK

                    if (fields.getMonth().getOperation() == Operation.ALWAYS
                            && fields.getYear().getOperation() == Operation.ALWAYS) {
                        dowcron.setOperation(Operation.EVERY);
                        isEveryWeek = true;
                    }

                    fields.setDayOfTheWeek(dowcron);
                } else if (fieldExpression instanceof Every) {
                    dowcron.setValue(((Every) fieldExpression).getPeriod().getValue());
                    dowcron.setOperation(Operation.EVERY);
                    fields.setDayOfTheWeek(dowcron);
                    isEveryWeek = true;
                } else if (fieldExpression instanceof Always) {
                    dowcron.setOperation(Operation.ALWAYS);
                    fields.setDayOfTheWeek(dowcron);
                } else if (fieldExpression instanceof QuestionMark) {
                    dowcron.setOperation(Operation.QUESTIONMARK);
                    fields.setDayOfTheWeek(dowcron);
                }
            }

            // DAY_OF_MONTH

            if (expressions.containsKey(CronFieldName.DAY_OF_MONTH)) {

                fieldExpression = expressions.get(CronFieldName.DAY_OF_MONTH).getExpression();
                com.sap.iot.ain.rules.cron.Cron domcron = new com.sap.iot.ain.rules.cron.Cron();
                if (fieldExpression instanceof On) {
                    domcron.setValue(((On) fieldExpression).getTime().getValue());
                    domcron.setOperation(Operation.ON);
                    // If YEAR = ALWAYS , MONTH = ALWAYS AND DAY has some value
                    // that means on that day every month - by relativity
                    if (fields.getMonth().getOperation() == Operation.ALWAYS) {
                        fields.getMonth().setOperation(Operation.EVERY);
                        fields.getMonth().setValue(1);
                        isEveryMonth = true;
                    }
                    fields.setDayOfTheMonth(domcron);
                } else if (fieldExpression instanceof Every) {
                    domcron.setValue(((Every) fieldExpression).getPeriod().getValue());
                    domcron.setOperation(Operation.EVERY);
                    fields.setDayOfTheMonth(domcron);
                    isEveryDay = true;
                } else if (fieldExpression instanceof Always) {
                    domcron.setOperation(Operation.ALWAYS);
                    if (isEveryDay) {
                        domcron.setOperation(Operation.EVERY);
                        domcron.setValue(1);
                    }
                    fields.setDayOfTheMonth(domcron);
                } else if (fieldExpression instanceof QuestionMark) {
                    domcron.setOperation(Operation.QUESTIONMARK);
                    fields.setDayOfTheMonth(domcron);
                }
            }

            // Hour

            if (expressions.containsKey(CronFieldName.HOUR)) {

                fieldExpression = expressions.get(CronFieldName.HOUR).getExpression();
                com.sap.iot.ain.rules.cron.Cron hcron = new com.sap.iot.ain.rules.cron.Cron();
                if (fieldExpression instanceof On) {
                    hcron.setValue(((On) fieldExpression).getTime().getValue());
                    hcron.setOperation(Operation.ON);
                    // YEAR , MONTH , DAY ALWAYS AND DAY HAS SOME VALUE THET MEANS EVERY DAY ON PERTICULER
                    // TIME
                    if (fields.getDayOfTheMonth().getOperation() == Operation.ALWAYS
                            || fields.getDayOfTheMonth().getOperation() == Operation.QUESTIONMARK) {
                        fields.getDayOfTheMonth().setOperation(Operation.EVERY);
                        fields.getDayOfTheMonth().setValue(1);
                        isEveryDay = true;
                    }
                    fields.setHour(hcron);
                } else if (fieldExpression instanceof Every) {
                    hcron.setValue(((Every) fieldExpression).getPeriod().getValue());
                    hcron.setOperation(Operation.EVERY);
                    fields.setHour(hcron);
                    isEveryHour = true;
                } else if (fieldExpression instanceof Always) {
                    hcron.setOperation(Operation.ALWAYS);
                    hcron.setOperation(Operation.ALWAYS);
                    if (isEveryHour) {
                        hcron.setOperation(Operation.EVERY);
                        hcron.setValue(1);
                    }
                    fields.setHour(hcron);
                }
            }

            // Minute
            if (expressions.containsKey(CronFieldName.MINUTE)) {

                fieldExpression = expressions.get(CronFieldName.MINUTE).getExpression();
                com.sap.iot.ain.rules.cron.Cron mcron = new com.sap.iot.ain.rules.cron.Cron();
                if (fieldExpression instanceof On) {
                    mcron.setValue(((On) fieldExpression).getTime().getValue());
                    mcron.setOperation(Operation.ON);
                    // YEAR , MONTH , DAY HOUR == ALWAYS AND MINUE HAS VALUE THEN EVERY HOUR

                    if (fields.getHour().getOperation() == Operation.ALWAYS) {

                        fields.getHour().setOperation(Operation.EVERY);
                        fields.getHour().setValue(1);
                        isEveryHour = true;
                    }
                    fields.setMinute(mcron);
                } else if (fieldExpression instanceof Every) {
                    mcron.setValue(((Every) fieldExpression).getPeriod().getValue());
                    mcron.setOperation(Operation.EVERY);
                    fields.setMinute(mcron);
                    isEveryMinute = true;
                } else if (fieldExpression instanceof Always) {
                    mcron.setOperation(Operation.ALWAYS);
                    if (isEveryMinute) {
                        mcron.setOperation(Operation.EVERY);
                        mcron.setValue(1);
                    }
                    fields.setMinute(mcron);
                }
            }

            // Seconds
            if (expressions.containsKey(CronFieldName.SECOND)) {

                fieldExpression = expressions.get(CronFieldName.SECOND).getExpression();
                com.sap.iot.ain.rules.cron.Cron scron = new com.sap.iot.ain.rules.cron.Cron();
                if (fieldExpression instanceof On) {
                    scron.setValue(((On) fieldExpression).getTime().getValue());
                    scron.setOperation(Operation.ON);
                    if (fields.getMinute().getOperation() == Operation.ALWAYS) {

                        fields.getMinute().setOperation(Operation.EVERY);
                        fields.getMinute().setValue(1);
                        isEveryMinute = true;
                    }
                    fields.setSecond(scron);
                } else if (fieldExpression instanceof Every) {
                    scron.setValue(((Every) fieldExpression).getPeriod().getValue());
                    scron.setOperation(Operation.EVERY);
                    fields.setSecond(scron);
                    isEverySecond = true;
                } else if (fieldExpression instanceof Always) {
                    scron.setOperation(Operation.ALWAYS);
                    if (isEverySecond) {
                        scron.setOperation(Operation.EVERY);
                        scron.setValue(1);
                    }
                    fields.setSecond(scron);
                }
            }
        } catch (NullPointerException exception) {
            logger.error(
                    "Parser::getCronExpression## Null pointer exception while converting payload to cron expression::");
            throw new RuleCronParsingException(
                    "Null Pounter exception while parsing rule", exception);
        } catch (ArrayIndexOutOfBoundsException exception) {
            logger.error(
                    "Parser::getCronExpression## ArrayIndexOutOfBoundsException  exception while converting payload to cron expression::");
            throw new RuleCronParsingException(
                    "ArrayIndexOutOfBoundsException exception while parsing rule",
                    exception);
        } catch (Exception exception) {
            logger.error(
                    "Parser::getCronExpression##  exception while converting payload to cron expression::");
            throw new RuleCronParsingException("Exception  while parsing rule",
                    exception);
        }


        if (isEveryYear) {
            fields.setDayOfTheWeek(null);
        } else if (isEveryWeek) {
            fields.setYear(null);
            fields.setMonth(null);
            fields.setDayOfTheMonth(null);
        } else if (isEveryMonth) {
            fields.setYear(null);
            fields.setDayOfTheWeek(null);
        } else if (isEveryDay) {
            fields.setMonth(null);
            fields.setYear(null);
            fields.setDayOfTheWeek(null);
        } else if (isEveryHour) {
            fields.setYear(null);
            fields.setMonth(null);
            fields.setDayOfTheMonth(null);
            fields.setDayOfTheWeek(null);
        } else if (isEveryMinute) {
            fields.setYear(null);
            fields.setMonth(null);
            fields.setDayOfTheMonth(null);
            fields.setDayOfTheWeek(null);
            fields.setHour(null);
        } else if (isEverySecond) {
            fields.setYear(null);
            fields.setMonth(null);
            fields.setDayOfTheMonth(null);
            fields.setDayOfTheWeek(null);
            fields.setHour(null);
            fields.setMinute(null);
        }

        return fields;
    }


    public String getSchedulerDescription(String expression) {
        if (expression == null) {
            return null;
        }
        CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);
        CronParser parser = new CronParser(cronDefinition);
        String description = null;
        try {
            Cron quartzCron = parser.parse(expression);
            description = descriptor.describe(quartzCron);
        } catch (Exception exception) {
            logger.error(
                    "Parser::getSchedulerDescription##  Error while getting cron description::");
        }
        return description;
    }

}
