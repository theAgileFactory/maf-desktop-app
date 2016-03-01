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
package controllers.api.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.api.ApiController;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.kpi.IKpiService;
import play.Configuration;
import play.libs.Json;
import play.mvc.Result;
import services.tableprovider.ITableProvider;
import utils.reporting.IReportingUtils;

/**
 * The API controller for root actions.
 * 
 * @author Johann Kohler
 */
public class RootApiController extends ApiController {

    @Inject
    private IAccountManagerPlugin accountManagerPlugin;
    @Inject
    private II18nMessagesPlugin i8nMessagesPlugin;
    @Inject
    private IKpiService kpiService;
    @Inject
    private IReportingUtils reportingUtils;
    @Inject
    private Configuration configuration;
    @Inject
    private ITableProvider tableProvider;

    /**
     * Get the status of the instance.
     **/
    @ApiAuthentication(onlyRootKey = true)
    public Result instanceStatus() {

        try {

            RootResponse response = new RootResponse();
            response.attributes = new HashMap<String, JsonNode>();
            response.attributes.put("platform.name", Json.toJson(getConfiguration().getString("maf.platformName")));
            return getJsonSuccessResponse(response);

        } catch (Exception e) {

            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));

        }
    }

    /**
     * Return some information regarding the memory status of the instance.
     */
    @ApiAuthentication(onlyRootKey = true)
    public Result instanceMemoryStatus() {

        try {

            RootResponse response = new RootResponse();

            response.attributes = new HashMap<String, JsonNode>();
            List<MemoryPoolMXBean> mbeans = ManagementFactory.getMemoryPoolMXBeans();
            if (mbeans != null) {
                for (MemoryPoolMXBean mbean : mbeans) {
                    System.out.println(mbean.getName());
                    MemoryUsage memUsage = mbean.getUsage();
                    HashMap<String, Long> memoryUsageAsMap = new HashMap<String, Long>();
                    memoryUsageAsMap.put("init", memUsage.getInit());
                    memoryUsageAsMap.put("max", memUsage.getMax());
                    memoryUsageAsMap.put("committed", memUsage.getCommitted());
                    memoryUsageAsMap.put("used", memUsage.getUsed());
                    response.attributes.put(mbean.getName(), Json.toJson(memoryUsageAsMap));
                }
            }

            return getJsonSuccessResponse(response);

        } catch (Exception e) {

            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));

        }
    }

    /**
     * Trigger the resync of the user accounts.
     */
    @ApiAuthentication(onlyRootKey = true)
    public Result userResync() {

        try {

            RootResponse response = new RootResponse();

            response.attributes = new HashMap<String, JsonNode>();
            List<IUserAccount> userAccounts = getAccountManagerPlugin().getUserAccountsFromName("*");
            for (IUserAccount userAccount : userAccounts) {
                getAccountManagerPlugin().resync(userAccount.getUid());
                response.attributes.put(userAccount.getMail(), Json.toJson("Resync requested"));
            }

            return getJsonSuccessResponse(response);

        } catch (Exception e) {

            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));

        }
    }

    /**
     * Flush the user account cache.
     */
    @ApiAuthentication(onlyRootKey = true)
    public Result userFlushCache() {

        try {

            RootResponse response = new RootResponse();

            response.attributes = new HashMap<String, JsonNode>();
            List<IUserAccount> userAccounts = getAccountManagerPlugin().getUserAccountsFromName("*");
            for (IUserAccount userAccount : userAccounts) {
                getAccountManagerPlugin().invalidateUserAccountCache(userAccount.getUid());
                response.attributes.put(userAccount.getUid(), Json.toJson("flushed"));
            }

            return getJsonSuccessResponse(response);

        } catch (Exception e) {

            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));

        }
    }

    /**
     * Flush the i18n cache.
     */
    @ApiAuthentication(onlyRootKey = true)
    public Result i18nFlushCache() {

        try {
            getI8nMessagesPlugin().reload(true);

            RootResponse response = new RootResponse();

            return getJsonSuccessResponse(response);

        } catch (Exception e) {

            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));

        }
    }

    /**
     * Flush the tables cache.
     */
    @ApiAuthentication(onlyRootKey = true)
    public Result tableFlushCache() {

        try {

            this.getTableProvider().flushTables();

            RootResponse response = new RootResponse();

            return getJsonSuccessResponse(response);

        } catch (Exception e) {

            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));

        }
    }

    /**
     * Flush the filters cache.
     */
    @ApiAuthentication(onlyRootKey = true)
    public Result filterFlushCache() {

        try {

            this.getTableProvider().flushFilterConfig();

            RootResponse response = new RootResponse();

            return getJsonSuccessResponse(response);

        } catch (Exception e) {

            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));

        }
    }

    /**
     * Reload the report definitions (jasper).
     */
    @ApiAuthentication(onlyRootKey = true)
    public Result reportReload() {

        try {

            getReportingUtils().loadDefinitions();

            RootResponse response = new RootResponse();

            return getJsonSuccessResponse(response);

        } catch (Exception e) {

            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));

        }
    }

    /**
     * Reload the KPI definitions.
     */
    @ApiAuthentication(onlyRootKey = true)
    public Result kpiReload() {

        try {

            getKpiService().reload();

            RootResponse response = new RootResponse();

            return getJsonSuccessResponse(response);

        } catch (Exception e) {

            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));

        }
    }

    /**
     * The object class which represents a response to a root operation. This
     * one is marshalled using Jackson (JSON).
     * 
     * @author Johann Kohler
     */
    public static class RootResponse {
        public Map<String, JsonNode> attributes;
    }

    /**
     * Get the account manager service.
     */
    private IAccountManagerPlugin getAccountManagerPlugin() {
        return accountManagerPlugin;
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI8nMessagesPlugin() {
        return i8nMessagesPlugin;
    }

    /**
     * Get the KPI service.
     */
    private IKpiService getKpiService() {
        return kpiService;
    }

    /**
     * Get the reporting utils.
     */
    private IReportingUtils getReportingUtils() {
        return reportingUtils;
    }

    /**
     * Get the Play configuration service.
     */
    private Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }

}
