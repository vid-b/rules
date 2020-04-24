package com.sap.iot.ain.rules.cron;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Rules API cron parsing and encoding test case")
public class CronParserTest {

    static File PAYLOAD_YEAR = null;
    static File PAYLOAD_MONTH = null;
    static File PAYLOAD_WEEK = null;
    static File PAYLOAD_DAY = null;
    static File PAYLOAD_HOUR = null;
    static File PAYLOAD_MINUTE = null;
    static File PAYLOAD_NESTED_EVERY = null;
    CronExpression expression = new Parser();
    ObjectMapper mapper = new ObjectMapper();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeAll
    public static void init() {
        PAYLOAD_YEAR = new File("src/test/java/com/sap/iot/ain/rules/cron/Payload_Every_Year.json");
        PAYLOAD_MONTH = new File("src/test/java/com/sap/iot/ain/rules/cron/Payload_Every_Month.json");
        PAYLOAD_WEEK = new File("src/test/java/com/sap/iot/ain/rules/cron/Payload_Every_Week.json");
        PAYLOAD_DAY = new File("src/test/java/com/sap/iot/ain/rules/cron/Payload_Every_Day.json");
        PAYLOAD_HOUR = new File("src/test/java/com/sap/iot/ain/rules/cron/Payload_Every_Hour.json");
        PAYLOAD_MINUTE = new File("src/test/java/com/sap/iot/ain/rules/cron/Payload_Every_Minute.json");
        PAYLOAD_NESTED_EVERY =  new File("src/test/java/com/sap/iot/ain/rules/cron/Payload_Nested_Every.json");
    }

    @Nested
    @DisplayName("Test case on cron json to expression conversion")
    class TestCronParserEncoding {
        @Test
        @DisplayName("Cron expresion for every year on Feburary at 2 AM 10 minutes 0 seconds")
        void testCronParser_year() throws IOException, RuleCronParsingException {
            CronFields cronfields = mapper.readValue(PAYLOAD_YEAR, CronFields.class);
            String cronExpression = expression.getCronExpression(cronfields);
            assertEquals("0 10 2 * 2 ? */1", cronExpression);

        }

        @Test
        @DisplayName("Cron expression for every month ON 4TH at 2 AM 10 minutes 0 seconds")
        void testCronParser_month() throws IOException, RuleCronParsingException {
            CronFields cronfields = mapper.readValue(PAYLOAD_MONTH, CronFields.class);
            String cronExpression = expression.getCronExpression(cronfields);
            assertEquals("0 10 2 4 */1 ? *", cronExpression);

        }

        @Test
        @DisplayName("Cron expression for every sunday at 2 AM 10 minutes 0 seconds")
        void testCronParser_week() throws IOException, RuleCronParsingException {
            CronFields cronfields = mapper.readValue(PAYLOAD_WEEK, CronFields.class);
            String cronExpression = expression.getCronExpression(cronfields);
            assertEquals("0 10 2 ? * 3 *", cronExpression);

        }

        @Test
        @DisplayName("Cron expression for every day at 5 AM 15 minutes 0 seconds")
        void testCronParser_day() throws IOException, RuleCronParsingException {
            CronFields cronfields = mapper.readValue(PAYLOAD_DAY, CronFields.class);
            String cronExpression = expression.getCronExpression(cronfields);
            assertEquals("0 15 5 */1 * ? *", cronExpression);

        }

        @Test
        @DisplayName("Cron expression for every hour on 15 minutes 0 seconds")
        void testCronParser_hour() throws IOException, RuleCronParsingException {
            CronFields cronfields = mapper.readValue(PAYLOAD_HOUR, CronFields.class);
            String cronExpression = expression.getCronExpression(cronfields);
            assertEquals("0 15 */1 * * ? *", cronExpression);

        }

        @Test
        @DisplayName("Cron expression for every 16 minute 0 seconds")
        void testCronParser_minute() throws IOException, RuleCronParsingException {
            CronFields cronfields = mapper.readValue(PAYLOAD_MINUTE, CronFields.class);
            String cronExpression = expression.getCronExpression(cronfields);
            assertEquals("0 */16 * * * ? *", cronExpression);

        }

        @Test
        @Disabled
        @DisplayName("Cron parser should throw exception if hour is passed more than 23")
        void test_hour_invalid_time_atgument() throws IOException {
            boolean hasException = false;
            try {
                mapper.readValue(CronStub.CRON_HOUR_INVALID_ARGUMENT, CronFields.class);
            } catch (JsonMappingException e) {
                hasException = true;
            }
            assertEquals(hasException, true);
        }

