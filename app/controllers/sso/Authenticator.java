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
package controllers.sso;

import org.pac4j.play.java.JavaController;
import org.pac4j.play.java.RequiresAuthentication;

import play.Logger;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import services.licensesmanagement.LicensesManagementServiceImpl;
import services.licensesmanagement.LoginEventRequest.ErrorCode;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import controllers.ControllersUtils;
import framework.commons.IFrameworkConstants;
import framework.services.ServiceManager;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Language;
import framework.utils.Utilities;

/**
 * The controller that deals with the user authentication.<br/>
 * The authenticator can deal with several types of clients:
 * <ul>
 * <li>The "Saml2Client" implementation of PAC4J if BizDock is working in
 * FEDERATED mode</li>
 * <li>The "CasClient" implementation of PAC4J if BizDock is working in CAS mode
 * (MASTER or SLAVE)</li>
 * <li>The "HttpClient" implementation of PAC4J if BizDock is working in
 * standalone mode (no SSO with other plugins)</li>
 * </ul>
 * 
 * @author Pierre-Yves Cloux
 */
public class Authenticator extends JavaController {
    private static Logger.ALogger log = Logger.of(Authenticator.class);

    /**
     * Perform a login, if successful then redirect to the home page.<br/>
     * This redirect to the FEDERATED login page.
     * 
     * @param redirectUrl
     *            the redirect url
     * 
     * @return redirectUrl to the home page or display an error (access to a
     *         protected area)
     */
    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result loginFederated(String redirectUrl) {
        return loginCode(redirectUrl);
    }

    /**
     * Perform a login, if successful then redirect to the home page.<br/>
     * This redirect to the CAS_MASTER or CAS_SLAVE login page.
     * 
     * @param redirectUrl
     *            the redirect url
     * 
     * @return redirectUrl to the home page or display an error (access to a
     *         protected area)
     */
    @RequiresAuthentication(clientName = "CasClient")
    public static Result loginCasMaster(String redirectUrl) {
        return loginCode(redirectUrl);
    }

    /**
     * Perform a login, if successful then redirect to the home page. This
     * redirect to the STANDALONE login page.
     * 
     * @param redirectUrl
     *            the redirect url
     * 
     * @return redirectUrl to the home page or display an error (access to a
     *         protected area)
     */
    @RequiresAuthentication(clientName = "FormClient")
    public static Result loginStandalone(String redirectUrl) {
        return loginCode(redirectUrl);
    }

    /**
     * Redirect the user to the previous saved URL.
     */
    @SubjectPresent
    public static Result redirectToThePreviouslySavedUrl() {

        // event: success login / STANDALONE
        IUserSessionManagerPlugin userSessionPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
        ServiceManager.getService(LicensesManagementServiceImpl.NAME, LicensesManagementServiceImpl.class).addLoginEvent(
                userSessionPlugin.getUserSessionId(ctx()), true, null, null);

        return redirect(AuthenticationConfigurationUtils.getRedirectUrlInSession());
    }

    /**
     * The code executed depending on the login implementation.
     * 
     * @param redirectUrl
     *            the URL to which the user must be redirected after
     *            authentication
     * @return
     */
    private static Result loginCode(String redirectUrl) {

        try {
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            IUserSessionManagerPlugin userSessionPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            IUserAccount userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionPlugin.getUserSessionId(ctx()));

            // User is not found
            if (userAccount == null) {
                return badRequest();
            }

            // event: success login / CAS
            ServiceManager.getService(LicensesManagementServiceImpl.NAME, LicensesManagementServiceImpl.class).addLoginEvent(
                    userSessionPlugin.getUserSessionId(ctx()), true, null, null);

            // get the preferred language as object
            Language language = new Language(userAccount.getPreferredLanguage());

            // verify the language is valid
            if (language.isValid()) {
                Logger.debug("change language to: " + language.getCode());
                ctx().changeLang(language.getCode());
                Utilities.setSsoLanguage(ctx(), language.getCode());
            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
        return redirect(redirectUrl);
    }

    /**
     * Not accessible page (license).
     */
    public static Result notAccessible() {
        return ok(views.html.sso.not_accessible.render());
    }

    /**
     * Clear the user session and logout the user.<br/>
     * The user is then redirected to the login page.
     */
    public static Promise<Result> callback() {

        if (!ServiceManager.getService(LicensesManagementServiceImpl.NAME, LicensesManagementServiceImpl.class).isInstanceAccessible()) {

            // event: not accessible / STANDALONE + CAS
            ServiceManager.getService(LicensesManagementServiceImpl.NAME, LicensesManagementServiceImpl.class).addLoginEvent(null, false,
                    ErrorCode.IS_NOT_ACCESSIBLE, null);

            return Promise.promise(new Function0<Result>() {
                public Result apply() throws Throwable {
                    // redirect to new page
                    return redirect(controllers.sso.routes.Authenticator.notAccessible());
                }
            });

        }

        if (log.isDebugEnabled()) {
            log.debug("Received call back from CAS server : " + ctx().request().toString());
        }
        return org.pac4j.play.CallbackController.callback();
    }

    /**
     * Clear the user session and logout the user.<br/>
     * The user is then redirected to the login page.
     */
    public static Result logout() {
        if (log.isDebugEnabled()) {
            log.debug("Logout requested");
        }
        // Workaround
        // Clear redmine cookie
        Cookie redmineCookie = ctx().request().cookie("_redmine_session");
        if (redmineCookie != null) {
            ctx().response().discardCookie("_redmine_session");
        }
        // Workaround
        return org.pac4j.play.CallbackController.logoutAndRedirect();
    }

    /**
     * The logout page for the federated authentication.
     * 
     * @return
     */
    public static Result logoutFederated() {
        return ok(views.html.sso.federated_logout.render(Utilities.getPreferenceElseConfigurationValue(IFrameworkConstants.PUBLIC_URL_PREFERENCE,
                "maf.public.url")));
    }
}
