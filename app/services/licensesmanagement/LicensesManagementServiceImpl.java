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
package services.licensesmanagement;

import java.util.ArrayList;
import java.util.List;

import models.framework_models.account.Principal;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import play.Logger;
import play.Play;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.licensesmanagement.LoginEventRequest.ErrorCode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import constants.IMafConstants;
import dao.pmo.PortfolioEntryDao;
import framework.services.ServiceManager;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.ext.IExtensionManagerService;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.services.storage.ISharedStorageService;

/**
 * The licenses management plugin.
 * 
 * @author Johann Kohler
 * 
 */
public class LicensesManagementServiceImpl implements ILicensesManagementService {

    private boolean isActive;
    private String echannelApiUrl;
    private String apiSecretKey;

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
     * Initialize the plugin.
     */
    public LicensesManagementServiceImpl() {
        this.isActive = Play.application().configuration().getBoolean("maf.licenses_management.is_active");
        this.echannelApiUrl = Play.application().configuration().getString("maf.licenses_management.echannel_api.url");
        this.apiSecretKey = null;
    }

    @Override
    public boolean canCreateUser() {

        try {
            if (this.isActive) {

                List<NameValuePair> queryParams = new ArrayList<>();
                queryParams.add(new BasicNameValuePair("consumedUsers", String.valueOf(Principal.getConsumedUsers())));

                JsonNode response = this.call(HttpMethod.GET, CAN_CREATE_USER_ACTION, queryParams, null);
                if (response != null) {
                    return response.asBoolean();
                }
            }
        } catch (Exception e) {
            Logger.error("Licenses managament unexpected error / canCreateUser", e);
        }

        return true;
    }

    @Override
    public boolean canCreatePortfolioEntry() {

        try {
            if (this.isActive) {

                List<NameValuePair> queryParams = new ArrayList<>();
                queryParams.add(new BasicNameValuePair("consumedPortfolioEntries", String.valueOf(PortfolioEntryDao.getPEAsExpr(false).findRowCount())));

                JsonNode response = this.call(HttpMethod.GET, CAN_CREATE_PORTOLIO_ENTRY_ACTION, queryParams, null);
                if (response != null) {
                    return response.asBoolean();
                }
            }
        } catch (Exception e) {
            Logger.error("Licenses managament unexpected error / canCreatePortfolioEntry", e);
        }

        return true;
    }

    @Override
    public boolean isInstanceAccessible() {

        try {
            if (this.isActive) {
                JsonNode response = this.call(HttpMethod.GET, IS_ACCESSIBLE_ACTION, null, null);
                if (response != null) {
                    return response.asBoolean();
                }
            }
        } catch (Exception e) {
            Logger.error("Licenses managament unexpected error / isInstanceAccessible", e);
        }

        return true;
    }

    @Override
    public void updateConsumedUsers() {

        try {
            if (this.isActive) {

                UpdateConsumedUsersRequest updateConsumedUsersRequest = new UpdateConsumedUsersRequest();
                updateConsumedUsersRequest.consumedUsers = Principal.getConsumedUsers();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode content = mapper.valueToTree(updateConsumedUsersRequest);
                this.call(HttpMethod.PUT, CONSUMED_USERS_ACTION, null, content);
            }
        } catch (Exception e) {
            Logger.error("Licenses managament unexpected error / updateConsumedUsers", e);
        }

    }

    @Override
    public void updateConsumedPortfolioEntries() {

        try {
            if (this.isActive) {

                UpdateConsumedPortfolioEntriesRequest updateConsumedPortfolioEntriesRequest = new UpdateConsumedPortfolioEntriesRequest();
                updateConsumedPortfolioEntriesRequest.consumedPortfolioEntries = PortfolioEntryDao.getPEAsExpr(false).findRowCount();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode content = mapper.valueToTree(updateConsumedPortfolioEntriesRequest);
                this.call(HttpMethod.PUT, CONSUMED_PORTFOLIO_ENTRIES_ACTION, null, content);
            }
        } catch (Exception e) {
            Logger.error("Licenses managament unexpected error / updateConsumedPortfolioEntries", e);
        }

    }

    @Override
    public void updateConsumedStorage() {

        try {
            if (this.isActive) {

                // shared storage
                long sharedStorage = ServiceManager.getService(ISharedStorageService.NAME, ISharedStorageService.class).getSize();
                Logger.debug("sharedStorage (B): " + sharedStorage);

                // personal storage
                long personalStorage = ServiceManager.getService(IPersonalStoragePlugin.NAME, IPersonalStoragePlugin.class).getSize();
                Logger.debug("personalStorage (B): " + personalStorage);

                // attachments
                long attachments = ServiceManager.getService(IAttachmentManagerPlugin.NAME, IAttachmentManagerPlugin.class).getSize();
                Logger.debug("attachments (B): " + attachments);

                // extensions
                long extensions = ServiceManager.getService(IExtensionManagerService.NAME, IExtensionManagerService.class).getSize();
                Logger.debug("extensions (B): " + extensions);

                int storage = (int) (sharedStorage + personalStorage + attachments + extensions) / (1024 * 1024 * 1024);
                Logger.debug("storage (GB): " + storage);

                UpdateConsumedStorageRequest updateConsumedStorageRequest = new UpdateConsumedStorageRequest();
                updateConsumedStorageRequest.consumedStorage = storage;
                ObjectMapper mapper = new ObjectMapper();
                JsonNode content = mapper.valueToTree(updateConsumedStorageRequest);
                this.call(HttpMethod.PUT, CONSUMED_STORAGE_ACTION, null, content);

            }
        } catch (Exception e) {
            Logger.error("Licenses managament unexpected error / updateConsumedStorage", e);
        }

    }

    @Override
    public void addLoginEvent(String uid, Boolean result, ErrorCode errorCode, String errorMessage) {

        try {
            if (this.isActive) {

                LoginEventRequest loginEventRequest = new LoginEventRequest();
                loginEventRequest.errorCode = errorCode;
                loginEventRequest.errorMessage = errorMessage;
                loginEventRequest.result = result;
                loginEventRequest.uid = uid;
                ObjectMapper mapper = new ObjectMapper();
                JsonNode content = mapper.valueToTree(loginEventRequest);
                this.call(HttpMethod.POST, LOGIN_EVENT_ACTION, null, content);

            }
        } catch (Exception e) {
            Logger.error("Licenses managament unexpected error / addLoginEvent", e);
        }

    }

    /**
     * Get the URL for an action.
     * 
     * @param action
     *            the action
     */
    private String getActionUrl(String action) {

        String domain =
                ServiceManager.getService(IPreferenceManagerPlugin.NAME, IPreferenceManagerPlugin.class).getPreferenceValueAsString(
                        IMafConstants.LICENSE_INSTANCE_DOMAIN_PREFERENCE);

        String url = ACTION_PATTERN.replace("{domain}", domain);
        url = url.replace("{action}", action);

        return echannelApiUrl + url;
    }

    /**
     * Get the API secret key.
     */
    private String getApiSecretKey() {
        if (this.apiSecretKey == null) {
            this.apiSecretKey =
                    ServiceManager.getService(IPreferenceManagerPlugin.NAME, IPreferenceManagerPlugin.class).getPreferenceValueAsString(
                            IMafConstants.LICENSE_ECHANNEL_API_SECRET_KEY_PREFERENCE);
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
            Logger.error("Licenses managament call error / url: " + url + " / status: " + responseContent.getLeft() + " / errors: "
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
}