        @Test
        @DisplayName("Cron expression should schedule for every  hour every hour even 0 is passed as value")
        void testCronParser_every_hour_zero() throws IOException, RuleCronParsingException {
            CronFields cronfields = mapper.readValue(CronStub.CRON_HOUR_ZERO_ARGUMENT, CronFields.class);
            String cronExpression = expression.getCronExpression(cronfields);
            assertEquals("0 15 */1 * * ? *", cronExpression);

        }
        @Test
        @DisplayName("Cron expression should schedule for every year 8th month day 23 at 15 th hour every 12th minute")
        void testCronParser_every_year_on_8thmonth_23date_15th_hour_every12_minute() throws IOException, RuleCronParsingException {
            CronFields cronfields = mapper.readValue(PAYLOAD_NESTED_EVERY, CronFields.class);
            String cronExpression = expression.getCronExpression(cronfields);
            assertEquals("0 */12 15 23 8 ? */1", cronExpression);

        }

    }

    @Nested
    @DisplayName("Test cron expression decoding to JSON object")
    class TestCronParserDecoding {

        @Test
        @DisplayName("Cron decoder for every hour at 10 minute")
        public void test_every_hour_cron_decoder() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 10 */1 * * ? *");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_hour = mapper.writeValueAsString(cronFields);
            // {"second":{"value":0,"operation":"ON"},"minute":{"value":10,"operation":"ON"},"hour":{"value":1,"operation":"EVERY"}}
            assertEquals(every_hour, CronStub.CRON_RESPONSE_EVERY_HOUR);

        }

