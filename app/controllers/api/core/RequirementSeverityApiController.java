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
import controllers.api.request.post.RequirementSeverityRequest;
import dao.delivery.RequirementDAO;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.delivery.RequirementSeverity;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link RequirementSeverity}.
 * 
 * @author Oury Diallo
 * @author Marc Schaer
 */
@Api(value = "/api/core/requirement-severity", description = "Operations on Requirement Severities")
public class RequirementSeverityApiController extends ApiController {

    public static Form<RequirementSeverityRequest> requirementSeverityRequestFormTemplate = Form.form(RequirementSeverityRequest.class);

    /**
     * Get all requirement severities.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Requirement Severities", notes = "Return the list of Requirement Severities in the system", response = RequirementSeverity.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getRequirementSeveritiesList() {
        try {
            return getJsonSuccessResponse(RequirementDAO.getRequirementSeverityAsList());
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a requirement severity by id.
     * 
     * @param id
     *            the requirement severity id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Requirement Severity", notes = "Return the Requirement Severity with the specified id", response = RequirementSeverity.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getRequirementSeverityById(@ApiParam(value = "Requirement Severity id", required = true) @PathParam("id") Long id) {

        try {
            if (RequirementDAO.getRequirementSeverityById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Requirement Severity with the specified id is not found"));
            }
            return getJsonSuccessResponse(RequirementDAO.getRequirementSeverityById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create a requirement severity.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a Requirement Severity", notes = "Create a Requirement Severity", response = RequirementSeverityRequest.class, httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A requirement severity", required = true, dataType = "RequirementSeverityRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createRequirementSeverity() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<RequirementSeverityRequest> requirementSeverityRequestForm = requirementSeverityRequestFormTemplate.bind(json);

            // if errors
            if (requirementSeverityRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = requirementSeverityRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            RequirementSeverityRequest requirementPriorityRequest = requirementSeverityRequestForm.get();

            RequirementSeverity requirementSeverity = new RequirementSeverity();

            // fill to match with DB
            requirementSeverity.name = requirementPriorityRequest.name;
            requirementSeverity.description = requirementPriorityRequest.description;
            requirementSeverity.isBlocker = requirementPriorityRequest.isBlocker;

            requirementSeverity.save();

            // return json success
            return getJsonSuccessCreatedResponse(requirementSeverity);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a requirement severity.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the requirement severity id
     * 
     * @return the JSON object of the corresponding requirement severity.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Requirement Severity, default for empty fields : null", notes = "Update a requirement severity", response = RequirementSeverityRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A input Requirement Severity object", required = true, dataType = "RequirementSeverityRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result udpateRequirementSeverity(@ApiParam(value = "A requirement severity id", required = true) @PathParam("id") Long id) {
        try {

            RequirementSeverity requirementSeverity = RequirementDAO.getRequirementSeverityById(id);
            if (requirementSeverity == null) {
                return getJsonErrorResponse(new ApiError(404, "The Requirement Severity with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<RequirementSeverityRequest> requirementSeverityRequestForm = requirementSeverityRequestFormTemplate.bind(json);

            // if errors
            if (requirementSeverityRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = requirementSeverityRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            RequirementSeverityRequest requirementSeverityRequest = requirementSeverityRequestForm.get();

            // fill to match with DB
            requirementSeverity.name = requirementSeverityRequest.name;
            requirementSeverity.description = requirementSeverityRequest.description;
            requirementSeverity.isBlocker = requirementSeverityRequest.isBlocker;

            requirementSeverity.save();

            // json success
            return getJsonSuccessResponse(requirementSeverity);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
