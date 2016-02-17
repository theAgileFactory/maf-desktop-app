package services.budgettracking;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang3.tuple.Pair;

import constants.IMafConstants;
import dao.finance.PortfolioEntryBudgetDAO;
import dao.finance.PurchaseOrderDAO;
import dao.finance.WorkOrderDAO;
import dao.timesheet.TimesheetDao;
import framework.services.account.IPreferenceManagerPlugin;
import models.finance.Currency;
import models.finance.PortfolioEntryBudgetLine;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.finance.PurchaseOrderLineItem;
import models.finance.WorkOrder;
import models.governance.LifeCycleInstancePlanning;
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

        TimesheetMap timesheetMap = new TimesheetMap();
        List<TimesheetLog> timesheetLogs = TimesheetDao.getTimesheetLogActiveAsExprByPortfolioEntry(portfolioEntry.id).findList();
        for (TimesheetLog timesheetLog : timesheetLogs) {
            timesheetMap.put(timesheetLog);
        }

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
            Pair<BigDecimal, BigDecimal> forecast = getForecastFromAllocatedActor(allocatedActor, timesheetMap);

            generateBudgetAndForecastFromResource(planning, allocatedActor.id, PortfolioEntryResourcePlanAllocatedActor.class.getName(),
                    allocatedActor.actor.getName(), allocatedActor.followPackageDates, allocatedActor.portfolioEntryPlanningPackage, allocatedActor.startDate,
                    allocatedActor.endDate, allocatedActor.currency, allocatedActor.currencyRate, budgetAmount, forecast.getLeft(), forecast.getRight(),
                    budgetsAsMap, workOrdersAsMap);

        }

        // org unit
        for (PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit : planning.portfolioEntryResourcePlan.portfolioEntryResourcePlanAllocatedOrgUnits) {

            // compute the amounts
            BigDecimal budgetAmount = allocatedOrgUnit.dailyRate.multiply(allocatedOrgUnit.days);
            Pair<BigDecimal, BigDecimal> forecast = getForecastFromAllocatedOrgUnit(allocatedOrgUnit, timesheetMap);

            generateBudgetAndForecastFromResource(planning, allocatedOrgUnit.id, PortfolioEntryResourcePlanAllocatedOrgUnit.class.getName(),
                    allocatedOrgUnit.orgUnit.getName(), allocatedOrgUnit.followPackageDates, allocatedOrgUnit.portfolioEntryPlanningPackage,
                    allocatedOrgUnit.startDate, allocatedOrgUnit.endDate, allocatedOrgUnit.currency, allocatedOrgUnit.currencyRate, budgetAmount,
                    forecast.getLeft(), forecast.getRight(), budgetsAsMap, workOrdersAsMap);

        }

        // competency
        List<PortfolioEntryResourcePlanAllocatedCompetency> ac = planning.portfolioEntryResourcePlan.portfolioEntryResourcePlanAllocatedCompetencies;
        for (PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency : ac) {

            // compute the budget
            BigDecimal budgetAmount = allocatedCompetency.dailyRate.multiply(allocatedCompetency.days);

            generateBudgetAndForecastFromResource(planning, allocatedCompetency.id, PortfolioEntryResourcePlanAllocatedCompetency.class.getName(),
                    allocatedCompetency.competency.getName(), allocatedCompetency.followPackageDates, allocatedCompetency.portfolioEntryPlanningPackage,
                    allocatedCompetency.startDate, allocatedCompetency.endDate, allocatedCompetency.currency, allocatedCompetency.currencyRate, budgetAmount,
                    null, null, budgetsAsMap, workOrdersAsMap);

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
        portfolioEntry.budgetTrackingHasUnallocatedTimesheet = !timesheetMap.isEmpty();
        portfolioEntry.save();

    }

    /**
     * Compute and get the forecast amounts (cost to complete and engage) from
     * an allocated actor.
     * 
     * @param allocatedActor
     *            the allocated actor
     * @param timesheetMap
     *            the timesheet map
     */
    private Pair<BigDecimal, BigDecimal> getForecastFromAllocatedActor(PortfolioEntryResourcePlanAllocatedActor allocatedActor, TimesheetMap timesheetMap) {

        Long packageId = allocatedActor.portfolioEntryPlanningPackage != null ? allocatedActor.portfolioEntryPlanningPackage.id : null;

        BigDecimal engagedHours = new BigDecimal(timesheetMap.consumeByActor(allocatedActor.actor.id, packageId)).setScale(5);
        BigDecimal engagedDays = engagedHours.divide(TimesheetDao.getTimesheetReportHoursPerDay(this.getPreferenceManagerPlugin()), BigDecimal.ROUND_HALF_UP);

        BigDecimal forecastDays = allocatedActor.forecastDays != null ? allocatedActor.forecastDays : allocatedActor.days;
        BigDecimal forecastDailyRate = allocatedActor.forecastDailyRate != null ? allocatedActor.forecastDailyRate : allocatedActor.dailyRate;

        BigDecimal costToCompleteDays = forecastDays.subtract(engagedDays);
        if (costToCompleteDays.compareTo(BigDecimal.ZERO) < 0) {
            costToCompleteDays = BigDecimal.ZERO;
        }

        BigDecimal engagedAmount = forecastDailyRate.multiply(engagedDays);
        BigDecimal costToCompleteAmount = forecastDailyRate.multiply(costToCompleteDays);

        return Pair.of(costToCompleteAmount, engagedAmount);
    }

    /**
     * Compute and get the forecast amounts (cost to complete and engage) from
     * an allocated org unit.
     * 
     * @param allocatedOrgUnit
     *            the allocated org unit
     * @param timesheetMap
     *            the timesheet map
     */
    private Pair<BigDecimal, BigDecimal> getForecastFromAllocatedOrgUnit(PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit,
            TimesheetMap timesheetMap) {

        Long packageId = allocatedOrgUnit.portfolioEntryPlanningPackage != null ? allocatedOrgUnit.portfolioEntryPlanningPackage.id : null;

        BigDecimal engagedHours = new BigDecimal(timesheetMap.consumeByOrgUnit(allocatedOrgUnit.orgUnit.id, packageId)).setScale(5);
        BigDecimal engagedDays = engagedHours.divide(TimesheetDao.getTimesheetReportHoursPerDay(this.getPreferenceManagerPlugin()), BigDecimal.ROUND_HALF_UP);

        BigDecimal forecastDays = allocatedOrgUnit.forecastDays != null ? allocatedOrgUnit.forecastDays : allocatedOrgUnit.days;
        BigDecimal forecastDailyRate = allocatedOrgUnit.forecastDailyRate != null ? allocatedOrgUnit.forecastDailyRate : allocatedOrgUnit.dailyRate;

        BigDecimal costToCompleteDays = forecastDays.subtract(engagedDays);
        if (costToCompleteDays.compareTo(BigDecimal.ZERO) < 0) {
            costToCompleteDays = BigDecimal.ZERO;
        }

        BigDecimal engagedAmount = forecastDailyRate.multiply(engagedDays);
        BigDecimal costToCompleteAmount = forecastDailyRate.multiply(costToCompleteDays);

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
     * @param startDate
     *            the start date
     * @param endDate
     *            the end date
     * @param currency
     *            the currency
     * @param currencyRate
     *            the conversion rate for the currency
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
    private void generateBudgetAndForecastFromResource(LifeCycleInstancePlanning planning, Long resourceObjectId, String resourceObjectType, String name,
            Boolean followPackageDates, PortfolioEntryPlanningPackage planningPackage, Date startDate, Date endDate, Currency currency,
            BigDecimal currencyRate, BigDecimal budgetAmount, BigDecimal costToCompleteAmount, BigDecimal engagedAmount,
            Map<Long, PortfolioEntryBudgetLine> budgetsAsMap, Map<Long, WorkOrder> workOrdersAsMap) {

        PortfolioEntry portfolioEntry = planning.lifeCycleInstance.portfolioEntry;

        boolean usePurchaseOrder = PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(getPreferenceManagerPlugin());

        // budget
        if (budgetAmount != null && budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            PortfolioEntryBudgetLine budget = PortfolioEntryBudgetDAO.getPEBudgetLineByPEAndResource(portfolioEntry.id, resourceObjectType, resourceObjectId);
            if (budget == null) {
                budget = new PortfolioEntryBudgetLine();
                budget.currency = currency;
                budget.currencyRate = currencyRate;
                budget.name = name;
                budget.portfolioEntryBudget = planning.portfolioEntryBudget;
                budget.resourceObjectId = resourceObjectId;
                budget.resourceObjectType = resourceObjectType;
            }
            budget.amount = budgetAmount;
            budget.isOpex = planningPackage != null ? planningPackage.isOpex : portfolioEntry.defaultIsOpex;
            budget.save();
            budgetsAsMap.remove(budget.id);
        }

        // cost to complete
        if (costToCompleteAmount != null && costToCompleteAmount.compareTo(BigDecimal.ZERO) > 0) {
            WorkOrder costToComplete = WorkOrderDAO.getWorkOrderByPEAndResource(portfolioEntry.id, resourceObjectType, resourceObjectId, false,
                    usePurchaseOrder);
            if (costToComplete == null) {
                costToComplete = new WorkOrder();
                costToComplete.creationDate = new Date();
                costToComplete.currency = currency;
                costToComplete.currencyRate = currencyRate;
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
            costToComplete.startDate = startDate;
            costToComplete.dueDate = endDate;
            costToComplete.isOpex = planningPackage != null ? planningPackage.isOpex : portfolioEntry.defaultIsOpex;
            costToComplete.save();
            workOrdersAsMap.remove(costToComplete.id);
        }

        // engaged
        if (engagedAmount != null && engagedAmount.compareTo(BigDecimal.ZERO) > 0) {
            WorkOrder engaged = WorkOrderDAO.getWorkOrderByPEAndResource(portfolioEntry.id, resourceObjectType, resourceObjectId, true, usePurchaseOrder);
            if (engaged == null) {
                engaged = new WorkOrder();
                engaged.creationDate = new Date();
                engaged.currency = currency;
                engaged.currencyRate = currencyRate;
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
            engaged.startDate = startDate;
            engaged.dueDate = endDate;
            engaged.isOpex = planningPackage != null ? planningPackage.isOpex : portfolioEntry.defaultIsOpex;
            engaged.save();
            workOrdersAsMap.remove(engaged.id);
            if (usePurchaseOrder) {
                if (engaged.purchaseOrderLineItem == null) {
                    engaged.purchaseOrderLineItem = new PurchaseOrderLineItem();
                    engaged.purchaseOrderLineItem.creationDate = new Date();
                    engaged.purchaseOrderLineItem.currency = currency;
                    engaged.purchaseOrderLineItem.currencyRate = currencyRate;
                    engaged.purchaseOrderLineItem.isCancelled = false;
                    engaged.purchaseOrderLineItem.isOpex = engaged.isOpex;
                    engaged.purchaseOrderLineItem.purchaseOrder = PurchaseOrderDAO
                            .getPurchaseOrderByRefId(IMafConstants.PURCHASE_ORDER_REF_ID_FOR_BUDGET_TRACKING);
                    engaged.purchaseOrderLineItem.refId = IMafConstants.PURCHASE_ORDER_REF_ID_FOR_BUDGET_TRACKING + "_" + engaged.id;
                }
                engaged.purchaseOrderLineItem.amount = engagedAmount;
                engaged.purchaseOrderLineItem.amountReceived = engagedAmount;
                engaged.purchaseOrderLineItem.save();
                engaged.save();
            }
        }
    }

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return this.preferenceManagerPlugin;
    }

    /**
     * A complex Map to manage the timesheet logs.
     * 
     * @author Johann Kohler
     *
     */
    private static class TimesheetMap {

        /**
         * <timesheetLogId, hours>.
         */
        private Map<Long, Double> values;

        /**
         * <Tuple[actorId, packageId], List[timesheetLogId]>.
         */
        private Map<MultiKey, List<Long>> byActor;

        /**
         * <Tuple[orgUnitId, packageId], List[timesheetLogId]>.
         */
        private Map<MultiKey, List<Long>> byOrgUnit;

        /**
         * Default constructor.
         */
        public TimesheetMap() {
            this.values = new HashMap<>();
            this.byActor = new HashMap<>();
            this.byOrgUnit = new HashMap<>();
        }

        /**
         * Return true if all logs have been consumed.
         */
        public boolean isEmpty() {
            return this.values.size() == 0;
        }

        /**
         * Put a timesheet log.
         * 
         * @param timesheetLog
         *            the timesheet log
         */
        public void put(TimesheetLog timesheetLog) {

            this.values.put(timesheetLog.id, timesheetLog.hours);

            Long actorId = timesheetLog.timesheetEntry.timesheetReport.actor.id;
            Long orgUnitId = timesheetLog.timesheetEntry.timesheetReport.orgUnit != null ? timesheetLog.timesheetEntry.timesheetReport.orgUnit.id
                    : timesheetLog.timesheetEntry.timesheetReport.actor.orgUnit.id;
            Long packageId = timesheetLog.timesheetEntry.portfolioEntryPlanningPackage != null ? timesheetLog.timesheetEntry.portfolioEntryPlanningPackage.id
                    : null;

            MultiKey byActorKey = new MultiKey(actorId, packageId);
            MultiKey byOrgUnitKey = new MultiKey(orgUnitId, packageId);

            List<Long> byActorList = this.byActor.containsKey(byActorKey) ? this.byActor.get(byActorKey) : new ArrayList<>();
            List<Long> byOrgUnitList = this.byOrgUnit.containsKey(byOrgUnitKey) ? this.byOrgUnit.get(byOrgUnitKey) : new ArrayList<>();

            byActorList.add(timesheetLog.id);
            byOrgUnitList.add(timesheetLog.id);

            this.byActor.put(byActorKey, byActorList);
            this.byOrgUnit.put(byOrgUnitKey, byOrgUnitList);

        }

        /**
         * Get the total hours for a tuple [actorId, packageId] and consume the
         * corresponding timesheet logs.
         * 
         * @param actorId
         *            the actor id
         * @param packageId
         *            the package id
         */
        public Double consumeByActor(Long actorId, Long packageId) {
            MultiKey byActorKey = new MultiKey(actorId, packageId);
            Double total = 0.0;
            if (this.byActor.containsKey(byActorKey)) {
                for (Long timesheetLogId : this.byActor.get(byActorKey)) {
                    total += this.values.containsKey(timesheetLogId) ? this.values.get(timesheetLogId) : 0.0;
                    this.values.remove(timesheetLogId);
                }
                this.byActor.remove(byActorKey);
            }
            return total;
        }

        /**
         * Get the total hours for a tuple [orgUnitId, packageId] and consume
         * the corresponding timesheet logs.
         * 
         * @param orgUnitId
         *            the org unit id
         * @param packageId
         *            the package id
         */
        public Double consumeByOrgUnit(Long orgUnitId, Long packageId) {
            MultiKey byOrgUnitKey = new MultiKey(orgUnitId, packageId);
            Double total = 0.0;
            if (this.byOrgUnit.containsKey(byOrgUnitKey)) {
                for (Long timesheetLogId : this.byOrgUnit.get(byOrgUnitKey)) {
                    total += this.values.containsKey(timesheetLogId) ? this.values.get(timesheetLogId) : 0.0;
                    this.values.remove(timesheetLogId);
                }
                this.byOrgUnit.remove(byOrgUnitKey);
            }
            return total;
        }

    }

}
