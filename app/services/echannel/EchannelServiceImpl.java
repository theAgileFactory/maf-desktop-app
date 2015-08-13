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
package services.echannel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import constants.IMafConstants;
import framework.services.account.IPreferenceManagerPlugin;
import models.framework_models.api.ApiRegistration;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.echannel.request.LoginEventRequest;
import services.echannel.request.LoginEventRequest.ErrorCode;
import services.echannel.request.UpdateConsumedPortfolioEntriesRequest;
import services.echannel.request.UpdateConsumedStorageRequest;
import services.echannel.request.UpdateConsumedUsersRequest;
import services.echannel.response.DataSyndicationAgreement;
import services.echannel.response.DataSyndicationAgreementItem;
import services.echannel.response.DataSyndicationAgreementLink;
import services.echannel.response.DataSyndicationPartner;

/**
 * The eChannel service.
 * 
 * @author Johann Kohler
 * 
 */
@Singleton
public class EchannelServiceImpl implements IEchannelService {

    private String echannelApiUrl;
    private String apiSecretKey;

    private IPreferenceManagerPlugin preferenceManagerPlugin;

    private static final String ACTION_PATTERN = "/{domain}/{action}";

    private static final String CAN_CREATE_USER_ACTION = "can-create-user";
    private static final String CAN_CREATE_PORTOLIO_ENTRY_ACTION = "can-create-portfolio-entry";
    private static final String IS_ACCESSIBLE_ACTION = "is-accessible";
    private static final String CONSUMED_USERS_ACTION = "consumed-users";
    private static final String CONSUMED_PORTFOLIO_ENTRIES_ACTION = "consumed-portfolio-entries";
    private static final String CONSUMED_STORAGE_ACTION = "consumed-storage";
    private static final String LOGIN_EVENT_ACTION = "login-event";

    private static final long WS_TIMEOUT = 2000;

    private static final String HTTP_HEADER_API_KEY = "X-echannel-API-Key";

    /**
     * Configurations of the the service.
     * 
     * @author Johann Kohler
     *
     */
    public enum Config {

        ECHANNEL_API_URL("maf.echannel_api.url");

        private String configurationKey;

        /**
         * Construct a configuration with its key.
         * 
         * @param configurationKey
         *            the configuration key
         */
        private Config(String configurationKey) {
            this.configurationKey = configurationKey;
        }

        /**
         * Get the configuration key.
         */
        public String getConfigurationKey() {
            return configurationKey;
        }
    }

    /**
     * Initialize the service.
     * 
     * @param lifecycle
     *            the Play life cycle service
     * @param configuration
     *            the Play configuration service
     * @param preferenceManagerPlugin
     *            the preference service
     */

    @Inject
    public EchannelServiceImpl(ApplicationLifecycle lifecycle, Configuration configuration, IPreferenceManagerPlugin preferenceManagerPlugin) {
        Logger.info("SERVICE>>> EchannelServiceImpl starting...");

        this.echannelApiUrl = configuration.getString(Config.ECHANNEL_API_URL.getConfigurationKey());
        this.apiSecretKey = null;

        this.preferenceManagerPlugin = preferenceManagerPlugin;

        lifecycle.addStopHook(() -> {
            Logger.info("SERVICE>>> EchannelServiceImpl stopping...");
            Logger.info("SERVICE>>> EchannelServiceImpl stopped");
            return Promise.pure(null);
        });

        Logger.info("SERVICE>>> EchannelServiceImpl started");
    }

    @Override
    public boolean canCreateUser(int consumedUsers) {

        try {
            List<NameValuePair> queryParams = new ArrayList<>();
            queryParams.add(new BasicNameValuePair("consumedUsers", String.valueOf(consumedUsers)));

            JsonNode response = this.call(HttpMethod.GET, CAN_CREATE_USER_ACTION, queryParams, null);
            if (response != null) {
                return response.asBoolean();
            }
        } catch (Exception e) {
            Logger.error("eChannel service unexpected error / canCreateUser", e);
        }

        return true;
    }

    @Override
    public boolean canCreatePortfolioEntry(int consumedPortfolioEntries) {

        try {
            List<NameValuePair> queryParams = new ArrayList<>();
            queryParams.add(new BasicNameValuePair("consumedPortfolioEntries", String.valueOf(consumedPortfolioEntries)));

            JsonNode response = this.call(HttpMethod.GET, CAN_CREATE_PORTOLIO_ENTRY_ACTION, queryParams, null);
            if (response != null) {
                return response.asBoolean();
            }
        } catch (Exception e) {
            Logger.error("eChannel service unexpected error / canCreatePortfolioEntry", e);
        }

        return true;
    }

    @Override
    public boolean isInstanceAccessible() {

        try {
            JsonNode response = this.call(HttpMethod.GET, IS_ACCESSIBLE_ACTION, null, null);
            if (response != null) {
                return response.asBoolean();
            }
        } catch (Exception e) {
            Logger.error("eChannel service unexpected error / isInstanceAccessible", e);
        }

        return true;
    }

    @Override
    public void updateConsumedUsers(int consumedUsers) {

        try {
            UpdateConsumedUsersRequest updateConsumedUsersRequest = new UpdateConsumedUsersRequest();
            updateConsumedUsersRequest.consumedUsers = consumedUsers;
            ObjectMapper mapper = new ObjectMapper();
            JsonNode content = mapper.valueToTree(updateConsumedUsersRequest);
            this.call(HttpMethod.PUT, CONSUMED_USERS_ACTION, null, content);
        } catch (Exception e) {
            Logger.error("eChannel service unexpected error / updateConsumedUsers", e);
        }

    }

