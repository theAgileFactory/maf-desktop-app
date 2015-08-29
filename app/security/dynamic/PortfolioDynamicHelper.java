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
package security.dynamic;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.OrderBy;

import constants.IMafConstants;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioDao;
import framework.security.ISecurityService;
import framework.services.account.AccountManagementException;
import framework.services.account.IUserAccount;
import framework.utils.Utilities;
import models.pmo.Actor;
import models.pmo.Portfolio;
import play.Logger;

/**
 * Provides all method to compute the dynamic permissions for a portfolio.
 * 
 * @author Johann Kohler
 * 
 */
public class PortfolioDynamicHelper {

    /**
     * Get the ebean expression list for all authorized portfolios of the
     * sign-in user. It's possible to filter and sort the list.
     * 
     * @param expression
     *            the ebean filter expression
     * @param orderBy
     *            the ebean order by
     * @param securityService
     *            the security service
     */
    public static ExpressionList<Portfolio> getPortfoliosViewAllowedAsQuery(Expression expression, OrderBy<Portfolio> orderBy, ISecurityService securityService)
            throws AccountManagementException {

        IUserAccount userAccount = securityService.getCurrentUser();

        String raw = "(";

        // user has permission PORTFOLIO_VIEW_DETAILS_ALL_PERMISSION
        // OR
        if (securityService.restrict(IMafConstants.PORTFOLIO_VIEW_DETAILS_ALL_PERMISSION, userAccount)) {
            raw += "1 = '1' OR ";
        }

        Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
        if (actor != null) {

            // user has permission
            // PORTFOLIO_VIEW_DETAILS_AS_MANAGER_PERMISSION AND
            // user is manager of the portfolio OR
            if (securityService.restrict(IMafConstants.PORTFOLIO_VIEW_DETAILS_AS_MANAGER_PERMISSION, userAccount)) {
                raw += "manager.id=" + actor.id + " OR ";
            }

            // user has permission
            // PORTFOLIO_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION AND
            // user is direct stakeholder of the portfolio
            if (securityService.restrict(IMafConstants.PORTFOLIO_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION, userAccount)) {
                raw += "(stakeholders.deleted=false AND stakeholders.actor.id=" + actor.id + ") OR ";
            }

        }

        raw += "1 = '0')";

        ExpressionList<Portfolio> expressionList;

        if (orderBy != null) {
            expressionList = PortfolioDao.findPortfolio.where();
            Utilities.updateExpressionListWithOrderBy(orderBy, expressionList);
        } else {
            expressionList = PortfolioDao.findPortfolio.where();
        }

        expressionList = expressionList.eq("deleted", false);

        if (expression != null) {
            return expressionList.add(expression).raw(raw);
        } else {
            return expressionList.raw(raw);
        }

    }

    /**
     * Define if a user can view the details of a portfolio.
     * 
     * @param portfolioId
     *            the portfolio id
     * @param securityService
     *            the security service
     */
    public static boolean isPortfolioViewAllowed(Long portfolioId, ISecurityService securityService) {
        try {
            return getPortfoliosViewAllowedAsQuery(Expr.eq("id", portfolioId), null, securityService).findRowCount() > 0 ? true : false;
        } catch (AccountManagementException e) {
            Logger.error("PortfolioDynamicHelper.isPortfolioViewAllowed: impossible to get the user account");
            Logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Define if a user can edit a portfolio.
     * 
     * @param portfolio
     *            the portfolio
     * @param securityService
     *            the security service
     */
    public static boolean isPortfolioEditAllowed(Portfolio portfolio, ISecurityService securityService) {

        try {
            IUserAccount userAccount = securityService.getCurrentUser();

            // user has permission PORTFOLIO_EDIT_ALL_PERMISSION OR
            if (securityService.restrict(IMafConstants.PORTFOLIO_EDIT_ALL_PERMISSION, userAccount)) {
                return true;
            }

            // user has permission
            // PORTFOLIO_EDIT_AS_PORTFOLIO_MANAGER_PERMISSION
            // AND user is manager of the portfolio
            Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
            if (actor != null && securityService.restrict(IMafConstants.PORTFOLIO_EDIT_AS_PORTFOLIO_MANAGER_PERMISSION, userAccount)
                    && actor.id.equals(portfolio.manager.id)) {
                return true;
            }

        } catch (Exception e) {
            Logger.error("impossible to get the user account", e);
        }

        return false;
    }

    /**
     * Define if a user can view the financial part of a portfolio.
     * 
     * @param portfolio
     *            the portfolio
     * @param securityService
     *            the security service
     */
    public static boolean isPortfolioViewFinancialAllowed(Portfolio portfolio, ISecurityService securityService) {

        try {
            IUserAccount userAccount = securityService.getCurrentUser();

            // user has permission
            // PORTFOLIO_VIEW_FINANCIAL_INFO_ALL_PERMISSION
            // OR
            if (securityService.restrict(IMafConstants.PORTFOLIO_VIEW_FINANCIAL_INFO_ALL_PERMISSION, userAccount)) {
                return true;
            }

            // user has permission
            // PORTFOLIO_VIEW_FINANCIAL_INFO_AS_MANAGER_PERMISSION
            // AND is manager of the portfolio
            Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
            if (actor != null && securityService.restrict(IMafConstants.PORTFOLIO_VIEW_FINANCIAL_INFO_AS_MANAGER_PERMISSION, userAccount)
                    && actor.id.equals(portfolio.manager.id)) {
                return true;
            }
        } catch (Exception e) {
            Logger.error("impossible to get the user account", e);
        }

        return false;

    }
}
