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
import framework.services.account.AccountManagementException;
import framework.services.account.IUserAccount;
import framework.utils.Utilities;
import models.reporting.Reporting;
import play.Logger;
import security.ISecurityService;

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
     * @param securityService
     *            the security service
     */
    public static ExpressionList<Reporting> getReportsViewAllowedAsQuery(Expression expression, OrderBy<Reporting> orderBy, ISecurityService securityService) throws AccountManagementException {

        IUserAccount userAccount = securityService.getCurrentUser();

        String raw = "(";

        // user has permission REPORTING_VIEW_ALL_PERMISSION
        // OR
        if (securityService.restrict( IMafConstants.REPORTING_VIEW_ALL_PERMISSION,userAccount)) {
            raw += "1 = '1' OR ";
        }

        // user has permission
        // REPORTING_VIEW_AS_VIEWER_PERMISSION AND
        // (the report is public OR the user has access to it)
        if (securityService.restrict( IMafConstants.REPORTING_VIEW_AS_VIEWER_PERMISSION,userAccount)) {
            raw += "(isPublic = 1 OR reportingAuthorization.principals.uid='" + userAccount.getIdentifier() + "') OR ";
        }

        raw += "1 = '0')";

        ExpressionList<Reporting> expressionList;

        if (orderBy != null) {
            expressionList = Reporting.find.where();
            Utilities.updateExpressionListWithOrderBy(orderBy, expressionList);
        } else {
            expressionList = Reporting.find.where();
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
     * @param securityService
     *            the security service
     */
    public static boolean isReportViewAllowed(Long reportingId, ISecurityService securityService) {
        try {
            return getReportsViewAllowedAsQuery(Expr.eq("id", reportingId), null, securityService).query().setDistinct(true).findRowCount() > 0 ? true : false;
        } catch (AccountManagementException e) {
            Logger.error("ReportingDynamicHelper.isReportViewAllowed: impossible to get the user account");
            Logger.error(e.getMessage());
            return false;
        }
    }

}
