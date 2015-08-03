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

import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.sql.ActorHierarchy;
import play.Logger;
import play.mvc.Http;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.OrderBy;

import constants.IMafConstants;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import framework.security.DeadboltUtils;
import framework.services.ServiceManager;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.session.IUserSessionManagerPlugin;

/**
 * Provides all method to compute the dynamic permissions for an org unit.
 * 
 * @author Johann Kohler
 * 
 */
public class OrgUnitDynamicHelper {

    /**
     * Get the ebean expression list for all authorized org units of the sign-in
     * user. It's possible to filter and sort the list.
     * 
     * @param expression
     *            the ebean filter expression
     * @param orderBy
     *            the ebean order by
     */
    public static ExpressionList<OrgUnit> getOrgUnitsViewAllowedAsQuery(Expression expression, OrderBy<OrgUnit> orderBy) throws AccountManagementException {

        IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
        IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
        IUserAccount userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionManagerPlugin.getUserSessionId(Http.Context.current()));

        String raw = "(";

        // user has permission ORG_UNIT_VIEW_ALL_PERMISSION
        // OR
        if (DeadboltUtils.hasRole(userAccount, IMafConstants.ORG_UNIT_VIEW_ALL_PERMISSION)) {
            raw += "1 = '1' OR ";
        }

        // user has permission ORG_UNIT_VIEW_AS_RESPONSIBLE_PERMISSION AND
        // user or his subordinates is manager of the orgUnit OR
        Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
        if (actor != null && DeadboltUtils.hasRole(userAccount, IMafConstants.ORG_UNIT_VIEW_AS_RESPONSIBLE_PERMISSION)) {

            raw += "manager.id = " + actor.id + " OR ";

            String subordinatesString = ActorHierarchy.getSubordinatesAsString(actor.id, ",");
            if (subordinatesString != null && !subordinatesString.trim().isEmpty()) {
                raw += "manager.id IN (" + subordinatesString + ") OR ";
            }

        }

        raw += "1 = '0')";

        ExpressionList<OrgUnit> expressionList;

        if (orderBy != null) {
            expressionList = OrgUnitDao.findOrgUnit.setOrderBy(orderBy).where();
        } else {
            expressionList = OrgUnitDao.findOrgUnit.where();
        }

        expressionList = expressionList.eq("deleted", false);

        if (expression != null) {
            return expressionList.add(expression).raw(raw);
        } else {
            return expressionList.raw(raw);
        }

    }

    /**
     * Define if a user can view the details of an org unit.
     * 
     * @param orgUnitId
     *            the org unit id
     */
    public static boolean isOrgUnitViewAllowed(Long orgUnitId) {
        try {
            return getOrgUnitsViewAllowedAsQuery(Expr.eq("id", orgUnitId), null).findRowCount() > 0 ? true : false;
        } catch (AccountManagementException e) {
            Logger.error("OrgUnitDynamicHelper.isOrgUnitViewAllowed: impossible to get the user account");
            Logger.error(e.getMessage());
            return false;
        }
    }

}
