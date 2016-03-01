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
import controllers.api.request.post.RequirementStatusRequest;
import dao.delivery.RequirementDAO;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.delivery.RequirementStatus;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link RequirementStatus}.
 * 
 * @author Oury Diallo
 * @author Marc Schaer
 */
@Api(value = "/api/core/requirement-status", description = "Operations on Requirement Status")
public class RequirementStatusApiController extends ApiController {

    public static Form<RequirementStatusRequest> requirementStatusRequestFormTemplate = Form.form(RequirementStatusRequest.class);

    /**
     * Get all requirement status.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Requirement Status", notes = "Return the list of Requirement Status in the system", response = RequirementStatus.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getRequirementStatusList() {
        try {
            return getJsonSuccessResponse(RequirementDAO.getRequirementStatusAsList());
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a requirement status by id.
     * 
     * @param id
     *            the requirement status id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Requirement Status", notes = "Return the Requirement Status with the specified id", response = RequirementStatus.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getRequirementStatusById(@ApiParam(value = "Requirement Status id", required = true) @PathParam("id") Long id) {
        try {
            if (RequirementDAO.getRequirementStatusById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Requirement Status with the specified id is not found"));
            }
            return getJsonSuccessResponse(RequirementDAO.getRequirementStatusById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create a requirement status.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a Requirement Status", notes = "Create a Requirement Status", response = RequirementStatusRequest.class, httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A requirement status", required = true, dataType = "RequirementStatusRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createRequirementStatus() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<RequirementStatusRequest> requirementStatusRequestForm = requirementStatusRequestFormTemplate.bind(json);

            // if errors
            if (requirementStatusRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = requirementStatusRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            RequirementStatusRequest requirementStatusRequest = requirementStatusRequestForm.get();

            RequirementStatus requirementStatus = new RequirementStatus();

            // fill to match with DB
            requirementStatus.name = requirementStatusRequest.name;
            requirementStatus.description = requirementStatusRequest.description;
            requirementStatus.type = requirementStatusRequest.type;

            requirementStatus.save();

            // return json success
            return getJsonSuccessCreatedResponse(requirementStatus);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a requirement status.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the requirement status id
     * 
     * @return the JSON object of the corresponding requirement status.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Requirement Status, default for empty fields : null", notes = "Update a requirement status", response = RequirementStatusRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A input Requirement Status object", required = true, dataType = "RequirementStatusRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updateRequirementStatus(@ApiParam(value = "A requirement status id", required = true) @PathParam("id") Long id) {
        try {

            RequirementStatus requirementStatus = RequirementDAO.getRequirementStatusById(id);
            if (requirementStatus == null) {
                return getJsonErrorResponse(new ApiError(404, "The Requirement Status with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<RequirementStatusRequest> requirementStatusRequestForm = requirementStatusRequestFormTemplate.bind(json);

            // if errors
            if (requirementStatusRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = requirementStatusRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            RequirementStatusRequest requirementStatusRequest = requirementStatusRequestForm.get();

            // fill to match with DB
            requirementStatus.name = requirementStatusRequest.name;
            requirementStatus.description = requirementStatusRequest.description;
            requirementStatus.type = requirementStatusRequest.type;

            requirementStatus.save();

            // json success
            return getJsonSuccessResponse(requirementStatus);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
