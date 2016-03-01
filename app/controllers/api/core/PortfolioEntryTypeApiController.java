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
import controllers.api.request.post.PortfolioEntryTypeRequest;
import dao.pmo.PortfolioEntryDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.pmo.PortfolioEntryType;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link PortfolioEntryType}.
 * 
 * @author Oury Diallo
 * @author Marc Schaer
 */
@Api(value = "/api/core/portfolio-entry-type", description = "Operations on Portfolio Entry Types")
public class PortfolioEntryTypeApiController extends ApiController {

    public static Form<PortfolioEntryTypeRequest> portfolioEntryTypeRequestFormTemplate = Form.form(PortfolioEntryTypeRequest.class);

    /**
     * Get the portfolio entry types list with filters.
     * 
     * @param selectable
     *            true to return only active portfolio entry type, false only
     *            non-active, null all.
     * @param isRelease
     *            true to return only release portfolio entry type, false only
     *            initiative, null all.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Portfolio Entry Types", notes = "Return the list of Portfolio Entry Types in the system", response = PortfolioEntryType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryTypesList(@ApiParam(value = "selectable", required = false) @QueryParam("selectable") Boolean selectable,
            @ApiParam(value = "isRelease", required = false) @QueryParam("isRelease") Boolean isRelease) {
        try {
            return getJsonSuccessResponse(PortfolioEntryDao.getPETypeAsListByFilter(selectable, isRelease));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a portfolio entry type by id.
     * 
     * @param id
     *            the portfolio entry type id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Portfolio Entry Type", notes = "Return the Portfolio Entry Type with the specified id", response = PortfolioEntryType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryTypeById(@ApiParam(value = "portfolio entry type id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryDao.getPETypeById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry Type with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryDao.getPETypeById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create an portfolio entry type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a portfolio entry type", notes = "Create a portfolio entry type", response = PortfolioEntryTypeRequest.class, httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A PortfolioEntry type", required = true, dataType = "PortfolioEntryTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createPortfolioEntryType() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioEntryTypeRequest> portfolioEntryTypeRequestForm = portfolioEntryTypeRequestFormTemplate.bind(json);

            // if errors
            if (portfolioEntryTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioEntryTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            PortfolioEntryTypeRequest portfolioEntryTypeRequest = portfolioEntryTypeRequestForm.get();

            // Save portfolio entry type
            PortfolioEntryType portfolioEntryType = new PortfolioEntryType();

            // fill to match with DB
            portfolioEntryType.selectable = portfolioEntryTypeRequest.selectable;
            portfolioEntryType.name = portfolioEntryTypeRequest.name;
            portfolioEntryType.description = portfolioEntryTypeRequest.description;
            portfolioEntryType.isRelease = portfolioEntryTypeRequest.isRelease;

            portfolioEntryType.save();

            // return json success
            return getJsonSuccessCreatedResponse(portfolioEntryType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a portfolio entry type.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the portfolio entry type id
     * 
     * @return the JSON object of the corresponding portfolio entry type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Portfolio entry type, default for empty fields : null", notes = "Update a Portfolio Entry Type", response = PortfolioEntryTypeRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "An input Portfolio Entry Type object", required = true, dataType = "PortfolioEntryTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updatePortfolioEntryType(@ApiParam(value = "A portfolio entry type id", required = true) @PathParam("id") Long id) {
        try {

            // check if actor exist
            PortfolioEntryType portfolioEntryType = PortfolioEntryDao.getPETypeById(id);
            if (portfolioEntryType == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry Type with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioEntryTypeRequest> portfolioEntryTypeRequestForm = portfolioEntryTypeRequestFormTemplate.bind(json);

            // if errors
            if (portfolioEntryTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioEntryTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            PortfolioEntryTypeRequest portfolioEntryTypeRequest = portfolioEntryTypeRequestForm.get();

            // fill to match with DB
            portfolioEntryType.selectable = portfolioEntryTypeRequest.selectable;
            portfolioEntryType.name = portfolioEntryTypeRequest.name;
            portfolioEntryType.description = portfolioEntryTypeRequest.description;
            portfolioEntryType.isRelease = portfolioEntryTypeRequest.isRelease;

            portfolioEntryType.save();

            // json success
            return getJsonSuccessResponse(portfolioEntryType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
