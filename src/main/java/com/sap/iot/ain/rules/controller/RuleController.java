/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.sap.iot.ain.rules.controller;

import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.sap.iot.ain.indicator.v2.entities.IndicatorConfigurationThresholdEntity;
import com.sap.dsc.ac.iotae.reuse.Utility;
import com.sap.iot.ain.reuse.utils.EnvironmentUtils;
import com.sap.iot.ain.rules.models.GetUsersForAnOrganization;
import com.sap.iot.ain.rules.models.ResponseData;
import com.sap.iot.ain.rules.models.RuleEquipmentMapping;
import com.sap.iot.ain.rules.models.RuleEquipmentMappingPOST;
import com.sap.iot.ain.rules.models.RuleWithSteps;
import com.sap.iot.ain.rules.services.RuleService;
import com.sap.iot.ain.rules.validation.AssignEquipmenttoObjectValidations;
import com.sap.iot.ain.rules.validation.RuleAlertTypeValidations;
import com.sap.iot.ain.rules.validation.RuleIdValidations;
import com.sap.iot.ain.rules.validation.UnAssignEquipmentFromObjectValidation;
import com.sap.iot.ain.rules.validation.ValidationHelperDao;
import com.sap.iot.ain.security.AuthenticatedUserDetails;
import com.sap.iot.ain.security.Secure;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfoException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import com.sap.iot.ain.reuse.CustomExceptionList;

import javax.ws.rs.core.Response;

import com.sap.iot.ain.gen.businesspartner.ErrorMessage;

@Component
@Path("/api/v1")
public class RuleController {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    RuleService ruleService;

    @Autowired
    private ReloadableResourceBundleMessageSource resourceBundle;

    @Autowired
    private AuthenticatedUserDetails aud;

