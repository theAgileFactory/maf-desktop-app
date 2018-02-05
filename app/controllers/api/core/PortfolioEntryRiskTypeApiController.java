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
import controllers.api.request.post.PortfolioEntryRiskTypeRequest;
import dao.pmo.PortfolioEntryRiskAndIssueDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.pmo.PortfolioEntryRiskType;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link PortfolioEntryRiskType}.
 * 
 * @author Oury Diallo
 */
@Api(value = "/api/core/portfolio-entry-risk-type", description = "Operations on Portfolio Entry Risk Types")
public class PortfolioEntryRiskTypeApiController extends ApiController {

    public static Form<PortfolioEntryRiskTypeRequest> portfolioEntryRiskTypeRequestFormTemplate = Form.form(PortfolioEntryRiskTypeRequest.class);

    /**
     * Get the portfolio entry risk types list with filters.
     * 
     * @param selectable
     *            true to return only active risks, false only non-active, null
     *            all.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Portfolio Entry Risk Types", notes = "Return the list of Portfolio Entry Risk Types in the system", response = PortfolioEntryRiskType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryRiskTypesList(@ApiParam(value = "selectable", required = false) @QueryParam("selectable") Boolean selectable) {
        try {
            return getJsonSuccessResponse(PortfolioEntryRiskAndIssueDao.getPERiskTypeAsListByFilter(selectable));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a portfolio entry risk type by id.
     * 
     * @param id
     *            the portfolio entry risk type id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Portfolio Entry Risk Type", notes = "Return the Portfolio Entry Risk Type with the specified id", response = PortfolioEntryRiskType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryRiskTypeById(@ApiParam(value = "portfolio entry risk type id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryRiskAndIssueDao.getPERiskTypeById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry Risk Type with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryRiskAndIssueDao.getPERiskTypeById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create a portfolio entry risk type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a PE Risk Type", notes = "Create a Portfolio Entry Risk Type", response = PortfolioEntryRiskTypeRequest.class, httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A PE Risk type", required = true, dataType = "PortfolioEntryRiskTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createPortfolioEntryRiskType() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioEntryRiskTypeRequest> portfolioEntryRiskTypeRequestForm = portfolioEntryRiskTypeRequestFormTemplate.bind(json);

            // if errors
            if (portfolioEntryRiskTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioEntryRiskTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            PortfolioEntryRiskTypeRequest portfolioEntryRiskTypeRequest = portfolioEntryRiskTypeRequestForm.get();

            PortfolioEntryRiskType portfolioEntryRiskType = new PortfolioEntryRiskType();

            // fill to match with DB
            portfolioEntryRiskType.name = portfolioEntryRiskTypeRequest.name;
            portfolioEntryRiskType.description = portfolioEntryRiskTypeRequest.description;
            portfolioEntryRiskType.selectable = portfolioEntryRiskTypeRequest.selectable;

            portfolioEntryRiskType.save();

            // return json success
            return getJsonSuccessCreatedResponse(portfolioEntryRiskType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a portoflio entry Risk type.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the PE Risk type id
     * 
     * @return the JSON object of the corresponding PE Risk type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Portfolio Entry Risk Type, default for empty fields : null", notes = "Update a Portfolio Entry Risk Type", response = PortfolioEntryRiskTypeRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A input Portfolio Entry Risk Type object", required = true, dataType = "PortfolioEntryRiskTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result udpatePortfolioEntryRiskType(@ApiParam(value = "A PE Risk type id", required = true) @PathParam("id") Long id) {
        try {

            PortfolioEntryRiskType portfolioEntryRiskType = PortfolioEntryRiskAndIssueDao.getPERiskTypeById(id);
            if (portfolioEntryRiskType == null) {
                return getJsonErrorResponse(new ApiError(404, "The PE Risk Type with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioEntryRiskTypeRequest> portfolioEntryRiskTypeRequestForm = portfolioEntryRiskTypeRequestFormTemplate.bind(json);

            // if errors
            if (portfolioEntryRiskTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioEntryRiskTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            PortfolioEntryRiskTypeRequest portfolioEntryRiskTypeRequest = portfolioEntryRiskTypeRequestForm.get();

            // Save
            // fill to match with DB
            portfolioEntryRiskType.name = portfolioEntryRiskTypeRequest.name;
            portfolioEntryRiskType.description = portfolioEntryRiskTypeRequest.description;
            portfolioEntryRiskType.selectable = portfolioEntryRiskTypeRequest.selectable;

            portfolioEntryRiskType.save();

            // json success
            return getJsonSuccessResponse(portfolioEntryRiskType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
