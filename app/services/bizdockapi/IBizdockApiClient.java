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
package services.bizdockapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import framework.services.api.commons.ApiMethod;

/**
 * The BizDock API client.
 * 
 * @author Johann Kohler
 * 
 */
public interface IBizdockApiClient {

    String NAME = "bizdockApiClient";

    /**
     * Perform a call.
     * 
     * @param applicationKey
     *            the application key of the BizDock instance to reach
     * @param secretKey
     *            the secret key of the BizDock instance to reach
     * @param apiMethod
     *            the HTTP method (GET, POST...)
     * @param url
     *            the API url
     * @param content
     *            the request content (for POST)
     */
    JsonNode call(String applicationKey, String secretKey, ApiMethod apiMethod, String url, JsonNode content) throws BizdockApiException;

    /**
     * Get the JSON object mapper.
     */
    ObjectMapper getMapper();

    /**
     * The BizDock API exception.
     * 
     * @author Johann Kohler
     *
     */
    public static class BizdockApiException extends Exception {
        private static final long serialVersionUID = 451215933974L;

        /**
         * Construct with message.
         * 
         * @param message
         *            the exception message
         */
        public BizdockApiException(String message) {
            super(message);
        }

    }

}
