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
import dao.pmo.PortfolioEntryDao;
import dao.pmo.StakeholderDao;
import framework.security.ISecurityService;
import framework.services.account.AccountManagementException;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.account.IUserAccount;
import framework.utils.Utilities;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;
import play.Logger;

/**
 * Provides all method to compute the dynamic permissions for a portfolio entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryDynamicHelper {

    /**
     * Get the ebean expression list for all authorized portfolio entries of the
     * sign-in user.
     * @param securityService
     *            the security service
     */
    public static ExpressionList<PortfolioEntry> getPortfolioEntriesViewAllowedAsQuery(ISecurityService securityService) throws AccountManagementException {
        return getPortfolioEntriesViewAllowedAsQuery(null, null, securityService);
    }

    /**
     * Get the ebean expression list for all authorized portfolio entries of the
     * sign-in user. It's possible to filter the list with an ebean expression.
     * 
     * @param expression
     *            the ebean filter expression
     * @param securityService
     *            the security service
     */
    public static ExpressionList<PortfolioEntry> getPortfolioEntriesViewAllowedAsQuery(Expression expression, ISecurityService securityService) throws AccountManagementException {
        return getPortfolioEntriesViewAllowedAsQuery(expression, null, securityService);
    }

    /**
     * Get the ebean expression list for all authorized portfolio entries of the
     * sign-in user. It's possible to sort the list with an ebean order by.
     * 
     * @param orderBy
     *            the ebean order by
     * @param securityService
     *            the security service
     */
    public static ExpressionList<PortfolioEntry> getPortfolioEntriesViewAllowedAsQuery(OrderBy<PortfolioEntry> orderBy, ISecurityService securityService) throws AccountManagementException {
        return getPortfolioEntriesViewAllowedAsQuery(null, orderBy, securityService);
    }

    /**
     * Get the ebean expression list for all authorized portfolio entries of the
     * sign-in user. It's possible to filter and sort the list.
     * 
     * @param expression
     *            the ebean filter expression
     * @param orderBy
     *            the ebean order by
     * @param securityService
     *            the security service
     */
    public static ExpressionList<PortfolioEntry> getPortfolioEntriesViewAllowedAsQuery(Expression expression, OrderBy<PortfolioEntry> orderBy, ISecurityService securityService)
            throws AccountManagementException {

        IUserAccount userAccount = securityService.getCurrentUser();

        String raw = "(";

        // user has permission PORTFOLIO_ENTRY_VIEW_DETAILS_ALL_PERMISSION
        // OR
        if (securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_VIEW_DETAILS_ALL_PERMISSION, userAccount)) {
            raw += "1 = '1' OR ";
        }

        // user has permission PORTFOLIO_ENTRY_VIEW_PUBLIC_PERMISSION
        // AND portfolioEntry is public AND portfolioEntry is not a concept
        // OR
        if (securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_VIEW_PUBLIC_PERMISSION, userAccount)) {
            raw += "(isPublic=true AND activeLifeCycleInstance.isConcept=false) OR ";
        }

        Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
        if (actor != null) {

            // user has permission
            // PORTFOLIO_ENTRY_VIEW_DETAILS_AS_MANAGER_PERMISSION AND
            // user is manager of the portfolioEntry OR
            if (securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_VIEW_DETAILS_AS_MANAGER_PERMISSION, userAccount)) {
                raw += "manager.id=" + actor.id + " OR ";
            }

            // user has permission
            // PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION AND
            // user is direct stakeholder of the portfolioEntry
            if (securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION, userAccount)) {
                raw += "(stakeholders.deleted=false AND stakeholders.actor.id=" + actor.id + ") OR ";
            }

            // user has permission
            // PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION AND
            // user is stakeholder of a portfolio of the portfolioEntry
            if (securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION, userAccount)) {
                raw += "(portfolios.deleted=false AND portfolios.stakeholders.deleted=false AND portfolios.stakeholders.actor.id=" + actor.id + ") OR ";
            }

            // user has permission
            // PORTFOLIO_ENTRY_VIEW_DETAILS_AS_PORTFOLIO_MANAGER_PERMISSION
            // AND
            // user
            // is portfolio manager of the portfolioEntry
            if (securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_VIEW_DETAILS_AS_PORTFOLIO_MANAGER_PERMISSION, userAccount)) {
                raw += "(portfolios.deleted=false AND portfolios.manager.id=" + actor.id + ") OR ";
            }

        }

        raw += "1 = '0')";

        ExpressionList<PortfolioEntry> expressionList;

        if (orderBy != null) {
            expressionList = PortfolioEntryDao.findPortfolioEntry.where();
            Utilities.updateExpressionListWithOrderBy(orderBy, expressionList);
        } else {
            expressionList = PortfolioEntryDao.findPortfolioEntry.where();
        }

        expressionList = expressionList.eq("deleted", false);

        if (expression != null) {
            return expressionList.add(expression).raw(raw);
        } else {
            return expressionList.raw(raw);
        }

    }

    /**
     * Define if a user can view the main information of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param securityService
     *            the security service
     */
    public static boolean isPortfolioEntryViewAllowed(Long portfolioEntryId, ISecurityService securityService) {
        try {
            return getPortfolioEntriesViewAllowedAsQuery(Expr.eq("id", portfolioEntryId), securityService).findRowCount() > 0 ? true : false;
        } catch (AccountManagementException e) {
            Logger.error("DefaultDynamicResourceHandler.isPortfolioEntryViewAllowed: impossible to get the user account");
            Logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Define if a user can view the details (governance, risk...) of a
     * portfolio entry.
     * 
     * @param portfolioEntry
     *            the portfolio entry
     * @param securityService
     *            the security service
     */
    public static boolean isPortfolioEntryDetailsAllowed(PortfolioEntry portfolioEntry, ISecurityService securityService) {

        try {
            IUserAccount userAccount = securityService.getCurrentUser();

            // user has permission PORTFOLIO_ENTRY_VIEW_DETAILS_ALL_PERMISSION
            // OR
            if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_VIEW_DETAILS_ALL_PERMISSION, userAccount)) {
                Logger.debug("has PORTFOLIO_ENTRY_VIEW_DETAILS_ALL_PERMISSION");
                return true;
            }

            Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
            if (actor != null) {

                // user has permission
                // PORTFOLIO_ENTRY_VIEW_DETAILS_AS_MANAGER_PERMISSION AND user
                // is manager of the portfolioEntry OR
                if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_VIEW_DETAILS_AS_MANAGER_PERMISSION, userAccount)
                        && actor.id.equals(portfolioEntry.manager.id)) {
                    Logger.debug("has PORTFOLIO_ENTRY_VIEW_DETAILS_AS_MANAGER_PERMISSION and is manager");
                    return true;
                }

                // user has permission
                // PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION AND
                // user is direct stakeholder of the portfolioEntry
                if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION, userAccount)
                        && StakeholderDao.isStakeholderOfPE(actor.id, portfolioEntry.id)) {
                    Logger.debug("has PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION and is stakeholder");
                    return true;
                }

                // user has permission
                // PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION AND
                // user is stakeholder of a portfolio of the portfolioEntry
                if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION, userAccount)
                        && PortfolioEntryDao.isPortfolioStakeholderOfPE(actor.id, portfolioEntry.id)) {
                    Logger.debug("has PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION and is portfolio stakeholder");
                    return true;
                }

                // user has permission
                // PORTFOLIO_ENTRY_VIEW_DETAILS_AS_PORTFOLIO_MANAGER_PERMISSION
                // AND user is portfolio manager of the portfolioEntry
                if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_VIEW_DETAILS_AS_PORTFOLIO_MANAGER_PERMISSION, userAccount)
                        && PortfolioEntryDao.isPortfolioManagerOfPE(actor.id, portfolioEntry.id)) {
                    Logger.debug("has PORTFOLIO_ENTRY_VIEW_DETAILS_AS_PORTFOLIO_MANAGER_PERMISSION and is portfolio manager");
                    return true;
                }

            }

        } catch (Exception e) {
            Logger.error("impossible to get the user account", e);
        }

        return false;

    }

    /**
     * Define if a user can edit all information and details of a portfolio
     * entry.
     * 
     * @param portfolioEntry
     *            the portfolio entry
     * @param securityService
     *            the security service
     * @param preferenceManagerPlugin
     *            the plugin which is managing the user and system preferences
     */
    public static boolean isPortfolioEntryEditAllowed(PortfolioEntry portfolioEntry, ISecurityService securityService, IPreferenceManagerPlugin preferenceManagerPlugin) {

        try {
            IUserAccount userAccount = securityService.getCurrentUser();

            boolean canManageArchived = preferenceManagerPlugin.getPreferenceValueAsBoolean(
                    IMafConstants.LICENSE_CAN_MANAGE_ARCHIVED_PORTFOLIO_ENTRY_PREFERENCE);

            // the preference
            // LICENSE_CAN_MANAGE_ARCHIVED_PORTFOLIO_ENTRY_PREFERENCE is false
            // and the portfolio entry is archived => this is not possible to
            // edit it
            if (!canManageArchived && portfolioEntry.archived) {
                return false;
            }

            // user has permission PORTFOLIO_ENTRY_EDIT_ALL_PERMISSION OR
            if (securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_EDIT_ALL_PERMISSION, userAccount)) {
                Logger.debug("has PORTFOLIO_ENTRY_EDIT_ALL_PERMISSION");
                return true;
            }

            Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
            if (actor != null) {

                // user has permission
                // PORTFOLIO_ENTRY_EDIT_AS_MANAGER_PERMISSION
                // AND is
                // manager of the portfolioEntry
                if (securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_EDIT_AS_MANAGER_PERMISSION, userAccount) && actor.id.equals(portfolioEntry.manager.id)) {
                    Logger.debug("has PORTFOLIO_ENTRY_EDIT_AS_MANAGER_PERMISSION and is manager");
                    return true;
                }

                // user has permission
                // PORTFOLIO_ENTRY_EDIT_AS_PORTFOLIO_MANAGER_PERMISSION AND user
                // is portfolio manager of the portfolioEntry
                if (securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_EDIT_AS_PORTFOLIO_MANAGER_PERMISSION, userAccount)
                        && PortfolioEntryDao.isPortfolioManagerOfPE(actor.id, portfolioEntry.id)) {
                    Logger.debug("has PORTFOLIO_ENTRY_EDIT_AS_PORTFOLIO_MANAGER_PERMISSION and is portfolio manager");
                    return true;
                }

            }

        } catch (Exception e) {
            Logger.error("impossible to get the user account", e);
        }

        return false;

    }

    /**
     * Define if a user can delete a portfolio entry.
     * 
     * @param portfolioEntry
     *            the portfolio entry
     * @param securityService
     *            the security service
     */
    public static boolean isPortfolioEntryDeleteAllowed(PortfolioEntry portfolioEntry, ISecurityService securityService) {

        try {
            IUserAccount userAccount = securityService.getCurrentUser();

            // user has permission PORTFOLIO_ENTRY_DELETE_ALL_PERMISSION
            if (securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_DELETE_ALL_PERMISSION, userAccount)) {
                Logger.debug("has PORTFOLIO_ENTRY_DELETE_ALL_PERMISSION");
                return true;
            }
        } catch (Exception e) {
            Logger.error("impossible to get the user account", e);
        }

        return false;

    }

    /**
     * Define if a user can view the financial part of a portfolio entry.
     * 
     * @param portfolioEntry
     *            the portfolio entry
     * @param securityService
     *            the security service
     */
    public static boolean isPortfolioEntryViewFinancialAllowed(PortfolioEntry portfolioEntry, ISecurityService securityService) {

        try {
            IUserAccount userAccount = securityService.getCurrentUser();

            // user has permission
            // PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_ALL_PERMISSION
            // OR
            if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_ALL_PERMISSION, userAccount)) {
                Logger.debug("has PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_ALL_PERMISSION");
                return true;
            }

            Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
            if (actor != null) {

                // user has permission
                // PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_MANAGER_PERMISSION
                // AND is manager of the portfolioEntry
                if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_MANAGER_PERMISSION, userAccount)
                        && actor.id.equals(portfolioEntry.manager.id)) {
                    Logger.debug("has PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_MANAGER_PERMISSION and is manager");
                    return true;
                }

                // user has permission
                // PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_STAKEHOLDER_PERMISSION
                // AND
                // user is direct stakeholder of the portfolioEntry
                if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_STAKEHOLDER_PERMISSION, userAccount)
                        && StakeholderDao.isStakeholderOfPE(actor.id, portfolioEntry.id)) {
                    Logger.debug("has PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_STAKEHOLDER_PERMISSION and is stakeholder");
                    return true;
                }

                // user has permission
                // PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_STAKEHOLDER_PERMISSION
                // AND
                // user is stakeholder of a portfolio of the
                // portfolioEntry
                if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_STAKEHOLDER_PERMISSION, userAccount)
                        && PortfolioEntryDao.isPortfolioStakeholderOfPE(actor.id, portfolioEntry.id)) {
                    Logger.debug("has PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_STAKEHOLDER_PERMISSION and is portfolio stakeholder");
                    return true;
                }

                // user has permission
                // PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_PORTFOLIO_MANAGER_PERMISSION
                // AND is portfolio manager of the portfolioEntry
                if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_PORTFOLIO_MANAGER_PERMISSION, userAccount)
                        && PortfolioEntryDao.isPortfolioManagerOfPE(actor.id, portfolioEntry.id)) {
                    Logger.debug("has PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_PORTFOLIO_MANAGER_PERMISSION and is portfolio manager");
                    return true;
                }

            }
        } catch (Exception e) {
            Logger.error("impossible to get the user account", e);
        }

        return false;

    }

    /**
     * Define if a user can edit the financial part of a portfolio entry.
     * 
     * @param portfolioEntry
     *            the portfolio entry
     * @param securityService
     *            the security service
     */
    public static boolean isPortfolioEntryEditFinancialAllowed(PortfolioEntry portfolioEntry, ISecurityService securityService) {

        try {
            IUserAccount userAccount = securityService.getCurrentUser();

            // user has permission
            // PORTFOLIO_ENTRY_EDIT_FINANCIAL_INFO_ALL_PERMISSION
            // OR
            if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_EDIT_FINANCIAL_INFO_ALL_PERMISSION,userAccount)) {
                Logger.debug("has PORTFOLIO_ENTRY_EDIT_FINANCIAL_INFO_ALL_PERMISSION");
                return true;
            }

            // user has permission
            // PORTFOLIO_ENTRY_EDIT_FINANCIAL_INFO_AS_MANAGER_PERMISSION
            // AND is manager of the portfolioEntry
            Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
            if (actor != null && securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_EDIT_FINANCIAL_INFO_AS_MANAGER_PERMISSION,userAccount)
                    && actor.id.equals(portfolioEntry.manager.id)) {
                Logger.debug("has PORTFOLIO_ENTRY_EDIT_FINANCIAL_INFO_AS_MANAGER_PERMISSION and is manager");
                return true;
            }

        } catch (Exception e) {
            Logger.error("impossible to get the user account", e);
        }

        return false;

    }

    /**
     * Define if a user can display and review a request for a portfolio entry.
     * 
     * @param portfolioEntry
     *            the portfolio entry
     * @param securityService
     *            the security service
     */
    public static boolean isPortfolioEntryReviewRequestAllowed(PortfolioEntry portfolioEntry, ISecurityService securityService) {

        try {
            IUserAccount userAccount = securityService.getCurrentUser();

            // user has permission PORTFOLIO_ENTRY_REVIEW_REQUEST_ALL_PERMISSION
            // OR
            if (securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_ALL_PERMISSION,userAccount)) {
                Logger.debug("has PORTFOLIO_ENTRY_REVIEW_REQUEST_ALL_PERMISSION");
                return true;
            }

            // user has permission
            // PORTFOLIO_ENTRY_REVIEW_REQUEST_AS_PORTFOLIO_MANAGER_PERMISSION
            // AND user is portfolio manager of the portfolioEntry
            Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
            if (actor != null && securityService.restrict( IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_AS_PORTFOLIO_MANAGER_PERMISSION,userAccount)
                    && PortfolioEntryDao.isPortfolioManagerOfPE(actor.id, portfolioEntry.id)) {
                Logger.debug("has PORTFOLIO_ENTRY_REVIEW_REQUEST_AS_PORTFOLIO_MANAGER_PERMISSION and is portfolio manager");
                return true;
            }

        } catch (Exception e) {
            Logger.error("impossible to get the user account", e);
        }

        return false;

    }

}
