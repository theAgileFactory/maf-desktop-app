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
package security;

import play.mvc.Http;
import play.mvc.Result;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import controllers.sso.AuthenticationConfigurationUtils;
import framework.security.CommonDeadboltHandler;
import framework.services.ServiceManager;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Msg;

/**
 * The handler for the authorization mechanism based on Deadbold.<br/>
 * The class also holds a set of static methods which are to manage the user
 * session (basically create, get or delete the session entry containing the
 * unique user id)
 * 
 * @author Pierre-Yves Cloux
 */
public class DefaultDeadboltHandler extends CommonDeadboltHandler {

    @Override
    public Result redirectToLoginPage(String redirectUrl) {
        return AuthenticationConfigurationUtils.redirectToLoginPage(redirectUrl);
    }

    @Override
    public Result displayAccessForbidden() {
        return badRequest(views.html.error.access_forbidden.render(Msg.get("forbidden.access.title")));
    }

    @Override
    public DynamicResourceHandler getDynamicResourceHandler(Http.Context context) {
        return new DefaultDynamicResourceHandler();
    }

    /**
     * Return true if the sign-in user is allowed for the given permission.
     * 
     * @param permission
     *            the permission
     */
    public static boolean isAllowed(String permission) {
        try {
            IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            IUserAccount userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionManagerPlugin.getUserSessionId(Http.Context.current()));
            return userAccount.getSystemPermissionNames().contains(permission);
        } catch (Exception e) {
            return false;
        }
    }
}
