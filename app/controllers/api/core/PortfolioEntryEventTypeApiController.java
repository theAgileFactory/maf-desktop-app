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
import controllers.api.request.post.PortfolioEntryEventTypeRequest;
import dao.pmo.PortfolioEntryEventDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.pmo.PortfolioEntryEventType;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link PortfolioEntryEventType}.
 * 
 * @author Oury Diallo
 * @author Marc Schaer
 * 
 */
@Api(value = "/api/core/portfolio-entry-event-type", description = "Operations on Portfolio Entry Event Type")
public class PortfolioEntryEventTypeApiController extends ApiController {

    public static Form<PortfolioEntryEventTypeRequest> portfolioEntryEventTypeRequestFormTemplate = Form.form(PortfolioEntryEventTypeRequest.class);

    /**
     * Get the portfolio entry event types list with filter.
     * 
     * @param selectable
     *            true to return only active event types, false only non-active,
     *            null all.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Portfolio Entry Event Types", notes = "Return the list of Portfolio Entry Event Types in the system", response = PortfolioEntryEventType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryEventTypesList(@ApiParam(value = "selectable", required = false) @QueryParam("selectable") Boolean selectable) {
        try {
            return getJsonSuccessResponse(PortfolioEntryEventDao.getPEEventTypeAsListByFilter(selectable));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a portfolio entry event type by id.
     * 
     * @param id
     *            the portfolio entry event type id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Portfolio Entry Event Type", notes = "Return the Portfolio Entry Event Type with the specified id", response = PortfolioEntryEventType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryEventTypeById(@ApiParam(value = "portfolio entry event type id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryEventDao.getPEEventTypeById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry Event Type with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryEventDao.getPEEventTypeById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create a portfolio entry event type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a PE Event Type", notes = "Create a Portfolio Entry Event Type", response = PortfolioEntryEventTypeRequest.class, httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A PE event type", required = true, dataType = "PortfolioEntryEventTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createPortfolioEntryEventType() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioEntryEventTypeRequest> portfolioEntryEventTypeRequestForm = portfolioEntryEventTypeRequestFormTemplate.bind(json);

            // if errors
            if (portfolioEntryEventTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioEntryEventTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            PortfolioEntryEventTypeRequest portfolioEntryEventTypeRequest = portfolioEntryEventTypeRequestForm.get();

            PortfolioEntryEventType portfolioEntryEventType = new PortfolioEntryEventType();

            // fill to match with DB
            portfolioEntryEventType.name = portfolioEntryEventTypeRequest.name;
            portfolioEntryEventType.bootstrapGlyphicon = portfolioEntryEventTypeRequest.bootstrapGlyphicon;
            portfolioEntryEventType.selectable = portfolioEntryEventTypeRequest.selectable;

            portfolioEntryEventType.save();

            // return json success
            return getJsonSuccessCreatedResponse(portfolioEntryEventType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a portoflio entry event type.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the PE event type id
     * 
     * @return the JSON object of the corresponding PE event type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Portfolio Entry Event Type, default for empty fields : null", notes = "Update a Portfolio Entry Event Type", response = PortfolioEntryEventTypeRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A input Portfolio Entry Event Type object", required = true, dataType = "PortfolioEntryEventTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result udpatePortfolioEntryEventType(@ApiParam(value = "A PE event type id", required = true) @PathParam("id") Long id) {
        try {

            PortfolioEntryEventType peEventType = PortfolioEntryEventDao.getPEEventTypeById(id);
            if (peEventType == null) {
                return getJsonErrorResponse(new ApiError(404, "The PE Event Type with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PortfolioEntryEventTypeRequest> portfolioEntryEventTypeRequestForm = portfolioEntryEventTypeRequestFormTemplate.bind(json);

            // if errors
            if (portfolioEntryEventTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioEntryEventTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            PortfolioEntryEventTypeRequest portfolioEntryEventTypeRequest = portfolioEntryEventTypeRequestForm.get();

            // Save
            // fill to match with DB
            peEventType.name = portfolioEntryEventTypeRequest.name;
            peEventType.bootstrapGlyphicon = portfolioEntryEventTypeRequest.bootstrapGlyphicon;
            peEventType.selectable = portfolioEntryEventTypeRequest.selectable;

            peEventType.save();

            // json success
            return getJsonSuccessResponse(peEventType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
