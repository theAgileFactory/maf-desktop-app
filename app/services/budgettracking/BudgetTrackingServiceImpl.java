package services.budgettracking;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;

import constants.IMafConstants;
import dao.finance.CurrencyDAO;
import dao.finance.PortfolioEntryBudgetDAO;
import dao.finance.PurchaseOrderDAO;
import dao.finance.WorkOrderDAO;
import dao.timesheet.TimesheetDao;
import framework.services.account.IPreferenceManagerPlugin;
import models.finance.PortfolioEntryBudgetLine;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.finance.PurchaseOrderLineItem;
import models.finance.WorkOrder;
import models.governance.LifeCycleInstancePlanning;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryPlanningPackage;
import models.timesheet.TimesheetLog;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;

/**
 * The implementation of the budget tracking service.
 * 
 * @author Johann Kohler
 *
 */
@Singleton
public class BudgetTrackingServiceImpl implements IBudgetTrackingService {

    private IPreferenceManagerPlugin preferenceManagerPlugin;

    /**
     * Initialize the service.
     * 
     * @param lifecycle
     *            the Play life cycle service
     * @param configuration
     *            the Play configuration service
     * @param preferenceManagerPlugin
     *            the preference manager service
     */
    @Inject
    public BudgetTrackingServiceImpl(ApplicationLifecycle lifecycle, Configuration configuration, IPreferenceManagerPlugin preferenceManagerPlugin) {

        Logger.info("SERVICE>>> BudgetTrackingServiceImpl starting...");

        lifecycle.addStopHook(() -> {
            Logger.info("SERVICE>>> BudgetTrackingServiceImpl stopping...");
            Logger.info("SERVICE>>> BudgetTrackingServiceImpl stopped");
            return Promise.pure(null);
        });

        this.preferenceManagerPlugin = preferenceManagerPlugin;

        Logger.info("SERVICE>>> BudgetTrackingServiceImpl started");
    }

    @Override
    public boolean isActive() {
        return this.getPreferenceManagerPlugin().getPreferenceValueAsBoolean(IMafConstants.BUDGET_TRACKING_EFFORT_BASED_PREFERENCE);
    }

    @Override
    public void recomputeAllBugdetAndForecastFromResource(LifeCycleInstancePlanning planning) {

        PortfolioEntry portfolioEntry = planning.lifeCycleInstance.portfolioEntry;

        Map<Long, Map<Long, Double>> timesheetLogsAsMap = getTimesheetLogsAsMapByPE(portfolioEntry.id);

        Map<Long, PortfolioEntryBudgetLine> budgetsAsMap = new HashMap<>();
        for (PortfolioEntryBudgetLine budgetLine : PortfolioEntryBudgetDAO.getPEBudgetLineAsListAndResourceByPE(portfolioEntry.id)) {
            budgetsAsMap.put(budgetLine.id, budgetLine);
        }

        Map<Long, WorkOrder> workOrdersAsMap = new HashMap<>();
        for (WorkOrder workOrder : WorkOrderDAO.getWorkOrderAsListAndResourceByPE(portfolioEntry.id)) {
            workOrdersAsMap.put(workOrder.id, workOrder);
        }

        for (PortfolioEntryResourcePlanAllocatedActor allocatedActor : planning.portfolioEntryResourcePlan.portfolioEntryResourcePlanAllocatedActors) {

            // compute the amounts
            BigDecimal budgetAmount = allocatedActor.dailyRate.multiply(allocatedActor.days);
            Pair<BigDecimal, BigDecimal> forecast = getForecastFromAllocatedActor(allocatedActor, timesheetLogsAsMap);

            genetateBudgetAndForecastFromResource(planning, allocatedActor.id, PortfolioEntryResourcePlanAllocatedActor.class.getName(),
                    allocatedActor.actor.getName(), allocatedActor.followPackageDates, allocatedActor.portfolioEntryPlanningPackage, budgetAmount,
                    forecast.getLeft(), forecast.getRight(), budgetsAsMap, workOrdersAsMap);

        }

        // org unit
        for (PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit : planning.portfolioEntryResourcePlan.portfolioEntryResourcePlanAllocatedOrgUnits) {

            // compute the amount
            BigDecimal budgetAmount = allocatedOrgUnit.dailyRate.multiply(allocatedOrgUnit.days);
            Pair<BigDecimal, BigDecimal> forecast = getForecastFromAllocatedOrgUnit(allocatedOrgUnit, timesheetLogsAsMap);

            genetateBudgetAndForecastFromResource(planning, allocatedOrgUnit.id, PortfolioEntryResourcePlanAllocatedOrgUnit.class.getName(),
                    allocatedOrgUnit.orgUnit.getName(), allocatedOrgUnit.followPackageDates, allocatedOrgUnit.portfolioEntryPlanningPackage, budgetAmount,
                    forecast.getLeft(), forecast.getRight(), budgetsAsMap, workOrdersAsMap);

        }

        // competency
        for (PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency : planning.portfolioEntryResourcePlan.portfolioEntryResourcePlanAllocatedCompetencies) {

            // compute the budget
            BigDecimal budgetAmount = allocatedCompetency.dailyRate.multiply(allocatedCompetency.days);
            Pair<BigDecimal, BigDecimal> forecast = getForecastFromAllocatedCompetency(allocatedCompetency, timesheetLogsAsMap);

            genetateBudgetAndForecastFromResource(planning, allocatedCompetency.id, PortfolioEntryResourcePlanAllocatedCompetency.class.getName(),
                    allocatedCompetency.competency.getName(), allocatedCompetency.followPackageDates, allocatedCompetency.portfolioEntryPlanningPackage,
                    budgetAmount, forecast.getLeft(), forecast.getRight(), budgetsAsMap, workOrdersAsMap);

        }

        for (Entry<Long, PortfolioEntryBudgetLine> entry : budgetsAsMap.entrySet()) {
            entry.getValue().doDelete();
        }
        for (Entry<Long, WorkOrder> entry : workOrdersAsMap.entrySet()) {
            WorkOrder workOrder = entry.getValue();
            if (workOrder.purchaseOrderLineItem != null) {
                workOrder.purchaseOrderLineItem.doDelete();
            }
            workOrder.doDelete();
        }

        portfolioEntry.budgetTrackingLastRun = new Date();
        portfolioEntry.budgetTrackingHasUnallocatedTimesheet = timesheetLogsAsMap.size() > 0;
        portfolioEntry.save();

    }

