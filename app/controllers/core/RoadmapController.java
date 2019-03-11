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
package controllers.core;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.governance.LifeCycleMilestoneDao;
import dao.governance.LifeCyclePlanningDao;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioEntryDao;
import dao.timesheet.TimesheetDao;
import framework.highcharts.pattern.BasicBar;
import framework.security.ISecurityService;
import framework.services.account.AccountManagementException;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.services.system.ISysAdminUtils;
import framework.utils.*;
import models.common.ResourceAllocation;
import models.common.ResourceAllocationDetail;
import models.finance.*;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.governance.LifeCycleInstance;
import models.governance.LifeCyclePhase;
import models.governance.PlannedLifeCycleMilestoneInstance;
import models.pmo.*;
import models.timesheet.TimesheetActivityAllocatedActor;
import models.timesheet.TimesheetActivityAllocatedActorDetail;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import security.dynamic.PortfolioEntryDynamicHelper;
import services.budgettracking.IBudgetTrackingService;
import services.tableprovider.ITableProvider;
import utils.gantt.SourceDataValue;
import utils.gantt.SourceItem;
import utils.gantt.SourceValue;
import utils.table.PortfolioEntryListView;
import views.html.core.roadmap.roadmap_capacity_forecast_cell_details_fragment;
import views.html.core.roadmap.roadmap_capacity_forecast_table_orgunits_fragment;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The controller which displays the roadmap (list of portfolio entries that can
 * be filtered).
 * 
 * @author Johann Kohler
 */
@Restrict({ @Group(IMafConstants.ROADMAP_DISPLAY_PERMISSION) })
public class RoadmapController extends Controller {

    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private INotificationManagerPlugin notificationManagerPlugin;
    @Inject
    private ISysAdminUtils sysAdminUtils;
    @Inject
    private IPersonalStoragePlugin personalStoragePlugin;
    @Inject
    private ISecurityService securityService;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;
    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;
    @Inject
    private ITableProvider tableProvider;
    @Inject
    private IBudgetTrackingService budgetTrackingService;

    private static Logger.ALogger log = Logger.of(RoadmapController.class);

    private static Form<CapacityForecastForm> capacityForecastFormTemplate = Form.form(CapacityForecastForm.class);

    /**
     * The default CSS class for a gantt bar. The classes are defined in the
     * file main.css of app-framework.
     */
    private static final String GANTT_DEFAULT_CSS_CLASS = "default";

    /**
     * Display the roadmap.<br/>
     * -sidebar: pre-configured filters and reset link<br/>
     * -content: the portfolio entries
     * 
     * Note: the list of portfolio entries depends of the last filter
     * configuration.
     */

