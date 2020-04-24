package com.sap.iot.ain.rules.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class RulesSubject implements RowMapper<RulesSubject> {

    @SerializedName("guid")
    @Expose
    private String guid;
    @SerializedName("deviceCategoryId")
    @Expose
    private String deviceCategoryId;
    @SerializedName("deviceTypeId")
    @Expose
    private String deviceTypeId;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("imageId")
    @Expose
    private Object imageId;
    @SerializedName("statusCode")
    @Expose
    private Object statusCode;
    @SerializedName("extDeviceTypeId")
    @Expose
    private String extDeviceTypeId;
    @SerializedName("deviceTypeExtId")
    @Expose
    private Object deviceTypeExtId;
    @SerializedName("alternateId")
    @Expose
    private Object alternateId;
    @SerializedName("createdBy")
    @Expose
    private String createdBy;
    @SerializedName("createdTime")
    @Expose
    private Integer createdTime;
    @SerializedName("lastUpdatedBy")
    @Expose
    private Object lastUpdatedBy;
    @SerializedName("lastUpdatedTime")
    @Expose
    private Object lastUpdatedTime;
    @SerializedName("deviceTypeProperties")
    @Expose
    private Object deviceTypeProperties;
    @SerializedName("deviceMessageTypes")
    @Expose
    private Object deviceMessageTypes;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getDeviceCategoryId() {
        return deviceCategoryId;
    }

    public void setDeviceCategoryId(String deviceCategoryId) {
        this.deviceCategoryId = deviceCategoryId;
    }

    public String getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(String deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getImageId() {
        return imageId;
    }

    public void setImageId(Object imageId) {
        this.imageId = imageId;
    }

    public Object getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Object statusCode) {
        this.statusCode = statusCode;
    }

    public String getExtDeviceTypeId() {
        return extDeviceTypeId;
    }

    public void setExtDeviceTypeId(String extDeviceTypeId) {
        this.extDeviceTypeId = extDeviceTypeId;
    }

    public Object getDeviceTypeExtId() {
        return deviceTypeExtId;
    }

    public void setDeviceTypeExtId(Object deviceTypeExtId) {
        this.deviceTypeExtId = deviceTypeExtId;
    }

    public Object getAlternateId() {
        return alternateId;
    }

    public void setAlternateId(Object alternateId) {
        this.alternateId = alternateId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Integer createdTime) {
        this.createdTime = createdTime;
    }

    public Object getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Object lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Object getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Object lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public Object getDeviceTypeProperties() {
        return deviceTypeProperties;
    }

    public void setDeviceTypeProperties(Object deviceTypeProperties) {
        this.deviceTypeProperties = deviceTypeProperties;
    }

    public Object getDeviceMessageTypes() {
        return deviceMessageTypes;
    }

    public void setDeviceMessageTypes(Object deviceMessageTypes) {
        this.deviceMessageTypes = deviceMessageTypes;
    }

    @Override
    public RulesSubject mapRow(ResultSet rs, int i) throws SQLException {
        RulesSubject rulesDeviceType = null;
        try {
            rulesDeviceType = this.getClass().newInstance();
            rulesDeviceType.setDeviceCategoryId(rs.getString("ModelId"));
            rulesDeviceType.setDescription(rs.getString("ShortDescription"));
            rulesDeviceType.setDeviceTypeId(rs.getString("ModelId"));
            rulesDeviceType.setExtDeviceTypeId(rs.getString("ModelExternalId"));
            rulesDeviceType.setDeviceTypeExtId(rs.getString("ModelExternalId"));
            return rulesDeviceType;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
