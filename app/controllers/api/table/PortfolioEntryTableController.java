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

import com.avaje.ebean.ExpressionList;
import com.wordnik.swagger.annotations.*;
import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.finance.PortfolioEntryBudgetDAO;
import dao.pmo.PortfolioEntryDao;
import framework.security.ISecurityService;
import framework.services.account.AccountManagementException;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.finance.PortfolioEntryBudgetLine;
import models.pmo.PortfolioEntry;
import play.mvc.Result;
import security.dynamic.PortfolioEntryDynamicHelper;
import utils.datatable.PortfolioEntryDTO;
import utils.table.PortfolioEntryBudgetLineListView;
import utils.table.PortfolioEntryListView;

import javax.inject.Inject;
import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The API controller for Portfolio entry related tables
 *
 * @author Guillaume Petit
 */
@Api(value = "/api/table/portfolio-entry", description = "Data source for portfolio entries tables")
public class PortfolioEntryTableController extends ApiController {

    @Inject
    private ISecurityService securityService;

    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get Portfolio Entries", notes = "Return a view of Portfolio Entries", response = PortfolioEntryDTO.class, httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 403, message = "unauthorized"),
            @ApiResponse(code = 500, message = "error", response = ApiError.class)
    })
    public Result getPortfolioEntries() {
        try {
            ExpressionList<PortfolioEntry> portfolioEntryExpressionList = PortfolioEntryDynamicHelper.getPortfolioEntriesViewAllowedAsQuery(getSecurityService());
            List<PortfolioEntryDTO> portfolioEntryListView = portfolioEntryExpressionList.findList().stream().map(PortfolioEntryDTO::new).collect(Collectors.toList());
            return getJsonSuccessResponse(portfolioEntryListView);
        } catch (AccountManagementException e) {
            return getJsonErrorResponse(new ApiError(403, "You are not authorized to query portfolio entries"));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get Portfolio Entry budget lines", notes = "Return a view of Portfolio Entry budget lines", response = PortfolioEntryBudgetLineListView.class, httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class)
    })
    public Result getPortfolioEntryBudgetLines(
            @ApiParam(value = "portfolio entry id", required = true) @PathParam("portfolioEntryId") Long portfolioEntryId
    ) {
        try {
            if (PortfolioEntryDao.getPEById(portfolioEntryId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The portfolio entry with the specified id is not found"));
            }
            List<PortfolioEntryBudgetLine> budgetLines = PortfolioEntryBudgetDAO.getPEBudgetLineAsListByPE(portfolioEntryId);
            List<PortfolioEntryBudgetLineListView> listView = budgetLines.stream().map(PortfolioEntryBudgetLineListView::new).collect(Collectors.toList());
            return getJsonSuccessResponse(listView);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    public ISecurityService getSecurityService() {
        return securityService;
    }
}