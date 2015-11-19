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
import framework.services.remote.IAdPanelManagerService;
import play.Configuration;
import play.libs.Json;
import play.mvc.Result;
import utils.reporting.IReportingUtils;
import utils.table.ActorListView;
import utils.table.AttachmentListView;
import utils.table.BudgetBucketLineListView;
import utils.table.BudgetBucketListView;
import utils.table.GoodsReceiptListView;
import utils.table.IterationListView;
import utils.table.OrgUnitListView;
import utils.table.PortfolioEntryBudgetLineListView;
import utils.table.PortfolioEntryEventListView;
import utils.table.PortfolioEntryListView;
import utils.table.PortfolioEntryPlanningPackageListView;
import utils.table.PortfolioEntryReportListView;
import utils.table.PortfolioEntryResourcePlanAllocatedActorListView;
import utils.table.PortfolioEntryResourcePlanAllocatedOrgUnitListView;
import utils.table.PortfolioEntryResourcePlanAllocatedResourceListView;
import utils.table.PortfolioEntryRiskListView;
import utils.table.PortfolioListView;
import utils.table.PurchaseOrderLineItemListView;
import utils.table.PurchaseOrderLineItemWorkOrderListView;
import utils.table.PurchaseOrderListView;
import utils.table.RequirementListView;
import utils.table.TimesheetActivityAllocatedActorListView;
import utils.table.WorkOrderListView;

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
    private IAdPanelManagerService adPanelManagerService;
    @Inject
    private IKpiService kpiService;
    @Inject
    private IReportingUtils reportingUtils;
    @Inject
    private Configuration Configuration;

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

            flushTables();

            RootResponse response = new RootResponse();

            return getJsonSuccessResponse(response);

        } catch (Exception e) {

            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));

        }
    }

    /**
     * Flush the tables cache (must be called after having updated the tables:
     * custom attributes...).
     */
    public static void flushTables() {
        PortfolioEntryListView.templateTable = PortfolioEntryListView.getTable();
        ActorListView.templateTable = ActorListView.getTable();
        AttachmentListView.templateTable = AttachmentListView.getTable();
        BudgetBucketLineListView.templateTable = BudgetBucketLineListView.getTable();
        BudgetBucketListView.templateTable = BudgetBucketListView.getTable();
        GoodsReceiptListView.templateTable = GoodsReceiptListView.getTable();
        OrgUnitListView.templateTable = OrgUnitListView.getTable();
        PortfolioEntryBudgetLineListView.templateTable = PortfolioEntryBudgetLineListView.getTable();
        PortfolioEntryReportListView.templateTable = PortfolioEntryReportListView.getTable();
        PortfolioEntryResourcePlanAllocatedActorListView.templateTable = PortfolioEntryResourcePlanAllocatedActorListView.getTable();
        PortfolioEntryResourcePlanAllocatedOrgUnitListView.templateTable = PortfolioEntryResourcePlanAllocatedOrgUnitListView.getTable();
        PortfolioEntryResourcePlanAllocatedResourceListView.templateTable = PortfolioEntryResourcePlanAllocatedResourceListView.getTable();
        PortfolioEntryRiskListView.templateTable = PortfolioEntryRiskListView.getTable();
        PortfolioEntryEventListView.templateTable = PortfolioEntryEventListView.getTable();
        PortfolioEntryPlanningPackageListView.templateTable = PortfolioEntryPlanningPackageListView.getTable();
        PortfolioListView.templateTable = PortfolioListView.getTable();
        PurchaseOrderLineItemListView.templateTable = PurchaseOrderLineItemListView.getTable();
        PurchaseOrderLineItemWorkOrderListView.templateTable = PurchaseOrderLineItemWorkOrderListView.getTable();
        PurchaseOrderListView.templateTable = PurchaseOrderListView.getTable();
        WorkOrderListView.templateTable = WorkOrderListView.getTable();
        RequirementListView.templateTable = RequirementListView.getTable();
        IterationListView.templateTable = IterationListView.getTable();
        TimesheetActivityAllocatedActorListView.templateTable = TimesheetActivityAllocatedActorListView.getTable();
    }

    /**
     * Flush the filters cache.
     */
    @ApiAuthentication(onlyRootKey = true)
    public Result filterFlushCache() {

        try {

            flushFilters();

            RootResponse response = new RootResponse();

            return getJsonSuccessResponse(response);

        } catch (Exception e) {

            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));

        }
    }

    /**
     * Flush the filters cache (must be called after having updated the filters:
     * custom attributes, select values...).
     */
    public static void flushFilters() {
        PortfolioEntryListView.filterConfig = PortfolioEntryListView.getFilterConfig();
        RequirementListView.filterConfig = RequirementListView.getFilterConfig();
        IterationListView.filterConfig = IterationListView.getFilterConfig();
        PortfolioEntryPlanningPackageListView.filterConfig = PortfolioEntryPlanningPackageListView.getFilterConfig();
        PortfolioEntryEventListView.filterConfig = PortfolioEntryEventListView.getFilterConfig();
        PortfolioEntryResourcePlanAllocatedActorListView.filterConfig = PortfolioEntryResourcePlanAllocatedActorListView.getFilterConfig();
        TimesheetActivityAllocatedActorListView.filterConfig = TimesheetActivityAllocatedActorListView.getFilterConfig();
    }

    /**
     * Flush the adPanel cache.
     */
    @ApiAuthentication(onlyRootKey = true)
    public Result adPanelFlushCache() {

        try {

            getAdPanelManagerService().clearCache();

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

    private IAccountManagerPlugin getAccountManagerPlugin() {
        return accountManagerPlugin;
    }

    private II18nMessagesPlugin getI8nMessagesPlugin() {
        return i8nMessagesPlugin;
    }

    private IAdPanelManagerService getAdPanelManagerService() {
        return adPanelManagerService;
    }

    private IKpiService getKpiService() {
        return kpiService;
    }

    private IReportingUtils getReportingUtils() {
        return reportingUtils;
    }

    private Configuration getConfiguration() {
        return Configuration;
    }

}
