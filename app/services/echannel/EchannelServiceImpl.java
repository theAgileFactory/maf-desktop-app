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
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.datasyndication.models.DataSyndicationAgreement;
import services.datasyndication.models.DataSyndicationAgreementItem;
import services.datasyndication.models.DataSyndicationAgreementLink;
import services.datasyndication.models.DataSyndicationApiKey;
import services.datasyndication.models.DataSyndicationPartner;
import services.echannel.request.AcceptDataSyndicationAgreementLinkRequest;
import services.echannel.request.AcceptDataSyndicationAgreementRequest;
import services.echannel.request.LoginEventRequest;
import services.echannel.request.LoginEventRequest.ErrorCode;
import services.echannel.request.PatchDataSyndicationAgreementLinkRequest;
import services.echannel.request.PatchDataSyndicationAgreementRequest;
import services.echannel.request.SubmitDataSyndicationAgreementLinkRequest;
import services.echannel.request.SubmitDataSyndicationAgreementRequest;
import services.echannel.request.UpdateConsumedPortfolioEntriesRequest;
import services.echannel.request.UpdateConsumedStorageRequest;
import services.echannel.request.UpdateConsumedUsersRequest;

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

    private static final String DATA_SYNDICATION_PARTNER_ACTION = "data-syndication-partner";
    private static final String DATA_SYNDICATION_AGREEMENT_ACTION = "data-syndication-agreement";
    private static final String DATA_SYNDICATION_AGREEMENT_ITEM_ACTION = "data-syndication-agreement-item";
    private static final String DATA_SYNDICATION_AGREEMENT_LINK_ACTION = "data-syndication-agreement-link";

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
    public List<DataSyndicationPartner> findPartners(boolean eligibleAsSlave, String keywords) {

        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair("eligibleAsSlave", String.valueOf(eligibleAsSlave)));
        queryParams.add(new BasicNameValuePair("keywords", keywords));

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_PARTNER_ACTION + "/find", queryParams, null);

        ObjectMapper mapper = new ObjectMapper();
        List<DataSyndicationPartner> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(mapper.convertValue(item, DataSyndicationPartner.class));
        }

        return r;
    }

    @Override
    public DataSyndicationPartner getPartner(String domain) {
        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_PARTNER_ACTION + "/" + domain, null, null);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(response, DataSyndicationPartner.class);
    }

    @Override
    public List<DataSyndicationAgreementItem> getDataAgreementItems() {
        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_ITEM_ACTION + "/find", null, null);
        ObjectMapper mapper = new ObjectMapper();
        List<DataSyndicationAgreementItem> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(mapper.convertValue(item, DataSyndicationAgreementItem.class));
        }
        return r;
    }

    @Override
    public void submitAgreement(String refId, String name, Date startDate, Date endDate, List<Long> agreementItemIds, String slaveDomain,
            String permissions) {

        // TODO check the slave could be a slave

        SubmitDataSyndicationAgreementRequest submitAgreementRequest = new SubmitDataSyndicationAgreementRequest();
        submitAgreementRequest.refId = refId;
        submitAgreementRequest.name = name;
        submitAgreementRequest.startDate = startDate;
        submitAgreementRequest.endDate = endDate;
        submitAgreementRequest.agreementItemIds = agreementItemIds;
        submitAgreementRequest.slaveDomain = slaveDomain;
        submitAgreementRequest.permissions = permissions;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.valueToTree(submitAgreementRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/submit", null, content);

    }

    @Override
    public void acceptAgreement(Long id, DataSyndicationApiKey apiKey, String permissions) {

        // TODO to check in echannel
        // Impossible to accept a non-pending agreement
        // The current instance should be the slave of the agreement

        AcceptDataSyndicationAgreementRequest acceptAgreementRequest = new AcceptDataSyndicationAgreementRequest();
        acceptAgreementRequest.apiName = apiKey.name;
        acceptAgreementRequest.apiSecretKey = apiKey.secretKey;
        acceptAgreementRequest.apiSecretKey = apiKey.applicationKey;
        acceptAgreementRequest.permissions = permissions;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.valueToTree(acceptAgreementRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/accept", null, content);

    }

    @Override
    public void rejectAgreement(Long id, String permissions) {

        // TODO to check in echannel
        // Impossible to reject a non-pending agreement
        // The current instance should be the slave of the agreement

        PatchDataSyndicationAgreementRequest patchAgreementRequest = new PatchDataSyndicationAgreementRequest();
        patchAgreementRequest.permissions = permissions;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.valueToTree(patchAgreementRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/reject", null, content);

    }

    @Override
    public void cancelAgreement(Long id, String permissions) {

        // TODO to check in echannel
        // The current instance should be the master or the slave of the
        // agreement

        PatchDataSyndicationAgreementRequest patchAgreementRequest = new PatchDataSyndicationAgreementRequest();
        patchAgreementRequest.permissions = permissions;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.valueToTree(patchAgreementRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/cancel", null, content);

    }

    @Override
    public void suspendAgreement(Long id, String permissions) {

        // TODO to check in echannel
        // Impossible to suspend a non-ongoing agreement
        // The current instance should be the master of the agreement

        PatchDataSyndicationAgreementRequest patchAgreementRequest = new PatchDataSyndicationAgreementRequest();
        patchAgreementRequest.permissions = permissions;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.valueToTree(patchAgreementRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/suspend", null, content);

    }

    @Override
    public void restartAgreement(Long id, String permissions) {

        // TODO to check in echannel
        // Impossible to restart a non-suspended agreement
        // The current instance should be the master of the agreement

        PatchDataSyndicationAgreementRequest patchAgreementRequest = new PatchDataSyndicationAgreementRequest();
        patchAgreementRequest.permissions = permissions;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.valueToTree(patchAgreementRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/restart", null, content);

    }

    @Override
    public DataSyndicationAgreement getAgreement(Long id) {

        // TODO The current instance should be the master or the slave of the
        // agreement

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id, null, null);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(response, DataSyndicationAgreement.class);
    }

    @Override
    public void deleteAgreement(Long id) {

        // TODO The current instance should be the master or the slave of the
        // agreement

        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/delete", null, null);
    }

    @Override
    public List<DataSyndicationAgreement> getMasterAgreements() {

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_ACTION + "/find/as-master", null, null);

        ObjectMapper mapper = new ObjectMapper();
        List<DataSyndicationAgreement> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(mapper.convertValue(item, DataSyndicationAgreement.class));
        }

        return r;
    }

    @Override
    public List<DataSyndicationAgreement> getSlaveAgreements() {

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_ACTION + "/find/as-slave", null, null);

        ObjectMapper mapper = new ObjectMapper();
        List<DataSyndicationAgreement> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(mapper.convertValue(item, DataSyndicationAgreement.class));
        }

        return r;
    }

    @Override
    public List<DataSyndicationAgreementLink> getLinksOfAgreement(Long id) {

        // TODO The current instance should be the master or the slave of the
        // agreement

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/link/find", null, null);

        ObjectMapper mapper = new ObjectMapper();
        List<DataSyndicationAgreementLink> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(mapper.convertValue(item, DataSyndicationAgreementLink.class));
        }

        return r;
    }

    @Override
    public void submitAgreementLink(Long agreementId, List<DataSyndicationAgreementItem> agreementItems, String dataType, Long masterObjectId,
            String permissions) {

        // TODO to check in echannel
        // The current instance should be the master of the agreement

        SubmitDataSyndicationAgreementLinkRequest submitAgreementLinkRequest = new SubmitDataSyndicationAgreementLinkRequest();
        submitAgreementLinkRequest.agreementId = agreementId;
        submitAgreementLinkRequest.agreementItemIds = new ArrayList<>();
        for (DataSyndicationAgreementItem agreementItem : agreementItems) {
            submitAgreementLinkRequest.agreementItemIds.add(agreementItem.id);
        }
        submitAgreementLinkRequest.dataType = dataType;
        submitAgreementLinkRequest.masterObjectId = masterObjectId;
        submitAgreementLinkRequest.permissions = permissions;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.valueToTree(submitAgreementLinkRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/submit", null, content);

    }

    @Override
    public void acceptAgreementLink(Long id, Long slaveObjectId, String permissions) {

        // TODO to check in echannel
        // Impossible to accept a non-pending agreement link
        // The current instance should be the slave of the agreement

        AcceptDataSyndicationAgreementLinkRequest acceptAgreementLinkRequest = new AcceptDataSyndicationAgreementLinkRequest();
        acceptAgreementLinkRequest.slaveObjectId = slaveObjectId;
        acceptAgreementLinkRequest.permissions = permissions;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.valueToTree(acceptAgreementLinkRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/" + id + "/accept", null, content);

    }

    @Override
    public void rejectAgreementLink(Long id, String permissions) {

        // TODO to check in echannel
        // Impossible to accept a non-pending agreement link
        // The current instance should be the slave of the agreement

        PatchDataSyndicationAgreementLinkRequest patchAgreementLinkRequest = new PatchDataSyndicationAgreementLinkRequest();
        patchAgreementLinkRequest.permissions = permissions;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.valueToTree(patchAgreementLinkRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/" + id + "/reject", null, content);

    }

    @Override
    public void cancelAgreementLink(Long id, String permissions) {

        // TODO to check in echannel
        // The current instance should be the master or the slave of the
        // agreement

        PatchDataSyndicationAgreementLinkRequest patchAgreementLinkRequest = new PatchDataSyndicationAgreementLinkRequest();
        patchAgreementLinkRequest.permissions = permissions;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode content = mapper.valueToTree(patchAgreementLinkRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/" + id + "/cancel", null, content);

    }

    @Override
    public DataSyndicationAgreementLink getAgreementLink(Long id) {

        // TODO The current instance should be the master or the slave of the
        // agreement

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/" + id, null, null);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(response, DataSyndicationAgreementLink.class);
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksToSynchronize() {

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/find/to-synchronize", null, null);

        ObjectMapper mapper = new ObjectMapper();
        List<DataSyndicationAgreementLink> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(mapper.convertValue(item, DataSyndicationAgreementLink.class));
        }

        return r;

    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksOfMasterObject(String dataType, Long masterObjectId) {

        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair("dataType", dataType));
        queryParams.add(new BasicNameValuePair("masterObjectId", String.valueOf(masterObjectId)));

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/find/as-master", queryParams, null);

        ObjectMapper mapper = new ObjectMapper();
        List<DataSyndicationAgreementLink> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(mapper.convertValue(item, DataSyndicationAgreementLink.class));
        }

        return r;

    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksOfSlaveObject(String dataType, Long slaveObjectId) {

        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair("dataType", dataType));
        queryParams.add(new BasicNameValuePair("slaveObjectId", String.valueOf(slaveObjectId)));

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/find/as-slave", queryParams, null);

        ObjectMapper mapper = new ObjectMapper();
        List<DataSyndicationAgreementLink> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(mapper.convertValue(item, DataSyndicationAgreementLink.class));
        }

        return r;
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
