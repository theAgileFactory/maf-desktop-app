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
import dao.timesheet.TimesheetDao;
import framework.services.account.AccountManagementException;
import framework.services.account.IUserAccount;
import framework.utils.Utilities;
import models.pmo.Actor;
import models.sql.ActorHierarchy;
import models.timesheet.TimesheetReport;
import play.Logger;
import security.ISecurityService;

/**
 * Provides all method to compute the dynamic permissions for a timesheet
 * report.
 * 
 * @author Johann Kohler
 * 
 */
public class TimesheetReportDynamicHelper {

    /**
     * Get the ebean expression list for all authorized timesheet report of the
     * sign-in user (meaning the reports he can display and provide approval).
     * It's possible to filter and sort the list.
     * 
     * @param expression
     *            the ebean filter expression
     * @param orderBy
     *            the ebean order by
     * @param securityService
     *            the security service
     */
    public static ExpressionList<TimesheetReport> getTimesheetReportsApprovalAllowedAsQuery(Expression expression, OrderBy<TimesheetReport> orderBy, ISecurityService securityService)
            throws AccountManagementException {

        IUserAccount userAccount = securityService.getCurrentUser();

        String raw = "(";

        // user has permission TIMESHEET_APPROVAL_ALL_PERMISSION
        // OR
        if (securityService.hasRole(userAccount, IMafConstants.TIMESHEET_APPROVAL_ALL_PERMISSION)) {
            raw += "1 = '1' OR ";
        }

        // user has permission
        // TIMESHEET_APPROVAL_AS_MANAGER_PERMISSION AND
        // user or his subordinates is manager of the actor of the report OR
        Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
        if (actor != null && securityService.hasRole(userAccount, IMafConstants.TIMESHEET_APPROVAL_AS_MANAGER_PERMISSION)) {

            raw += "actor.manager.id=" + actor.id + " OR ";

            String subordinatesString = ActorHierarchy.getSubordinatesAsString(actor.id, ",");
            if (subordinatesString != null && !subordinatesString.trim().isEmpty()) {
                raw += "actor.manager.id IN (" + subordinatesString + ") OR ";
            }

        }

        raw += "1 = '0')";

        ExpressionList<TimesheetReport> expressionList;

        if (orderBy != null) {
            expressionList = TimesheetDao.findTimesheetReport.where();
            Utilities.updateExpressionListWithOrderBy(orderBy, expressionList);
        } else {
            expressionList = TimesheetDao.findTimesheetReport.where();
        }

        expressionList = expressionList.eq("deleted", false);

        if (expression != null) {
            return expressionList.add(expression).raw(raw);
        } else {
            return expressionList.raw(raw);
        }

    }

    /**
     * Define if a user can display and provide approval for a timesheet report.
     * 
     * @param timesheetReportId
     *            the timesheet report id
     * @param securityService
     *            the security service
     */
    public static boolean isTimesheetReportApprovalAllowed(Long timesheetReportId, ISecurityService securityService) {
        try {
            return getTimesheetReportsApprovalAllowedAsQuery(Expr.eq("id", timesheetReportId), null,securityService).findRowCount() > 0 ? true : false;
        } catch (AccountManagementException e) {
            Logger.error(e.getMessage());
            return false;
        }
    }
}
