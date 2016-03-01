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
import controllers.api.request.post.RequirementPriorityRequest;
import dao.delivery.RequirementDAO;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.delivery.RequirementPriority;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link RequirementPriority}.
 * 
 * @author Oury Diallo
 * @author Marc Schaer
 */
@Api(value = "/api/core/requirement-priority", description = "Operations on Requirement Priority")
public class RequirementPriorityApiController extends ApiController {

    public static Form<RequirementPriorityRequest> requirementPriorityRequestFormTemplate = Form.form(RequirementPriorityRequest.class);

    /**
     * Get all requirement priorities.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Requirement Priorities", notes = "Return the list of Requirement Priorities in the system", response = RequirementPriority.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getRequirementPrioritiesList() {
        try {
            return getJsonSuccessResponse(RequirementDAO.getRequirementPriorityAsList());
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a requirement priority by id.
     * 
     * @param id
     *            the requirement priority id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Requirement Priority", notes = "Return the Requirement Priority with the specified id", response = RequirementPriority.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getRequirementPriorityById(@ApiParam(value = "Requirement Priority id", required = true) @PathParam("id") Long id) {
        try {
            if (RequirementDAO.getRequirementPriorityById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Requirement Priority with the specified id is not found"));
            }
            return getJsonSuccessResponse(RequirementDAO.getRequirementPriorityById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create a requirement priority.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a Requirement Priority", notes = "Create a Requirement Priority", response = RequirementPriorityRequest.class, httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A requirement priority", required = true, dataType = "RequirementPriorityRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createRequirementPriority() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<RequirementPriorityRequest> requirementPriorityRequestForm = requirementPriorityRequestFormTemplate.bind(json);

            // if errors
            if (requirementPriorityRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = requirementPriorityRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            RequirementPriorityRequest requirementPriorityRequest = requirementPriorityRequestForm.get();

            RequirementPriority requirementPriority = new RequirementPriority();

            // fill to match with DB
            requirementPriority.name = requirementPriorityRequest.name;
            requirementPriority.description = requirementPriorityRequest.description;
            requirementPriority.isMust = requirementPriorityRequest.isMust;

            requirementPriority.save();

            // return json success
            return getJsonSuccessCreatedResponse(requirementPriority);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a requirement priority.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the requirement priority id
     * 
     * @return the JSON object of the corresponding requirement priority.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Requirement Priority, default for empty fields : null", notes = "Update a requirement priority", response = RequirementPriorityRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A input Requirement Priority object", required = true, dataType = "RequirementPriorityRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result udpateRequirementPriority(@ApiParam(value = "A requirement priority id", required = true) @PathParam("id") Long id) {
        try {

            RequirementPriority requirementPriority = RequirementDAO.getRequirementPriorityById(id);
            if (requirementPriority == null) {
                return getJsonErrorResponse(new ApiError(404, "The Requirement Priority with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<RequirementPriorityRequest> requirementPriorityRequestForm = requirementPriorityRequestFormTemplate.bind(json);

            // if errors
            if (requirementPriorityRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = requirementPriorityRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            RequirementPriorityRequest requirementPriorityRequest = requirementPriorityRequestForm.get();

            // fill to match with DB
            requirementPriority.name = requirementPriorityRequest.name;
            requirementPriority.description = requirementPriorityRequest.description;
            requirementPriority.isMust = requirementPriorityRequest.isMust;

            requirementPriority.save();

            // json success
            return getJsonSuccessResponse(requirementPriority);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
