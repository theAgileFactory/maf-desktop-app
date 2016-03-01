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
import controllers.api.request.post.PortfolioEntryPlanningPackageTypeRequest;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.pmo.PortfolioEntryPlanningPackageType;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link PortfolioEntryPlanningPackageType}.
 * 
 * @author Oury Diallo
 * @author Marc Schaer
 */
@Api(value = "/api/core/portfolio-entry-planning-package-type", description = "Operations on Portfolio Entry Planning Package Types")
public class PortfolioEntryPlanningPackageTypeApiController extends ApiController {

    public static Form<PortfolioEntryPlanningPackageTypeRequest> portfolioEntryPlanningPackageTypeRequestFormTemplate = Form
            .form(PortfolioEntryPlanningPackageTypeRequest.class);

    /**
     * Get the portfolio entry planning package types list with filter.
     * 
     * @param isActive
     *            true to return only active package type, false only
     *            non-active, null all.
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Portfolio Entry Planning Package Types", notes = "Return the list of Portfolio Entry Planning Package Types in the system", response = PortfolioEntryPlanningPackageType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryPlanningPackageTypesList(@ApiParam(value = "isActive", required = false) @QueryParam("isActive") Boolean isActive) {
        try {
            return getJsonSuccessResponse(PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeAsListByFilter(isActive));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a portfolio entry planning package type by id.
     * 
     * @param id
     *            the portfolio entry planning package type id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Portfolio Entry Planning Package Type", notes = "Return the Portfolio Entry Planning Package Type with the specified id", response = PortfolioEntryPlanningPackageType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryPlanningPackageTypeById(
            @ApiParam(value = "portfolio entry planning package type id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry Planning Package Type with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create a portfolio entry planning package type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a PE Planning Package Type", notes = "Create a Portfolio Entry Planning Package Type", response = PortfolioEntryPlanningPackageTypeRequest.class, httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A PE planning package type", required = true, dataType = "PortfolioEntryPlanningPackageTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createPortfolioEntryPlanningPackageType() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioEntryPlanningPackageTypeRequest> portfolioEntryPlanningPackageTypeRequestForm = portfolioEntryPlanningPackageTypeRequestFormTemplate
                    .bind(json);

            // if errors
            if (portfolioEntryPlanningPackageTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioEntryPlanningPackageTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            PortfolioEntryPlanningPackageTypeRequest portfolioEntryPlanningPackageTypeRequest = portfolioEntryPlanningPackageTypeRequestForm.get();

            PortfolioEntryPlanningPackageType portfolioEntryPlanningPackageType = new PortfolioEntryPlanningPackageType();

            // fill to match with DB
            portfolioEntryPlanningPackageType.name = portfolioEntryPlanningPackageTypeRequest.name;
            portfolioEntryPlanningPackageType.isActive = portfolioEntryPlanningPackageTypeRequest.isActive;
            portfolioEntryPlanningPackageType.cssClass = portfolioEntryPlanningPackageTypeRequest.cssClass;

            portfolioEntryPlanningPackageType.save();

            // return json success
            return getJsonSuccessCreatedResponse(portfolioEntryPlanningPackageType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a portoflio entry planning package type.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the PE planning package type id
     * 
     * @return the JSON object of the corresponding PE planning package type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Portfolio Entry Planning Package Type, default for empty fields : null", notes = "Update a Portfolio Entry Planning Package Type", response = PortfolioEntryPlanningPackageTypeRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A input Portfolio Entry Planning Package Type object", required = true, dataType = "PortfolioEntryPlanningPackageTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result udpatePortfolioEntryPlanningPackageType(@ApiParam(value = "A PE planning package type id", required = true) @PathParam("id") Long id) {
        try {

            PortfolioEntryPlanningPackageType pePlanningPackageType = PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeById(id);
            if (pePlanningPackageType == null) {
                return getJsonErrorResponse(new ApiError(404, "The PE Planning Package Type with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioEntryPlanningPackageTypeRequest> portfolioEntryPlanningPackageTypeRequestForm = portfolioEntryPlanningPackageTypeRequestFormTemplate
                    .bind(json);

            // if errors
            if (portfolioEntryPlanningPackageTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioEntryPlanningPackageTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            PortfolioEntryPlanningPackageTypeRequest portfolioEntryPlanningPackageTypeRequest = portfolioEntryPlanningPackageTypeRequestForm.get();

            // Save
            // fill to match with DB
            pePlanningPackageType.name = portfolioEntryPlanningPackageTypeRequest.name;
            pePlanningPackageType.isActive = portfolioEntryPlanningPackageTypeRequest.isActive;
            pePlanningPackageType.cssClass = portfolioEntryPlanningPackageTypeRequest.cssClass;

            pePlanningPackageType.save();

            // json success
            return getJsonSuccessResponse(pePlanningPackageType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