        @Test
        @DisplayName("Cron decoder at every day at 3:10")
        public void test_every_day_cron_decoder() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 10 3 */1 * ? *");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_day = mapper.writeValueAsString(cronFields);
            // {"second":{"value":0,"operation":"ON"},"minute":{"value":10,"operation":"ON"},"hour":{"value":3,"operation":"ON"},"day":{"value":0,"operation":"EVERY"}}
            assertEquals(every_day, CronStub.CRON_RESPONSE_EVERY_DAY);

        }

        @Test
        @DisplayName("Cron decoder at every minute ")
        public void test_every_minute_cron_decoder() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 */1 * * * ? *");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_minute = mapper.writeValueAsString(cronFields);
            // {"second":{"value":0,"operation":"ON"},"minute":{"value":1,"operation":"EVERY"}}
            assertEquals(every_minute, CronStub.CRON_RESPONSE_EVERY_MINUTE);

        }

        @Test
        @DisplayName("Cron decoder at every week on Monday at 3:2:00 ")
        public void test_every_week_cron_decoder() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 2 3 ? * 1 *");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_week = mapper.writeValueAsString(cronFields);
            // {"second":{"value":0,"operation":"ON"},"minute":{"value":2,"operation":"ON"},"hour":{"value":3,"operation":"ON"},"week":{"value":1,"operation":"EVERY"}}
            assertEquals(every_week, CronStub.CRON_RESPONSE_EVERY_WEEK);

        }

        @Test
        @DisplayName("Cron decoder for every year June 4 at 03:02:00 ")
        public void test_every_year_cron_decoder() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 2 3 4 6 ? */1");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_year = mapper.writeValueAsString(cronFields);
            // {"second":{"value":0,"operation":"ON"},"minute":{"value":2,"operation":"ON"},"hour":{"value":3,"operation":"ON"},"month":{"value":6,"operation":"ON"},"year":{"value":0,"operation":"EVERY"},"day":{"value":4,"operation":"ON"}}
            assertEquals(every_year, CronStub.CRON_RESPONSE_EVERY_YEAR);

        }

        @Test
        @DisplayName("Cron decoder for old way of saying every 4 minute")
        public void test_old_cron_every_minute() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 0/4 * * * ?");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_4_minute = mapper.writeValueAsString(cronFields);
            //{"second":{"value":0,"operation":"ON"},"minute":{"value":4,"operation":"EVERY"}}
            assertEquals(every_4_minute, CronStub.CRON_OLD_RESPONSE_EVERY_4_MINUTE);
        }
        
        @Test
        @DisplayName("Cron decoder for old way of saying every hour at 58th minute *")
        public void test_old_cron_every_hour_at_58_minute() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 58 * * * ?");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_hour_58_minute = mapper.writeValueAsString(cronFields);
            //{"second":{"value":0,"operation":"ON"},"minute":{"value":58,"operation":"ON"},"hour":{"value":1,"operation":"EVERY"}}
            assertEquals(every_hour_58_minute, CronStub.CRON_OLD_RESPONSE_EVERY_HOUR_58_MINUTE);
        }
        @Test
        @DisplayName("Cron decoder for old way of saying every hour at 58th minute 0/1")
        public void test_old_cron_every_hour_at_58_minute_0_format() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 58 0/1 * * ?");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_hour_58_minute = mapper.writeValueAsString(cronFields);
            //{"second":{"value":0,"operation":"ON"},"minute":{"value":58,"operation":"ON"},"hour":{"value":1,"operation":"EVERY"}}
            assertEquals(every_hour_58_minute, CronStub.CRON_OLD_RESPONSE_EVERY_HOUR_58_MINUTE);
        }
        @Test
        @DisplayName("Cron decoder for old way of saying every hour at 58th minute */1")
        public void test_old_cron_every_hour_at_58_minute_1_format() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 58 */1 * * ?");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_hour_58_minute = mapper.writeValueAsString(cronFields);
            //{"second":{"value":0,"operation":"ON"},"minute":{"value":58,"operation":"ON"},"hour":{"value":1,"operation":"EVERY"}}
            assertEquals(every_hour_58_minute, CronStub.CRON_OLD_RESPONSE_EVERY_HOUR_58_MINUTE);
        }
        
        @Test
        @DisplayName("Cron decoder for old way of saying every day at 20 13")
        public void test_old_cron_every_day_at_20_13_0() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 13 20 * * ?");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_day_20_13 = mapper.writeValueAsString(cronFields);
            //{"second":{"value":0,"operation":"ON"},"minute":{"value":13,"operation":"ON"},"hour":{"value":20,"operation":"ON"},"day":{"value":1,"operation":"EVERY"}}
            assertEquals(every_day_20_13, CronStub.CRON_OLD_RESPONSE_EVERY_DAY_20_13);
        }
        
        @Test
        @DisplayName("Cron decoder for old way of saying every month on 23rd at 15:14")
        public void test_old_cron_every_month_at_23rd_on15_14() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 14 15 23 * ?");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_month_23_15_14 = mapper.writeValueAsString(cronFields);
            //{"second":{"value":0,"operation":"ON"},"minute":{"value":14,"operation":"ON"},"hour":{"value":15,"operation":"ON"},"month":{"value":1,"operation":"EVERY"},"day":{"value":23,"operation":"ON"}}
            assertEquals(every_month_23_15_14, CronStub.CRON_OLD_EVERY_MONTH_23RD_HOUR15_MINUTE14);
        }
        @Test
        @DisplayName("Cron decoder for old way of saying every year on 8th month on 21st at 15:12")
        public void test_old_cron_every_year_on_8thmonth_on_21st_at_time_15_12() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 12 15 23 8 ? *");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_year_8_15_12 = mapper.writeValueAsString(cronFields);
            //{"second":{"value":0,"operation":"ON"},"minute":{"value":12,"operation":"ON"},"hour":{"value":15,"operation":"ON"},"month":{"value":8,"operation":"ON"},"year":{"value":1,"operation":"EVERY"},"day":{"value":23,"operation":"ON"}}
            assertEquals(every_year_8_15_12, CronStub.CRON_OLD_EVRY_YEAR_8TH_MONTH);
        }
        @Test
        @DisplayName("Cron decoder for old way of saying every year on 8th month on 21st at 15:12 at */1 support")
        public void test_old_cron_every_year_on_8thmonth_on_21st_at_time_15_12_0_by_1() throws JsonProcessingException, RuleCronParsingException {
            CronFields cronFields = expression.getCronDescription("0 12 15 23 8 ? */1");
            mapper.setSerializationInclusion(Include.NON_NULL);
            String every_year_8_15_12_0_1 = mapper.writeValueAsString(cronFields);
            //{"second":{"value":0,"operation":"ON"},"minute":{"value":12,"operation":"ON"},"hour":{"value":15,"operation":"ON"},"month":{"value":8,"operation":"ON"},"year":{"value":1,"operation":"EVERY"},"day":{"value":23,"operation":"ON"}}
            assertEquals(every_year_8_15_12_0_1, CronStub.CRON_OLD_EVRY_YEAR_8TH_MONTH);
        }
        
        @Test
        @DisplayName("Cron decoder test for exception")
        public void test_parser_should_throw_exception() {
           
        }
    }
}
