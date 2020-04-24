package com.sap.iot.ain.rules.models;

import java.util.List;

/**
 * Created by i808440 on 9/18/16.
 */
public class RuleRunDetailModel {
    private RuleRunModel ruleRun;
    private List<Field> fields;

    public RuleRunDetailModel(RuleRunModel ruleRun, List<Field> fields) {
        this.ruleRun = ruleRun;
        this.fields = fields;
    }

    public RuleRunModel getRuleRun() {
        return this.ruleRun;
    }

    public List<Field> getFields() {
        return this.fields;
    }

    public static class Field {
        private final String name;
        private final String value;

        public Field(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return this.name;
        }
        public String getValue() {
            return this.value;
        }
    }
}