    /**
     * Get the timesheet logs of a portfolio entry and reorganize them as a map:
     * Actor[id]->Package[id]->Hours.
     * 
     * If the log concerns an entry without a planning package, then the "null"
     * key is used.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    private Map<Long, Map<Long, Double>> getTimesheetLogsAsMapByPE(Long portfolioEntryId) {
        Map<Long, Map<Long, Double>> timesheetLogsAsMap = new HashMap<>();
        List<TimesheetLog> timesheetLogs = TimesheetDao.getTimesheetLogActiveAsExprByPortfolioEntry(portfolioEntryId).findList();
        for (TimesheetLog timesheetLog : timesheetLogs) {
            Long actorId = timesheetLog.timesheetEntry.timesheetReport.actor.id;
            Long packageId = timesheetLog.timesheetEntry.portfolioEntryPlanningPackage != null ? timesheetLog.timesheetEntry.portfolioEntryPlanningPackage.id
                    : null;
            Map<Long, Double> actorMap = !timesheetLogsAsMap.containsKey(actorId) ? timesheetLogsAsMap.put(actorId, new HashMap<>())
                    : timesheetLogsAsMap.get(actorId);
            Double total = actorMap.containsKey(packageId) ? actorMap.get(packageId) : 0.0;
            actorMap.put(packageId, total + timesheetLog.hours);
        }
        return timesheetLogsAsMap;
    }

    /**
     * Compute and get the forecast amounts (cost to complete and engage) from
     * an allocated actor.
     * 
     * @param allocatedActor
     *            the allocated actor
     * @param timesheetLogsAsMap
     *            the timesheet logs of the portfolio entry as a map
     */
    private Pair<BigDecimal, BigDecimal> getForecastFromAllocatedActor(PortfolioEntryResourcePlanAllocatedActor allocatedActor,
            Map<Long, Map<Long, Double>> timesheetLogsAsMap) {
        // compute the cost to complete and engaged amounts
        Long packageId = allocatedActor.portfolioEntryPlanningPackage != null ? allocatedActor.portfolioEntryPlanningPackage.id : null;
        BigDecimal engagedAmount = BigDecimal.ZERO;
        BigDecimal costToCompleteAmount = BigDecimal.ZERO;
        if (timesheetLogsAsMap.containsKey(allocatedActor.actor.id) && timesheetLogsAsMap.get(allocatedActor.actor.id).containsKey(packageId)) {
            BigDecimal hours = new BigDecimal(timesheetLogsAsMap.get(allocatedActor.actor.id).get(packageId));
            BigDecimal days = hours.divide(TimesheetDao.getTimesheetReportHoursPerDay(), BigDecimal.ROUND_HALF_UP);
            engagedAmount = allocatedActor.dailyRate.multiply(days);
            costToCompleteAmount = allocatedActor.dailyRate.multiply(allocatedActor.forecastDays.subtract(days));
            timesheetLogsAsMap.get(allocatedActor.actor.id).remove(packageId);
            if (timesheetLogsAsMap.get(allocatedActor.actor.id).size() == 0) {
                timesheetLogsAsMap.remove(allocatedActor.actor.id);
            }
        }
        return Pair.of(costToCompleteAmount, engagedAmount);
    }

