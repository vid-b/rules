package com.sap.iot.ain.rules.models;

import java.util.List;

/**
 * Created by i808440 on 9/19/16.
 */
public class RuleRunListModel {
    private List<RuleRunDetailModel> ruleRuns;
    public RuleRunListModel(List<RuleRunDetailModel> runs) {
        this.ruleRuns = runs;
    }
    public List<RuleRunDetailModel> getRuleRuns() {
        return this.ruleRuns;
    }
}