    private static final Logger logger = LoggerFactory.getLogger(RuleController.class);


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rules/{id}/status/{statuscode}")
    @Secure(roles = {"RULES_EDIT","RULES_DELETE"})
    @RuleAlertTypeValidations
    public void changeRuleStatus(@PathParam("id") String id, @PathParam("statuscode") int statuscode)
            throws NamingException {
        ruleService.changeRuleStatus(id, statuscode, em);

    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/rules/{id}")
    @Secure(roles = {"RULES_DELETE"})
    public void deleteRule(@PathParam("id") String id) throws SchedulerException, NamingException {
        ruleService.deleteRuleById(id, em);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/rules/{id}")
    @Secure(roles = {"RULES_EDIT", "RULES_DELETE"})
    public RuleWithSteps postRuleWithSteps(@PathParam("id") String id,
                                           @Valid RuleWithSteps oRuleSteps)
            throws SchedulerException, NamingException, UserInfoException {
        String tenantId = null;
        RuleWithSteps rs = new RuleWithSteps();
        if (EnvironmentUtils.isCF()) {
            tenantId = SecurityContext.getUserInfo().getSubdomain();
        } else {
            tenantId = Utility.getTenantFromJWT(
                    AuthenticatedUserDetails.getInstance().getUserDetails().getUserTenantId());
        }
        ruleService.extractAlertTypeId(oRuleSteps);
        ruleService.updateRuleWithSteps(id, oRuleSteps, tenantId);
        rs = ruleService.getRuleWithStepsById(id);
        return rs;

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/rules")
    @Secure(roles = {"RULES_EDIT", "RULES_DELETE"})

    public RuleWithSteps createRuleWithSteps(@Valid RuleWithSteps oRuleSteps)
            throws SchedulerException, NamingException, UserInfoException {
        String tenantId = null;
        if (EnvironmentUtils.isCF()) {
            tenantId = SecurityContext.getUserInfo().getSubdomain();
        } else {
            tenantId = Utility.getTenantFromJWT(
                    AuthenticatedUserDetails.getInstance().getUserDetails().getUserTenantId());
        }
        ValidationHelperDao validations = new ValidationHelperDao();
        ErrorMessage errorMessage = new ErrorMessage();
        List<ErrorMessage> listErrorMessage = new ArrayList<ErrorMessage>();

        if (validations.isRuleNameValid(oRuleSteps.rule.getName())) {

            ErrorMessage message = new ErrorMessage();
            message.setErrorMessage(
                    resourceBundle.getMessage("rule.name.valid", null, aud.getUserDetails().getLocale()));
            listErrorMessage.add(message);
            throw new CustomExceptionList(Response.Status.BAD_REQUEST.getStatusCode(), listErrorMessage);

        }

        ruleService.extractAlertTypeId(oRuleSteps);

        RuleWithSteps response = ruleService.createRuleWithSteps(oRuleSteps, tenantId);
        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rules/organization/users")
    @Secure(roles = {"CONFIGURATION_EDIT", "CONFIGURATION_DELETE"})
    public List<GetUsersForAnOrganization> getUsersForAnOrganization() {
        List<GetUsersForAnOrganization> getUsersForAnOrganizations =
                ruleService.getUsersForAnOrganization();
        return getUsersForAnOrganizations;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rules/threshold")
    @Secure(roles = {"RULES_READ","RULES_EDIT", "RULES_DELETE"})
    public List<IndicatorConfigurationThresholdEntity> getThresholdsForIndicator(
            @QueryParam("$modelId") String modelId, @QueryParam("$templateId") String templateId,
            @QueryParam("$pstId") String pstId, @QueryParam("$indicatorId") String indicatorId) {
        List<IndicatorConfigurationThresholdEntity> thresholdsForIndicator =
                ruleService.getThresholdsForIndicator(modelId, templateId, pstId, indicatorId);
        return thresholdsForIndicator;
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rules/{id}/assign/equipments")
    @Secure(roles = {"EQUIPMENT_EDIT", "EQUIPMENT_DELETE"})
    @RuleIdValidations

    public ResponseData postRuleEquipmentMapping(@PathParam("id") String id,
                                                 RuleEquipmentMappingPOST ruleEquipmentMappingPOST) {
        ResponseData response = new ResponseData();
        ruleEquipmentMappingPOST.setRuleId(id);
        response = ruleService.createRuleEquipmentMapping(ruleEquipmentMappingPOST);
        return response;
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rules/{id}/unassign/equipments")
    @Secure(roles = {"EQUIPMENT_EDIT", "EQUIPMENT_DELETE"})
    @RuleIdValidations
    public void unassignRuleEquipmentMapping(@PathParam("id") String id,
                                             @Valid RuleEquipmentMappingPOST ruleEquipmentMappingPOST) {
        ruleEquipmentMappingPOST.setRuleId(id);
        ruleService.unAssignEquipmentForARule(ruleEquipmentMappingPOST);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rules/{id}")
    @Secure(roles = {"RULES_READ","RULES_EDIT", "RULES_DELETE"})
    public RuleWithSteps getRuleWithSteps(@PathParam("id") String id) throws NamingException {
        RuleWithSteps rs = new RuleWithSteps();
        rs = ruleService.getRuleWithStepsById(id);
        return rs;
    }

    @DELETE
    @Path("/rules/deletelogs")
    @Secure(roles = {"RULES_EDIT", "RULES_DELETE"})
    public void deleteRuleRunLogs(@QueryParam("startDate") String startDate,
                                  @QueryParam("endDate") String endDate) throws NamingException {
        ruleService.deleteRuleRunLogs(startDate, endDate);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rules/{id}/assign/equipment")
    @Secure(roles = {"EQUIPMENT_EDIT", "EQUIPMENT_DELETE"})
    @RuleIdValidations
    @AssignEquipmenttoObjectValidations
    public ResponseData postRuleEquipmentMapping(@PathParam("id") String id,
                                                 RuleEquipmentMapping ruleEquipmentMapping) {

        return ruleService.createRuleEquipmentMapping(id, ruleEquipmentMapping);

    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rules/{id}/unassign/equipment")
    @Secure(roles = {"EQUIPMENT_EDIT", "EQUIPMENT_DELETE"})
    @RuleIdValidations
    @UnAssignEquipmentFromObjectValidation
    public ResponseData unassignRuleEquipmentMapping(@PathParam("id") String id,
                                                     RuleEquipmentMapping ruleEquipmentMapping) {

        return ruleService.unAssignEquipmentForARule(id, ruleEquipmentMapping);

    }
}