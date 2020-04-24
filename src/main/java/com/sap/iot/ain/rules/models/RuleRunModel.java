package com.sap.iot.ain.rules.models;

import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RuleRunModel {

    private final String runId;
    private final String dateStr;
    private final Timestamp timestamp;
    private final boolean hasResult;

    public RuleRunModel(String runId, Timestamp timestamp, boolean hasResult) {
        this.runId = runId;
        Date date = new Date(timestamp.getTime());
        Format format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        this.dateStr = format.format(date);
        this.timestamp = timestamp;
        this.hasResult = hasResult;
    }

    public String getRunId() {
        return this.runId;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public String getDateStr() {
        return this.dateStr;
    }

    public boolean getHasResult() {
        return this.hasResult;
    }
}
