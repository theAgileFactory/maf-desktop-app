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

import models.pmo.PortfolioEntryPlanningPackageGroup;
import models.pmo.PortfolioEntryPlanningPackagePattern;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link PortfolioEntryPlanningPackageGroup}.
 * 
 * @author Oury Diallo
 */
@Api(value = "/api/core/portfolio-entry-planning-package-group", description = "Operations on Portfolio Entry Planning Package Groups")
public class PortfolioEntryPlanningPackageGroupApiController extends ApiController {

    /**
     * Get the portfolio entry planning package groups list with filter.
     * 
     * @param isActive
     *            true to return only active package group, false only
     *            non-active, null all.
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Portfolio Entry Planning Package Groups",
            notes = "Return the list of Portfolio Entry Planning Package Groups in the system", response = PortfolioEntryPlanningPackageGroup.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryPlanningPackageGroupsList(@ApiParam(value = "isActive", required = false) @QueryParam("isActive") Boolean isActive) {
        try {
            return getJsonSuccessResponse(PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupAsListByFilter(isActive));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a portfolio entry planning package group by id.
     * 
     * @param id
     *            the portfolio entry planning package group id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Portfolio Entry Planning Package Group",
            notes = "Return the Portfolio Entry Planning Package Group with the specified id", response = PortfolioEntryPlanningPackageGroup.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryPlanningPackageGroupById(
            @ApiParam(value = "portfolio entry planning package group id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry Planning Package Group with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a portfolio entry planning package pattern by id.
     * 
     * @param id
     *            the portfolio entry planning package id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Patterns of the specified Portfolio Entry Planning Package Group",
            notes = "Return the list of Patterns of the specified Portfolio Entry Planning Package Group in the system",
            response = PortfolioEntryPlanningPackagePattern.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryPlanningPackagePatternsList(
            @ApiParam(value = "portfolio entry planning package group id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry Planning Package Group with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryPlanningPackageDao.getPEPlanningPackagePatternAsListByGroup(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
