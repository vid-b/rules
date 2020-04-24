package com.sap.iot.ain.rules.models;

import java.util.List;

public class THEN {

    public List<RuleActionIn> getActions() {
        return actions;
    }

    public void setActions(List<RuleActionIn> actions) {
        this.actions = actions;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getFrequencyNumber() {
        return frequencyNumber;
    }

    public void setFrequencyNumber(String frequencyNumber) {
        this.frequencyNumber = frequencyNumber;
    }

    List<RuleActionIn> actions;
    String frequency;
    String frequencyNumber;

}
