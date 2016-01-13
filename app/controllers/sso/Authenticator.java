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

import java.net.MalformedURLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import framework.commons.IFrameworkConstants;
import framework.commons.IFrameworkConstants.AuthenticationMode;
import framework.security.AbstractAuthenticator;
import framework.security.IInstanceAccessSupervisor;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IAuthenticationAccountReaderPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import play.Configuration;
import play.cache.CacheApi;
import play.mvc.Call;
import play.mvc.Result;

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
@Singleton
public class Authenticator extends AbstractAuthenticator {

    /**
     * Create a new Authenticator.
     * 
     * @param configuration
     *            the Play configuration service.
     * @param cache
     *            the Play cache service
     * @param userSessionManagerPlugin
     *            the user session manager service
     * @param accountManagerPlugin
     *            the account manager service
     * @param authenticationAccountReader
     *            the authentication account reader service
     * @param instanceAccessSupervisor
     *            the instance access supervisor
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param i18nMessagesPlugin
     *            the i18n messages service
     * @param authenticationMode
     *            the authentication mode
     */
    @Inject
    public Authenticator(Configuration configuration, CacheApi cache, IUserSessionManagerPlugin userSessionManagerPlugin,
            IAccountManagerPlugin accountManagerPlugin, IAuthenticationAccountReaderPlugin authenticationAccountReader,
            IInstanceAccessSupervisor instanceAccessSupervisor, IPreferenceManagerPlugin preferenceManagerPlugin, II18nMessagesPlugin i18nMessagesPlugin,
            @Named("AuthenticatonMode") AuthenticationMode authenticationMode) throws MalformedURLException {
        super(configuration, cache, userSessionManagerPlugin, accountManagerPlugin, authenticationAccountReader, instanceAccessSupervisor,
                preferenceManagerPlugin, i18nMessagesPlugin, authenticationMode, new IAuthenticationLocalRoutes() {

                    @Override
                    public Call getRedirectToThePreviouslySavedUrl() {
                        return controllers.sso.routes.Authenticator.redirectToThePreviouslySavedUrl();
                    }

                    @Override
                    public Call getLogoutRoute() {
                        return controllers.sso.routes.Authenticator.customLogout();
                    }

                    @Override
                    public Call getLoginStandaloneRoute(String redirectUrl) {
                        return controllers.sso.routes.Authenticator.loginStandalone(redirectUrl);
                    }

                    @Override
                    public Call getLoginFederatedRoute(String redirectUrl) {
                        return controllers.sso.routes.Authenticator.loginFederated(redirectUrl);
                    }

                    @Override
                    public Call getLoginCasRoute(String redirectUrl) {
                        return controllers.sso.routes.Authenticator.loginCasMaster(redirectUrl);
                    }

                    @Override
                    public Call getCallbackRoute() {
                        return controllers.sso.routes.Authenticator.customCallback();
                    }

                    @Override
                    public Call getSamlCallbackRoute() {
                        return controllers.sso.routes.Authenticator.samlCallback();
                    }

                    @Override
                    public Call getDisplayStandaloneLoginFormRoute() {
                        return controllers.sso.routes.StandaloneAuthenticationController.displayLoginForm();
                    }

                    @Override
                    public Call getNotAccessibleRoute() {
                        return controllers.sso.routes.Authenticator.notAccessible();
                    }

                    @Override
                    public Call getNoFederatedAccount() {
                        return controllers.sso.routes.Authenticator.noFederatedAccount();
                    }
                });
    }

    @Override
    public Result getFederatedLogoutDisplay() {
        return ok(views.html.sso.federated_logout
                .render(getPreferenceManagerPlugin().getPreferenceElseConfigurationValue(IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url")));
    }

    @Override
    public Result notAccessible() {
        return ok(views.html.sso.not_accessible.render());
    }

    @Override
    public Result noFederatedAccount() {
        return ok(views.html.sso.no_account.render());
    }
}
