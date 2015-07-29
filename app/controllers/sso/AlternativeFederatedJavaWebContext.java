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
import java.net.URL;

import org.jfree.util.Log;
import org.pac4j.play.java.JavaWebContext;

import framework.commons.IFrameworkConstants;
import framework.utils.Utilities;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Http.Session;

/**
 * PAC4J workaround !!! This is an alternative version to the standard
 * {@link JavaWebContext}. This one replaces the "local" hostname by a the
 * public one defined in the configuration
 * 
 * @author Pierre-Yves Cloux
 */
public class AlternativeFederatedJavaWebContext extends JavaWebContext {

    /**
     * Constructor.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @param session
     *            the session
     */
    public AlternativeFederatedJavaWebContext(Request request, Response response, Session session) {
        super(request, response, session);
    }

    @Override
    public String getServerName() {

        String alternativeHostName = null;

        try {
            alternativeHostName = new URL(getFullRequestURL()).getHost();
        } catch (MalformedURLException e) {
            Log.error("Unable to initialize the FEDERATED authentication", e);
        }

        return alternativeHostName;
    }

    @Override
    public String getFullRequestURL() {
        return Utilities.getPreferenceElseConfigurationValue(IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url")
                + controllers.sso.routes.AlternativeFederatedCallbackController.callback().url() + AuthenticationConfigurationUtils.SAML_CLIENT_ID_EXTENTION;
    }

    @Override
    public String getScheme() {
        return "https";
    }

}
