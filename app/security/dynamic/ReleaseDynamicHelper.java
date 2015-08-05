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

import models.delivery.Release;
import models.pmo.Actor;
import play.Logger;
import play.mvc.Http;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.OrderBy;

import constants.IMafConstants;
import dao.delivery.ReleaseDAO;
import dao.pmo.ActorDao;
import framework.security.SecurityUtils;
import framework.services.ServiceManager;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.session.IUserSessionManagerPlugin;
import framework.security.SecurityUtils;
import framework.security.SecurityUtils;
import framework.utils.Utilities;

/**
 * Provides all method to compute the dynamic permissions for a release.
 * 
 * @author Johann Kohler
 * 
 */
public class ReleaseDynamicHelper {

    /**
     * Get the ebean expression list for all authorized releases of the sign-in
     * user. It's possible to filter and sort the list.
     * 
     * @param expression
     *            the ebean filter expression
     * @param orderBy
     *            the ebean order by
     */
    public static ExpressionList<Release> getReleasesViewAllowedAsQuery(Expression expression, OrderBy<Release> orderBy) throws AccountManagementException {

        IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
        IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
        IUserAccount userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionManagerPlugin.getUserSessionId(Http.Context.current()));

        String raw = "(";

        // user has permission RELEASE_VIEW_ALL_PERMISSION
        // OR
        if (SecurityUtils.hasRole(userAccount, IMafConstants.RELEASE_VIEW_ALL_PERMISSION)) {
            raw += "1 = '1' OR ";
        }

        // user has permission RELEASE_VIEW_AS_MANAGER_PERMISSION AND is the
        // manager of the release OR
        Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
        if (actor != null && SecurityUtils.hasRole(userAccount, IMafConstants.RELEASE_VIEW_AS_MANAGER_PERMISSION)) {
            raw += "manager.id = " + actor.id + " OR ";
        }

        raw += "1 = '0')";

        ExpressionList<Release> expressionList;

        if (orderBy != null) {
            expressionList = ReleaseDAO.findRelease.where();
            Utilities.updateExpressionListWithOrderBy(orderBy, expressionList);
        } else {
            expressionList = ReleaseDAO.findRelease.where();
        }

        expressionList = expressionList.eq("deleted", false);

        if (expression != null) {
            return expressionList.add(expression).raw(raw);
        } else {
            return expressionList.raw(raw);
        }

    }

    /**
     * Define if a user can view the details of a release.
     * 
     * @param releaseId
     *            the release id
     */
    public static boolean isReleaseViewAllowed(Long releaseId) {
        try {
            return getReleasesViewAllowedAsQuery(Expr.eq("id", releaseId), null).findRowCount() > 0 ? true : false;
        } catch (AccountManagementException e) {
            Logger.error("ReleaseDynamicHelper.isReleaseViewAllowed: impossible to get the user account");
            Logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Define if a user can edit the details of a release.
     * 
     * @param release
     *            the release to edit
     */
    public static boolean isReleaseEditAllowed(Release release) {

        try {
            IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            IUserAccount userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionManagerPlugin.getUserSessionId(Http.Context.current()));

            // user has permission RELEASE_EDIT_ALL_PERMISSION OR
            if (SecurityUtils.hasRole(userAccount, IMafConstants.RELEASE_EDIT_ALL_PERMISSION)) {
                return true;
            }

            // user has permission RELEASE_EDIT_AS_MANAGER_PERMISSION AND is
            // manager of the release
            Actor actor = ActorDao.getActorByUid(userAccount.getIdentifier());
            if (actor != null && SecurityUtils.hasRole(userAccount, IMafConstants.RELEASE_EDIT_AS_MANAGER_PERMISSION) && actor.id.equals(release.manager.id)) {
                return true;
            }

        } catch (Exception e) {

            Logger.error("impossible to get the user account", e);

        }

        return false;

    }

}
