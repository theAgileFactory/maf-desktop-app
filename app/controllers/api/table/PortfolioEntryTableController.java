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
package controllers.api.table;

import com.wordnik.swagger.annotations.*;
import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.finance.PortfolioEntryBudgetDAO;
import dao.pmo.PortfolioEntryDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.finance.PortfolioEntryBudgetLine;
import play.mvc.Result;
import utils.table.PortfolioEntryBudgetLineListView;

import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.List;

/**
 * The API controller for Portfolio entry related tables
 *
 * @author Guillaume Petit
 */
@Api(value = "/api/table/portfolio-entry", description = "Data source for portfolio entries tables")
public class PortfolioEntryTableController extends ApiController {

    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get Portfolio Entries", notes = "Return a view of Portfolio Entries", response = PortfolioEntryListView.class, httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class),
    })
    public Result getPortfolioEntryListView() {

    }

    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get Portfolio Entry budget lines", notes = "Return a view of Portfolio Entry budget lines", response = PortfolioEntryBudgetLineListView.class, httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class),
    })
    public Result getPortfolioEntryBudgetLineListView(
            @ApiParam(value = "portfolio entry id", required = true) @PathParam("portfolioEntryId") Long portfolioEntryId
    ) {
        try {
            if (PortfolioEntryDao.getPEById(portfolioEntryId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The portfolio entry with the specified id is not found"));
            }
            List<PortfolioEntryBudgetLine> budgetLines = PortfolioEntryBudgetDAO.getPEBudgetLineAsListByPE(portfolioEntryId);
            List<PortfolioEntryBudgetLineListView> listView = new ArrayList<>();
            budgetLines.forEach(budgetLine -> listView.add(new PortfolioEntryBudgetLineListView(budgetLine)));
            return getJsonSuccessResponse(listView);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}