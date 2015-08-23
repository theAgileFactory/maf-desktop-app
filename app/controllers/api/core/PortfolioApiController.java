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

import models.pmo.Portfolio;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import controllers.api.request.PortfolioListRequest;
import dao.pmo.PortfolioDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link Portfolio}.
 * 
 * @author Oury Diallo
 * 
 */
@Api(value = "/api/core/portfolio", description = "Operations on Portfolios")
public class PortfolioApiController extends ApiController {

    public static Form<PortfolioListRequest> portfolioListRequestFormTemplate = Form.form(PortfolioListRequest.class);
    public static ObjectMapper portfolioMapper = new ObjectMapper();

    /**
     * Get the portfolios list with filter.
     * 
     * @param isActive
     *            true to return only active portfolios, false only non-active,
     *            null all.
     * @param managerId
     *            if not null then return only portfolios with the given
     *            manager.
     * @param portfolioEntryId
     *            if not null then return only portfolios containing the given
     *            portfolio entry.
     * @param portfolioTypeId
     *            if not null then return only portfolios with the given type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Portfolios", notes = "Return the list of Portfolios in the system", response = Portfolio.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfoliosList(@ApiParam(value = "isActive", required = false) @QueryParam("isActive") Boolean isActive, @ApiParam(
            value = "managerId", required = false) @QueryParam("managerId") Long managerId,
            @ApiParam(value = "portfolioEntryId", required = false) @QueryParam("portfolioEntryId") Long portfolioEntryId, @ApiParam(
                    value = "portfolioTypeId", required = false) @QueryParam("portfolioTypeId") Long portfolioTypeId) {

        try {

            // Validation form
            PortfolioListRequest portfolioListRequest = new PortfolioListRequest(isActive, managerId, portfolioEntryId, portfolioTypeId);

            // object to jsonNode
            JsonNode node = portfolioMapper.valueToTree(portfolioListRequest);

            // fill a play form
            Form<PortfolioListRequest> portfolioListRequestForm = portfolioListRequestFormTemplate.bind(node);

            if (portfolioListRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = portfolioListRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            return getJsonSuccessResponse(PortfolioDao.getPortfolioAsListByFilter(isActive, managerId, portfolioEntryId, portfolioTypeId));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Get a portfolio by id.
     * 
     * @param id
     *            the porfolio id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Portfolio", notes = "Return the Portfolio with the specified id", response = Portfolio.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioById(@ApiParam(value = "Id of the portfolio", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioDao.getPortfolioById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioDao.getPortfolioById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
