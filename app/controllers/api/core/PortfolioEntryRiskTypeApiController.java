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

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import models.pmo.PortfolioEntryRiskType;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.pmo.PortfolioEntryRiskDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link PortfolioEntryRiskType}.
 * 
 * @author Oury Diallo
 */
@Api(value = "/api/core/portfolio-entry-risk-type", description = "Operations on Portfolio Entry Risk Types")
public class PortfolioEntryRiskTypeApiController extends ApiController {

    /**
     * Get the portfolio entry risk types list with filters.
     * 
     * @param selectable
     *            true to return only active risks, false only non-active, null
     *            all.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Portfolio Entry Risk Types", notes = "Return the list of Portfolio Entry Risk Types in the system",
            response = PortfolioEntryRiskType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryRiskTypesList(@ApiParam(value = "selectable", required = false) @QueryParam("selectable") Boolean selectable) {
        try {
            return getJsonSuccessResponse(PortfolioEntryRiskDao.getPERiskTypeAsListByFilter(selectable));
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
    @ApiOperation(value = "Get the specified Portfolio Entry Risk Type", notes = "Return the Portfolio Entry Risk Type with the specified id",
            response = PortfolioEntryRiskType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryRiskTypeById(@ApiParam(value = "portfolio entry risk type id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryRiskDao.getPERiskTypeById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry Risk Type with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryRiskDao.getPERiskTypeById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
