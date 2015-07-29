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

import models.reporting.Reporting;
import play.Logger;
import play.mvc.Http;
import be.objectify.deadbolt.core.DeadboltAnalyzer;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.OrderBy;

import constants.IMafConstants;
import framework.services.ServiceManager;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.session.IUserSessionManagerPlugin;

/**
 * Provides all method to compute the dynamic permissions for a report.
 * 
 * @author Johann Kohler
 * 
 */
public class ReportingDynamicHelper {

    /**
     * get the ebean expression list for all authorized reports of the sign-in
     * user. It's possible to filter and sort the list.
     * 
     * @param expression
     *            the ebean filter expression
     * @param orderBy
     *            the ebean order by
     */
    public static ExpressionList<Reporting> getReportsViewAllowedAsQuery(Expression expression, OrderBy<Reporting> orderBy) throws AccountManagementException {

        IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
        IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
        IUserAccount userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionManagerPlugin.getUserSessionId(Http.Context.current()));

        String raw = "(";

        // user has permission REPORTING_VIEW_ALL_PERMISSION
        // OR
        if (DeadboltAnalyzer.hasRole(userAccount, IMafConstants.REPORTING_VIEW_ALL_PERMISSION)) {
            raw += "1 = '1' OR ";
        }

        // user has permission
        // REPORTING_VIEW_AS_VIEWER_PERMISSION AND
        // (the report is public OR the user has access to it)
        if (DeadboltAnalyzer.hasRole(userAccount, IMafConstants.REPORTING_VIEW_AS_VIEWER_PERMISSION)) {
            raw += "(isPublic = 1 OR reportingAuthorization.principals.uid='" + userAccount.getIdentifier() + "') OR ";
        }

        raw += "1 = '0')";

        ExpressionList<Reporting> expressionList;

        if (orderBy != null) {
            expressionList = Reporting.find.setDistinct(true).setOrderBy(orderBy).where();
        } else {
            expressionList = Reporting.find.setDistinct(true).where();
        }

        expressionList = expressionList.eq("deleted", false);

        if (expression != null) {
            return expressionList.add(expression).raw(raw);
        } else {
            return expressionList.raw(raw);
        }

    }

    /**
     * Define if a user can view the details of a report.
     * 
     * @param reportingId
     *            the report id
     */
    public static boolean isReportViewAllowed(Long reportingId) {
        try {
            return getReportsViewAllowedAsQuery(Expr.eq("id", reportingId), null).findRowCount() > 0 ? true : false;
        } catch (AccountManagementException e) {
            Logger.error("ReportingDynamicHelper.isReportViewAllowed: impossible to get the user account");
            Logger.error(e.getMessage());
            return false;
        }
    }

}
