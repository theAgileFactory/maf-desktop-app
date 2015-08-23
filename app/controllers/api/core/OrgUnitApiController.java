/*! LICENSE
 *
 * Copyright (c) 2015, The Agile Factory SA and/or its affiliates. All rights
 * reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package controllers.api.core;

import java.util.List;
import java.util.Map;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import models.pmo.OrgUnit;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import controllers.api.request.OrgUnitListRequest;
import controllers.api.request.post.OrgUnitRequest;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link OrgUnit}.
 * 
 * @author Oury Diallo
 * 
 */
@Api(value = "/api/core/org-unit", description = "Operations on Org Unit")
public class OrgUnitApiController extends ApiController {

    public static Form<OrgUnitListRequest> orgUnitListRequestFormTemplate = Form.form(OrgUnitListRequest.class);
    public static Form<OrgUnitRequest> orgUnitRequestFormTemplate = Form.form(OrgUnitRequest.class);
    public static ObjectMapper orgUnitMapper = new ObjectMapper();

    /**
     * Get the org units list with filters.
     * 
     * @param isActive
     *            true to return only active org units, false only non-active,
     *            null all.
     * @param managerId
     *            if not null then return only org units with the given manager.
     * @param parentId
     *            if not null then return only org units with the given parent.
     * @param orgUnitTypeId
     *            if not null then return only org units with the given type.
     * @param canSponsor
     *            if not null then return only sponsoring units.
     * @param canDeliver
     *            if not null then return only delivery units.
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Org Units", notes = "Return the list of Org Units in the system", response = OrgUnit.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getOrgUnitsList(@ApiParam(value = "isActive", required = false) @QueryParam("isActive") Boolean isActive, @ApiParam(
            value = "managerId", required = false) @QueryParam("managerId") Long managerId,
            @ApiParam(value = "parentId", required = false) @QueryParam("parentId") Long parentId,
            @ApiParam(value = "orgUnitTypeId", required = false) @QueryParam("orgUnitTypeId") Long orgUnitTypeId, @ApiParam(value = "canSponsor",
                    required = false) @QueryParam("canSponsor") Boolean canSponsor,
            @ApiParam(value = "canDeliver", required = false) @QueryParam("canDeliver") Boolean canDeliver) {

        try {

            // Validation form
            OrgUnitListRequest orgUnitListRequest = new OrgUnitListRequest(isActive, managerId, parentId, orgUnitTypeId, canSponsor, canDeliver);

            // object to jsonNode
            JsonNode node = orgUnitMapper.valueToTree(orgUnitListRequest);

            // fill a play form
            Form<OrgUnitListRequest> orgUnitListRequestForm = orgUnitListRequestFormTemplate.bind(node);

            if (orgUnitListRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = orgUnitListRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            return getJsonSuccessResponse(OrgUnitDao.getOrgUnitAsListByFilter(isActive, managerId, parentId, orgUnitTypeId, canSponsor, canDeliver));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get an org unit by id.
     * 
     * @param id
     *            the org unit id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Org Unit", notes = "Return the Org Unit with the specified id", response = OrgUnit.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getOrgUnitById(@ApiParam(value = "Id of the org unit", required = true) @PathParam("id") Long id) {

        try {
            if (OrgUnitDao.getOrgUnitById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Org Unit with the specified id is not found"));
            }
            return getJsonSuccessResponse(OrgUnitDao.getOrgUnitById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create an org unit.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create an OrgUnit", notes = "Create an OrgUnit", response = OrgUnitRequest.class, httpMethod = "POST")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "An OrgUnit", required = true, dataType = "OrgUnitRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createOrgUnit() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<OrgUnitRequest> orgUnitRequestForm = orgUnitRequestFormTemplate.bind(json);

            // if errors
            if (orgUnitRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = orgUnitRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            OrgUnitRequest orgUnitRequest = orgUnitRequestForm.get();

            // fill to match with DB
            OrgUnit orgUnit = new OrgUnit();
            orgUnit.refId = orgUnitRequest.refId;
            orgUnit.name = orgUnitRequest.name;
            orgUnit.isActive = orgUnitRequest.isActive;
            orgUnit.canSponsor = orgUnitRequest.canSponsor;
            orgUnit.canDeliver = orgUnitRequest.canDeliver;
            orgUnit.orgUnitType = OrgUnitDao.getOrgUnitTypeById(orgUnitRequest.orgUnitTypeId);
            orgUnit.manager = ActorDao.getActorById(orgUnitRequest.managerId);
            orgUnit.parent = OrgUnitDao.getOrgUnitById(orgUnitRequest.parentId);

            // Save OrgUnit
            orgUnit.save();

            // return json success
            return getJsonSuccessCreatedResponse(orgUnit);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update an org unit.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the org unit id
     * 
     * @return the JSON object of the corresponding org unit.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified OrgUnit, default for empty fields : null", notes = "Update an OrgUnit", response = OrgUnitRequest.class,
            httpMethod = "PUT")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "An OrgUnit", required = true, dataType = "OrgUnitRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updateOrgUnit(@ApiParam(value = "id", required = true) @PathParam("id") Long id) {
        try {

            // check if orgunit exist
            if (OrgUnitDao.getOrgUnitById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Org Unit with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<OrgUnitRequest> orgUnitRequestForm = orgUnitRequestFormTemplate.bind(json);

            // if errors
            if (orgUnitRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = orgUnitRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            OrgUnitRequest orgUnitRequest = orgUnitRequestForm.get();

            // Save OrgUnit
            OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(id);

            // fill to match with DB
            orgUnit.refId = orgUnitRequest.refId;
            orgUnit.name = orgUnitRequest.name;
            orgUnit.isActive = orgUnitRequest.isActive;
            orgUnit.canSponsor = orgUnitRequest.canSponsor;
            orgUnit.canDeliver = orgUnitRequest.canDeliver;
            orgUnit.orgUnitType = OrgUnitDao.getOrgUnitTypeById(orgUnitRequest.orgUnitTypeId);
            orgUnit.manager = ActorDao.getActorById(orgUnitRequest.managerId);
            orgUnit.parent = OrgUnitDao.getOrgUnitById(orgUnitRequest.parentId);

            orgUnit.save();

            // json success
            return getJsonSuccessResponse(orgUnit);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
