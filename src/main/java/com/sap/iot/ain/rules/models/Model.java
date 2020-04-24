package com.sap.iot.ain.rules.models;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

public class Model implements Serializable, RowMapper<Model> {
    private static final long serialVersionUID = 1L;

    private String modelId;

    private String modelExternalId;
    
    private String description;
    
    private static final Logger logger = LoggerFactory.getLogger(Model.class);
    
    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelExternalId() {
        return modelExternalId;
    }

    public void setModelExternalId(String modelExternalId) {
        this.modelExternalId = modelExternalId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.modelId);
        hash = 59 * hash + Objects.hashCode(this.modelExternalId);
        hash = 59 * hash + Objects.hashCode(this.description);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Model other = (Model) obj;
        if (!Objects.equals(this.modelId, other.modelId)) {
            return false;
        }
        if (!Objects.equals(this.modelExternalId, other.modelExternalId)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Model{" + "modelId=" + modelId + ", modelExternalId=" + modelExternalId + ", description=" + description + '}';
    }

    @Override
    public Model mapRow(ResultSet rs, int rowNum) {
        Model model = null;
        try{
            model = this.getClass().newInstance();
            model.setModelId(rs.getString("ModelId"));
            model.setModelExternalId(rs.getString("ModelExternalId"));
            model.setDescription("ShortDescription");
        }catch(InstantiationException | IllegalAccessException | SQLException e){
            logger.error("Rules.Model mapping error {} " + e.getMessage());
        }
        return  model;
    }

}