    public Result index() {

        try {

            boolean existPortfolioEntries = PortfolioEntryDao.getPEAsExpr(true).findRowCount() > 0;

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            PostQueryFilterConfig<PortfolioEntryListView, PortfolioEntry> filterConfig = this.getTableProvider().get().portfolioEntry.filterConfig.getCurrent(uid, request());

            // get the table
            Pair<Table<PortfolioEntryListView>, Pagination<PortfolioEntry>> t = getTable(filterConfig);

            return ok(views.html.core.roadmap.roadmap_index.render(existPortfolioEntries, t.getLeft(), t.getRight(), filterConfig));

        } catch (Exception e) {

            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());

        }
    }

    /**
     * Filter the portfolio entry table (this action is called when the user
     * update the table selector/filter).
     */
    public Result indexFilter() {

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            PostQueryFilterConfig<PortfolioEntryListView, PortfolioEntry> filterConfig = this.getTableProvider().get().portfolioEntry.filterConfig.persistCurrentInDefault(uid,
                    request());

            if (filterConfig == null) {
                return ok(views.html.framework_views.parts.table.dynamic_tableview_no_more_compatible.render());
            } else {

                // get the table
                Pair<Table<PortfolioEntryListView>, Pagination<PortfolioEntry>> t = getTable(filterConfig);

                return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));

            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }

    }

    /**
     * Export the content of the current table as Excel.
     */
    public Promise<Result> exportAsExcel() {
        return Promise.promise(() -> {

            try {

                // Get the current user
                final String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());

                // construct the table
                PostQueryFilterConfig<PortfolioEntryListView, PortfolioEntry> filterConfig = getTableProvider().get().portfolioEntry.filterConfig.getCurrent(uid, request());

                ExpressionList<PortfolioEntry> expressionList = filterConfig.updateWithSearchExpression(PortfolioEntryDynamicHelper.getPortfolioEntriesViewAllowedAsQuery(getSecurityService()));
                filterConfig.updateWithSortExpression(expressionList);

                List<PortfolioEntryListView> portfolioEntryListView = expressionList.findList().stream().map(PortfolioEntryListView::new).collect(Collectors.toList());

                Table<PortfolioEntryListView> table = getTableProvider().get().portfolioEntry.templateTable.fillForFilterConfig(portfolioEntryListView,
                        getColumnsToHide(filterConfig));

                final byte[] excelFile = TableExcelRenderer.renderFormatted(table);

                final String fileName = String.format("roadmapExport_%1$td_%1$tm_%1$ty_%1$tH-%1$tM-%1$tS.xlsx", new Date());
                final String successTitle = Msg.get("excel.export.success.title");
                final String successMessage = Msg.get("excel.export.success.message", fileName);
                final String failureTitle = Msg.get("excel.export.failure.title");
                final String failureMessage = Msg.get("excel.export.failure.message");

                // Execute asynchronously
                getSysAdminUtils().scheduleOnce(false, "Roadmap Excel Export", Duration.create(0, TimeUnit.MILLISECONDS), new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OutputStream out = getPersonalStoragePlugin().createNewFile(uid, fileName);
                            IOUtils.copy(new ByteArrayInputStream(excelFile), out);
                            getNotificationManagerPlugin().sendNotification(uid, NotificationCategory.getByCode(Code.DOCUMENT), successTitle,
                                    successMessage, controllers.my.routes.MyPersonalStorage.index().url());
                        } catch (IOException e) {
                            log.error("Unable to export the excel file", e);
                            getNotificationManagerPlugin().sendNotification(uid, NotificationCategory.getByCode(Code.ISSUE), failureTitle, failureMessage,
                                    routes.RoadmapController.index().url());
                        }
                    }
                });

                return ok(Json.newObject());

            } catch (Exception e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }
        });

    }

    /**
     * Display the planning (gantt) of the current roadmap.
     * 
     * the list of portfolio entries depends of the current filter configuration
     * 
     * the gantt view is construct as:<br/>
     * -for each portfolio entry we get its life cycle process and its last
     * planned dates (there is one date by milestone)<br/>
     * -for each phase of the life cycle process, we get its start milestone and
     * we find the corresponding planned date (that is the start date) from the
     * last planned dates. We do the same for the end milestone<br/>
     * -we display one interval (bar) by phase according to the computed start
     * and end dates (see just above)
     */
    public Result viewPlanning() {

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            PostQueryFilterConfig<PortfolioEntryListView, PortfolioEntry> filterConfig = this.getTableProvider().get().portfolioEntry.filterConfig.getCurrent(uid, request());

            ExpressionList<PortfolioEntry> expressionList = filterConfig.updateWithSearchExpression(PortfolioEntryDynamicHelper.getPortfolioEntriesViewAllowedAsQuery(getSecurityService()));
            filterConfig.updateWithSortExpression(expressionList);

            // initiate the source items (gantt)
            List<SourceItem> items = new ArrayList<>();

            // compute the items (for each portfolio entry)
            for (PortfolioEntry portfolioEntry : expressionList.findList()) {

                // get the active life cycle process instance
                LifeCycleInstance processInstance = portfolioEntry.activeLifeCycleInstance;

                // get the roadmap phases of a process
                List<LifeCyclePhase> lifeCyclePhases = LifeCycleMilestoneDao.getLCPhaseRoadmapAsListByLCProcess(processInstance.lifeCycleProcess.id);

                if (lifeCyclePhases != null && !lifeCyclePhases.isEmpty()) {

                    // get the last planned milestone instances
                    List<PlannedLifeCycleMilestoneInstance> lastPlannedMilestoneInstances = LifeCyclePlanningDao
                            .getPlannedLCMilestoneInstanceLastAsListByPE(portfolioEntry.id);

                    if (lastPlannedMilestoneInstances != null && lastPlannedMilestoneInstances.size() > 0) {

                        // transform the list of last planned milestone
                        // instances to
                        // a map
                        Map<Long, PlannedLifeCycleMilestoneInstance> lastPlannedMilestoneInstancesAsMap = new HashMap<>();
                        for (PlannedLifeCycleMilestoneInstance plannedMilestoneInstance : lastPlannedMilestoneInstances) {
                            lastPlannedMilestoneInstancesAsMap.put(plannedMilestoneInstance.lifeCycleMilestone.id, plannedMilestoneInstance);
                        }

                        /*
                         * compute the common components for all phases
                         */

                        // get the CSS class
                        String cssClass = GANTT_DEFAULT_CSS_CLASS;
                        PortfolioEntryReport report = portfolioEntry.lastPortfolioEntryReport;
                        if (report != null && report.portfolioEntryReportStatusType != null) {
                            cssClass = report.portfolioEntryReportStatusType.cssClass;
                        }

                        // create the source data value (used when clicking on a
                        // phase)
                        SourceDataValue sourceDataValue = new SourceDataValue(
                                controllers.core.routes.PortfolioEntryController.overview(portfolioEntry.id).url(), portfolioEntry.getName(),
                                portfolioEntry.getDescription(), views.html.modelsparts.display_actor.render(portfolioEntry.manager).body(),
                                views.html.framework_views.parts.formats.display_list_of_values.render(portfolioEntry.portfolios, "display").body());

                        boolean isFirstLoop = true;

                        for (LifeCyclePhase phase : lifeCyclePhases) {

                            if (lastPlannedMilestoneInstancesAsMap.containsKey(phase.startLifeCycleMilestone.id)
                                    && lastPlannedMilestoneInstancesAsMap.containsKey(phase.endLifeCycleMilestone.id)) {

                                // get the from date
                                Date from = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceAsPassedDate(
                                        lastPlannedMilestoneInstancesAsMap.get(phase.startLifeCycleMilestone.id).id);

                                // get the to date
                                Date to = LifeCyclePlanningDao
                                        .getPlannedLCMilestoneInstanceAsPassedDate(lastPlannedMilestoneInstancesAsMap.get(phase.endLifeCycleMilestone.id).id);

                                if (from != null && to != null) {

                                    to = JqueryGantt.cleanToDate(from, to);

                                    // add gap for the from date
                                    if (phase.gapDaysStart != null && phase.gapDaysStart > 0) {
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(from);
                                        c.add(Calendar.DATE, phase.gapDaysStart);
                                        from = c.getTime();
                                    }

                                    // remove gap for the to date
                                    if (phase.gapDaysEnd != null && phase.gapDaysEnd > 0) {
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(to);
                                        c.add(Calendar.DATE, -1 * phase.gapDaysEnd);
                                        to = c.getTime();
                                    }

                                    String name = "";
                                    String str = "";
                                    if (isFirstLoop) {
                                        if (portfolioEntry.governanceId != null) {
                                            name += portfolioEntry.governanceId + " - ";
                                        }
                                        name += portfolioEntry.getName();
                                        str = String.format("<a href= %s > %s </a>" , controllers.core.routes.PortfolioEntryGovernanceController.index(portfolioEntry.id).url() , name ) ;
                                    }

                                    SourceItem item = new SourceItem(str, "");

                                    item.values.add(new SourceValue(from, to, "", phase.getName(), cssClass, sourceDataValue));

                                    items.add(item);

                                    isFirstLoop = false;

                                }

                            }

                        }

                    }

                }
            }

            String source = "";
            try {
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                source = ow.writeValueAsString(items);
            } catch (JsonProcessingException e) {
                Logger.error(e.getMessage());
            }

            return ok(views.html.core.roadmap.roadmap_view_planning.render(source));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Get the allocated days
     * 
     * @param days
     *            the days
     * @param forecastDays
     *            forecast days
     */
    private BigDecimal getAllocatedDays(BigDecimal days, BigDecimal forecastDays) {
        if (this.getBudgetTrackingService().isActive()) {
            if (forecastDays != null && !forecastDays.equals(BigDecimal.ZERO)) {
                return forecastDays;
            }
        }
        return days;
    }

    /**
     * Get all portfolio entries id according to the current filter.
     */
    public Result getAllIds() {

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            PostQueryFilterConfig<PortfolioEntryListView, PortfolioEntry> filterConfig = this.getTableProvider().get().portfolioEntry.filterConfig.getCurrent(uid, request());

            ObjectMapper mapper = new ObjectMapper();
            List<String> ids = new ArrayList<>();

            ExpressionList<PortfolioEntry> expressionList = filterConfig.updateWithSearchExpression(PortfolioEntryDynamicHelper.getPortfolioEntriesViewAllowedAsQuery(getSecurityService()));
            filterConfig.updateWithSortExpression(expressionList);

            ids.addAll(expressionList.findList().stream().map(portfolioEntry -> String.valueOf(portfolioEntry.id)).collect(Collectors.toList()));

            JsonNode node = mapper.valueToTree(ids);

            return ok(node);

        } catch (Exception e) {
            return internalServerError();
        }
    }

    /**
     * Return the HTML fragment of the KPIs for "scenario simulator".
     * 
     * @throws AccountManagementException
     */
    @Restrict({ @Group(IMafConstants.ROADMAP_SIMULATOR_PERMISSION) })
    public Result simulatorKpisFragment() throws AccountManagementException {

        List<String> ids = FilterConfig.getIdsFromRequest(request());

        BigDecimal allocation = BigDecimal.ZERO;
        BigDecimal allocationConfirmed = BigDecimal.ZERO;
        BigDecimal allocationNotConfirmed = BigDecimal.ZERO;

        BigDecimal budget = BigDecimal.ZERO;
        BigDecimal budgetCapex = BigDecimal.ZERO;
        BigDecimal budgetOpex = BigDecimal.ZERO;

        BigDecimal forecast = BigDecimal.ZERO;
        BigDecimal forecastCapex = BigDecimal.ZERO;
        BigDecimal forecastOpex = BigDecimal.ZERO;

        BigDecimal engaged = BigDecimal.ZERO;
        BigDecimal engagedCapex = BigDecimal.ZERO;
        BigDecimal engagedOpex = BigDecimal.ZERO;

        for (String idString : ids) {

            Long id = Long.valueOf(idString);

            PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

            // allocation
            BigDecimal entryAllocatedActorDaysConfirmed = PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsDaysByPEAndConfirmed(portfolioEntry, true);
            BigDecimal entryAllocatedActorDaysNotConfirmed = PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsDaysByPEAndConfirmed(portfolioEntry,
                    false);
            BigDecimal entryAllocatedOrgUnitDaysConfirmed = PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitAsDaysByPE(portfolioEntry, true);
            BigDecimal entryAllocatedOrgUnitDaysNotConfirmed = PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitAsDaysByPE(portfolioEntry,
                    false);
            BigDecimal entryAllocatedCompetencyDaysConfirmed = PortfolioEntryResourcePlanDAO
                    .getPEResourcePlanAllocatedCompetencyAsDaysByPortfolioEntry(portfolioEntry, true);
            BigDecimal entryAllocatedCompetencyDaysNotConfirmed = PortfolioEntryResourcePlanDAO
                    .getPEResourcePlanAllocatedCompetencyAsDaysByPortfolioEntry(portfolioEntry, false);

            BigDecimal entryAllocationDaysConfirmed = entryAllocatedActorDaysConfirmed.add(entryAllocatedOrgUnitDaysConfirmed)
                    .add(entryAllocatedCompetencyDaysConfirmed);
            BigDecimal entryAllocationDaysNotConfirmed = entryAllocatedActorDaysNotConfirmed.add(entryAllocatedOrgUnitDaysNotConfirmed)
                    .add(entryAllocatedCompetencyDaysNotConfirmed);

            allocation = allocation.add(entryAllocationDaysConfirmed).add(entryAllocationDaysNotConfirmed);
            allocationConfirmed = allocationConfirmed.add(entryAllocationDaysConfirmed);
            allocationNotConfirmed = allocationNotConfirmed.add(entryAllocationDaysNotConfirmed);

            if (getSecurityService().restrict(IMafConstants.PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_ALL_PERMISSION)) {

                // budget
                Double entryBudgetCapex = PortfolioEntryDao.getPEAsBudgetAmountByOpex(id, false);
                Double entryBudgetOpex = PortfolioEntryDao.getPEAsBudgetAmountByOpex(id, true);

                budget = budget.add(new BigDecimal(entryBudgetCapex + entryBudgetOpex));
                budgetCapex = budgetCapex.add(new BigDecimal(entryBudgetCapex));
                budgetOpex = budgetOpex.add(new BigDecimal(entryBudgetOpex));

                // forecast
                Double entryCostToCompleteCapex = PortfolioEntryDao.getPEAsCostToCompleteAmountByOpex(this.getPreferenceManagerPlugin(), id, false);
                Double entryCostToCompleteOpex = PortfolioEntryDao.getPEAsCostToCompleteAmountByOpex(this.getPreferenceManagerPlugin(), id, true);
                Double entryEngagedCapex = PortfolioEntryDao.getPEAsEngagedAmountByOpex(this.getPreferenceManagerPlugin(), id, false);
                Double entryEngagedOpex = PortfolioEntryDao.getPEAsEngagedAmountByOpex(this.getPreferenceManagerPlugin(), id, true);

                forecast = forecast.add(new BigDecimal(entryCostToCompleteCapex + entryCostToCompleteOpex + entryEngagedCapex + entryEngagedOpex));
                forecastCapex = forecastCapex.add(new BigDecimal(entryCostToCompleteCapex + entryEngagedCapex));
                forecastOpex = forecastOpex.add(new BigDecimal(entryCostToCompleteOpex + entryEngagedOpex));

                // engaged
                engaged = engaged.add(new BigDecimal(entryEngagedCapex + entryEngagedOpex));
                engagedCapex = engagedCapex.add(new BigDecimal(entryEngagedCapex));
                engagedOpex = engagedOpex.add(new BigDecimal(entryEngagedOpex));

            }

        }

        budget = budget.setScale(2, RoundingMode.HALF_UP);
        budgetCapex = budgetCapex.setScale(2, RoundingMode.HALF_UP);
        budgetOpex = budgetOpex.setScale(2, RoundingMode.HALF_UP);

        forecast = forecast.setScale(2, RoundingMode.HALF_UP);
        forecastCapex = forecastCapex.setScale(2, RoundingMode.HALF_UP);
        forecastOpex = forecastOpex.setScale(2, RoundingMode.HALF_UP);

        engaged = engaged.setScale(2, RoundingMode.HALF_UP);
        engagedCapex = engagedCapex.setScale(2, RoundingMode.HALF_UP);
        engagedOpex = engagedOpex.setScale(2, RoundingMode.HALF_UP);

        return ok(views.html.core.roadmap.roadmap_simulator_kpis_fragment.render(allocation, allocationConfirmed, allocationNotConfirmed, budget, budgetCapex,
                budgetOpex, forecast, forecastCapex, forecastOpex, engaged, engagedCapex, engagedOpex));

    }

    /**
     * The capacity forecast table.
     */
    @Restrict({ @Group(IMafConstants.ROADMAP_SIMULATOR_PERMISSION) })
    public Result simulatorCapacityForecast() {

        try {
            List<String> ids = FilterConfig.getIdsFromRequest(request());

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String idsAsJson = ow.writeValueAsString(ids);

            session("roadmap.selected.rows", idsAsJson);

            return ok(views.html.core.roadmap.roadmap_capacity_forecast.render());

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }

    }

    private Date getEndTime(Integer year, Integer month) {
        Calendar yearEndDay = Calendar.getInstance();
        yearEndDay.set(Calendar.YEAR, year + 1);
        yearEndDay.set(Calendar.MONTH, month - 1);
        yearEndDay.set(Calendar.DAY_OF_MONTH, 31);
        yearEndDay.set(Calendar.HOUR_OF_DAY, 23);
        yearEndDay.set(Calendar.MINUTE, 59);
        yearEndDay.set(Calendar.SECOND, 59);
        yearEndDay.set(Calendar.MILLISECOND, 999);
        return yearEndDay.getTime();
    }

    private Date getStartTime(Integer year, Integer month) {
        Calendar yearStartDay = Calendar.getInstance();
        yearStartDay.set(Calendar.YEAR, year);
        yearStartDay.set(Calendar.MONTH, month);
        yearStartDay.set(Calendar.DAY_OF_MONTH, 1);
        yearStartDay.set(Calendar.HOUR_OF_DAY, 0);
        yearStartDay.set(Calendar.MINUTE, 0);
        yearStartDay.set(Calendar.SECOND, 0);
        yearStartDay.set(Calendar.MILLISECOND, 0);
        return yearStartDay.getTime();
    }

    /**
     * Get capacity table by employee
     */
    public Result simulatorCapacityForecastTableActorsFragment(Integer year, Integer month, Boolean onlyConfirmed, Integer monthsDisplayed) {

        int warningLimitPercent = getPreferenceManagerPlugin().getPreferenceValueAsInteger(IMafConstants.ROADMAP_CAPACITY_SIMULATOR_WARNING_LIMIT_PREFERENCE);

        JsonNode idsAsJson = Json.parse(session("roadmap.selected.rows"));

        ArrayList<Long> ids = new ArrayList<>();
        for(JsonNode id: idsAsJson) {
            ids.add(id.asLong());
        }

        List<PortfolioEntryResourcePlanAllocatedActor> allocatedActors = new ArrayList<>();

        ids.forEach(
            id -> allocatedActors.addAll(PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsListByPE(id, getStartTime(year, month), getEndTime(year, month), onlyConfirmed, null, null))
        );

        // Add actor capacities group by actor
        Map<Long, ActorCapacity> actorCapacities = new HashMap<>();
        for (PortfolioEntryResourcePlanAllocatedActor allocatedActor: allocatedActors) {

            ActorCapacity actorCapacity;
            if (actorCapacities.containsKey(allocatedActor.actor.id)) {
                actorCapacity = actorCapacities.get(allocatedActor.actor.id);
            } else {
                actorCapacity = new ActorCapacity(warningLimitPercent, allocatedActor.actor);

                // Add availables just once
                List<models.pmo.ActorCapacity> actorAvailables = ActorDao.getActorCapacityAsArrayByActorAndYear(allocatedActor.actor, year, month);
                for (models.pmo.ActorCapacity available : actorAvailables) {
                    actorCapacity.addAvailable(getColumnIndex(month, available.month - 1), available.value == null ? 0.0 : available.value);
                }

            }

            // Add planned
            if (allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.isEmpty()) {
                allocatedActor.computeAllocationDetails(getBudgetTrackingService().isActive(), getPreferenceManagerPlugin().getPreferenceValueAsBoolean(IMafConstants.RESOURCES_WEEK_DAYS_ALLOCATION_PREFERENCE));
            }
            allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails
                    .stream()
                    .filter(detail -> (detail.year.equals(year) && detail.month >= month) || (detail.year.equals(year + 1) && detail.month < month))
                    .forEach(detail -> actorCapacity.addPlanned(getColumnIndex(month, detail.month), detail.days == null ? 0.0 : detail.days));

            actorCapacities.put(allocatedActor.actor.id, actorCapacity);
        }

        return ok(views.html.core.roadmap.roadmap_capacity_forecast_table_actors_fragment.render(actorCapacities.values().stream().sorted(Comparator.comparing(a -> a.getActor().firstName)).collect(Collectors.toList()), year, month, monthsDisplayed));
    }

    public Result simulatorCapacityForecastTableOrgUnitsFragment(Integer year, Integer month, Boolean onlyConfirmed, Integer monthsDisplayed) {

        int warningLimitPercent = getPreferenceManagerPlugin().getPreferenceValueAsInteger(IMafConstants.ROADMAP_CAPACITY_SIMULATOR_WARNING_LIMIT_PREFERENCE);

        JsonNode idsAsJson = Json.parse(session("roadmap.selected.rows"));

        ArrayList<Long> ids = new ArrayList<>();
        for(JsonNode id: idsAsJson) {
            ids.add(id.asLong());
        }

        List<PortfolioEntryResourcePlanAllocatedActor> allocatedActors = new ArrayList<>();
        List<PortfolioEntryResourcePlanAllocatedOrgUnit> allocatedOrgUnits = new ArrayList<>();
        Date yearStartTime = getStartTime(year, month);
        Date yearEndTime = getEndTime(year, month);

        ids.forEach(
                id -> {
                    allocatedActors.addAll(PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsListByPE(id, yearStartTime, yearEndTime, onlyConfirmed, null, null));
                    allocatedOrgUnits.addAll(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitAsListByPE(id, yearStartTime, yearEndTime, onlyConfirmed, null));
                }
        );

        /**
         * Compute the capacities for the org unit and actor allocations and
         * group them by org unit.
         */

        Map<Long, OrgUnitCapacity> orgUnitCapacities = new HashMap<>();

        // Org unit: the org unit is simply the one of the allocated org
        // unit.
        Boolean workingDaysOnly = getPreferenceManagerPlugin().getPreferenceValueAsBoolean(IMafConstants.RESOURCES_WEEK_DAYS_ALLOCATION_PREFERENCE);

        for (PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit : allocatedOrgUnits) {

            OrgUnitCapacity orgUnitCapacity;
            if (orgUnitCapacities.containsKey(allocatedOrgUnit.orgUnit.id)) {
                orgUnitCapacity = orgUnitCapacities.get(allocatedOrgUnit.orgUnit.id);
            } else {
                orgUnitCapacity = new OrgUnitCapacity(warningLimitPercent, allocatedOrgUnit.orgUnit);
            }

            // Add planned
            if (allocatedOrgUnit.portfolioEntryResourcePlanAllocatedOrgUnitDetails.isEmpty()) {
                allocatedOrgUnit.computeAllocationDetails(getBudgetTrackingService().isActive(), workingDaysOnly);
            }

            allocatedOrgUnit.portfolioEntryResourcePlanAllocatedOrgUnitDetails
                    .stream()
                    .filter(detail -> (detail.year.equals(year) && detail.month >= month) || (detail.year.equals(year + 1) && detail.month < month))
                    .forEach(detail -> orgUnitCapacity.addPlanned(getColumnIndex(month, detail.month), detail.days == null ? 0.0 : detail.days));

            orgUnitCapacities.put(allocatedOrgUnit.orgUnit.id, orgUnitCapacity);
        }

        // Actor: the org unit is the one of the actor of the allocated
        // actor.
        for (PortfolioEntryResourcePlanAllocatedActor allocatedActor : allocatedActors) {

            if (allocatedActor.actor.orgUnit != null) {

                OrgUnitCapacity orgUnitCapacity;
                if (orgUnitCapacities.containsKey(allocatedActor.actor.orgUnit.id)) {
                    orgUnitCapacity = orgUnitCapacities.get(allocatedActor.actor.orgUnit.id);
                } else {
                    orgUnitCapacity = new OrgUnitCapacity(warningLimitPercent, allocatedActor.actor.orgUnit);
                }

                if (allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.isEmpty()) {
                    allocatedActor.computeAllocationDetails(getBudgetTrackingService().isActive(), workingDaysOnly);
                }

                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails
                        .stream()
                        .filter(detail -> (detail.year.equals(year) && detail.month >= month) || (detail.year.equals(year + 1) && detail.month < month))
                        .forEach(detail -> orgUnitCapacity.addPlanned(getColumnIndex(month, detail.month), detail.days == null ? 0.0 : detail.days));

                orgUnitCapacities.put(allocatedActor.actor.orgUnit.id, orgUnitCapacity);
            }
        }

        /**
         * Get and compute the activity capacities and the actor available
         * capacities.
         */

        for (Entry<Long, OrgUnitCapacity> entry : orgUnitCapacities.entrySet()) {

            OrgUnitCapacity orgUnitCapacity = entry.getValue();

            // Get the activity allocations.
            List<TimesheetActivityAllocatedActor> allocatedActivities = TimesheetDao.getTimesheetActivityAllocatedActorAsListByOrgUnitAndPeriod(
                    orgUnitCapacity.getOrgUnit().id, yearStartTime, yearEndTime);

            // Compute the activity allocations.
            for (TimesheetActivityAllocatedActor allocatedActivity : allocatedActivities) {
                if (allocatedActivity.timesheetActivityAllocatedActorDetails.isEmpty()) {
                    allocatedActivity.computeAllocationDetails(false, workingDaysOnly);
                }
                allocatedActivity.timesheetActivityAllocatedActorDetails
                        .stream()
                        .filter(detail -> (detail.year.equals(year) && detail.month >= month) || (detail.year.equals(year + 1) && detail.month < month))
                        .forEach(detail -> orgUnitCapacity.addPlanned(getColumnIndex(month, detail.month), detail.days == null ? 0.0 : detail.days));

                orgUnitCapacities.put(allocatedActivity.actor.orgUnit.id, orgUnitCapacity);
            }

            // Get the available actor capacities.
            List<models.pmo.ActorCapacity> actorCapacities = ActorDao.getActorCapacityAsListByOrgUnitAndYear(orgUnitCapacity.getOrgUnit().id, year, month);

            // Compute the available actor capacities.
            for (models.pmo.ActorCapacity actorCapacity : actorCapacities) {
                orgUnitCapacity.addAvailable(getColumnIndex(month, actorCapacity.month - 1), actorCapacity.value);
            }
        }

        return ok(roadmap_capacity_forecast_table_orgunits_fragment.render(
                orgUnitCapacities
                        .values()
                        .stream()
                        .sorted((orgUnit1, orgUnit2) -> {
                            if (orgUnit1 == null || orgUnit2 == null) {
                                return 0;
                            }
                            return orgUnit1.getOrgUnit().name.compareTo(orgUnit2.getOrgUnit().name);
                        })
                        .collect(Collectors.toList())
                , year
                , month
                , monthsDisplayed
        ));
    }

    public Result simulatorCapacityForecastTableCompetenciesFragment(Integer year, Integer month, Boolean onlyConfirmed, Integer monthsDisplayed) {

        int warningLimitPercent = getPreferenceManagerPlugin().getPreferenceValueAsInteger(IMafConstants.ROADMAP_CAPACITY_SIMULATOR_WARNING_LIMIT_PREFERENCE);

        JsonNode idsAsJson = Json.parse(session("roadmap.selected.rows"));

        ArrayList<Long> ids = new ArrayList<>();
        for(JsonNode id: idsAsJson) {
            ids.add(id.asLong());
        }

        List<PortfolioEntryResourcePlanAllocatedActor> allocatedActors = new ArrayList<>();
        List<PortfolioEntryResourcePlanAllocatedCompetency> allocatedCompetencies = new ArrayList<>();
        Date yearStartTime = getStartTime(year, month);
        Date yearEndTime = getEndTime(year, month);

        ids.forEach(
                id -> {
                    allocatedActors.addAll(PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsListByPE(id, yearStartTime, yearEndTime, onlyConfirmed, null, null));
                    allocatedCompetencies.addAll(PortfolioEntryResourcePlanDAO.getPEPlanAllocatedCompetencyAsListByPE(id, yearStartTime, yearEndTime, onlyConfirmed, null));
                }
        );

        /**
         * Compute the capacities for the competencies and actor allocations
         * and group them by competency.
         */

        Map<Long, CompetencyCapacity> competencyCapacities = new HashMap<>();

        // Competency: the competency is simply the one of the allocated
        // comptency.
        for (PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency : allocatedCompetencies) {

            CompetencyCapacity competencyCapacity = null;
            if (competencyCapacities.containsKey(allocatedCompetency.competency.id)) {
                competencyCapacity = competencyCapacities.get(allocatedCompetency.competency.id);
            } else {
                competencyCapacity = new CompetencyCapacity(warningLimitPercent, allocatedCompetency.competency);
                competencyCapacities.put(allocatedCompetency.competency.id, competencyCapacity);
            }

            computeCapacity(allocatedCompetency.startDate, allocatedCompetency.endDate, allocatedCompetency.days, year, month,
                    competencyCapacity);
        }

        // Actor: the competency is the one of the actor of the allocated
        // actor.
        for (PortfolioEntryResourcePlanAllocatedActor allocatedActor : allocatedActors) {

            if (allocatedActor.actor.defaultCompetency != null) {

                CompetencyCapacity competencyCapacity = null;
                if (competencyCapacities.containsKey(allocatedActor.actor.defaultCompetency.id)) {
                    competencyCapacity = competencyCapacities.get(allocatedActor.actor.defaultCompetency.id);
                } else {
                    competencyCapacity = new CompetencyCapacity(warningLimitPercent, allocatedActor.actor.defaultCompetency);
                    competencyCapacities.put(allocatedActor.actor.defaultCompetency.id, competencyCapacity);
                }

                computeCapacity(allocatedActor.startDate, allocatedActor.endDate, getAllocatedDays(allocatedActor.days, allocatedActor.forecastDays),
                        year, month, competencyCapacity);

            }
        }

        for (Entry<Long, CompetencyCapacity> entry : competencyCapacities.entrySet()) {

            CompetencyCapacity competencyCapacity = entry.getValue();

            // Get the activity allocations.
            List<TimesheetActivityAllocatedActor> allocatedActivities = TimesheetDao.getTimesheetActivityAllocatedActorAsListByCompetencyAndPeriod(
                    competencyCapacity.getCompetency().id, yearStartTime, yearEndTime);

            // Compute the activity allocations.
            for (TimesheetActivityAllocatedActor allocatedActivity : allocatedActivities) {
                computeCapacity(allocatedActivity.startDate, allocatedActivity.endDate, allocatedActivity.days, year, month, competencyCapacity);
            }

            // Get the available actor capacities.
            List<models.pmo.ActorCapacity> actorCapacities = ActorDao.getActorCapacityAsListByCompetencyAndYear(competencyCapacity.getCompetency().id, year);

            // Compute the available actor capacities.
            for (models.pmo.ActorCapacity actorCapacity : actorCapacities) {
                competencyCapacity.addAvailable(getColumnIndex(month, actorCapacity.month - 1), actorCapacity.value);
            }
        }

        return ok(views.html.core.roadmap.roadmap_capacity_forecast_table_competencies_fragment.render(new ArrayList<>(competencyCapacities.values()), year, month, monthsDisplayed));
    }

    /**
     * The details of a cell of the the capacity forecast table.
     *
     * @param objectType
     *            the object type (org unit or competency)
     * @param objectId
     *            the object id (org unit id or competency id)
     * @param year
     *            the year
     * @param month
     *            the month
     */
    public Result simulatorCapacityForecastCellDetailsFragment(String objectType, Long objectId, Integer year, Integer month) {

        try {

            OrgUnit orgUnit = null;
            Competency competency = null;
            Class<?> cls = Class.forName(objectType);
            if (cls.equals(OrgUnit.class)) {
                orgUnit = OrgUnitDao.getOrgUnitById(objectId);
            } else if (cls.equals(Competency.class)) {
                competency = ActorDao.getCompetencyById(objectId);
            }

            JsonNode idsAsJson = Json.parse(session("roadmap.selected.rows"));

            ArrayList<Long> ids = new ArrayList<>();
            for(JsonNode id: idsAsJson) {
                ids.add(id.asLong());
            }

            /**
             * Compute the period according to the selected year.
             */
            Calendar monthStartDay = Calendar.getInstance();
            monthStartDay.set(Calendar.YEAR, year);
            monthStartDay.set(Calendar.MONTH, month);
            monthStartDay.set(Calendar.DAY_OF_MONTH, 1);
            monthStartDay.set(Calendar.HOUR_OF_DAY, 0);
            monthStartDay.set(Calendar.MINUTE, 0);
            monthStartDay.set(Calendar.SECOND, 0);
            monthStartDay.set(Calendar.MILLISECOND, 0);

            Calendar monthEndDay = Calendar.getInstance();
            monthEndDay.set(Calendar.YEAR, year);
            monthEndDay.set(Calendar.MONTH, month);
            monthEndDay.set(Calendar.DAY_OF_MONTH, monthEndDay.getActualMaximum(Calendar.DAY_OF_MONTH));
            monthEndDay.set(Calendar.HOUR_OF_DAY, 23);
            monthEndDay.set(Calendar.MINUTE, 59);
            monthEndDay.set(Calendar.SECOND, 59);
            monthEndDay.set(Calendar.MILLISECOND, 999);

            /**
             * Get the PE allocations.
             */
            List<PortfolioEntryResourcePlanAllocatedOrgUnit> allocatedOrgUnits = new ArrayList<>();
            List<PortfolioEntryResourcePlanAllocatedCompetency> allocatedCompetencies = new ArrayList<>();
            List<PortfolioEntryResourcePlanAllocatedActor> allocatedActors = new ArrayList<>();

            for (Long id : ids) {

                if (orgUnit != null) {

                    allocatedOrgUnits.addAll(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitAsListByPE(id, monthStartDay.getTime(),
                            monthEndDay.getTime(), false, objectId));

                    allocatedActors.addAll(PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsListByPE(id, monthStartDay.getTime(), monthEndDay.getTime(),
                            false, objectId, null));
                }
                if (competency != null) {
                    allocatedCompetencies.addAll(PortfolioEntryResourcePlanDAO.getPEPlanAllocatedCompetencyAsListByPE(id, monthStartDay.getTime(),
                            monthEndDay.getTime(), false, objectId));

                    allocatedActors.addAll(PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsListByPE(id, monthStartDay.getTime(), monthEndDay.getTime(),
                            false, null, objectId));
                }

            }

            /**
             * Compute the capacities for the objet (org unit or competency) and
             * actor allocations and group them by actor.
             */

            Map<Long, CapacityDetails> capacityDetailsRows = new HashMap<>();
            Map<Long, CapacityDetailsByInitiative> capacityDetailsByInitiativeRows = new HashMap<>();

            if (orgUnit != null) {
                // There is exactly one org unit.
                CapacityDetails capacityDetailsOrgUnit = new CapacityDetails(orgUnit);
                capacityDetailsOrgUnit.addPlannedPortfolioEntryConfirmed(
                        allocatedOrgUnits
                                .stream()
                                .filter(ResourceAllocation::isConfirmed)
                                .mapToDouble(allocatedOrgUnit -> {
                                    ResourceAllocationDetail detail = allocatedOrgUnit.getDetail(year, month);
                                    return detail == null ? 0 : detail.getDays();
                                })
                                .sum()
                );
                capacityDetailsOrgUnit.addPlannedPortfolioEntryNotConfirmed(
                        allocatedOrgUnits
                                .stream()
                                .filter(allocatedOrgUnit -> !allocatedOrgUnit.isConfirmed())
                                .mapToDouble(allocatedOrgUnit -> {
                                    ResourceAllocationDetail detail = allocatedOrgUnit.getDetail(year, month);
                                    return detail == null ? 0 : detail.getDays();
                                })
                                .sum()
                );
                capacityDetailsRows.put(0L, capacityDetailsOrgUnit);

                // Compute allocation by initiative
                allocatedOrgUnits.stream()
                        .forEach(orgUnitAllocation -> {
                            PortfolioEntry portfolioEntry = orgUnitAllocation.getAssociatedPortfolioEntry();

                            CapacityDetailsByInitiative capacityDetailsByInitiative;
                            if (capacityDetailsByInitiativeRows.containsKey(portfolioEntry.id)) {
                                capacityDetailsByInitiative = capacityDetailsByInitiativeRows.get(portfolioEntry.id);
                            } else {
                                capacityDetailsByInitiative = new CapacityDetailsByInitiative(portfolioEntry);
                            }

                            ResourceAllocationDetail orgUnitAllocationDetail = orgUnitAllocation.getDetail(year, month);
                            if (orgUnitAllocationDetail != null) {
                                if (orgUnitAllocation.isConfirmed()) {
                                    capacityDetailsByInitiative.addGenericConfirmed(orgUnitAllocationDetail.getDays());
                                } else {
                                    capacityDetailsByInitiative.addGenericNotConfirmed(orgUnitAllocationDetail.getDays());
                                }
                            }

                            capacityDetailsByInitiativeRows.put(portfolioEntry.id, capacityDetailsByInitiative);
                        });
            }

            if (competency != null) {
                // There is exactly one competency.
                CapacityDetails capacityDetailsCompetency = new CapacityDetails(competency);
                capacityDetailsRows.put(0L, capacityDetailsCompetency);
                for (PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency : allocatedCompetencies) {
                    computeCapacityDetails(allocatedCompetency.startDate, allocatedCompetency.endDate, allocatedCompetency.days, year, month, false,
                            allocatedCompetency.portfolioEntryResourcePlanAllocationStatusType.status.equals(PortfolioEntryResourcePlanAllocationStatusType.AllocationStatus.CONFIRMED), capacityDetailsCompetency);
                }
            }

            // Actor
            if (orgUnit != null) {
                orgUnit.actors
                        .stream()
                        .filter(a -> a.isActive)
                        .forEach(
                                actor -> capacityDetailsRows.put(actor.id, new CapacityDetails(actor))
                        );
            }

            if (competency != null) {
                for (Actor actor : competency.actorsWithDefault) {
                    capacityDetailsRows.put(actor.id, new CapacityDetails(actor));
                }
            }

            // Add project allocations
            allocatedActors.stream().forEach(allocatedActor -> {

                CapacityDetails capacityDetailsActor = capacityDetailsRows.get(allocatedActor.actor.id);
                ResourceAllocationDetail allocatedActorDetail = allocatedActor.getDetail(year, month);
                if (allocatedActorDetail != null) {

                    if (allocatedActor.isConfirmed()) {
                        capacityDetailsActor.addPlannedPortfolioEntryConfirmed(allocatedActorDetail.getDays());
                    } else {
                        capacityDetailsActor.addPlannedPortfolioEntryNotConfirmed(allocatedActorDetail.getDays());
                    }

                    // Compute allocation by initiative
                    PortfolioEntry portfolioEntry = allocatedActor.getAssociatedPortfolioEntry();

                    CapacityDetailsByInitiative capacityDetailsByInitiative;
                    if (capacityDetailsByInitiativeRows.containsKey(portfolioEntry.id)) {
                        capacityDetailsByInitiative = capacityDetailsByInitiativeRows.get(portfolioEntry.id);
                    } else {
                        capacityDetailsByInitiative = new CapacityDetailsByInitiative(portfolioEntry);
                    }

                    if (allocatedActor.isConfirmed()) {
                        capacityDetailsByInitiative.addNominativeConfirmed(allocatedActorDetail.getDays());
                    } else {
                        capacityDetailsByInitiative.addNominativeNotConfirmed(allocatedActorDetail.getDays());
                    }

                    capacityDetailsByInitiativeRows.put(portfolioEntry.id, capacityDetailsByInitiative);
                }
            });

            // Add activities and available

            capacityDetailsRows.entrySet().stream().filter(entry -> !entry.getKey().equals(0L)).forEach(entry -> {

                CapacityDetails capacityDetails = entry.getValue();

                // Get the activity allocations.
                capacityDetails.addPlannedActivity(
                    capacityDetails
                            .actor
                            .timesheetActivityAllocatedActors
                            .stream()
                            // Get detail for the given month and year
                            .map(allocatedActivity -> allocatedActivity.getDetail(year, month))
                            .filter(detail -> detail != null && detail.getDays() != null)
                            // Sum the days of the allocations
                            .mapToDouble(ResourceAllocationDetail::getDays)
                            .sum()
                );

                // Get the available actor capacity
                models.pmo.ActorCapacity actorCapacity = ActorDao.getActorCapacityByActorAndPeriod(capacityDetails.getActor().id, year, month + 1);
                if (actorCapacity != null) {
                    capacityDetails.addAvailable(actorCapacity.value);
                }
            });

            // get the month name
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, month);

            // compute the resource chart
            BasicBar resourceBarChart = new BasicBar();

            capacityDetailsRows.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals(0L))
                    .forEach(entry -> resourceBarChart.addCategory(entry.getValue().getActor().getName()));

            BasicBar.Elem elem1 = new BasicBar.Elem(Msg.get("core.roadmap.simulator.capacity_forecast.planned.label"));
            BasicBar.Elem elem2 = new BasicBar.Elem(Msg.get("core.roadmap.simulator.capacity_forecast.available.label"));

            capacityDetailsRows.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals(0L))
                    .forEach(entry -> {
                        CapacityDetails capacityDetails = entry.getValue();
                        elem1.addValue(
                                capacityDetails.getPlannedActivity()
                              + capacityDetails.getPlannedPortfolioEntryConfirmed()
                              + capacityDetails.getPlannedPortfolioEntryNotConfirmed()
                        );
                        elem2.addValue(capacityDetails.getAvailable());

            });

            resourceBarChart.addElem(elem1);
            resourceBarChart.addElem(elem2);

            // Compute initiative chart
            BasicBar initiativeBarChart = new BasicBar();

            capacityDetailsByInitiativeRows.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals(0L))
                    .forEach(entry -> {
                        String category = entry.getValue().portfolioEntry.governanceId != null ? entry.getValue().portfolioEntry.governanceId : entry.getValue().portfolioEntry.name;
                        initiativeBarChart.addCategory(category);
                    });

            BasicBar.Elem confirmedElem = new BasicBar.Elem(Msg.get("core.roadmap.simulator.capacity_forecast.confirmed.label"));
            BasicBar.Elem notConfirmedElem = new BasicBar.Elem(Msg.get("core.roadmap.simulator.capacity_kpis.allocation.additional2"));

            capacityDetailsByInitiativeRows.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals(0L))
                    .forEach(entry -> {
                        CapacityDetailsByInitiative capacityDetails = entry.getValue();
                        confirmedElem.addValue(capacityDetails.genericConfirmed + capacityDetails.nominativeConfirmed);
                        notConfirmedElem.addValue(capacityDetails.genericNotConfirmed + capacityDetails.nominativeNotConfirmed);
                    });

            initiativeBarChart.addElem(confirmedElem);
            initiativeBarChart.addElem(notConfirmedElem);

            return ok(roadmap_capacity_forecast_cell_details_fragment.render(
                    orgUnit,
                    competency,
                    new SimpleDateFormat("MMMM").format(cal.getTime()),
                    year,
                    new ArrayList<>(capacityDetailsRows.values()),
                    new ArrayList<>(capacityDetailsByInitiativeRows.values()),
                    resourceBarChart,
                    initiativeBarChart
            ));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }

    }

    public Result simulatorCapacityForecastActorsCellDetailsFragment(Long actorId, Integer year, Integer month) {

        JsonNode idsAsJson = Json.parse(session("roadmap.selected.rows"));

        ArrayList<Long> ids = new ArrayList<>();
        for(JsonNode id: idsAsJson) {
            ids.add(id.asLong());
        }

        Actor actor = ActorDao.getActorById(actorId);

        List<PortfolioEntryResourcePlanAllocatedActor> allocations = PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsListByActorAndActiveAndArchived(actorId, false, true);

        // Filter selected initiatives only
        allocations = allocations
                .stream()
                .filter(
                    allocation -> ids.contains(
                        allocation.portfolioEntryResourcePlan.lifeCycleInstancePlannings
                                .stream()
                                .filter(planning -> !planning.isFrozen)
                                .findFirst()
                                .get()
                                .lifeCycleInstance.portfolioEntry.id
                    )
                )
                .collect(Collectors.toList());

        Map<Long, ActorCapacityDetails> capacityDetailsRows = new HashMap<>();

        allocations.stream().forEach(allocation -> {
            if (allocation.portfolioEntryResourcePlanAllocatedActorDetails.isEmpty()) {
                allocation.computeAllocationDetails(budgetTrackingService.isActive(), getPreferenceManagerPlugin().getPreferenceValueAsBoolean(IMafConstants.RESOURCES_WEEK_DAYS_ALLOCATION_PREFERENCE));
            }
            ResourceAllocationDetail allocationDetail = allocation.getDetail(year, month);
            if (allocationDetail != null && allocationDetail.getDays() != 0.0) {
                PortfolioEntry pe = allocation.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry;
                ActorCapacityDetails detail = null;
                if (capacityDetailsRows.containsKey(allocation.id)) {
                    detail = capacityDetailsRows.get(allocation.id);
                } else {
                    detail = new ActorCapacityDetails(allocation.id, pe);
                }
                if (allocation.portfolioEntryResourcePlanAllocationStatusType.status.equals(PortfolioEntryResourcePlanAllocationStatusType.AllocationStatus.CONFIRMED)) {
                    detail.confirmedAllocation += allocationDetail.getDays();
                } else {
                    detail.notConfirmedAllocation += allocationDetail.getDays();
                }
                capacityDetailsRows.put(allocation.id, detail);
            }
        });

        BasicBar basicBarChart = new BasicBar();

        BasicBar.Elem confirmedElem = new BasicBar.Elem(Msg.get("core.roadmap.simulator.capacity_kpis.allocation.additional1"));
        BasicBar.Elem notConfirmedElem = new BasicBar.Elem(Msg.get("core.roadmap.simulator.capacity_kpis.allocation.additional2"));

        Map<Long, ActorCapacityDetails> barMap = capacityDetailsRows.values().stream()
            .collect(Collectors.toMap(
                detail -> detail.portfolioEntry.id,
                detail -> detail,
                (d1, d2) -> {
                    ActorCapacityDetails mergedDetail = new ActorCapacityDetails(d1.id, d1.portfolioEntry);
                    mergedDetail.confirmedAllocation = d1.confirmedAllocation + d2.confirmedAllocation;
                    mergedDetail.notConfirmedAllocation = d1.notConfirmedAllocation + d2.notConfirmedAllocation;
                    return mergedDetail;
                }
            ));

        barMap.values().forEach(value -> {
                String category = value.portfolioEntry.governanceId != null ? value.portfolioEntry.governanceId : value.portfolioEntry.name;
                basicBarChart.addCategory(category);

                notConfirmedElem.addValue(value.notConfirmedAllocation);
                confirmedElem.addValue(value.confirmedAllocation);
            });

        basicBarChart.addElem(notConfirmedElem);
        basicBarChart.addElem(confirmedElem);

        return ok(views.html.core.roadmap.roadmap_capacity_forecast_actors_cell_details_fragment.render(
                actor,
                year,
                month,
                new ArrayList<>(capacityDetailsRows.values()),
                basicBarChart
        ));
    }

    /**
     * Get the portfolio entry table and a filter config.
     * 
     * @param filterConfig
     *            the filter config.
     */
    private Pair<Table<PortfolioEntryListView>, Pagination<PortfolioEntry>> getTable(PostQueryFilterConfig<PortfolioEntryListView, PortfolioEntry> filterConfig)
            throws AccountManagementException {

        try {

            ExpressionList<PortfolioEntry> expressionList = filterConfig.updateWithSearchExpression(PortfolioEntryDynamicHelper.getPortfolioEntriesViewAllowedAsQuery(getSecurityService()));
            filterConfig.updateWithSortExpression(expressionList);

            Pagination<PortfolioEntry> pagination = new Pagination<>(this.getPreferenceManagerPlugin(), filterConfig.getSize(expressionList),
                    expressionList, filterConfig.getPostQueryFilters(), filterConfig.getPostQueryOrderBy());

            pagination.setCurrentPage(filterConfig.getCurrentPage());

            List<PortfolioEntryListView> portfolioEntryListView = pagination.getListOfObjects().stream().map(PortfolioEntryListView::new).collect(Collectors.toList());

            Table<PortfolioEntryListView> table = this.getTableProvider().get().portfolioEntry.templateTable.fillForFilterConfig(portfolioEntryListView,
                    getColumnsToHide(filterConfig));

            if (getSecurityService().restrict(IMafConstants.ROADMAP_SIMULATOR_PERMISSION)) {

                table.addAjaxRowAction(Msg.get("core.roadmap.simulator.capacity_kpis"),
                        controllers.core.routes.RoadmapController.simulatorKpisFragment().url(), "simulator-kpis");
                table.addLinkRowAction(Msg.get("core.roadmap.simulator.capacity_forecast"),
                        controllers.core.routes.RoadmapController.simulatorCapacityForecast().url());

                table.setAllIdsUrl(controllers.core.routes.RoadmapController.getAllIds().url());
            }

            return Pair.of(table, pagination);

        } catch (AccountManagementException e) {
            return null;
        }

    }

    /**
     * Get the columns to hide.
     * 
     * @param filterConfig
     *            the filter config
     */
    private static Set<String> getColumnsToHide(PostQueryFilterConfig<PortfolioEntryListView, PortfolioEntry> filterConfig) {
        Set<String> columnsToHide = filterConfig.getColumnsToHide();
        columnsToHide.add("stakeholderTypes");
        return columnsToHide;
    }

    /**
     * Compute and share the capacity of an allocation.
     * 
     * @param startDate
     *            the allocation start date
     * @param endDate
     *            the allocation end date
     * @param allocatedDays
     *            the number of allocated days
     * @param year
     *            the year
     * @param resourceRequestCapacity
     *            the resrouce request capacity
     */
    private static void computeCapacity(Date startDate, Date endDate, BigDecimal allocatedDays, Integer year, Integer month,
            ResourceRequestCapacity resourceRequestCapacity) {

        // compute the day rate
        long endMillis = removeTime(endDate).getTimeInMillis();
        long startMillis = removeTime(startDate).getTimeInMillis();
        int days = 1 + (int) ((endMillis - startMillis) / (1000 * 60 * 60 * 24));
        Double dayRate = allocatedDays.doubleValue() / days;

        Calendar start = removeTime(startDate);
        for (int i = 0; i < days; i++) {
            if (year == start.get(Calendar.YEAR)) {
                resourceRequestCapacity.addPlanned(getColumnIndex(month, start.get(Calendar.MONTH)), dayRate);
            }
            start.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    /**
     * Get the 0-based index of the displayed column for a given month based on the starting month number
     *
     * Example with starting month in june and current month april:
     * columnIndex = 4 - 6 + 12 = 10
     *
     * @param startMonth the starting month (first column month)
     * @param currentMonth the current month (the one to get the column index)
     * @return
     */
    public static Integer getColumnIndex(Integer startMonth, Integer currentMonth) {
        Integer columnIndex = currentMonth - startMonth;

        if (columnIndex > 11) {
            columnIndex -= 12;
        }

        if (columnIndex < 0) {
            columnIndex += 12;
        }

        return columnIndex;
    }

    public static double getTotalAvailableByColumnIndex(List<ResourceRequestCapacity> capacities, Integer column) {
        return BigDecimal.valueOf(capacities.stream().mapToDouble(capacity -> capacity.resourceCapacityColumns.get(column).available).sum()).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    public static Double getTotalPlannedByColumnIndex(List<ResourceRequestCapacity> capacities, Integer column) {
        return BigDecimal.valueOf(capacities.stream().mapToDouble(capacity -> capacity.resourceCapacityColumns.get(column).planned).sum()).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Compute and share the capacity of an allocation.
     * 
     * @param startDate
     *            the allocation start date
     * @param endDate
     *            the allocation end date
     * @param allocatedDays
     *            the number of allocated days
     * @param year
     *            the year
     * @param month
     *            the month
     * @param isActivity
     *            true if the allocation is for an activity
     * @param isConfirmed
     *            true if the allocation is confirmed
     * @param capacityDetails
     *            the capacity details
     */
    private static void computeCapacityDetails(Date startDate, Date endDate, BigDecimal allocatedDays, Integer year, Integer month, boolean isActivity,
            boolean isConfirmed, CapacityDetails capacityDetails) {

        // compute the day rate
        long endMillis = removeTime(endDate).getTimeInMillis();
        long startMillis = removeTime(startDate).getTimeInMillis();
        int days = 1 + (int) ((endMillis - startMillis) / (1000 * 60 * 60 * 24));
        Double dayRate = allocatedDays.doubleValue() / days;

        Calendar start = removeTime(startDate);
        for (int i = 0; i < days; i++) {
            if (year.intValue() == start.get(Calendar.YEAR) && month.intValue() == start.get(Calendar.MONTH)) {
                if (isActivity) {
                    capacityDetails.addPlannedActivity(dayRate);
                } else {
                    if (isConfirmed) {
                        capacityDetails.addPlannedPortfolioEntryConfirmed(dayRate);
                    } else {
                        capacityDetails.addPlannedPortfolioEntryNotConfirmed(dayRate);
                    }
                }
            }
            start.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    /**
     * Remove the time part of a date and return a calendar.
     * 
     * @param date
     *            the date
     */
    private static Calendar removeTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * The capacity forecast configuration form.
     * 
     * @author Johann Kohler
     * 
     */
    public static class CapacityForecastForm {

        public String selectedRows;

        @Required
        public Integer year;

        public boolean onlyConfirmed;

        /**
         * Default constructor.
         */
        public CapacityForecastForm() {
        }

        /**
         * Initiate the form with default values.
         * 
         * @param selectedRows
         *            the selected rows
         * @param year
         *            the year
         */
        public CapacityForecastForm(String selectedRows, Integer year) {
            this.selectedRows = selectedRows;
            this.year = year;
            this.onlyConfirmed = false;
        }

        /**
         * Get the possible years list.
         * 
         * First = current year - 1<br/>
         * Last = current year + 5
         */
        public static ISelectableValueHolderCollection<Integer> getYears() {
            ISelectableValueHolderCollection<Integer> r = new DefaultSelectableValueHolderCollection<Integer>();
            Calendar today = Calendar.getInstance();
            today.add(Calendar.YEAR, -1);
            int start = today.get(Calendar.YEAR);
            today.add(Calendar.YEAR, 7);
            int end = today.get(Calendar.YEAR);
            for (int year = start; year < end; year++) {
                r.add(new DefaultSelectableValueHolder<Integer>(year, String.valueOf(year)));
            }
            return r;
        }
    }

    /**
     * The capacity of a resource request (org unit or competency).
     * 
     * @author Johann Kohler
     * 
     */
    public abstract static class ResourceRequestCapacity {

        private Map<Integer, ResourceCapacityMonth> resourceCapacityColumns;

        /**
         * Default constructor.
         * 
         * @param warningLimitPercent
         *            the warning limit in percent
         */
        public ResourceRequestCapacity(int warningLimitPercent) {

            this.resourceCapacityColumns = new HashMap<>();
            for (int i = 0; i < 12; i++) {
                this.resourceCapacityColumns.put(i, new ResourceCapacityMonth(warningLimitPercent));
            }
        }

        /**
         * @return the resourceCapacityColumns
         */
        public Map<Integer, ResourceCapacityMonth> getResourceCapacityColumns() {
            return resourceCapacityColumns;
        }

        /**
         * Increase the planned value for a column.
         * 
         * @param column
         *            the column
         * @param planned
         *            the planned value to add
         */
        public void addPlanned(int column, double planned) {
            this.resourceCapacityColumns.get(column).addPlanned(planned);
        }

        /**
         * increase the available value for a column.
         * 
         * @param column
         *            the column
         * @param available
         *            the available value to add
         */
        public void addAvailable(int column, double available) {
            this.resourceCapacityColumns.get(column).addAvailable(available);
        }

        public double getTotalPlanned(int numberOfMonths) {
            double total = 0.0;
            for (int i = 0; i < numberOfMonths; i++) {
                total += this.resourceCapacityColumns.get(i).planned;
            }
            return BigDecimal.valueOf(total).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }

        public double getTotalAvailable(int numberOfMonths) {
            double total = 0.0;
            for (int i = 0; i < numberOfMonths; i++) {
                total += this.resourceCapacityColumns.get(i).available;
            }
            return BigDecimal.valueOf(total).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
    }

    /**
     * The capacity of an org unit.
     * 
     * @author Johann Kohler
     * 
     */
    public static class OrgUnitCapacity extends ResourceRequestCapacity {

        private OrgUnit orgUnit;

        /**
         * Default constructor.
         * 
         * @param warningLimitPercent
         *            the warning limit in percent
         * @param orgUnit
         *            the org unit
         */
        public OrgUnitCapacity(int warningLimitPercent, OrgUnit orgUnit) {
            super(warningLimitPercent);
            this.orgUnit = orgUnit;
        }

        /**
         * @return the orgUnit
         */
        public OrgUnit getOrgUnit() {
            return orgUnit;
        }

    }

    public static class ActorCapacity extends ResourceRequestCapacity {

        private Actor actor;

        /**
         * Default constructor.
         *
         * @param warningLimitPercent the warning limit in percent
         */
        public ActorCapacity(int warningLimitPercent, Actor actor) {
            super(warningLimitPercent);
            this.actor = actor;
        }

        public Actor getActor() {
            return actor;
        }
    }

    /**
     * The capacity of a competency.
     * 
     * @author Johann Kohler
     * 
     */
    public static class CompetencyCapacity extends ResourceRequestCapacity {

        private Competency competency;

        /**
         * Default constructor.
         * 
         * @param warningLimitPercent
         *            the warning limit in percent
         * @param competency
         *            the competency
         */
        public CompetencyCapacity(int warningLimitPercent, Competency competency) {
            super(warningLimitPercent);
            this.competency = competency;
        }

        /**
         * @return the competency
         */
        public Competency getCompetency() {
            return competency;
        }

    }

    /**
     * The month capacity for a resource.
     * 
     * @author Johann Kohler
     * 
     */
    public static class ResourceCapacityMonth {

        private double planned;
        private double available;
        private int warningLimitPercent;

        /**
         * Default constructor.
         * 
         * @param warningLimitPercent
         *            the warning limit in percent
         */
        public ResourceCapacityMonth(int warningLimitPercent) {
            this.planned = 0.0;
            this.available = 0.0;
            this.warningLimitPercent = warningLimitPercent;
        }

        /**
         * @return the planned
         */
        public double getPlanned() {
            return Math.round(planned * 10) / 10.0;
        }

        /**
         * @return the available
         */
        public double getAvailable() {
            return Math.round(available * 10) / 10.0;
        }

        /**
         * Get the bootstrap class for the background.
         */
        public String getBootstrapBackground() {

            if (this.getAvailable() >= this.getPlanned() - 0.01) {
                return "success";
            } else if (this.getAvailable() * (1 + (warningLimitPercent / 100.0)) >= this.getPlanned() - 0.01) {
                return "warning";
            }
            return "danger";
        }

        /**
         * Increase the planned value.
         * 
         * @param planned
         *            the planned value to add
         */
        public void addPlanned(double planned) {
            this.planned += planned;
        }

        /**
         * increase the available value.
         * 
         * @param available
         *            the available value to add
         */
        public void addAvailable(double available) {
            this.available += available;
        }
    }

    public static class ActorCapacityDetails {
        public Long id;
        public PortfolioEntry portfolioEntry;
        public double confirmedAllocation;
        public double notConfirmedAllocation;

        public ActorCapacityDetails() {
            this.confirmedAllocation = 0;
            this.notConfirmedAllocation = 0;
        }

        public ActorCapacityDetails(Long id, PortfolioEntry portfolioEntry) {
            super();
            this.id = id;
            this.portfolioEntry = portfolioEntry;
        }
    }

    public static class CapacityDetailsByInitiative {

        private PortfolioEntry portfolioEntry;
        private double genericConfirmed;
        private double genericNotConfirmed;
        private double nominativeConfirmed;
        private double nominativeNotConfirmed;

        public CapacityDetailsByInitiative() {
            this.genericConfirmed = 0.0;
            this.genericNotConfirmed = 0.0;
            this.nominativeConfirmed = 0.0;
            this.nominativeNotConfirmed = 0.0;
        }

        public CapacityDetailsByInitiative(PortfolioEntry portfolioEntry) {
            this();
            this.portfolioEntry = portfolioEntry;
        }

        public void addGenericConfirmed(Double days) {
            this.genericConfirmed += days;
        }

        public void addGenericNotConfirmed(Double days) {
            this.genericNotConfirmed += days;
        }

        public void addNominativeConfirmed(Double days) {
            this.nominativeConfirmed += days;
        }

        public void addNominativeNotConfirmed(Double days) {
            this.nominativeNotConfirmed += days;
        }

        public PortfolioEntry getPortfolioEntry() {
            return portfolioEntry;
        }

        public double getGenericConfirmed() {
            return genericConfirmed;
        }

        public double getGenericNotConfirmed() {
            return genericNotConfirmed;
        }

        public double getNominativeConfirmed() {
            return nominativeConfirmed;
        }

        public double getNominativeNotConfirmed() {
            return nominativeNotConfirmed;
        }
    }

    /**
     * The capacity details of a resource.
     * 
     * @author Johann Kohler
     * 
     */
    public static class CapacityDetails {

        private Actor actor;
        private OrgUnit orgUnit;
        private Competency competency;
        private double plannedPortfolioEntryConfirmed;
        private double plannedPortfolioEntryNotConfirmed;
        private double plannedActivity;
        private double available;

        /**
         * Default constructor.
         */
        public CapacityDetails() {
            this.plannedPortfolioEntryConfirmed = 0;
            this.plannedPortfolioEntryNotConfirmed = 0;
            this.plannedActivity = 0;
            this.available = 0;
        }

        /**
         * Default constructor.
         * 
         * @param actor
         *            the actor
         */
        public CapacityDetails(Actor actor) {
            this();
            this.actor = actor;
        }

        /**
         * Default constructor.
         * 
         * @param orgUnit
         *            the org unit
         */
        public CapacityDetails(OrgUnit orgUnit) {
            this();
            this.orgUnit = orgUnit;
        }

        /**
         * Default constructor.
         * 
         * @param competency
         *            the competency
         */
        public CapacityDetails(Competency competency) {
            this();
            this.competency = competency;
        }

        /**
         * @return the actor
         */
        public Actor getActor() {
            return actor;
        }

        /**
         * @return the orgUnit
         */
        public OrgUnit getOrgUnit() {
            return orgUnit;
        }

        /**
         * @return the competency
         */
        public Competency getCompetency() {
            return competency;
        }

        /**
         * @return the plannedPortfolioEntryConfirmed
         */
        public double getPlannedPortfolioEntryConfirmed() {
            return Math.round(plannedPortfolioEntryConfirmed * 10) / 10.0;
        }

        /**
         * @return the plannedPortfolioEntryNotConfirmed
         */
        public double getPlannedPortfolioEntryNotConfirmed() {
            return Math.round(plannedPortfolioEntryNotConfirmed * 10) / 10.0;
        }

        /**
         * @return the plannedActivity
         */
        public double getPlannedActivity() {
            return Math.round(plannedActivity * 10) / 10.0;
        }

        /**
         * @return the available
         */
        public double getAvailable() {
            return Math.round(available * 10) / 10.0;
        }

        /**
         * Return true if the actor row should be displayed.
         */
        public boolean displayActorRow() {
            return !(!this.actor.isActive && this.getPlannedPortfolioEntryConfirmed() < 0.01 && this.getPlannedPortfolioEntryNotConfirmed() < 0.01
                    && this.getPlannedActivity() < 0.01 && this.getAvailable() < 0.01);
        }

        /**
         * Get the bootstrap class for the background.
         */
        public String getBootstrapBackground() {

            if (this.getAvailable() >= this.getPlannedPortfolioEntryConfirmed() + this.getPlannedPortfolioEntryNotConfirmed() + this.getPlannedActivity()
                    - 0.01) {
                return "success";
            } else if (this.getAvailable() >= this.getPlannedPortfolioEntryConfirmed() + this.getPlannedActivity() - 0.01) {
                return "warning";
            }

            return "danger";
        }

        /**
         * Increase the plannedPortfolioEntryConfirmed value.
         * 
         * @param planned
         *            the planned value to add
         */
        public void addPlannedPortfolioEntryConfirmed(double planned) {
            this.plannedPortfolioEntryConfirmed += planned;
        }

        /**
         * Increase the plannedPortfolioEntryNotConfirmed value.
         * 
         * @param planned
         *            the planned value to add
         */
        public void addPlannedPortfolioEntryNotConfirmed(double planned) {
            this.plannedPortfolioEntryNotConfirmed += planned;
        }

        /**
         * Increase the plannedActivity value.
         * 
         * @param planned
         *            the planned value to add
         */
        public void addPlannedActivity(double planned) {
            this.plannedActivity += planned;
        }

        /**
         * increase the available value.
         * 
         * @param available
         *            the available value to add
         */
        public void addAvailable(double available) {
            this.available += available;
        }
    }

    /**
     * Get the user session manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    /**
     * Get the notification manager service.
     */
    private INotificationManagerPlugin getNotificationManagerPlugin() {
        return notificationManagerPlugin;
    }

    /**
     * Get the personal storage service.
     */
    private IPersonalStoragePlugin getPersonalStoragePlugin() {
        return personalStoragePlugin;
    }

    /**
     * Get the system admin utils.
     */
    private ISysAdminUtils getSysAdminUtils() {
        return sysAdminUtils;
    }

    /**
     * Get the security service.
     */
    private ISecurityService getSecurityService() {
        return securityService;
    }

    /**
     * Get the i18n messages service.
     */
    II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    /**
     * Get the Play configuration service.
     */
    private Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return this.preferenceManagerPlugin;
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }

    /**
     * Get the budget tracking service.
     */
    private IBudgetTrackingService getBudgetTrackingService() {
        return this.budgetTrackingService;
    }

}
