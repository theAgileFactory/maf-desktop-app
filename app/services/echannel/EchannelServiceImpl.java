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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import constants.IMafConstants;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.api.AbstractApiController;
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
import services.echannel.models.NotificationEvent;
import services.echannel.models.RecipientsDescriptor;
import services.echannel.request.AcceptDataSyndicationAgreementLinkRequest;
import services.echannel.request.AcceptDataSyndicationAgreementRequest;
import services.echannel.request.LoginEventRequest;
import services.echannel.request.LoginEventRequest.ErrorCode;
import services.echannel.request.NotificationEventRequest;
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

    private ObjectMapper mapper;

    private static final String ACTION_PATTERN = "/{domain}/{action}";

    private static final String NOTIFICATION_EVENT_ACTION = "notification-event";

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

        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(AbstractApiController.DATE_FORMAT));
        this.mapper = mapper;

        Logger.info("SERVICE>>> EchannelServiceImpl started");
    }

    @Override
    public void createNotificationEvent(String domain, RecipientsDescriptor recipientsDescriptor, String title, String message, String actionLink)
            throws EchannelException {
        NotificationEventRequest notificationEventRequest = new NotificationEventRequest();
        notificationEventRequest.domain = domain;
        notificationEventRequest.recipientsDescriptor = recipientsDescriptor;
        notificationEventRequest.title = title;
        notificationEventRequest.message = message;
        notificationEventRequest.actionLink = actionLink;
        JsonNode content = getMapper().valueToTree(notificationEventRequest);
        this.call(HttpMethod.POST, NOTIFICATION_EVENT_ACTION, null, content);

    }

    @Override
    public List<NotificationEvent> getNotificationEventsToNotify() throws EchannelException {

        JsonNode response = this.call(HttpMethod.GET, NOTIFICATION_EVENT_ACTION + "/find", null, null);

        List<NotificationEvent> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(getMapper().convertValue(item, NotificationEvent.class));
        }

        return r;
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
            JsonNode content = getMapper().valueToTree(updateConsumedUsersRequest);
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
            JsonNode content = getMapper().valueToTree(updateConsumedPortfolioEntriesRequest);
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
            JsonNode content = getMapper().valueToTree(updateConsumedStorageRequest);
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
            JsonNode content = getMapper().valueToTree(loginEventRequest);
            this.call(HttpMethod.POST, LOGIN_EVENT_ACTION, null, content);
        } catch (Exception e) {
            Logger.error("eChannel service unexpected error / addLoginEvent", e);
        }

    }

    @Override
    public List<DataSyndicationPartner> findPartners(boolean eligibleAsSlave, String keywords) throws EchannelException {

        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair("eligibleAsSlave", String.valueOf(eligibleAsSlave)));
        queryParams.add(new BasicNameValuePair("keywords", keywords));

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_PARTNER_ACTION + "/find", queryParams, null);

        List<DataSyndicationPartner> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(getMapper().convertValue(item, DataSyndicationPartner.class));
        }

        return r;
    }

    @Override
    public DataSyndicationPartner getPartner(String domain) throws EchannelException {
        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_PARTNER_ACTION + "/" + domain, null, null);
        return getMapper().convertValue(response, DataSyndicationPartner.class);
    }

    @Override
    public List<DataSyndicationAgreementItem> getAgreementItems() throws EchannelException {
        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_ITEM_ACTION + "/find", null, null);
        List<DataSyndicationAgreementItem> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(getMapper().convertValue(item, DataSyndicationAgreementItem.class));
        }
        return r;
    }

    @Override
    public DataSyndicationAgreement submitAgreement(String refId, String name, Date startDate, Date endDate, List<Long> agreementItemIds, String slaveDomain)
            throws EchannelException {

        SubmitDataSyndicationAgreementRequest submitAgreementRequest = new SubmitDataSyndicationAgreementRequest();
        submitAgreementRequest.refId = refId;
        submitAgreementRequest.name = name;
        submitAgreementRequest.startDate = startDate;
        submitAgreementRequest.endDate = endDate;
        submitAgreementRequest.agreementItemIds = agreementItemIds;
        submitAgreementRequest.slaveDomain = slaveDomain;

        JsonNode content = getMapper().valueToTree(submitAgreementRequest);
        JsonNode response = this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/submit", null, content);
        return getMapper().convertValue(response, DataSyndicationAgreement.class);

    }

    @Override
    public void acceptAgreement(Long id, DataSyndicationApiKey apiKey) throws EchannelException {

        AcceptDataSyndicationAgreementRequest acceptAgreementRequest = new AcceptDataSyndicationAgreementRequest();
        acceptAgreementRequest.apiName = apiKey.name;
        acceptAgreementRequest.apiSecretKey = apiKey.secretKey;
        acceptAgreementRequest.apiApplicationKey = apiKey.applicationKey;

        JsonNode content = getMapper().valueToTree(acceptAgreementRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/accept", null, content);

    }

    @Override
    public void rejectAgreement(Long id) throws EchannelException {
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/reject", null, null);

    }

    @Override
    public void cancelAgreement(Long id) throws EchannelException {
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/cancel", null, null);
    }

    @Override
    public void suspendAgreement(Long id) throws EchannelException {
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/suspend", null, null);
    }

    @Override
    public void restartAgreement(Long id) throws EchannelException {
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/restart", null, null);
    }

    @Override
    public DataSyndicationAgreement getAgreement(Long id) throws EchannelException {
        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id, null, null);
        return getMapper().convertValue(response, DataSyndicationAgreement.class);
    }

    @Override
    public List<DataSyndicationAgreement> getAgreementsAsMaster() throws EchannelException {

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_ACTION + "/find/as-master", null, null);

        List<DataSyndicationAgreement> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(getMapper().convertValue(item, DataSyndicationAgreement.class));
        }

        return r;
    }

    @Override
    public List<DataSyndicationAgreement> getAgreementsAsSlave() throws EchannelException {

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_ACTION + "/find/as-slave", null, null);

        List<DataSyndicationAgreement> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(getMapper().convertValue(item, DataSyndicationAgreement.class));
        }

        return r;
    }

    @Override
    public List<DataSyndicationAgreementLink> getLinksOfAgreement(Long id) throws EchannelException {

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_ACTION + "/" + id + "/link/find", null, null);

        List<DataSyndicationAgreementLink> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(getMapper().convertValue(item, DataSyndicationAgreementLink.class));
        }

        return r;
    }

    @Override
    public DataSyndicationAgreementLink submitAgreementLink(String masterPrincipalUid, Long agreementId, String name, String description,
            List<Long> agreementItemIds, String dataType, Long masterObjectId) throws EchannelException {

        SubmitDataSyndicationAgreementLinkRequest submitAgreementLinkRequest = new SubmitDataSyndicationAgreementLinkRequest();
        submitAgreementLinkRequest.masterPrincipalUid = masterPrincipalUid;
        submitAgreementLinkRequest.agreementId = agreementId;
        submitAgreementLinkRequest.name = name;
        submitAgreementLinkRequest.description = description;
        submitAgreementLinkRequest.agreementItemIds = agreementItemIds;
        submitAgreementLinkRequest.dataType = dataType;
        submitAgreementLinkRequest.masterObjectId = masterObjectId;

        JsonNode content = getMapper().valueToTree(submitAgreementLinkRequest);
        JsonNode response = this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/submit", null, content);
        return getMapper().convertValue(response, DataSyndicationAgreementLink.class);

    }

    @Override
    public void acceptAgreementLink(Long id, Long slaveObjectId) throws EchannelException {

        AcceptDataSyndicationAgreementLinkRequest acceptAgreementLinkRequest = new AcceptDataSyndicationAgreementLinkRequest();
        acceptAgreementLinkRequest.slaveObjectId = slaveObjectId;

        JsonNode content = getMapper().valueToTree(acceptAgreementLinkRequest);
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/" + id + "/accept", null, content);

    }

    @Override
    public void rejectAgreementLink(Long id) throws EchannelException {
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/" + id + "/reject", null, null);
    }

    @Override
    public void cancelAgreementLink(Long id) throws EchannelException {
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/" + id + "/cancel", null, null);
    }

    @Override
    public DataSyndicationAgreementLink getAgreementLink(Long id) throws EchannelException {
        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/" + id, null, null);
        return getMapper().convertValue(response, DataSyndicationAgreementLink.class);
    }

    @Override
    public void deleteAgreementLink(Long id) throws EchannelException {
        this.call(HttpMethod.POST, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/" + id + "/delete", null, null);
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksToSynchronize() throws EchannelException {

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/find/to-synchronize", null, null);

        List<DataSyndicationAgreementLink> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(getMapper().convertValue(item, DataSyndicationAgreementLink.class));
        }

        return r;

    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksOfMasterObject(String dataType, Long masterObjectId) throws EchannelException {

        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair("dataType", dataType));
        queryParams.add(new BasicNameValuePair("masterObjectId", String.valueOf(masterObjectId)));

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/find/as-master", queryParams, null);

        List<DataSyndicationAgreementLink> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(getMapper().convertValue(item, DataSyndicationAgreementLink.class));
        }

        return r;

    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksOfSlaveObject(String dataType, Long slaveObjectId) throws EchannelException {

        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair("dataType", dataType));
        queryParams.add(new BasicNameValuePair("slaveObjectId", String.valueOf(slaveObjectId)));

        JsonNode response = this.call(HttpMethod.GET, DATA_SYNDICATION_AGREEMENT_LINK_ACTION + "/find/as-slave", queryParams, null);

        List<DataSyndicationAgreementLink> r = new ArrayList<>();
        for (JsonNode item : response) {
            r.add(getMapper().convertValue(item, DataSyndicationAgreementLink.class));
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
    private JsonNode call(HttpMethod httpMethod, String action, List<NameValuePair> queryParams, JsonNode content) throws EchannelException {

        String url = this.getActionUrl(action);

        Logger.debug("URL: " + url);

        WSRequest request = WS.url(url).setHeader("Content-Type", "application/octet-stream");
        request.setHeader(HTTP_HEADER_API_KEY, this.getApiSecretKey());

        if (queryParams != null) {
            for (NameValuePair param : queryParams) {
                request.setQueryParameter(param.getName(), param.getValue());
            }
        }

        Promise<WSResponse> response = null;

        String contentString = null;
        if (content != null) {
            try {
                contentString = getMapper().writeValueAsString(content);
            } catch (JsonProcessingException e) {
                throw new EchannelException(e.getMessage());
            }
        }

        switch (httpMethod) {
        case GET:
            response = request.get();
            break;
        case POST:
            response = request.post(contentString);
            break;
        case PUT:
            response = request.put(contentString);
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
            String errorMessage = "eChannel service call error / url: " + url + " / status: " + responseContent.getLeft() + " / errors: "
                    + responseContent.getRight().toString();
            throw new EchannelException(errorMessage);
        }

    }

    /**
     * Get the JSON object mapper.
     */
    private ObjectMapper getMapper() {
        return this.mapper;
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
