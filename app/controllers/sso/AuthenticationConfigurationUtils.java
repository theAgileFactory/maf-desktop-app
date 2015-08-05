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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Clients;
import org.pac4j.http.client.FormClient;
import org.pac4j.play.Config;
import org.pac4j.play.PlayLogoutHandler;
import org.pac4j.saml.client.Saml2Client;
import org.pac4j.http.profile.UsernameProfileCreator;

import framework.commons.IFrameworkConstants;
import framework.services.ServiceManager;
import framework.services.account.LightAuthenticationUserPasswordAuthenticator;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Utilities;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.Cookie;
import play.mvc.Result;

/**
 * Utilities for the authentication systems.
 * 
 * @author Pierre-Yves Cloux
 */
public abstract class AuthenticationConfigurationUtils {
    private static Logger.ALogger log = Logger.of(AuthenticationConfigurationUtils.class);

    public static final String REDIRECT_URL_COOKIE_NAME = "bzr";
    public static final String SAML_CLIENT_ID_EXTENTION = "?client_name=Saml2Client";

    /**
     * Default constructor.
     */
    public AuthenticationConfigurationUtils() {
    }

    /**
     * Initialize the SSO according to the configured authentication mode.
     * 
     * @param authenticationMode
     *            the selected authentication mode
     */
    public static void init(IFrameworkConstants.AuthenticationMode authenticationMode) {
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
     * Provide a redirect to the login page which is matching with the right
     * authentication mechanism.
     * 
     * @param redirectUrl
     *            the redirect URL
     */
    public static Result redirectToLoginPage(String redirectUrl) {
        switch (Utilities.getAuthenticationMode()) {
        case CAS_MASTER:
            return Controller.redirect(controllers.sso.routes.Authenticator.loginCasMaster(redirectUrl));
        case CAS_SLAVE:
            return Controller.redirect(controllers.sso.routes.Authenticator.loginCasMaster(redirectUrl));
        case STANDALONE: {
            setRedirectUrlInSession(redirectUrl);
            return Controller.redirect(controllers.sso.routes.Authenticator.loginStandalone(redirectUrl));
        }
        case FEDERATED:
            return Controller.redirect(controllers.sso.routes.Authenticator.loginFederated(redirectUrl));
        }
        return Controller.badRequest();
    }

    /**
     * Set a redirection URL in a cookie.<br/>
     * WARNING: this is used with the STANDALONE authentication mode since it
     * seems the redirect is not working
     * 
     * @param redirectUrl
     *            the redirect URL
     */
    public static void setRedirectUrlInSession(String redirectUrl) {
        Controller.response().setCookie(REDIRECT_URL_COOKIE_NAME, redirectUrl);
    }

    /**
     * Get the previously set redirection URL.<br/>
     * WARNING: this is used with the STANDALONE authentication mode since it
     * seems the redirect is not working
     * 
     * @return
     */
    public static String getRedirectUrlInSession() {
        Cookie redirectUrlCookie = Controller.request().cookie(REDIRECT_URL_COOKIE_NAME);
        if (redirectUrlCookie != null && redirectUrlCookie.value() != null) {
            Controller.response().discardCookie(REDIRECT_URL_COOKIE_NAME);
            return redirectUrlCookie.value();
        }
        // If no redirect URL then redirect to the public URL
        return play.Configuration.root().getString("maf.public.url");
    }

    /**
     * Initialize the BizDock SSO module based on CAS.
     */
    private static void initCasSingleSignOn() {
        log.info(">>>>>>>>>>>>>>>> Initialize CAS SSO");
        String casLoginUrl = play.Configuration.root().getString("cas.login.url");
        String casCallbackUrl = Utilities.getPreferenceElseConfigurationValue(IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url")
                + controllers.sso.routes.Authenticator.callback().url();
        final CasClient casClient = new CasClient();
        casClient.setLogoutHandler(new PlayLogoutHandler());
        casClient.setCasProtocol(CasClient.CasProtocol.SAML);
        casClient.setCasLoginUrl(casLoginUrl);
        casClient.setTimeTolerance(play.Configuration.root().getLong("cas.time_tolerance"));
        final Clients clients = new Clients(casCallbackUrl, casClient);
        Config.setClients(clients);
        Config.setDefaultLogoutUrl(play.Configuration.root().getString("cas.logout.url"));
        log.info(">>>>>>>>>>>>>>>> Initialize CAS SSO (end)");
    }

    /**
     * Initialize the SSO module based on SAMLv2.
     * 
     * @throws ConfigurationException
     */
    private static void initSAMLv2SingleSignOn() {
        log.info(">>>>>>>>>>>>>>>> Initialize SAMLv2 SSO");
        final Saml2Client saml2Client = new Saml2Client();
        File samlConfigFile = new File(play.Configuration.root().getString("saml.sso.config"));
        if (!samlConfigFile.exists() || samlConfigFile.isDirectory()) {
            throw new IllegalArgumentException("The authentication mode is FEDERATED but the SAML config file does not exists " + samlConfigFile);
        }
        log.info("SAML configuration found, loading properties");
        try {
//            PropertiesConfiguration cfg = new PropertiesConfiguration(samlConfigFile);
//            String publicUrl = Utilities.getPreferenceElseConfigurationValue(IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url");
//            if (cfg.containsKey("maf.saml.entityId")) {
//                saml2Client.setSpEntityId(cfg.getString("maf.saml.entityId"));
//            } else {
//                saml2Client.setSpEntityId(publicUrl);
//            }
//
//            // Set the user profile attribute defined in the configuration (if
//            // any)
//            if (cfg.containsKey("maf.saml.profile.attribute")) {
//                ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class)
//                        .setUserProfileAttributeName(cfg.getString("maf.saml.profile.attribute"));
//            }
//
//            File configurationDirectory = samlConfigFile.getParentFile();
//            saml2Client.setKeystorePath(new File(configurationDirectory, cfg.getString("maf.saml.keystorefile")).getAbsolutePath());
//            saml2Client.setKeystorePassword(cfg.getString("maf.saml.keystore.password"));
//            saml2Client.setPrivateKeyPassword(cfg.getString("maf.saml.privatekey.password"));
//            saml2Client.setIdpMetadataPath(new File(configurationDirectory, cfg.getString("maf.saml.idpmetadata")).getAbsolutePath());
//            saml2Client.setCallbackUrl(publicUrl + controllers.sso.routes.AlternativeFederatedCallbackController.callback().url() + SAML_CLIENT_ID_EXTENTION);
//            saml2Client.setMaximumAuthenticationLifetime(cfg.getInt("maf.saml.maximum.authentication.lifetime"));
//
//            // Write the client meta data to the file system
//            String spMetaDataFileName = cfg.getString("maf.saml.spmetadata");
//            FileUtils.write(new File(configurationDirectory, spMetaDataFileName), saml2Client.printClientMetadata());
//            log.info("Service Provider meta-data written to the file system in " + spMetaDataFileName);
//
//            final Clients clients = new Clients(
//                    publicUrl + controllers.sso.routes.AlternativeFederatedCallbackController.callback().url() + SAML_CLIENT_ID_EXTENTION, saml2Client);
//            clients.init();
//            Config.setClients(clients);
//            if (cfg.containsKey("maf.saml.logout.url")) {
//                Config.setDefaultLogoutUrl(cfg.getString("maf.saml.logout.url"));
//            } else {
//                Config.setDefaultLogoutUrl(controllers.sso.routes.Authenticator.logoutFederated().url());
//            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initialize the FEDERATED SSO", e);
        }
        log.info(">>>>>>>>>>>>>>>> Initialize SAMLv2 SSO (end)");
    }

    /**
     * Initialize the BizDock standalone authentication mode.
     */
    private static void initStandaloneAuthentication() {
        log.info(">>>>>>>>>>>>>>>> Initialize Standalone Authentication mode");
        String casCallbackUrl = Utilities.getPreferenceElseConfigurationValue(IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url")
                + controllers.sso.routes.Authenticator.callback().url();
        final FormClient formClient = new FormClient(controllers.sso.routes.StandaloneAuthenticationController.displayLoginForm().url(),
                new LightAuthenticationUserPasswordAuthenticator(),new UsernameProfileCreator());
        formClient.setUsernameParameter("uid");
        formClient.setPasswordParameter("password");
        final Clients clients = new Clients(casCallbackUrl, formClient);
        Config.setClients(clients);
        Config.setProfileTimeout(play.Configuration.root().getInt("standalone.sso.profile.timeout"));
        Config.setDefaultSuccessUrl(routes.Authenticator.redirectToThePreviouslySavedUrl().url());
        log.info(">>>>>>>>>>>>>>>> Initialize Standalone Authentication mode (end)");
    }
}
