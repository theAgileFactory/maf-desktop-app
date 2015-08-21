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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Clients;
import org.pac4j.http.client.FormClient;
import org.pac4j.http.profile.UsernameProfileCreator;
import org.pac4j.play.Config;
import org.pac4j.play.PlayLogoutHandler;
import org.pac4j.play.java.RequiresAuthentication;
import org.pac4j.play.java.SecureController;
import org.pac4j.saml.client.Saml2Client;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import controllers.ControllersUtils;
import framework.commons.IFrameworkConstants;
import framework.commons.IFrameworkConstants.AuthenticationMode;
import framework.services.ServiceStaticAccessor;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IAuthenticationAccountReaderPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.account.LightAuthenticationUserPasswordAuthenticator;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.configuration.Language;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Utilities;
import play.Configuration;
import play.Logger;
import play.api.http.MediaRange;
import play.api.mvc.RequestHeader;
import play.i18n.Lang;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Http.Cookie;
import play.mvc.Http.Cookies;
import play.mvc.Http.Request;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.With;
import services.echannel.request.LoginEventRequest.ErrorCode;
import services.licensesmanagement.ILicensesManagementService;

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
public class Authenticator extends SecureController {
    public static final String SAML_CLIENT_ID_EXTENTION = "?client_name=Saml2Client";
    public static final String REDIRECT_URL_COOKIE_NAME = "bzr";
    private static Logger.ALogger log = Logger.of(Authenticator.class);
    
    private Configuration configuration;
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    private IAccountManagerPlugin accountManagerPlugin;
    private ILicensesManagementService licensesManagementService;
    private AuthenticationMode authenticationMode;
    private IPreferenceManagerPlugin preferenceManagerPlugin;
    private IAuthenticationAccountReaderPlugin authenticationAccountReader;
    private II18nMessagesPlugin i18nMessagesPlugin;
    
    /**
     * Creates a new Authenticator controller
     * @param configuration
     * @param userSessionManagerPlugin
     * @param accountManagerPlugin
     * @param authenticationAccountReader
     * @param licensesManagementService
     * @param preferenceManagerPlugin
     * @param i18nMessagesPlugin
     * @param authenticationMode
     */
    @Inject
    public Authenticator(
            Configuration configuration,
            IUserSessionManagerPlugin userSessionManagerPlugin, 
            IAccountManagerPlugin accountManagerPlugin,
            IAuthenticationAccountReaderPlugin authenticationAccountReader,
            ILicensesManagementService licensesManagementService,
            IPreferenceManagerPlugin preferenceManagerPlugin,
            II18nMessagesPlugin i18nMessagesPlugin,
            @Named("AuthenticatonMode") AuthenticationMode authenticationMode) {
        super();
        this.configuration=configuration;
        this.authenticationMode=authenticationMode;
        this.userSessionManagerPlugin = userSessionManagerPlugin;
        this.accountManagerPlugin = accountManagerPlugin;
        this.authenticationAccountReader=authenticationAccountReader;
        this.licensesManagementService = licensesManagementService;
        this.preferenceManagerPlugin=preferenceManagerPlugin;
        this.i18nMessagesPlugin=i18nMessagesPlugin;
        log.info("Initialization based on authentication model "+authenticationMode);
        init(authenticationMode);
    }
    
    /**
     * Provide a redirect to the login page which is matching with the right
     * authentication mechanism.
     * 
     * @param redirectUrl
     *            the redirect URL
     */
    public Result redirectToLoginPage(String redirectUrl) {
        switch (getAuthenticationMode()) {
        case CAS_MASTER:
            return redirect(controllers.sso.routes.Authenticator.loginCasMaster(redirectUrl));
        case CAS_SLAVE:
            return redirect(controllers.sso.routes.Authenticator.loginCasMaster(redirectUrl));
        case STANDALONE: {
            setRedirectUrlInSession(redirectUrl);
            return redirect(controllers.sso.routes.Authenticator.loginStandalone(redirectUrl));
        }
        case FEDERATED:
            return redirect(controllers.sso.routes.Authenticator.loginFederated(redirectUrl));
        }
        return badRequest();
    }
    
    /**
     * Set a redirection URL in a cookie.<br/>
     * WARNING: this is used with the STANDALONE authentication mode since it
     * seems the redirect is not working
     * 
     * @param redirectUrl
     *            the redirect URL
     */
    public void setRedirectUrlInSession(String redirectUrl) {
        response().setCookie(REDIRECT_URL_COOKIE_NAME, redirectUrl);
    }