    @Override
    public void updateConsumedPortfolioEntries(int consumedPortfolioEntries) {

        try {
            UpdateConsumedPortfolioEntriesRequest updateConsumedPortfolioEntriesRequest = new UpdateConsumedPortfolioEntriesRequest();
            updateConsumedPortfolioEntriesRequest.consumedPortfolioEntries = consumedPortfolioEntries;
            ObjectMapper mapper = new ObjectMapper();
            JsonNode content = mapper.valueToTree(updateConsumedPortfolioEntriesRequest);
            this.call(HttpMethod.PUT, CONSUMED_PORTFOLIO_ENTRIES_ACTION, null, content);
        } catch (Exception e) {
            Logger.error("eChannel service unexpected error / updateConsumedPortfolioEntries", e);
        }

    }

    @Override
    public void updateConsumedStorage(int consumedStorage) {

        try {
            UpdateConsumedStorageRequest updateConsumedStorageRequest = new UpdateConsumedStorageRequest();
            updateConsumedStorageRequest.consumedStorage = consumedStorage;
            ObjectMapper mapper = new ObjectMapper();
            JsonNode content = mapper.valueToTree(updateConsumedStorageRequest);
            this.call(HttpMethod.PUT, CONSUMED_STORAGE_ACTION, null, content);
        } catch (Exception e) {
            Logger.error("eChannel service unexpected error / updateConsumedStorage", e);
        }

    }

    @Override
    public void addLoginEvent(String uid, Boolean result, ErrorCode errorCode, String errorMessage) {

        try {
            LoginEventRequest loginEventRequest = new LoginEventRequest();
            loginEventRequest.errorCode = errorCode;
            loginEventRequest.errorMessage = errorMessage;
            loginEventRequest.result = result;
            loginEventRequest.uid = uid;
            ObjectMapper mapper = new ObjectMapper();
            JsonNode content = mapper.valueToTree(loginEventRequest);
            this.call(HttpMethod.POST, LOGIN_EVENT_ACTION, null, content);
        } catch (Exception e) {
            Logger.error("eChannel service unexpected error / addLoginEvent", e);
        }

    }

    @Override
    public List<DataSyndicationPartner> getSlavePartners() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DataSyndicationPartner> searchFromSlavePartners(String keywords) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void submitAgreement(String refId, String name, Date startDate, Date endDate, List<DataSyndicationAgreementItem> agreementItems,
            String slaveDomain, String permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void acceptAgreement(Long id, ApiRegistration apiKey, String permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void rejectAgreement(Long id, String permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void cancelAgreement(Long id, String permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void suspendAgreement(Long id, String permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void restartAgreement(Long id, String permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void submitAgreementLink(DataSyndicationAgreement agreement, List<DataSyndicationAgreementItem> agreementItems, String dataType,
            Long masterObjectId, String permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void acceptAgreementLink(Long id, Long slaveObjectId, String permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void rejectAgreementLink(Long id, String permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void cancelAgreementLink(Long id, String permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public DataSyndicationAgreementLink getAgreementLink(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksToSynchronize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksOfMasterObject(String dataType, Long masterObjectId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksOfSlaveObject(String dataType, Long masterObjectId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Get the URL for an action.
     * 
     * @param action
     *            the action
     */
    private String getActionUrl(String action) {

        String domain = getPreferenceManagerPlugin().getPreferenceValueAsString(IMafConstants.LICENSE_INSTANCE_DOMAIN_PREFERENCE);

        String url = ACTION_PATTERN.replace("{domain}", domain);
        url = url.replace("{action}", action);

        return echannelApiUrl + url;
    }

    /**
     * Get the API secret key.
     */
    private String getApiSecretKey() {
        if (this.apiSecretKey == null) {
            this.apiSecretKey = getPreferenceManagerPlugin().getPreferenceValueAsString(IMafConstants.LICENSE_ECHANNEL_API_SECRET_KEY_PREFERENCE);
        }
        return this.apiSecretKey;
    }

    /**
     * Perform a call.
     * 
     * @param httpMethod
     *            the HTTP method (GET, POST...)
     * @param action
     *            the action name
     * @param queryParams
     *            the query parameters
     * @param content
     *            the request content (for POST)
     */
    private JsonNode call(HttpMethod httpMethod, String action, List<NameValuePair> queryParams, JsonNode content) {

        String url = this.getActionUrl(action);

        Logger.debug("URL: " + url);

        WSRequest request = WS.url(url);
        request.setHeader(HTTP_HEADER_API_KEY, this.getApiSecretKey());

        if (queryParams != null) {
            for (NameValuePair param : queryParams) {
                request = request.setQueryParameter(param.getName(), param.getValue());
            }
        }

        Promise<WSResponse> response = null;

        switch (httpMethod) {
        case GET:
            response = request.get();
            break;
        case POST:
            response = request.post(content);
            break;
        case PUT:
            response = request.put(content);
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

        Logger.debug("STATUS CODE: " + responseContent.getLeft());

        if (responseContent.getLeft().equals(200) || responseContent.getLeft().equals(204)) {
            return responseContent.getRight();
        } else {
            Logger.error("eChannel service call error / url: " + url + " / status: " + responseContent.getLeft() + " / errors: "
                    + responseContent.getRight().toString());
            return null;
        }

    }

    /**
     * The possible HTTP method.
     * 
     * @author Johann Kohler
     * 
     */
    private static enum HttpMethod {
        GET, POST, PUT;
    }

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return preferenceManagerPlugin;
    }

}
