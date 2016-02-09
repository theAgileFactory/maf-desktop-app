package services.tableprovider;

import javax.inject.Inject;
import javax.inject.Singleton;

import framework.services.kpi.IKpiService;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;
import utils.table.ActorListView;
import utils.table.ApplicationBlockListView;
import utils.table.BudgetBucketListView;
import utils.table.DeliverableListView;
import utils.table.IterationListView;
import utils.table.OrgUnitListView;
import utils.table.PortfolioEntryBudgetLineListView;
import utils.table.PortfolioEntryEventListView;
import utils.table.PortfolioEntryPlanningPackageListView;
import utils.table.PortfolioEntryReportListView;
import utils.table.PortfolioEntryResourcePlanAllocatedActorListView;
import utils.table.PortfolioEntryResourcePlanAllocatedOrgUnitListView;
import utils.table.PortfolioEntryResourcePlanAllocatedResourceListView;
import utils.table.PortfolioEntryRiskListView;
import utils.table.PortfolioListView;
import utils.table.RequirementListView;
import utils.table.TimesheetActivityAllocatedActorListView;
import utils.table.WorkOrderListView;

/**
 * The service that provides the tables.
 * 
 * @author Johann Kohler
 */
@Singleton
public class TableProviderImpl implements ITableProvider {

    private IKpiService kpiService;

    private TableDefinitions tableDefinitions;

    /**
     * Initialize the service.
     * 
     * @param lifecycle
     *            the Play life cycle service
     * @param configuration
     *            the Play configuration service
     * @param kpiService
     *            the KPI service
     */
    @Inject
    public TableProviderImpl(ApplicationLifecycle lifecycle, Configuration configuration, IKpiService kpiService) {

        Logger.info("SERVICE>>> TableProviderImpl starting...");

        this.kpiService = kpiService;
        this.tableDefinitions = null;

        lifecycle.addStopHook(() -> {
            Logger.info("SERVICE>>> TableProviderImpl stopping...");
            Logger.info("SERVICE>>> TableProviderImpl stopped");
            return Promise.pure(null);
        });

        Logger.info("SERVICE>>> TableProviderImpl started");
    }

    @Override
    public TableDefinitions get() {
        if (this.tableDefinitions == null) {
            this.tableDefinitions = new TableDefinitions(kpiService);
        }
        return this.tableDefinitions;
    }

    @Override
    public void flushTables() {

        // table
        PortfolioEntryResourcePlanAllocatedResourceListView.templateTable = PortfolioEntryResourcePlanAllocatedResourceListView.getTable();
        PortfolioEntryResourcePlanAllocatedOrgUnitListView.templateTable = PortfolioEntryResourcePlanAllocatedOrgUnitListView.getTable();
        PortfolioEntryResourcePlanAllocatedActorListView.templateTable = PortfolioEntryResourcePlanAllocatedActorListView.getTable();
        TimesheetActivityAllocatedActorListView.templateTable = TimesheetActivityAllocatedActorListView.getTable();
        ApplicationBlockListView.templateTable = ApplicationBlockListView.getTable();
        BudgetBucketListView.templateTable = BudgetBucketListView.getTable();
        ActorListView.templateTable = ActorListView.getTable();
        PortfolioEntryEventListView.templateTable = PortfolioEntryEventListView.getTable();
        DeliverableListView.templateTable = DeliverableListView.getTable();

        this.get().portfolioEntry.templateTable = this.get().portfolioEntry.getTable(this.getKpiService());

        PortfolioEntryBudgetLineListView.templateTable = PortfolioEntryBudgetLineListView.getTable();
        PortfolioEntryReportListView.templateTable = PortfolioEntryReportListView.getTable();
        IterationListView.templateTable = IterationListView.getTable();
        OrgUnitListView.templateTable = OrgUnitListView.getTable();
        PortfolioEntryPlanningPackageListView.templateTable = PortfolioEntryPlanningPackageListView.getTable();
        PortfolioListView.templateTable = PortfolioListView.getTable();
        RequirementListView.templateTable = RequirementListView.getTable();
        PortfolioEntryRiskListView.templateTable = PortfolioEntryRiskListView.getTable();
        WorkOrderListView.templateTable = WorkOrderListView.getTable();

        // filterConfig

    }

    @Override
    public void flushFilterConfig() {

        ApplicationBlockListView.filterConfig = ApplicationBlockListView.getFilterConfig();
        DeliverableListView.filterConfig = DeliverableListView.getFilterConfig();
        IterationListView.filterConfig = IterationListView.getFilterConfig();
        PortfolioEntryEventListView.filterConfig = PortfolioEntryEventListView.getFilterConfig();

        this.get().portfolioEntry.filterConfig = this.get().portfolioEntry.getFilterConfig(this.getKpiService());

        PortfolioEntryPlanningPackageListView.filterConfig = PortfolioEntryPlanningPackageListView.getFilterConfig();
        PortfolioEntryResourcePlanAllocatedActorListView.filterConfig = PortfolioEntryResourcePlanAllocatedActorListView.getFilterConfig();
        RequirementListView.filterConfig = RequirementListView.getFilterConfig();
        TimesheetActivityAllocatedActorListView.filterConfig = TimesheetActivityAllocatedActorListView.getFilterConfig();

    }

    /**
     * Get the KPI service.
     */
    private IKpiService getKpiService() {
        return this.kpiService;
    }

}