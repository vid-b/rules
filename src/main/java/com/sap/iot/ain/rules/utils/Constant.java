package com.sap.iot.ain.rules.utils;

import java.util.ResourceBundle;

public class Constant {

    public static enum DATATYPE {
        INTEGER("INTEGER"), FLOAT("FLOAT"), STRING("STRING"), BOOLEAN("BOOLEAN"), DOUBLE("DOUBLE");
        private String value;

        private DATATYPE(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static enum DATETYPE {
        NOW("NOW"), DAY("DAY"), WEEK("WEEK"), MONTH("MONTH");
        private String value;

        private DATETYPE(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static enum OPERATOR {
        EQ("="), GE(">="), LE("<="), GT(">"), LT("<"), LIKE("LIKE");
        private String value;

        private OPERATOR(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }

    }

    public static enum EXECUTIONSTATUS {
        SUCCESS("SUCCESS"), PARTIALLY_SUCCESS("PARTIALLY_SUCCESS"), FAILURE("FAILURE");
        private String value;

        private EXECUTIONSTATUS(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static final String CNG_RULES_NOTIFICATION = "com.sap.iot.scb.cng.rules";
    public static final String CNG_NS_ORIGIN_URL = "https://connected-goods-ns-java-dev3.cfapps.sap.hana.ondemand.com/Origin.svc";
    public static final String CNG_NS_PROVIDER_URL = "https://connected-goods-ns-java-dev3.cfapps.sap.hana.ondemand.com/Provider.svc";
    public static final String CNG_NS_ORIGIN = "CNG_ORIGIN";
    public static final String CNG_NS_PROVIDER = "CNG_PROVIDER";
    public static final String CNG_PROVIDER_SCOPE = "NotificationProviderCG";
    public static final String CNG_YAAS_EMAIL_CLIENTID = "EdX81uEThEhNHJsvtjmCcuCLzGtcDhrN";
    public static final String CNG_YAAS_EMAIL_CLIENTSECRET = "RwqnIKSsD3JAu0dS";
    public static final String CNG_YAAS_EMAIL_SERVICEURI = "https://api.yaas.io/hybris/email/v1/ruleengine/send-async";
    public static final String CNG_YAAS_TEMPLATE_OWNER = "ruleengine.cngmail";

    public static final String HDI_PERSISTENCE_UNIT_NAME = "connected-goods-hdi";
    public static final String HDI_SECURE_PERSISTENCE_UNIT_NAME = "connected-goods-hdi-secure";
    public static final String CONFIG_FILE = "application";
    public static final ResourceBundle CONFIG = ResourceBundle.getBundle(CONFIG_FILE);
    public static final String JDBC_DRIVER_CLASS = "";
    public static final Integer REST_RESPONSE_SUCCESS = 10;
    public static final Integer REST_RESPONSE_FAILURE = 20;

    public static final String LONGITUDE_SEMANTIC_TYPE = "SE_3";
    public static final String LATITUDE_SEMANTIC_TYPE = "SE_2";
    public static final String EVENT_TYPE_GENERAL = "01";
    public static final String EVENT_TYPE_GEOMATCHING = "02";
    public static final String EVENT_TYPE_GEOFENCING = "03";

    public static final String ACTION_TYPE_EMAIL = "EMAIL";
    public static final String ACTION_TYPE_REST = "REST";
    public static final String ACTION_TYPE_EMAIL2STOREEMPL = "EMAIL_STOREEMPL";
    public static final String ACTION_TYPE_GEOMATCHING = "GEO_MATCH";
    public static final String ACTION_TYPE_GEOFENCING = "GEO_FENCE";
    public static final String ACTION_TYPE_EXTERNALSERVICE = "EXTERNAL_SERVICE";
    public static final String ACTION_TYPE_GEO_REVERSECODING = "GEO_REVERSECODE";
    public static final String ACTION_TYPE_GEO_UNASSIGMENT = "GEO_UNASSIGN";
    public static final String ACTION_TYPE_MOVE_ALERT = "MOVE_ALERT";

    public static final String EMP_RESPONSIBLE_NAME = "Employee Resposible for the Device";
    public static final String EMP_RESPONSIBLE_ID = "EMP_RESPONSIBLE";
    public static final String EMP_RESPONSIBLE_EMAIL = "EMP_RES_EMAIL";

    public static final String GEO_MAPPING_NAME = "BP_STATUS";
    public static final String GEO_MAPPED_STATUS = "1";
    public static final String GEO_NEARBY_STATUS = "2";
    public static final String GEO_UNMAPPED_STATUS = "3";

    public static final String DEVICE_TABLENAME = "DEVICE";
    public static final String DEVICE_DATA_TABLENAME = "DEVICE_DATA";
    public static final String DEVICE_VIEW_TABLENAME = "CONS_DEVICE_VIEW";

    public static final String ACTION_REST_NAME = "REST";
    public static final String RULE_ACTION_SUCCESS_STATUS = "1";
    public static final String RULE_ACTION_FAILURE_STATUS = "2";
    public static final String RULE_ACTION_PENDING_STATUS = "3";
    public static final String RULE_ACTION_DISMISS_STATUS = "4";

    public static final String CNG_RULE_QUEUE = "cng_rules_queue";
    public static final String CNG_ML_QUEUE = "cng_ml_queue";

    public static final String STRING = "string";
    public static final String NUMBER = "number";
    public static final String NUMERIC = "numeric";
    public static final String NUMERICFLEXIBLE = "numericflexible";
    public static final String BOOLEAN = "boolean";
    public static final String DATE = "date";
    public static final String EQUAL = "=";
    public static final String NOTEQUAL = "!=";
    public static final String GREATER = ">";
    public static final String GREATEROREQUAL = ">=";
    public static final String LESSER = "<";
    public static final String LESSEROREQUAL = "<=";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String IN_RANGE = "[]";
    public static final String NOT_IN_RANGE = "![]";
    
    public static final String EXCHANGE_NAME = "RabbitMQDirectExchange";
    public static final String QUEUE_NAME = "ConsumptionQueue";
    public static final String ROUTING_KEY = "SecretKeyToReadAlert";
    
    public final static String VCAP_SERVICES = "VCAP_SERVICES";
    public final static String RABBITMQ = "rabbitmq";
    public final static String CREDENTIALS = "credentials";
    public final static String RABBITMQ_NAME = "ain-rabbitmq";
}