    /**
     * Compute and get the forecast amounts (cost to complete and engage) from
     * an allocated org unit.
     * 
     * @param allocatedOrgUnit
     *            the allocated org unit
     * @param timesheetLogsAsMap
     *            the timesheet logs of the portfolio entry as a map
     */
    private Pair<BigDecimal, BigDecimal> getForecastFromAllocatedOrgUnit(PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit,
            Map<Long, Map<Long, Double>> timesheetLogsAsMap) {
        Long packageId = allocatedOrgUnit.portfolioEntryPlanningPackage != null ? allocatedOrgUnit.portfolioEntryPlanningPackage.id : null;
        BigDecimal engagedAmount = BigDecimal.ZERO;
        BigDecimal costToCompleteAmount = BigDecimal.ZERO;
        for (Actor actor : allocatedOrgUnit.orgUnit.actors) {
            if (timesheetLogsAsMap.containsKey(actor.id) && timesheetLogsAsMap.get(actor.id).containsKey(packageId)) {
                BigDecimal hours = new BigDecimal(timesheetLogsAsMap.get(actor.id).get(packageId));
                BigDecimal days = hours.divide(TimesheetDao.getTimesheetReportHoursPerDay(), BigDecimal.ROUND_HALF_UP);
                engagedAmount = engagedAmount.add(allocatedOrgUnit.dailyRate.multiply(days));
                costToCompleteAmount = costToCompleteAmount.add(allocatedOrgUnit.dailyRate.multiply(allocatedOrgUnit.forecastDays.subtract(days)));
                timesheetLogsAsMap.get(actor.id).remove(packageId);
                if (timesheetLogsAsMap.get(actor.id).size() == 0) {
                    timesheetLogsAsMap.remove(actor.id);
                }
            }
        }
        return Pair.of(costToCompleteAmount, engagedAmount);
    }

    /**
     * Compute and get the forecast amounts (cost to complete and engage) from
     * an allocated competency.
     * 
     * @param allocatedCompetency
     *            the allocated competency
     * @param timesheetLogsAsMap
     *            the timesheet logs of the portfolio entry as a map
     */
    private Pair<BigDecimal, BigDecimal> getForecastFromAllocatedCompetency(PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency,
            Map<Long, Map<Long, Double>> timesheetLogsAsMap) {
        Long packageId = allocatedCompetency.portfolioEntryPlanningPackage != null ? allocatedCompetency.portfolioEntryPlanningPackage.id : null;
        BigDecimal engagedAmount = BigDecimal.ZERO;
        BigDecimal costToCompleteAmount = BigDecimal.ZERO;
        for (Actor actor : allocatedCompetency.competency.actors) {
            if (timesheetLogsAsMap.containsKey(actor.id) && timesheetLogsAsMap.get(actor.id).containsKey(packageId)) {
                BigDecimal hours = new BigDecimal(timesheetLogsAsMap.get(actor.id).get(packageId));
                BigDecimal days = hours.divide(TimesheetDao.getTimesheetReportHoursPerDay(), BigDecimal.ROUND_HALF_UP);
                engagedAmount = engagedAmount.add(allocatedCompetency.dailyRate.multiply(days));
                costToCompleteAmount = costToCompleteAmount.add(allocatedCompetency.dailyRate.multiply(allocatedCompetency.forecastDays.subtract(days)));
                timesheetLogsAsMap.get(actor.id).remove(packageId);
                if (timesheetLogsAsMap.get(actor.id).size() == 0) {
                    timesheetLogsAsMap.remove(actor.id);
                }
            }
        }
        return Pair.of(costToCompleteAmount, engagedAmount);
    }

