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

import models.finance.BudgetBucket;
import models.finance.BudgetBucketLine;
import models.finance.PortfolioEntryBudgetLine;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.finance.BudgetBucketDAO;
import dao.finance.PortfolioEntryBudgetDAO;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link BudgetBucket}.
 * 
 * @author Pierre-Yves Cloux
 * @author Oury Diallo
 * 
 */
@Api(value = "/api/core/budget-bucket", description = "Operations on Budget Buckets")
public class BudgetBucketApiController extends ApiController {

    /**
     * Get the budget buckets list with filter.
     * 
     * @param isActive
     *            filter on BudgetBucketDAO active flag
     * @param isApproved
     *            filter on BudgetBucketDAO approved flag
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Budget Buckets", notes = "Return the list of Budget Buckets in the system", response = BudgetBucket.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getBudgetBucketsList(@ApiParam(value = "isActive", required = false) @QueryParam("isActive") Boolean isActive, @ApiParam(
            value = "isApproved", required = false) @QueryParam("isApproved") Boolean isApproved) {
        try {
            return getJsonSuccessResponse(BudgetBucketDAO.getBudgetBucketAsListByActiveAndApproved(isActive, isApproved));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a budget bucket by id.
     * 
     * @param id
     *            the budget bucket id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Budget Bucket", notes = "Return the Budget Bucket with the specified id", response = BudgetBucket.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getBudgetBucketById(@ApiParam(value = "budgetBucket's id", required = true) @PathParam("id") Long id) {

        try {
            if (BudgetBucketDAO.getBudgetBucketById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Budget Bucket with the specified id is not found"));
            }
            return getJsonSuccessResponse(BudgetBucketDAO.getBudgetBucketById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Get the lines of a budget bucket.
     * 
     * @param budgetBucketId
     *            the budget bucket id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the active Budget Buckets line", notes = "Return list of Budget Buckets line in the system",
            response = BudgetBucketLine.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getBudgetBucketLinesList(@ApiParam(value = "budgetBucketId", required = true) @PathParam("id") Long budgetBucketId) {
        try {
            if (BudgetBucketDAO.getBudgetBucketById(budgetBucketId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The budget bucket with the specified id is not found"));
            }
            return getJsonSuccessResponse(BudgetBucketDAO.getBudgetBucketLineAsListByBucket(budgetBucketId));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the portfolio entry budget lines of a budget bucket.
     * 
     * @param budgetBucketId
     *            the budget bucket id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the active portfolio entry budget line", notes = "Return list of portfolio entry budget line in the system",
            response = PortfolioEntryBudgetLine.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getBudgetLinesOfBudgetBucketsList(@ApiParam(value = "budgetBucketId", required = true) @PathParam("id") Long budgetBucketId) {
        try {
            if (BudgetBucketDAO.getBudgetBucketById(budgetBucketId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The budget bucket with the specified id is not found"));
            }
            return getJsonSuccessResponse(PortfolioEntryBudgetDAO.getPEBudgetLineAsListByBucket(budgetBucketId));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }
}
