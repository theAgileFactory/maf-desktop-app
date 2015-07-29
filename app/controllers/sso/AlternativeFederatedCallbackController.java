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

import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.CallbackController;
import org.pac4j.play.Config;
import org.pac4j.play.Constants;
import org.pac4j.play.StorageHelper;
import org.pac4j.play.java.JavaWebContext;

import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Result;
import play.mvc.Results;
import services.licensesmanagement.LicensesManagementServiceImpl;
import services.licensesmanagement.LoginEventRequest.ErrorCode;
import framework.services.ServiceManager;

/**
 * PAC4J workaround !!! This is an alternative version to the standard
 * {@link CallbackController}. This one is changing the request URL with the
 * public URL of the service (and the right scheme).
 * 
 * @author Pierre-Yves Cloux
 */
public class AlternativeFederatedCallbackController extends CallbackController {
    /**
     * This method handles the callback call from the provider to finish the
     * authentication process. The credentials and then the profile of the
     * authenticated user is retrieved and the originally requested url (or the
     * specific saved url) is restored.
     * 
     * @return the redirection to the saved request
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Promise<Result> callback() {

        if (!ServiceManager.getService(LicensesManagementServiceImpl.NAME, LicensesManagementServiceImpl.class).isInstanceAccessible()) {

            // event: not accessible / FEDERATED
            ServiceManager.getService(LicensesManagementServiceImpl.NAME, LicensesManagementServiceImpl.class).addLoginEvent(null, false,
                    ErrorCode.IS_NOT_ACCESSIBLE, null);

            return Promise.promise(new Function0<Result>() {
                public Result apply() throws Throwable {
                    // redirect to new page
                    return redirect(controllers.sso.routes.Authenticator.notAccessible());
                }
            });

        }

        // clients group from config
        final Clients clientsGroup = Config.getClients();
        // web context
        final JavaWebContext context = new AlternativeFederatedJavaWebContext(request(), response(), session());
        // get the client from its type
        final BaseClient client = (BaseClient) clientsGroup.findClient(context);
        logger.debug("client : {}", client);
        // get credentials
        Promise<Result> promise = Promise.promise(new Function0<Result>() {
            public Result apply() {
                Credentials credentials = null;
                try {
                    credentials = client.getCredentials(context);
                    logger.debug("credentials : {}", credentials);
                } catch (final RequiresHttpAction e) {
                    // requires some specific HTTP action
                    final int code = context.getResponseStatus();
                    logger.debug("requires HTTP action : {}", code);
                    if (code == HttpConstants.UNAUTHORIZED) {
                        return unauthorized(Config.getErrorPage401()).as(Constants.HTML_CONTENT_TYPE);
                    } else if (code == HttpConstants.TEMP_REDIRECT) {
                        return Results.status(HttpConstants.TEMP_REDIRECT);
                    } else if (code == HttpConstants.OK) {
                        final String content = context.getResponseContent();
                        logger.debug("render : {}", content);
                        return ok(content).as(Constants.HTML_CONTENT_TYPE);
                    }
                    final String message = "Unsupported HTTP action : " + code;
                    logger.error(message);
                    throw new TechnicalException(message);
                }
                // get user profile
                final CommonProfile profile = client.getUserProfile(credentials, context);
                logger.debug("profile : {}", profile);
                // get or create sessionId
                final String sessionId = StorageHelper.getOrCreationSessionId(session());
                // save user profile only if it's not null
                if (profile != null) {
                    StorageHelper.saveProfile(sessionId, profile);
                }
                // get requested url
                final String requestedUrl = StorageHelper.getRequestedUrl(sessionId, client.getName());
                // retrieve saved request and redirect
                return redirect(defaultUrl(requestedUrl, Config.getDefaultSuccessUrl()));
            }
        });
        return promise;
    }
}