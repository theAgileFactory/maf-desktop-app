package services.tableprovider;

import javax.inject.Inject;
import javax.inject.Singleton;

import framework.services.kpi.IKpiService;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;

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
        this.get().portfolioEntryResourcePlanAllocatedResource.templateTable = this.get().portfolioEntryResourcePlanAllocatedResource.getTable();
        this.get().portfolioEntryResourcePlanAllocatedOrgUnit.templateTable = this.get().portfolioEntryResourcePlanAllocatedOrgUnit.getTable();
        this.get().portfolioEntryResourcePlanAllocatedActor.templateTable = this.get().portfolioEntryResourcePlanAllocatedActor.getTable();
        this.get().timesheetActivityAllocatedActor.templateTable = this.get().timesheetActivityAllocatedActor.getTable();
        this.get().applicationBlock.templateTable = this.get().applicationBlock.getTable();
        this.get().budgetBucket.templateTable = this.get().budgetBucket.getTable();
        this.get().actor.templateTable = this.get().actor.getTable();
        this.get().portfolioEntryEvent.templateTable = this.get().portfolioEntryEvent.getTable();
        this.get().deliverable.templateTable = this.get().deliverable.getTable();
        this.get().portfolioEntry.templateTable = this.get().portfolioEntry.getTable(this.getKpiService());
        this.get().portfolioEntryBudgetLine.templateTable = this.get().portfolioEntryBudgetLine.getTable();
        this.get().portfolioEntryReport.templateTable = this.get().portfolioEntryReport.getTable();
        this.get().iteration.templateTable = this.get().iteration.getTable();
        this.get().orgUnit.templateTable = this.get().orgUnit.getTable();
        this.get().portfolioEntryPlanningPackage.templateTable = this.get().portfolioEntryPlanningPackage.getTable();
        this.get().portfolio.templateTable = this.get().portfolio.getTable();
        this.get().requirement.templateTable = this.get().requirement.getTable();
        this.get().portfolioEntryRisk.templateTable = this.get().portfolioEntryRisk.getTable();
        this.get().workOrder.templateTable = this.get().workOrder.getTable();

    }

    @Override
    public void flushFilterConfig() {

        this.get().applicationBlock.filterConfig = this.get().applicationBlock.getFilterConfig();
        this.get().deliverable.filterConfig = this.get().deliverable.getFilterConfig();
        this.get().iteration.filterConfig = this.get().iteration.getFilterConfig();
        this.get().portfolioEntryEvent.filterConfig = this.get().portfolioEntryEvent.getFilterConfig();
        this.get().portfolioEntry.filterConfig = this.get().portfolioEntry.getFilterConfig(this.getKpiService());
        this.get().portfolioEntryPlanningPackage.filterConfig = this.get().portfolioEntryPlanningPackage.getFilterConfig();
        this.get().portfolioEntryResourcePlanAllocatedActor.filterConfig = this.get().portfolioEntryResourcePlanAllocatedActor.getFilterConfig();
        this.get().requirement.filterConfig = this.get().requirement.getFilterConfig();
        this.get().timesheetActivityAllocatedActor.filterConfig = this.get().timesheetActivityAllocatedActor.getFilterConfig();

    }

    /**
     * Get the KPI service.
     */
    private IKpiService getKpiService() {
        return this.kpiService;
    }

}