    /**
     * Generate the budget and the forecast for a resource.
     * 
     * @param planning
     *            the current planning of the portfolio entry
     * @param resourceObjectId
     *            the id of the corresponding allocation
     * @param resourceObjectType
     *            the type of the corresponding allocation
     * @param name
     *            the name of the resource
     * @param followPackageDates
     *            the followPackageDates flag
     * @param planningPackage
     *            the assigned planning package
     * @param budgetAmount
     *            the budget
     * @param costToCompleteAmount
     *            the cost to complete
     * @param engagedAmount
     *            the engaged amount
     * @param budgetsAsMap
     *            the resource budget lines
     * @param workOrdersAsMap
     *            the resource work orders
     */
    private void genetateBudgetAndForecastFromResource(LifeCycleInstancePlanning planning, Long resourceObjectId, String resourceObjectType, String name,
            Boolean followPackageDates, PortfolioEntryPlanningPackage planningPackage, BigDecimal budgetAmount, BigDecimal costToCompleteAmount,
            BigDecimal engagedAmount, Map<Long, PortfolioEntryBudgetLine> budgetsAsMap, Map<Long, WorkOrder> workOrdersAsMap) {

        PortfolioEntry portfolioEntry = planning.lifeCycleInstance.portfolioEntry;

        boolean usePurchaseOrder = PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(getPreferenceManagerPlugin());

        // budget
        PortfolioEntryBudgetLine budget = PortfolioEntryBudgetDAO.getPEBudgetLineByPEAndResource(portfolioEntry.id, resourceObjectType, resourceObjectId);
        if (budget == null) {
            budget = new PortfolioEntryBudgetLine();
            budget.currency = CurrencyDAO.getCurrencyDefault();
            budget.name = name;
            budget.portfolioEntryBudget = planning.portfolioEntryBudget;
            budget.resourceObjectId = resourceObjectId;
            budget.resourceObjectType = resourceObjectType;
        }
        budget.amount = budgetAmount;
        budget.isOpex = planningPackage != null ? planningPackage.isOpex : portfolioEntry.defaultIsOpex;
        budget.save();
        budgetsAsMap.remove(budget.id);

        // cost to complete
        WorkOrder costToComplete = WorkOrderDAO.getWorkOrderByPEAndResource(portfolioEntry.id, resourceObjectType, resourceObjectId, false, usePurchaseOrder);
        if (costToComplete == null) {
            costToComplete = new WorkOrder();
            costToComplete.creationDate = new Date();
            costToComplete.currency = CurrencyDAO.getCurrencyDefault();
            costToComplete.isEngaged = false;
            costToComplete.name = name;
            costToComplete.portfolioEntry = portfolioEntry;
            costToComplete.resourceObjectId = resourceObjectId;
            costToComplete.resourceObjectType = resourceObjectType;
            costToComplete.shared = false;
        }
        costToComplete.amount = costToCompleteAmount;
        costToComplete.followPackageDates = followPackageDates;
        costToComplete.portfolioEntryPlanningPackage = planningPackage;
        if (costToComplete.followPackageDates != null && costToComplete.followPackageDates) {
            costToComplete.startDate = planningPackage.startDate;
            costToComplete.dueDate = planningPackage.endDate;
        }
        costToComplete.isOpex = planningPackage != null ? planningPackage.isOpex : portfolioEntry.defaultIsOpex;
        costToComplete.save();
        workOrdersAsMap.remove(costToComplete.id);

        // engaged
        WorkOrder engaged = WorkOrderDAO.getWorkOrderByPEAndResource(portfolioEntry.id, resourceObjectType, resourceObjectId, true, usePurchaseOrder);
        if (engaged == null) {
            engaged = new WorkOrder();
            engaged.creationDate = new Date();
            engaged.currency = CurrencyDAO.getCurrencyDefault();
            engaged.isEngaged = true;
            engaged.name = name;
            engaged.portfolioEntry = portfolioEntry;
            engaged.resourceObjectId = resourceObjectId;
            engaged.resourceObjectType = resourceObjectType;
            engaged.shared = false;
        }
        engaged.amount = engagedAmount;
        engaged.followPackageDates = followPackageDates;
        engaged.portfolioEntryPlanningPackage = planningPackage;
        if (engaged.followPackageDates != null && engaged.followPackageDates) {
            engaged.startDate = planningPackage.startDate;
            engaged.dueDate = planningPackage.endDate;
        }
        engaged.isOpex = planningPackage != null ? planningPackage.isOpex : portfolioEntry.defaultIsOpex;
        engaged.save();
        workOrdersAsMap.remove(engaged.id);
        if (usePurchaseOrder) {
            if (engaged.purchaseOrderLineItem == null) {
                engaged.purchaseOrderLineItem = new PurchaseOrderLineItem();
                engaged.purchaseOrderLineItem.creationDate = new Date();
                engaged.purchaseOrderLineItem.currency = CurrencyDAO.getCurrencyDefault();
                engaged.purchaseOrderLineItem.isCancelled = false;
                engaged.purchaseOrderLineItem.isOpex = engaged.isOpex;
                engaged.purchaseOrderLineItem.purchaseOrder = PurchaseOrderDAO
                        .getPurchaseOrderByRefId(IMafConstants.PURCHASE_ORDER_REF_ID_FOR_BUDGET_TRACKING);
                engaged.purchaseOrderLineItem.refId = IMafConstants.PURCHASE_ORDER_REF_ID_FOR_BUDGET_TRACKING + "_" + engaged.id;
            }
            engaged.purchaseOrderLineItem.amount = engagedAmount;
            engaged.purchaseOrderLineItem.amountReceived = engagedAmount;
            engaged.purchaseOrderLineItem.save();
        }
    }

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return this.preferenceManagerPlugin;
    }

}
