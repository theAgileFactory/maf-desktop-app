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

import com.fasterxml.jackson.databind.JsonNode;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import controllers.api.request.post.OrgUnitTypeRequest;
import dao.pmo.OrgUnitDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.pmo.OrgUnitType;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link OrgUnitTypeRequest}.
 * 
 * @author Oury Diallo
 * 
 */
@Api(value = "/api/core/org-unit-type", description = "Operations on Org Unit Types")
public class OrgUnitTypeApiController extends ApiController {

    public static Form<OrgUnitTypeRequest> orgUnitTypeRequestFormTemplate = Form.form(OrgUnitTypeRequest.class);

    /**
     * Get the org unit types list with filters.
     * 
     * @param selectable
     *            true to return only active org units, false only non-active,
     *            null all.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Org Unit Types", notes = "Return the list of the Org Unit Types in the system", response = OrgUnitType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getOrgUnitTypesList(@ApiParam(value = "selectable", required = false) @QueryParam("selectable") Boolean selectable) {
        try {
            return getJsonSuccessResponse(OrgUnitDao.getOrgUnitTypeAsListByFilter(selectable));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get an org unit type by id.
     * 
     * @param id
     *            the org unit id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Org Unit Type", notes = "Return the Org Unit Type with the specified id", response = OrgUnitType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getOrgUnitTypeById(@ApiParam(value = "org unit id", required = true) @PathParam("id") Long id) {
        try {
            if (OrgUnitDao.getOrgUnitTypeById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Org Unit Type with the specified id is not found"));
            }
            return getJsonSuccessResponse(OrgUnitDao.getOrgUnitTypeById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create an OrgUnit type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create an OrgUnit Type", notes = "Create an OrgUnit Type", response = OrgUnitTypeRequest.class, httpMethod = "POST")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "An orgUnit type", required = true, dataType = "OrgUnitTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createOrgUnitType() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<OrgUnitTypeRequest> orgUnitTypeRequestForm = orgUnitTypeRequestFormTemplate.bind(json);

            // if errors
            if (orgUnitTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = orgUnitTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            OrgUnitTypeRequest orgUnitTypeRequest = orgUnitTypeRequestForm.get();

            OrgUnitType orgUnitType = new OrgUnitType();

            // fill to match with DB
            orgUnitType.selectable = orgUnitTypeRequest.selectable;
            orgUnitType.name = orgUnitTypeRequest.name;
            orgUnitType.description = orgUnitTypeRequest.description;
            orgUnitType.refId = orgUnitTypeRequest.refId;
            orgUnitType.save();

            // return json success
            return getJsonSuccessCreatedResponse(orgUnitType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update an OrgUnit type.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the OrgUnit type id
     * 
     * @return the JSON object of the corresponding Org Unit type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified OrgUnit type, default for empty fields : null", notes = "Update an OrgUnit Type", response = OrgUnitTypeRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A input OrgUnit Type object", required = true, dataType = "OrgUnitTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updateOrgUnitType(@ApiParam(value = "An orgUnit type id", required = true) @PathParam("id") Long id) {
        try {

            // check if actor exist
            OrgUnitType orgUnitType = OrgUnitDao.getOrgUnitTypeById(id);
            if (orgUnitType == null) {
                return getJsonErrorResponse(new ApiError(404, "The OrgUnit Type with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<OrgUnitTypeRequest> orgUnitTypeRequestForm = orgUnitTypeRequestFormTemplate.bind(json);

            // if errors
            if (orgUnitTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = orgUnitTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            OrgUnitTypeRequest orgUnitTypeRequest = orgUnitTypeRequestForm.get();

            // fill to match with DB
            orgUnitType.selectable = orgUnitTypeRequest.selectable;
            orgUnitType.name = orgUnitTypeRequest.name;
            orgUnitType.description = orgUnitTypeRequest.description;
            orgUnitType.refId = orgUnitTypeRequest.refId;
            orgUnitType.save();

            // json success
            return getJsonSuccessResponse(orgUnitType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
