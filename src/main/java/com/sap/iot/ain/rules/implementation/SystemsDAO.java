package com.sap.iot.ain.rules.implementation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.sap.iot.ain.externalidmapping.payload.SystemInfo;

@Component
public class SystemsDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<SystemInfo> getSystemInfo(String ClientId) {

        String querySystems
                = "select \"ID\", \"SystemID\", \"SystemType\",\"SystemName\",\"isOwnSystem\", \"SystemStatus\",\"SystemClient\", \"URL1\", \"URL2\", \r\n"
                + "\"HCIArtifactName\", \"isPrimary\",\"isActive\",\"AdditionalInfo\",\"AdditionalField1\",\"AdditionalField2\",\"AdditionalField3\",\r\n"
                + "case when C.\"LanguageISOCode\" is null then B.\"Description\"  else C.\"Description\" end as \"SystemStatusDescription\",\r\n"
                + "case when E.\"LanguageISOCode\" is null then D.\"Description\"  else E.\"Description\" end as \"SystemTypeDescription\"\r\n"
                + "from \"sap.ain.metaData::Configurations.ExternalSystemConfig\" as A\r\n"
                + "left outer join \"sap.ain.metaData::Enumerations.Text\"  as B\r\n"
                + "on  B.\"Type\" = 'ExternalSystemStatus' and A.\"SystemStatus\" =  B.\"Code\" and B.\"LanguageISOCode\" = 'en'\r\n"
                + "left outer join  \"sap.ain.metaData::Enumerations.Text\" as C\r\n"
                + "on C.\"Type\" = 'ExternalSystemStatus' and A.\"SystemStatus\" =  C.\"Code\" and C.\"LanguageISOCode\" = ?\r\n"
                + "left outer join \"sap.ain.metaData::Enumerations.Text\"  as D\r\n"
                + "on  D.\"Type\" = 'ExternalSystemType' and A.\"SystemType\" =  D.\"Code\" and D.\"LanguageISOCode\" = 'en'\r\n"
                + "left outer join  \"sap.ain.metaData::Enumerations.Text\" as E\r\n"
                + "on E.\"Type\" = 'ExternalSystemType' and A.\"SystemType\" =  E.\"Code\" and E.\"LanguageISOCode\" = ?\r\n"
                + "where A.\"Client\" = ? order by lcase(\"SystemName\")";

        List<SystemInfo> systemInfo = jdbcTemplate.query(querySystems,
                new Object[]{"en", "en", ClientId}, new RowMapper<SystemInfo>() {

            @Override
            public SystemInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                // TODO Auto-generated method stub

                SystemInfo info = new SystemInfo();
                info.setAdditionalInfo(rs.getString("AdditionalInfo"));
                info.setID(rs.getString("ID"));
                info.setSystemName(rs.getString("SystemName"));
                info.setSystemID(rs.getString("SystemID"));
                info.setIsOwnSystem(rs.getDouble("isOwnSystem"));
                info.setSystemStatus(rs.getString("SystemStatus"));
                info.setSystemStatusDescription(rs.getString("SystemStatusDescription"));
                info.setSystemType(rs.getString("SystemType"));
                info.setSystemClient(rs.getString("SystemClient"));
                info.setURL1(rs.getString("URL1"));
                info.setURL2(rs.getString("URL2"));
                info.setIsPrimary(Integer.toString(rs.getInt("isPrimary")));
                info.setIsActive(Integer.toString(rs.getInt("isActive")));
                info.setHCIArtifactName(rs.getString("HCIArtifactName"));
                info.setAdditionalField1(rs.getString("AdditionalField1"));
                info.setAdditionalField2(rs.getString("AdditionalField2"));
                info.setAdditionalField3(rs.getString("AdditionalField3"));
                info.setSystemTypeDescription(rs.getString("SystemTypeDescription"));

                return info;
            }

        });
        return systemInfo;
    }
}