    /**
     * Get the previously set redirection URL.<br/>
     * WARNING: this is used with the STANDALONE authentication mode since it
     * seems the redirect is not working
     * 
     * @return
     */
    public String getRedirectUrlInSession() {
        Cookie redirectUrlCookie = request().cookie(REDIRECT_URL_COOKIE_NAME);
        if (redirectUrlCookie != null && redirectUrlCookie.value() != null) {
            response().discardCookie(REDIRECT_URL_COOKIE_NAME);
            return redirectUrlCookie.value();
        }
        // If no redirect URL then redirect to the public URL
        return getConfiguration().getString("maf.public.url");
    }

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
    public Result loginFederated(String redirectUrl) {
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
    public Result loginCasMaster(String redirectUrl) {
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
    public Result loginStandalone(String redirectUrl) {
        return loginCode(redirectUrl);
    }

    /**
     * Redirect the user to the previous saved URL.
     */
    @SubjectPresent
    public Result redirectToThePreviouslySavedUrl() {
        // event: success login / STANDALONE
        getLicensesManagementService().addLoginEvent(
                getUserSessionManagerPlugin().getUserSessionId(ctx()), true, null, null);
        return redirect(getRedirectUrlInSession());
    }

    /**
     * The code executed depending on the login implementation.
     * 
     * @param redirectUrl
     *            the URL to which the user must be redirected after
     *            authentication
     * @return
     */
    private Result loginCode(String redirectUrl) {

        try {
            IUserAccount userAccount = getAccountManagerPlugin().getUserAccountFromUid(getUserSessionManagerPlugin().getUserSessionId(ctx()));

            // User is not found
            if (userAccount == null) {
                return badRequest();
            }

            // event: success login / CAS
            getLicensesManagementService().addLoginEvent(
                    getUserSessionManagerPlugin().getUserSessionId(ctx()), true, null, null);

            // get the preferred language as object
            Language language = new Language(userAccount.getPreferredLanguage());

            // verify the language is valid
            if (getI18nMessagesPlugin().isLanguageValid(language.getCode())) {
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
    public Result notAccessible() {
        return ok(views.html.sso.not_accessible.render());
    }
    
    /**
     * Clear the user session and logout the user.<br/>
     * The user is then redirected to the login page.
     */
    @With(FederatedAction.class)
    public Promise<Result> federatedCustomCallback() {
        return customCallback();
    }

    /**
     * Clear the user session and logout the user.<br/>
     * The user is then redirected to the login page.
     */
    public Promise<Result> customCallback() {

        if (!getLicensesManagementService().isInstanceAccessible()) {

            // event: not accessible / STANDALONE + CAS
            getLicensesManagementService().addLoginEvent(null, false,
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
        return callback();
    }

    /**
     * Clear the user session and logout the user.<br/>
     * The user is then redirected to the login page.
     */
    public Result customLogout() {
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
        return logoutAndRedirect();
    }
    
    /**
     * The user is then redirected to the login page.
     */
    public Result customLogoutCas(){
        return logoutAndRedirect();
    }

    /**
     * The logout page for the federated authentication.
     * 
     * @return
     */
    public Result customLogoutFederated() {
        return ok(views.html.sso.federated_logout.render(Utilities.getPreferenceElseConfigurationValue(getConfiguration(), IFrameworkConstants.PUBLIC_URL_PREFERENCE,
                "maf.public.url")));
    }
    
    /**
     * Initialize the SSO according to the configured authentication mode.
     * 
     * @param authenticationMode
     *            the selected authentication mode
     */
    public void init(IFrameworkConstants.AuthenticationMode authenticationMode) {
        // Initialize the authentication mode
        switch (authenticationMode) {
        case CAS_MASTER:
            initCasSingleSignOn();
            break;
        case CAS_SLAVE:
            initCasSingleSignOn();
            break;
        case STANDALONE:
            initStandaloneAuthentication();
            break;
        case FEDERATED:
            initSAMLv2SingleSignOn();
            break;
        }
    }
    
    /**
     * Initialize the BizDock SSO module based on CAS.
     */
    private void initCasSingleSignOn() {
        log.info(">>>>>>>>>>>>>>>> Initialize CAS SSO");
        String casLoginUrl = getConfiguration().getString("cas.login.url");
        String casCallbackUrl = Utilities.getPreferenceElseConfigurationValue(getConfiguration(),IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url")
                + controllers.sso.routes.Authenticator.customCallback().url();
        final CasClient casClient = new CasClient();
        casClient.setLogoutHandler(new PlayLogoutHandler());
        casClient.setCasProtocol(CasClient.CasProtocol.SAML);
        casClient.setCasLoginUrl(casLoginUrl);
        casClient.setTimeTolerance(getConfiguration().getLong("cas.time_tolerance"));
        final Clients clients = new Clients(casCallbackUrl, casClient);
        Config.setClients(clients);
        Config.setDefaultLogoutUrl(getConfiguration().getString("cas.logout.url"));
        log.info(">>>>>>>>>>>>>>>> Initialize CAS SSO (end)");
    }

    /**
     * Initialize the SSO module based on SAMLv2.
     * 
     * @throws ConfigurationException
     */
    private void initSAMLv2SingleSignOn() {
        log.info(">>>>>>>>>>>>>>>> Initialize SAMLv2 SSO");
        final Saml2Client saml2Client = new Saml2Client();
        File samlConfigFile = new File(getConfiguration().getString("saml.sso.config"));
        if (!samlConfigFile.exists() || samlConfigFile.isDirectory()) {
            throw new IllegalArgumentException("The authentication mode is FEDERATED but the SAML config file does not exists " + samlConfigFile);
        }
        log.info("SAML configuration found, loading properties");
        try {
            PropertiesConfiguration cfg = new PropertiesConfiguration(samlConfigFile);
            String publicUrl = Utilities.getPreferenceElseConfigurationValue(getConfiguration(),IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url");
            if (cfg.containsKey("maf.saml.entityId")) {
                saml2Client.setSpEntityId(cfg.getString("maf.saml.entityId"));
            } else {
                saml2Client.setSpEntityId(publicUrl);
            }

            // Set the user profile attribute defined in the configuration (if
            // any)
            if (cfg.containsKey("maf.saml.profile.attribute")) {
                ServiceStaticAccessor.getUserSessionManagerPlugin()
                        .setUserProfileAttributeName(cfg.getString("maf.saml.profile.attribute"));
            }

            File configurationDirectory = samlConfigFile.getParentFile();
            saml2Client.setKeystorePath(new File(configurationDirectory, cfg.getString("maf.saml.keystorefile")).getAbsolutePath());
            saml2Client.setKeystorePassword(cfg.getString("maf.saml.keystore.password"));
            saml2Client.setPrivateKeyPassword(cfg.getString("maf.saml.privatekey.password"));
            saml2Client.setIdpMetadataPath(new File(configurationDirectory, cfg.getString("maf.saml.idpmetadata")).getAbsolutePath());
            saml2Client.setCallbackUrl(publicUrl + controllers.sso.routes.Authenticator.customCallback().url() + SAML_CLIENT_ID_EXTENTION);
            saml2Client.setMaximumAuthenticationLifetime(cfg.getInt("maf.saml.maximum.authentication.lifetime"));
            
            // Write the client meta data to the file system
            String spMetaDataFileName = cfg.getString("maf.saml.spmetadata");
            FileUtils.write(new File(configurationDirectory, spMetaDataFileName), saml2Client.printClientMetadata());
            log.info("Service Provider meta-data written to the file system in " + spMetaDataFileName);

//            final Clients clients = new Clients(
//                    publicUrl + controllers.sso.routes.AlternativeFederatedCallbackController.callback().url() + SAML_CLIENT_ID_EXTENTION, saml2Client);
            final Clients clients = new Clients(publicUrl+controllers.sso.routes.Authenticator.customCallback().url(),saml2Client);
            clients.init();
            Config.setClients(clients);
            if (cfg.containsKey("maf.saml.logout.url")) {
                Config.setDefaultLogoutUrl(cfg.getString("maf.saml.logout.url"));
            } else {
                Config.setDefaultLogoutUrl(controllers.sso.routes.Authenticator.customLogoutFederated().url());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initialize the FEDERATED SSO", e);
        }
        log.info(">>>>>>>>>>>>>>>> Initialize SAMLv2 SSO (end)");
    }

    /**
     * Initialize the BizDock standalone authentication mode.
     */
    private void initStandaloneAuthentication() {
        log.info(">>>>>>>>>>>>>>>> Initialize Standalone Authentication mode");
        getPreferenceManagerPlugin().getPreferenceValueAsString(IFrameworkConstants.PUBLIC_URL_PREFERENCE);
        String casCallbackUrl = Utilities.getPreferenceElseConfigurationValue(getConfiguration(),IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url")
                + controllers.sso.routes.Authenticator.customCallback().url();
        final FormClient formClient = new FormClient(controllers.sso.routes.StandaloneAuthenticationController.displayLoginForm().url(),
                new LightAuthenticationUserPasswordAuthenticator(getAuthenticationAccountReader()),new UsernameProfileCreator());
        formClient.setUsernameParameter("uid");
        formClient.setPasswordParameter("password");
        final Clients clients = new Clients(casCallbackUrl, formClient);
        Config.setClients(clients);
        Config.setProfileTimeout(getConfiguration().getInt("standalone.sso.profile.timeout"));
        Config.setDefaultSuccessUrl(routes.Authenticator.redirectToThePreviouslySavedUrl().url());
        log.info(">>>>>>>>>>>>>>>> Initialize Standalone Authentication mode (end)");
    }

    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    private IAccountManagerPlugin getAccountManagerPlugin() {
        return accountManagerPlugin;
    }

    private ILicensesManagementService getLicensesManagementService() {
        return licensesManagementService;
    }

    private AuthenticationMode getAuthenticationMode() {
        return authenticationMode;
    }

    private Configuration getConfiguration() {
        return configuration;
    }

    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return preferenceManagerPlugin;
    }

    private IAuthenticationAccountReaderPlugin getAuthenticationAccountReader() {
        return authenticationAccountReader;
    }

    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    /**
     * A request wrapper to modify the host name for the SAMLv2 callback managed by Pac4j.
     * If BizDock is deployed behind a SSL proxy the SAMLv2 host in the assertion
     * will be different from the "real one" as understood by play.<br/>
     * We thus wrap the request in order to artificially modify the host name.
     * @author Pierre-Yves Cloux
     */
    private static class RequestWrapper implements Request{
        private Http.Request realRequest;
        private boolean alternativeSecure;
        private String alternativeHost;
        
        public RequestWrapper(Request realRequest, boolean alternativeSecure, String alternativeHost){
            super();
            this.realRequest = realRequest;
            this.alternativeHost=alternativeHost;
            this.alternativeSecure=alternativeSecure;
        }
    
        @Override
        public RequestHeader _underlyingHeader() {
            return realRequest._underlyingHeader();
        }
    
        @Override
        public List<Lang> acceptLanguages() {
            return realRequest.acceptLanguages();
        }
    
        @Override
        public List<MediaRange> acceptedTypes() {
            return realRequest.acceptedTypes();
        }
    
        @Override
        public boolean accepts(String arg0) {
            return realRequest.accepts(arg0);
        }
    
        @Override
        public Cookie cookie(String arg0) {
            return realRequest.cookie(arg0);
        }
    
        @Override
        public Cookies cookies() {
            return realRequest.cookies();
        }
    
        @Override
        public String getHeader(String arg0) {
            return realRequest.getHeader(arg0);
        }
    
        @Override
        public String getQueryString(String arg0) {
            return realRequest.getQueryString(arg0);
        }
    
        @Override
        public boolean hasHeader(String arg0) {
            return realRequest.hasHeader(arg0);
        }
    
        @Override
        public Map<String, String[]> headers() {
            return realRequest.headers();
        }
    
        @Override
        public String host() {
            return alternativeHost;
        }
    
        @Override
        public String method() {
            return realRequest.method();
        }
    
        @Override
        public String path() {
            return realRequest.path();
        }
    
        @Override
        public Map<String, String[]> queryString() {
            return realRequest.queryString();
        }
    
        @Override
        public String remoteAddress() {
            return realRequest.remoteAddress();
        }
    
        @Override
        public boolean secure() {
            return alternativeSecure;
        }
    
        @Override
        public String uri() {
            return realRequest.uri();
        }
    
        @Override
        public String version() {
            return realRequest.version();
        }
    
        @Override
        public play.api.mvc.Request<RequestBody> _underlyingRequest() {
            return realRequest._underlyingRequest();
        }
    
        @Override
        public RequestBody body() {
            return realRequest.body();
        }
    
        @SuppressWarnings("deprecation")
        @Override
        public void setUsername(String arg0) {
            realRequest.setUsername(arg0);
        }
    
        @Override
        public String username() {
            return realRequest.username();
        }
    
        @Override
        public Request withUsername(String arg0) {
            return realRequest.withUsername(arg0);
        }
        
    }

    /**
     * An {@link Context} wrapper.</br>
     * Please see {@link RequestWrapper}.
     * @author Pierre-Yves Cloux
     */
    private static class ContextWrapper extends Context{    
        public ContextWrapper(Context realCtx, boolean alternativeSecure,String alternativeHost){
            super(realCtx.id(),realCtx._requestHeader(),new RequestWrapper(realCtx.request(), alternativeSecure, alternativeHost),realCtx.session(),realCtx.flash(),realCtx.args);
        }
        
    }

    /**
     * An action to be used for the "SAMLv2" callback method.
     * Please see {@link RequestWrapper}.
     * @author Pierre-Yves Cloux
     */
    private class FederatedAction extends play.mvc.Action.Simple{
        private boolean alternativeSecure;
        private String alternativeHost;
        
        @SuppressWarnings("unused")
        public FederatedAction() throws MalformedURLException {
            URL url=new URL(getConfiguration().getString("maf.public.url"));
            alternativeSecure=url.getProtocol().equalsIgnoreCase("https");
            alternativeHost=url.getHost();
        }
        
        @Override
        public Promise<Result> call(Context ctx) throws Throwable {
            return delegate.call(new ContextWrapper(ctx, alternativeSecure, alternativeHost));
        }
    }
}
