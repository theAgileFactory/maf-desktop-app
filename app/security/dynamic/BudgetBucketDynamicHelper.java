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

import models.finance.BudgetBucket;
import models.pmo.Actor;
import models.sql.ActorHierarchy;
import play.Logger;
import play.mvc.Http;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.OrderBy;

import constants.IMafConstants;
import dao.finance.BudgetBucketDAO;
import dao.pmo.ActorDao;
import framework.security.DeadboltUtils;
import framework.services.ServiceManager;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.session.IUserSessionManagerPlugin;

/**
 * Provides all method to compute the dynamic permissions for a budget bucket.
 * 
 * @author Johann Kohler
 * 
 */
public class BudgetBucketDynamicHelper {

    /**
     * Get the ebean expression list for all authorized budget buckets of the
     * sign-in user. It's possible to filter and sort the list.
     * 
     * @param expression
     *            the ebean filter expression
     * @param orderBy
     *            the ebean order by
     */
    public static ExpressionList<BudgetBucket> getBudgetBucketsViewAllowedAsQuery(Expression expression, OrderBy<BudgetBucket> orderBy)
            throws AccountManagementException {

        IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
        IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
        IUserAccount userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionManagerPlugin.getUserSessionId(Http.Context.current()));

        String raw = "(";

        // user has permission BUDGET_BUCKET_VIEW_ALL_PERMISSION
        // OR
        if (DeadboltUtils.hasRole(userAccount, IMafConstants.BUDGET_BUCKET_VIEW_ALL_PERMISSION)) {
            raw += "1 = '1' OR ";
        }

        // user has permission BUDGET_BUCKET_VIEW_AS_OWNER_PERMISSION AND
        // user or his subordinates is owner of the budgetBucket OR
        Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
        if (actor != null && DeadboltUtils.hasRole(userAccount, IMafConstants.BUDGET_BUCKET_VIEW_AS_OWNER_PERMISSION)) {

            raw += "owner.id = " + actor.id + " OR ";

            String subordinatesString = ActorHierarchy.getSubordinatesAsString(actor.id, ",");
            if (subordinatesString != null && !subordinatesString.trim().isEmpty()) {
                raw += "owner.id IN (" + subordinatesString + ") OR ";
            }

        }

        raw += "1 = '0')";

        ExpressionList<BudgetBucket> expressionList;

        if (orderBy != null) {
            expressionList = BudgetBucketDAO.findBudgetBucket.setOrderBy(orderBy).where();
        } else {
            expressionList = BudgetBucketDAO.findBudgetBucket.where();
        }

        expressionList = expressionList.eq("deleted", false);

        if (expression != null) {
            return expressionList.add(expression).raw(raw);
        } else {
            return expressionList.raw(raw);
        }

    }

    /**
     * Define if a user can view the details of a budget bucket.
     * 
     * @param budgetBucketId
     *            the budget bucket id
     */
    public static boolean isBudgetBucketViewAllowed(Long budgetBucketId) {
        try {
            return getBudgetBucketsViewAllowedAsQuery(Expr.eq("id", budgetBucketId), null).findRowCount() > 0 ? true : false;
        } catch (AccountManagementException e) {
            Logger.error("BudgetBucketDynamicHelper.isBudgetBucketViewAllowed: impossible to get the user account");
            Logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Define if a user can edit the details of a budget bucket.
     * 
     * @param budgetBucket
     *            the budget bucket to edit
     */
    public static boolean isBudgetBucketEditAllowed(BudgetBucket budgetBucket) {

        try {
            IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            IUserAccount userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionManagerPlugin.getUserSessionId(Http.Context.current()));

            // user has permission BUDGET_BUCKET_EDIT_ALL_PERMISSION OR
            if (DeadboltUtils.hasRole(userAccount, IMafConstants.BUDGET_BUCKET_EDIT_ALL_PERMISSION)) {
                Logger.debug("has BUDGET_BUCKET_EDIT_ALL_PERMISSION");
                return true;
            }

            // user has permission BUDGET_BUCKET_EDIT_AS_OWNER_PERMISSION
            // AND is owner or responsible of the budget bucket
            Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
            if (actor != null && DeadboltUtils.hasRole(userAccount, IMafConstants.BUDGET_BUCKET_EDIT_AS_OWNER_PERMISSION)
                    && (actor.id.equals(budgetBucket.owner.id) || ActorHierarchy.getSubordinatesAsId(actor.id).contains(budgetBucket.owner.id))) {

                Logger.debug("has BUDGET_BUCKET_EDIT_AS_OWNER_PERMISSION and is owner or responsible");
                return true;

            }

        } catch (Exception e) {
            Logger.error("impossible to get the user account", e);
        }

        return false;

    }

}
