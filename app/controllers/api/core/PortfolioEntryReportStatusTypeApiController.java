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

import models.pmo.PortfolioEntryReportStatusType;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.pmo.PortfolioEntryReportDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link PortfolioEntryReportStatusType}.
 * 
 * @author Oury Diallo
 */
@Api(value = "/api/core/portfolio-entry-report-status-type", description = "Operations on Portfolio Entry Report Status Type")
public class PortfolioEntryReportStatusTypeApiController extends ApiController {

    /**
     * Get the portfolio entry report status types list with filters.
     * 
     * @param selectable
     *            true to return only active report status types, false only
     *            non-active, null all.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Portfolio Entry Report Status Types", notes = "Return the list of Portfolio Entry Report Status Types in the system",
            response = PortfolioEntryReportStatusType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result
            getPortfolioEntryReportStatusTypesList(@ApiParam(value = "selectable", required = false) @QueryParam("selectable") Boolean selectable) {
        try {
            return getJsonSuccessResponse(PortfolioEntryReportDao.getPEReportStatusTypeByFilter(selectable));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a portfolio entry report status type by id.
     * 
     * @param id
     *            the portfolio entry report status type id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Portfolio Entry Report Status Type",
            notes = "Return the Portfolio Entry Report Status Type with the specified id", response = PortfolioEntryReportStatusType.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPortfolioEntryReportStatusTypeById(
            @ApiParam(value = "portfolio entry report status type id", required = true) @PathParam("id") Long id) {

        try {
            if (PortfolioEntryReportDao.getPEReportStatusTypeById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Portfolio Entry Report Status Type with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryReportDao.getPEReportStatusTypeById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
