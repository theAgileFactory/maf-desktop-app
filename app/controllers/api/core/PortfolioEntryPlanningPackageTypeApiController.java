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

import javax.ws.rs.QueryParam;

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
import models.pmo.PortfolioEntryPlanningPackageType;
import play.mvc.Result;

/**
 * The API controller for the {@link PortfolioEntryPlanningPackageType}.
 * 
 * @author Oury Diallo
 */
@Api(value = "/api/core/portfolio-entry-planning-package-type", description = "Operations on Portfolio Entry Planning Package Types")
public class PortfolioEntryPlanningPackageTypeApiController extends ApiController {

    /**
     * Get the portfolio entry planning package types list with filter.
     * 
     * @param isActive
     *            true to return only active package type, false only
     *            non-active, null all.
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Portfolio Entry Planning Package Types",
            notes = "Return the list of Portfolio Entry Planning Package Types in the system", response = PortfolioEntryPlanningPackageType.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryPlanningPackageTypesList(@ApiParam(value = "isActive", required = false) @QueryParam("isActive") Boolean isActive) {
        try {
            return getJsonSuccessResponse(PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeAsListByFilter(isActive));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
