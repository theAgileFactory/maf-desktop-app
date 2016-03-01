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
import controllers.api.request.post.PortfolioTypeRequest;
import dao.pmo.PortfolioDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.pmo.PortfolioType;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link PortfolioType}.
 * 
 * @author Oury Diallo
 * @author Marc Schaer
 */
@Api(value = "/api/core/portfolio-type", description = "Operations on Portfolio Types")
public class PortfolioTypeApiController extends ApiController {

    public static Form<PortfolioTypeRequest> portfolioTypeRequestFormTemplate = Form.form(PortfolioTypeRequest.class);

    /**
     * Get the portfolio types list with filters.
     * 
     * @param selectable
     *            true to return only active portfolio types, false only
     *            non-active, null all.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Portfolio Types", notes = "Return the list of Portfolio Types in the system", response = PortfolioType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioTypesList(@ApiParam(value = "selectable", required = false) @QueryParam("selectable") Boolean selectable) {
        try {
            return getJsonSuccessResponse(PortfolioDao.getPortfolioTypeAsListByFilter(selectable));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a portfolio type by id.
     * 
     * @param id
     *            the portfolio type id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Portfolio Type", notes = "Return the Portfolio Type with the specified id", response = PortfolioType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioTypeById(@ApiParam(value = "portfolio type id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioDao.getPortfolioTypeById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Type with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioDao.getPortfolioTypeById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create an portfolio type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a portfolio type", notes = "Create a portfolio type", response = PortfolioTypeRequest.class, httpMethod = "POST")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "A Portfolio type", required = true, dataType = "PortfolioTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createPortfolioType() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioTypeRequest> portfolioTypeRequestForm = portfolioTypeRequestFormTemplate.bind(json);

            // if errors
            if (portfolioTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            PortfolioTypeRequest portfolioTypeRequest = portfolioTypeRequestForm.get();

            // Save portfolio entry type
            PortfolioType portfolioType = new PortfolioType();

            // fill to match with DB
            portfolioType.selectable = portfolioTypeRequest.selectable;
            portfolioType.name = portfolioTypeRequest.name;
            portfolioType.description = portfolioTypeRequest.description;

            portfolioType.save();

            // return json success
            return getJsonSuccessCreatedResponse(portfolioType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a portfolio type.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the portfolio type id
     * 
     * @return the JSON object of the corresponding portfolio type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Portfolio type, default for empty fields : null", notes = "Update a Portfolio Type", response = PortfolioTypeRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "An input Portfolio Type object", required = true, dataType = "PortfolioTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updatePortfolioType(@ApiParam(value = "A portfolio type id", required = true) @PathParam("id") Long id) {
        try {

            // check if actor exist
            PortfolioType portfolioType = PortfolioDao.getPortfolioTypeById(id);
            if (portfolioType == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Type with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioTypeRequest> portfolioTypeRequestForm = portfolioTypeRequestFormTemplate.bind(json);

            // if errors
            if (portfolioTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            PortfolioTypeRequest portfolioTypeRequest = portfolioTypeRequestForm.get();

            // fill to match with DB
            portfolioType.selectable = portfolioTypeRequest.selectable;
            portfolioType.name = portfolioTypeRequest.name;
            portfolioType.description = portfolioTypeRequest.description;

            portfolioType.save();

            // json success
            return getJsonSuccessResponse(portfolioType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
