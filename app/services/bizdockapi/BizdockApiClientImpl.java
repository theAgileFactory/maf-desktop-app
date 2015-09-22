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

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import framework.services.api.client.SignatureGeneratorImpl;
import framework.services.api.commons.ApiMethod;
import framework.services.api.commons.IApiConstants;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

/**
 * The BizDock API client.
 * 
 * @author Johann Kohler
 * 
 */
@Singleton
public class BizdockApiClientImpl implements IBizdockApiClient {

    private ObjectMapper mapper;

    private static final long WS_TIMEOUT = 5000;

    /**
     * Initialize the service.
     * 
     * @param lifecycle
     *            the Play life cycle service
     * @param configuration
     *            the Play configuration service
     */
    @Inject
    public BizdockApiClientImpl(ApplicationLifecycle lifecycle, Configuration configuration) {

        Logger.info("SERVICE>>> BizdockApiClientImpl starting...");

        lifecycle.addStopHook(() -> {
            Logger.info("SERVICE>>> BizdockApiClientImpl stopping...");
            Logger.info("SERVICE>>> BizdockApiClientImpl stopped");
            return Promise.pure(null);
        });

        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(IApiConstants.DATE_FORMAT));
        this.mapper = mapper;

        Logger.info("SERVICE>>> BizdockApiClientImpl started");
    }

    @Override
    public JsonNode call(String applicationKey, String secretKey, ApiMethod apiMethod, String url, JsonNode content) throws BizdockApiException {

        // convert the JSON content to a string
        String contentString = null;
        if (content != null) {
            try {
                contentString = getMapper().writeValueAsString(content);
                Logger.info("contentString: " + contentString);
            } catch (JsonProcessingException e) {
                throw new BizdockApiException(e.getMessage(), 500);
            }
        }

        // set the timestamp as now
        Date timestamp = new Date();

        // generate the timestamp
        byte[] signatureAsByte = null;
        try {
            SignatureGeneratorImpl signatureGenerator = new SignatureGeneratorImpl(secretKey, applicationKey);
            byte[] contentAsByte = contentString != null ? contentString.getBytes() : null;
            signatureAsByte = signatureGenerator.getRequestSignature(apiMethod, url, contentAsByte, timestamp.getTime());
        } catch (Exception e) {
            throw new BizdockApiException(e.getMessage(), 500);
        }

        // construct the request
        WSRequest request = WS.url(url).setHeader("Content-Type", "application/json");

        request.setHeader(IApiConstants.APPLICATION_KEY_HEADER, applicationKey);
        request.setHeader(IApiConstants.SIGNATURE_HEADER, new String(signatureAsByte));
        request.setHeader(IApiConstants.TIMESTAMP_HEADER, String.valueOf(timestamp.getTime()));

        // process the request
        Promise<WSResponse> response = null;

        switch (apiMethod) {
        case GET:
            response = request.get();
            break;
        case POST:
            response = request.post(contentString);
            break;
        case PUT:
            response = request.put(contentString);
            break;
        case DELETE:
            response = request.delete();
            break;
        default:
            break;
        }

        Promise<Pair<Integer, JsonNode>> jsonPromise = response.map(new Function<WSResponse, Pair<Integer, JsonNode>>() {
            public Pair<Integer, JsonNode> apply(WSResponse response) {
                try {
                    return Pair.of(response.getStatus(), response.asJson());
                } catch (Exception e) {
                    JsonNode error = JsonNodeFactory.instance.textNode(e.getMessage());
                    return Pair.of(response.getStatus(), error);
                }
            }
        });

        Pair<Integer, JsonNode> responseContent = jsonPromise.get(WS_TIMEOUT);

        // treat the response
        if (responseContent.getLeft().equals(200) || responseContent.getLeft().equals(204)) {
            return responseContent.getRight();
        } else {
            String errorMessage = "BizDock API call error / url: " + url + " / status: " + responseContent.getLeft() + " / errors: "
                    + responseContent.getRight().toString();
            throw new BizdockApiException(errorMessage, responseContent.getLeft());
        }

    }

    @Override
    public ObjectMapper getMapper() {
        return this.mapper;
    }

}
