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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.StakeholderDao;
import dao.timesheet.TimesheetDao;
import framework.security.ISecurityService;
import framework.services.ServiceStaticAccessor;
import framework.services.account.AccountManagementException;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.FilterConfig;
import framework.utils.IColumnFormatter;
import framework.utils.JqueryGantt;
import framework.utils.Menu.ClickableMenuItem;
import framework.utils.Menu.HeaderMenuItem;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.SideBar;
import framework.utils.Table;
import models.finance.BudgetBucket;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import models.sql.ActorHierarchy;
import models.timesheet.TimesheetActivityAllocatedActor;
import models.timesheet.TimesheetReport;
import play.Configuration;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import security.dynamic.BudgetBucketDynamicHelper;
import utils.SortableCollection;
import utils.SortableCollection.DateSortableObject;
import utils.gantt.SourceDataValue;
import utils.gantt.SourceItem;
import utils.gantt.SourceValue;
import utils.table.ActorListView;
import utils.table.BudgetBucketListView;
import utils.table.OrgUnitListView;
import utils.table.PortfolioEntryListView;
import utils.table.PortfolioEntryResourcePlanAllocatedActorListView;
import utils.table.PortfolioListView;
import utils.table.TimesheetActivityAllocatedActorListView;
import utils.table.TimesheetReportListView;

/**
 * The controller which displays different cockpits of the sign in user.
 * 
 * @author Johann Kohler
 */
@Restrict({ @Group(IMafConstants.COCKPIT_DISPLAY_PERMISSION) })
public class CockpitController extends Controller {

    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private ISecurityService securityService;
    @Inject
    private II18nMessagesPlugin messagesPlugin;
    @Inject
    private Configuration configuration;

    private static Logger.ALogger log = Logger.of(CockpitController.class);

    /**
     * Display the list of portfolio entries for which the sign-in user is the
     * manager or a stakeholder.
     * 
     * @param asManagerPage
     *            the current page for the "as manager" table
     * @param asStakeholderPage
     *            the current page for the "as stakeholder" table
     * @param viewAllAsManager
     *            set to true to display also the archived entries (for the
     *            "as manager" table)
     */
    public Result initiatives(Integer asManagerPage, Integer asStakeholderPage, Boolean viewAllAsManager) {

        /**
         * get the current actor id
         */
        Long actorId = null;
        try {
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            Actor actor = ActorDao.getActorByUid(uid);
            actorId = actor.id;
        } catch (Exception e) {
            Logger.error("impossible to find the actor of the sign-in user");
            return redirect(controllers.routes.Application.index());
        }

        /**
         * get the portfolio entries for which the current actor is the manager
         */

        Pagination<PortfolioEntry> asManagerPagination = PortfolioEntryDao.getPEAsPaginationByManager(actorId, viewAllAsManager);
        asManagerPagination.setPageQueryName("asManagerPage");
        asManagerPagination.setCurrentPage(asManagerPage);

        List<PortfolioEntryListView> portfolioEntryListView = new ArrayList<PortfolioEntryListView>();
        for (PortfolioEntry portfolioEntry : asManagerPagination.getListOfObjects()) {
            portfolioEntryListView.add(new PortfolioEntryListView(portfolioEntry));
        }

        Table<PortfolioEntryListView> asManagerTable = PortfolioEntryListView.templateTable.fill(portfolioEntryListView,
                PortfolioEntryListView.getHideNonDefaultColumns(true, true));

        /**
         * get the portfolio entries for which the current actor is a
         * stakeholder
         */

        Pagination<PortfolioEntry> asStakeholderPagination = PortfolioEntryDao.getPEActiveAsPaginationByDirectStakeholder(actorId);
        asStakeholderPagination.setPageQueryName("asStakeholderPage");
        asStakeholderPagination.setCurrentPage(asStakeholderPage);

        portfolioEntryListView = new ArrayList<PortfolioEntryListView>();
        for (PortfolioEntry portfolioEntry : asStakeholderPagination.getListOfObjects()) {
            portfolioEntryListView
                    .add(new PortfolioEntryListView(portfolioEntry, StakeholderDao.getStakeholderAsListByActorAndPE(actorId, portfolioEntry.id)));
        }

        Table<PortfolioEntryListView> asStakeholderTable = PortfolioEntryListView.templateTable.fill(portfolioEntryListView,
                PortfolioEntryListView.getHideNonDefaultColumns(false, true));

        return ok(views.html.core.cockpit.cockpit_myinitiatives_list.render(asManagerTable, asManagerPagination, viewAllAsManager, asStakeholderTable,
                asStakeholderPagination));

    }

    /**
     * Display the list of portfolio for which the sign-in user is the manager
     * or a stakeholder.
     * 
     * @param asManagerPage
     *            the current page for the "as manager" table
     * @param asStakeholderPage
     *            the current page for the "as stakeholder" table
     * @param viewAllAsManager
     *            set to true to display also the archived entries (for the
     *            "as manager" table)
     */
    public Result portfolios(Integer asManagerPage, Integer asStakeholderPage, Boolean viewAllAsManager) {

        /**
         * get the current actor id
         */
        Long actorId = null;
        try {
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            Actor actor = ActorDao.getActorByUid(uid);
            actorId = actor.id;
        } catch (Exception e) {
            Logger.error("impossible to find the actor of the sign-in user");
            return redirect(controllers.routes.Application.index());
        }

        /**
         * get the portfolios for which the current actor is the manager
         */

        Pagination<Portfolio> asManagerPagination = PortfolioDao.getPortfolioAsPaginationByManager(actorId, viewAllAsManager);
        asManagerPagination.setPageQueryName("asManagerPage");
        asManagerPagination.setCurrentPage(asManagerPage);

        List<PortfolioListView> portfolioListView = new ArrayList<PortfolioListView>();
        for (Portfolio portfolio : asManagerPagination.getListOfObjects()) {
            portfolioListView.add(new PortfolioListView(portfolio, StakeholderDao.getStakeholderAsListByActorAndPortfolio(actorId, portfolio.id)));
        }

        Set<String> hideColumnsForPortfolios = new HashSet<String>();
        hideColumnsForPortfolios.add("manager");
        hideColumnsForPortfolios.add("stakeholderTypes");

        Table<PortfolioListView> asManagerTable = PortfolioListView.templateTable.fill(portfolioListView, hideColumnsForPortfolios);

        /**
         * get the active portfolio for which the current actor is a stakeholder
         */

        Pagination<Portfolio> asStakeholderPagination = PortfolioDao.getPortfolioActiveAsPaginationByStakeholder(actorId);
        asStakeholderPagination.setPageQueryName("asStakeholderPage");
        asStakeholderPagination.setCurrentPage(asStakeholderPage);

        portfolioListView = new ArrayList<PortfolioListView>();
        for (Portfolio portfolio : asStakeholderPagination.getListOfObjects()) {
            portfolioListView.add(new PortfolioListView(portfolio, StakeholderDao.getStakeholderAsListByActorAndPortfolio(actorId, portfolio.id)));
        }

        Table<PortfolioListView> asStakeholderTable = PortfolioListView.templateTable.fill(portfolioListView);

        return ok(views.html.core.cockpit.cockpit_myportfolios_list.render(asManagerTable, asManagerPagination, viewAllAsManager, asStakeholderTable,
                asStakeholderPagination));

    }

    /**
     * Display the list of the employees for which the sign-in user is the
     * manager.
     */
    public Result subordinates() {

        /**
         * get the current actor id
         */
        Long actorId = null;
        try {

            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            Actor actor = ActorDao.getActorByUid(uid);
            actorId = actor.id;
        } catch (Exception e) {
            Logger.error("impossible to find the actor of the sign-in user");
            return redirect(controllers.routes.Application.index());
        }

        // construct the subordinate table

        List<Actor> actors = ActorDao.getActorAsListByManager(actorId);

        List<ActorListView> actorsListView = new ArrayList<ActorListView>();
        for (Actor a : actors) {
            actorsListView.add(new ActorListView(a));
        }

        Set<String> hideColumnsForEntry = new HashSet<String>();
        hideColumnsForEntry.add("manager");

        Table<ActorListView> actorFilledTable = ActorListView.templateTable.fill(actorsListView, hideColumnsForEntry);

        return ok(views.html.core.cockpit.cockpit_subordinates.render(actorFilledTable));
    }

    /**
     * Display the allocations' gantt chart of the employees for which the
     * sign-in user is the manager.
     */
    public Result subordinatesAllocations() {

        /**
         * get the current actor id
         */
        Long actorId = null;
        try {

            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            Actor actor = ActorDao.getActorByUid(uid);
            actorId = actor.id;
        } catch (Exception e) {
            Logger.error("impossible to find the actor of the sign-in user");
            return redirect(controllers.routes.Application.index());
        }

        // prepare the data (to order them)
        SortableCollection<DateSortableObject> sortableCollection = new SortableCollection<>();
        for (PortfolioEntryResourcePlanAllocatedActor allocatedActor : PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsListByManager(actorId, true)) {
            if (allocatedActor.endDate != null) {
                sortableCollection.addObject(new DateSortableObject(allocatedActor.endDate, allocatedActor));
            }
        }
        for (TimesheetActivityAllocatedActor allocatedActivity : TimesheetDao.getTimesheetActivityAllocatedActorAsListByManager(actorId, true)) {
            if (allocatedActivity.endDate != null) {
                sortableCollection.addObject(new DateSortableObject(allocatedActivity.endDate, allocatedActivity));
            }
        }

        // construct the gantt

        List<SourceItem> items = new ArrayList<SourceItem>();

        for (DateSortableObject dateSortableObject : sortableCollection.getSorted()) {

            if (dateSortableObject.getObject() instanceof PortfolioEntryResourcePlanAllocatedActor) {

                PortfolioEntryResourcePlanAllocatedActor allocatedActor = (PortfolioEntryResourcePlanAllocatedActor) dateSortableObject.getObject();

                // get the from date
                Date from = allocatedActor.startDate;

                // get the to date
                Date to = allocatedActor.endDate;

                // get the portfolio entry
                Long portfolioEntryId = allocatedActor.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
                PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(portfolioEntryId);

                String packageName = allocatedActor.portfolioEntryPlanningPackage != null ? allocatedActor.portfolioEntryPlanningPackage.getName() + " / "
                        : "";

                SourceItem item = new SourceItem(allocatedActor.actor.getNameHumanReadable(), portfolioEntry.getName());

                String cssClass = null;

                if (from != null) {

                    to = JqueryGantt.cleanToDate(from, to);
                    cssClass = "";

                } else {

                    from = to;
                    cssClass = "diamond diamond-";

                }

                if (allocatedActor.isConfirmed) {
                    cssClass += "success";
                } else {
                    cssClass += "warning";
                }

                SourceDataValue dataValue = new SourceDataValue(controllers.core.routes.PortfolioEntryPlanningController.resources(portfolioEntry.id).url(),
                        null, null, null, null);

                item.values.add(new SourceValue(from, to, "",
                        packageName + views.html.framework_views.parts.formats.display_number.render(allocatedActor.days, null, false).body(), cssClass,
                        dataValue));

                items.add(item);

            }

            if (dateSortableObject.getObject() instanceof TimesheetActivityAllocatedActor) {

                TimesheetActivityAllocatedActor allocatedActivity = (TimesheetActivityAllocatedActor) dateSortableObject.getObject();

                // get the from date
                Date from = allocatedActivity.startDate;

                // get the to date
                Date to = allocatedActivity.endDate;

                SourceItem item = new SourceItem(allocatedActivity.actor.getNameHumanReadable(), allocatedActivity.timesheetActivity.getName());

                String cssClass = null;

                if (from != null) {

                    to = JqueryGantt.cleanToDate(from, to);
                    cssClass = "";

                } else {

                    from = to;
                    cssClass = "diamond diamond-";

                }

                cssClass += "info";

                SourceDataValue dataValue = new SourceDataValue(
                        controllers.core.routes.ActorController.allocationDetails(allocatedActivity.actor.id, 0, 0, false).url(), null, null, null, null);

                item.values.add(new SourceValue(from, to, "",
                        views.html.framework_views.parts.formats.display_number.render(allocatedActivity.days, null, false).body(), cssClass, dataValue));

                items.add(item);
            }

        }

        String ganttSource = "";
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            ganttSource = ow.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            Logger.error(e.getMessage());
        }

        return ok(views.html.core.cockpit.cockpit_subordinates_allocations.render(ganttSource));

    }

    /**
     * Display the allocations (by portfolio entry and by activity) of the
     * employees for which the sign-in user is the manager.
     */
    public Result subordinatesAllocationsDetails() {

        /**
         * get the current actor id
         */
        Long actorId = null;
        try {

            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            Actor actor = ActorDao.getActorByUid(uid);
            actorId = actor.id;
        } catch (Exception e) {
            Logger.error("impossible to find the actor of the sign-in user");
            return redirect(controllers.routes.Application.index());
        }

        // construct the portfolio entry table

        String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
        FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> portfolioEntryFilter = PortfolioEntryResourcePlanAllocatedActorListView.filterConfig
                .getCurrent(uid, request());

        Pair<Table<PortfolioEntryResourcePlanAllocatedActorListView>, Pagination<PortfolioEntryResourcePlanAllocatedActor>> portfolioEntryTable;
        portfolioEntryTable = getPEAllocationsTable(actorId, portfolioEntryFilter);

        // construct the activity table

        FilterConfig<TimesheetActivityAllocatedActorListView> activityFilter = TimesheetActivityAllocatedActorListView.filterConfig.getCurrent(uid,
                request());

        Pair<Table<TimesheetActivityAllocatedActorListView>, Pagination<TimesheetActivityAllocatedActor>> activityTable = getActivityAllocationsTable(actorId,
                activityFilter);

        return ok(views.html.core.cockpit.cockpit_subordinates_allocations_details.render(portfolioEntryTable.getLeft(), portfolioEntryTable.getRight(),
                portfolioEntryFilter, activityTable.getLeft(), activityTable.getRight(), activityFilter));
    }

    /**
     * Filter the porfolio entry allocations.
     */
    public Result portfolioEntryAllocationsFilter() {

        try {

            /**
             * get the current actor id
             */
            Long actorId = null;
            try {
                String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
                Actor actor = ActorDao.getActorByUid(uid);
                actorId = actor.id;
            } catch (Exception e) {
                Logger.error("impossible to find the actor of the sign-in user");
                return redirect(controllers.routes.Application.index());
            }

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> filterConfig = PortfolioEntryResourcePlanAllocatedActorListView.filterConfig
                    .persistCurrentInDefault(uid, request());

            if (filterConfig == null) {
                return ok(views.html.framework_views.parts.table.dynamic_tableview_no_more_compatible.render());
            } else {

                // get the table
                Pair<Table<PortfolioEntryResourcePlanAllocatedActorListView>, Pagination<PortfolioEntryResourcePlanAllocatedActor>> t;
                t = getPEAllocationsTable(actorId, filterConfig);

                return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));

            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getMessagesPlugin());
        }

    }

    /**
     * Get all portfolio entry allocation ids according to the current filter
     * configuration.
     */
    public Result getAllportfolioEntryAllocationIds() {

        try {

            // fill the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> filterConfig = PortfolioEntryResourcePlanAllocatedActorListView.filterConfig
                    .persistCurrentInDefault(uid, request());

            // get the current actor
            Actor actor = ActorDao.getActorByUid(uid);

            ExpressionList<PortfolioEntryResourcePlanAllocatedActor> expressionList = filterConfig
                    .updateWithSearchExpression(PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsExprByManager(actor.id, true));

            List<String> ids = new ArrayList<>();
            for (PortfolioEntryResourcePlanAllocatedActor allocatedActor : expressionList.findList()) {
                ids.add(String.valueOf(allocatedActor.id));
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.valueToTree(ids);

            return ok(node);

        } catch (Exception e) {
            return internalServerError();
        }
    }

    /**
     * Confirm the selected portfolio entry allocations.
     */
    public Result confirmPortfolioEntryAllocations() {

        try {

            // get the current actor

            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            Actor actor = ActorDao.getActorByUid(uid);

            List<String> ids = FilterConfig.getIdsFromRequest(request());

            for (String idString : ids) {

                Long id = Long.parseLong(idString);

                PortfolioEntryResourcePlanAllocatedActor allocatedActor = PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorById(id);

                if (allocatedActor.actor.manager.id.equals(actor.id)) {
                    allocatedActor.isConfirmed = true;
                    allocatedActor.update();
                    Logger.debug("confirm: " + allocatedActor.id);
                } else {
                    Logger.error("impossible to confirm the allocation " + allocatedActor.id + " because the manager (" + allocatedActor.actor.manager.id
                            + ") of the concerned actor is not the current one (" + actor.id + ").");
                }
            }

            return ok("<div class=\"alert alert-success\">"
                    + Msg.get("core.cockpit.subordinates.allocations.details.portfolio_entry.action.confirm.successful") + "</div>");

            // return
            // redirect(controllers.core.routes.CockpitController.subordinatesAllocationsDetails());

        } catch (Exception e) {
            return internalServerError();
        }
    }

    /**
     * Filter the activity allocations.
     */
    public Result activityAllocationsFilter() {

        try {

            /**
             * get the current actor id
             */
            Long actorId = null;
            try {
                String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
                Actor actor = ActorDao.getActorByUid(uid);
                actorId = actor.id;
            } catch (Exception e) {
                Logger.error("impossible to find the actor of the sign-in user");
                return redirect(controllers.routes.Application.index());
            }

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<TimesheetActivityAllocatedActorListView> filterConfig = TimesheetActivityAllocatedActorListView.filterConfig
                    .persistCurrentInDefault(uid, request());

            if (filterConfig == null) {
                return ok(views.html.framework_views.parts.table.dynamic_tableview_no_more_compatible.render());
            } else {

                // get the table
                Pair<Table<TimesheetActivityAllocatedActorListView>, Pagination<TimesheetActivityAllocatedActor>> t = getActivityAllocationsTable(actorId,
                        filterConfig);

                return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));

            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getMessagesPlugin());
        }
    }

    /**
     * Display the late and the submitted timesheets of the subordinates of the
     * sign-in user.
     */
    public Result subordinatesTimesheet() {

        /**
         * get the current actor id
         */
        Long actorId = null;
        try {

            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            Actor actor = ActorDao.getActorByUid(uid);
            actorId = actor.id;
        } catch (Exception e) {
            Logger.error("impossible to find the actor of the sign-in user");
            return redirect(controllers.routes.Application.index());
        }

        /**
         * Construct the late timesheets table
         */

        // create missing timesheet reports
        List<Actor> actors = ActorDao.getActorAsListByManager(actorId);
        for (Actor a : actors) {
            TimesheetDao.createMissingTimesheetReport(TimesheetReport.Type.WEEKLY, a);
        }

        List<TimesheetReport> lateReports = TimesheetDao.getTimesheetReportLateAsListByManager(actorId);

        List<TimesheetReportListView> lateReportListView = new ArrayList<TimesheetReportListView>();
        for (TimesheetReport r : lateReports) {
            lateReportListView.add(new TimesheetReportListView(r));
        }

        Set<String> hideColumnsForLateReports = new HashSet<String>();
        hideColumnsForLateReports.add("approveActionLink");

        Table<TimesheetReportListView> lateReportsFilledTable = TimesheetReportListView.templateTable.fill(lateReportListView, hideColumnsForLateReports);

        /**
         * Construct the submitted timesheets table
         */

        List<TimesheetReport> submittedReports = TimesheetDao.getTimesheetReportSubmittedAsListByManager(actorId);

        List<TimesheetReportListView> submittedReportListView = new ArrayList<TimesheetReportListView>();
        for (TimesheetReport r : submittedReports) {
            submittedReportListView.add(new TimesheetReportListView(r));
        }

        Set<String> hideColumnsForSubmittedReports = new HashSet<String>();
        hideColumnsForSubmittedReports.add("reminderActionLink");

        Table<TimesheetReportListView> submittedReportsFilledTable = TimesheetReportListView.templateTable.fill(submittedReportListView,
                hideColumnsForSubmittedReports);

        return ok(views.html.core.cockpit.cockpit_subordinates_timesheet.render(lateReportsFilledTable, submittedReportsFilledTable));
    }

    /**
     * Display the list of org units for which the sign-in user is the manager.
     * 
     * @param page
     *            the current page
     * @param viewAll
     *            set to true to display also the inactive org units
     */
    public Result orgUnits(Integer page, Boolean viewAll) {

        /**
         * get the current actor id
         */
        Long actorId = null;
        try {

            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            Actor actor = ActorDao.getActorByUid(uid);
            actorId = actor.id;
        } catch (Exception e) {
            Logger.error("impossible to find the actor of the sign-in user");
            return redirect(controllers.routes.Application.index());
        }

        Pagination<OrgUnit> pagination = OrgUnitDao.getOrgUnitAsPaginationByActor(actorId, viewAll);
        pagination.setCurrentPage(page);

        List<OrgUnitListView> orgUnitListView = new ArrayList<OrgUnitListView>();
        for (OrgUnit orgUnit : pagination.getListOfObjects()) {
            orgUnitListView.add(new OrgUnitListView(orgUnit));
        }

        Set<String> columnsToHide = new HashSet<String>();
        columnsToHide.add("manager");

        Table<OrgUnitListView> filledTable = OrgUnitListView.templateTable.fill(orgUnitListView, columnsToHide);

        return ok(views.html.core.cockpit.cockpit_myorgunits_list.render(filledTable, pagination, viewAll));

    }

    /**
     * Display the budget buckets.
     * 
     * As owner table: display the budget buckets for which the sign-in user is
     * the owner.
     * 
     * As responsible table: Display the budget buckets for which the sign-in
     * user is responsible. Responsible means that the budet bucket's owner is a
     * subordinate (direct or not) of the sign-in user.
     * 
     * @param asOwnerPage
     *            the current page for the as owner table
     * @param asResponsiblePage
     *            the current page for the as responsible page
     * @param viewAllAsOwner
     *            set to true to include the inactive budget buckets
     * @param viewAllAsResponsible
     *            set to true to include the inactive budget buckets
     */
    public Result budgetBuckets(Integer asOwnerPage, Integer asResponsiblePage, Boolean viewAllAsOwner, Boolean viewAllAsResponsible) {

        /**
         * get the current actor id
         */
        Long actorId = null;
        try {

            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            Actor actor = ActorDao.getActorByUid(uid);
            actorId = actor.id;
        } catch (Exception e) {
            Logger.error("impossible to find the actor of the sign-in user");
            return redirect(controllers.routes.Application.index());
        }

        // as owner table

        Expression expressionAsOwner = Expr.eq("owner.id", actorId);
        if (!viewAllAsOwner) {
            expressionAsOwner = Expr.and(expressionAsOwner, Expr.eq("isActive", true));
        }

        Pagination<BudgetBucket> asOwnerPagination = getBudgetBucketPagination(asOwnerPage, expressionAsOwner);
        asOwnerPagination.setPageQueryName("asOwnerPage");

        Set<String> hideColumnsForBudgetBucketTable = new HashSet<String>();
        hideColumnsForBudgetBucketTable.add("owner");

        Table<BudgetBucketListView> filledAsOwnerTable = getBudgetBucketTable(asOwnerPagination, hideColumnsForBudgetBucketTable);

        // as responsible table

        Set<Long> subordinatesId = ActorHierarchy.getSubordinatesAsId(actorId);

        Expression expressionAsResponsible = null;
        if (subordinatesId != null && !subordinatesId.isEmpty()) {
            expressionAsResponsible = Expr.in("owner.id", subordinatesId);
        } else {
            expressionAsResponsible = Expr.eq("1", "0");
        }
        if (!viewAllAsResponsible) {
            expressionAsResponsible = Expr.and(expressionAsResponsible, Expr.eq("isActive", true));
        }

        Pagination<BudgetBucket> asResponsiblePagination = getBudgetBucketPagination(asResponsiblePage, expressionAsResponsible);
        asResponsiblePagination.setPageQueryName("asResponsiblePage");

        Table<BudgetBucketListView> filledAsResponsibleTable = getBudgetBucketTable(asResponsiblePagination, null);

        return ok(views.html.core.cockpit.cockpit_my_budget_buckets.render(filledAsOwnerTable, asOwnerPagination, filledAsResponsibleTable,
                asResponsiblePagination, viewAllAsOwner, viewAllAsResponsible));

    }

    /**
     * Get the pagination object for the authorized budget buckets according to
     * a filter on the owner.
     * 
     * @param page
     *            the current page
     * @param ownerIdExpression
     *            the filter expression on the owner
     */
    private Pagination<BudgetBucket> getBudgetBucketPagination(Integer page, Expression ownerIdExpression) {

        ExpressionList<BudgetBucket> query = null;
        try {
            query = BudgetBucketDynamicHelper.getBudgetBucketsViewAllowedAsQuery(ownerIdExpression, null, getSecurityService());
        } catch (Exception e) {
            Logger.error("impossible to construct the \"budget bucket view all\" query", e);
        }

        Pagination<BudgetBucket> pagination = new Pagination<BudgetBucket>(query);
        pagination.setCurrentPage(page);

        return pagination;

    }

    /**
     * Get the table object for a budget bucket pagination object.
     * 
     * @param pagination
     *            the budget bucket pagination object
     * @param hideColumns
     *            the columns to hid
     */
    private Table<BudgetBucketListView> getBudgetBucketTable(Pagination<BudgetBucket> pagination, Set<String> hideColumns) {

        List<BudgetBucketListView> budgetBucketListView = new ArrayList<BudgetBucketListView>();
        for (BudgetBucket budgetBucket : pagination.getListOfObjects()) {
            budgetBucketListView.add(new BudgetBucketListView(budgetBucket));
        }

        Table<BudgetBucketListView> filledTable = null;

        if (hideColumns != null) {
            filledTable = BudgetBucketListView.templateTable.fill(budgetBucketListView, hideColumns);
        } else {
            filledTable = BudgetBucketListView.templateTable.fill(budgetBucketListView);
        }

        return filledTable;
    }

    /**
     * Construct the side bar.
     * 
     * @param currentType
     *            the current menu item type, useful to select the correct item
     * @param securityService
     *            the security service
     * @throws AccountManagementException
     */
    public static SideBar getSideBar(MenuItemType currentType, ISecurityService securityService) throws AccountManagementException {

        SideBar sideBar = new SideBar();

        sideBar.addMenuItem(new ClickableMenuItem("core.cockpit.sidebar.initiatives", controllers.core.routes.CockpitController.initiatives(0, 0, false),
                "fa fa-sticky-note", currentType.equals(MenuItemType.MY_INITIATIVES)));

        sideBar.addMenuItem(new ClickableMenuItem("core.cockpit.sidebar.portfolios", controllers.core.routes.CockpitController.portfolios(0, 0, false),
                "fa fa-folder", currentType.equals(MenuItemType.MY_PORTFOLIOS)));

        HeaderMenuItem employeesMenu = new HeaderMenuItem("core.cockpit.sidebar.subordinates", "fa fa-child", currentType.equals(MenuItemType.MY_EMPLOYEES));

        ClickableMenuItem employeesOverviewMenu = new ClickableMenuItem("core.cockpit.sidebar.subordinates.members",
                controllers.core.routes.CockpitController.subordinates(), "fa fa-list", false);

        ClickableMenuItem employeesAllocationMenu = new ClickableMenuItem("core.cockpit.sidebar.subordinates.allocations_overview",
                controllers.core.routes.CockpitController.subordinatesAllocations(), "fa fa-tachometer", false);

        ClickableMenuItem employeesAllocationDetailsMenu = new ClickableMenuItem("core.cockpit.sidebar.subordinates.allocations_details",
                controllers.core.routes.CockpitController.subordinatesAllocationsDetails(), "fa fa-search-plus", false);

        ClickableMenuItem employeesTimesheetMenu = new ClickableMenuItem("core.cockpit.sidebar.subordinates.timesheet",
                controllers.core.routes.CockpitController.subordinatesTimesheet(), "fa fa-clock-o", false);
        ArrayList<String[]> list = new ArrayList<String[]>();
        list.add(new String[] { IMafConstants.TIMESHEET_APPROVAL_ALL_PERMISSION });
        list.add(new String[] { IMafConstants.TIMESHEET_APPROVAL_AS_MANAGER_PERMISSION });
        employeesTimesheetMenu.setAuthorizedPermissions(list);

        employeesMenu.addSubMenuItem(employeesOverviewMenu);
        employeesMenu.addSubMenuItem(employeesAllocationMenu);
        employeesMenu.addSubMenuItem(employeesAllocationDetailsMenu);
        employeesMenu.addSubMenuItem(employeesTimesheetMenu);

        sideBar.addMenuItem(employeesMenu);

        sideBar.addMenuItem(new ClickableMenuItem("core.cockpit.sidebar.org_units", controllers.core.routes.CockpitController.orgUnits(0, false),
                "fa fa-building", currentType.equals(MenuItemType.MY_ORG_UNITS)));

        if (securityService.restrict(IMafConstants.BUDGET_BUCKET_VIEW_ALL_PERMISSION)
                || securityService.restrict(IMafConstants.BUDGET_BUCKET_VIEW_AS_OWNER_PERMISSION)) {
            sideBar.addMenuItem(
                    new ClickableMenuItem("core.cockpit.sidebar.budget_buckets", controllers.core.routes.CockpitController.budgetBuckets(0, 0, false, false),
                            "fa fa-calculator", currentType.equals(MenuItemType.MY_BUDGET_BUCKETS)));
        }

        try {

            String uid = ServiceStaticAccessor.getUserSessionManagerPlugin().getUserSessionId(ctx());
            Actor actor = ActorDao.getActorByUid(uid);

            ClickableMenuItem myEmployeeCardMenu = new ClickableMenuItem("core.cockpit.sidebar.my_allocations",
                    controllers.core.routes.ActorController.allocation(actor.id), "fa fa-book", currentType.equals(MenuItemType.MY_EMPLOYEE_CARD));
            myEmployeeCardMenu.setIsImportant(true);
            sideBar.addMenuItem(myEmployeeCardMenu);

        } catch (Exception e) {
            Logger.error("impossible to find the actor of the sign-in user");
        }

        return sideBar;

    }

    /**
     * Get the initiative's allocations table for the subordinates of an actor.
     * 
     * @param actorId
     *            the actor id
     * @param filterConfig
     *            the filter config.
     */
    private Pair<Table<PortfolioEntryResourcePlanAllocatedActorListView>, Pagination<PortfolioEntryResourcePlanAllocatedActor>> getPEAllocationsTable(
            Long actorId, FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> filterConfig) {

        ExpressionList<PortfolioEntryResourcePlanAllocatedActor> expressionList = filterConfig
                .updateWithSearchExpression(PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsExprByManager(actorId, true));
        filterConfig.updateWithSortExpression(expressionList);

        Pagination<PortfolioEntryResourcePlanAllocatedActor> pagination = new Pagination<PortfolioEntryResourcePlanAllocatedActor>(expressionList);
        pagination.setCurrentPage(filterConfig.getCurrentPage());

        List<PortfolioEntryResourcePlanAllocatedActorListView> listView = new ArrayList<PortfolioEntryResourcePlanAllocatedActorListView>();
        for (PortfolioEntryResourcePlanAllocatedActor portfolioEntryResourcePlanAllocatedActor : pagination.getListOfObjects()) {
            listView.add(new PortfolioEntryResourcePlanAllocatedActorListView(portfolioEntryResourcePlanAllocatedActor));
        }

        Set<String> columnsToHide = filterConfig.getColumnsToHide();
        columnsToHide.add("editActionLink");
        columnsToHide.add("removeActionLink");
        columnsToHide.add("forecastDays");
        columnsToHide.add("dailyRate");

        Table<PortfolioEntryResourcePlanAllocatedActorListView> table = PortfolioEntryResourcePlanAllocatedActorListView.templateTable
                .fillForFilterConfig(listView, columnsToHide);

        table.addAjaxRowAction(Msg.get("core.cockpit.subordinates.allocations.details.portfolio_entry.action.confirm"),
                controllers.core.routes.CockpitController.confirmPortfolioEntryAllocations().url(), "confirm-result");

        table.setAllIdsUrl(controllers.core.routes.CockpitController.getAllportfolioEntryAllocationIds().url());

        table.setLineAction(new IColumnFormatter<PortfolioEntryResourcePlanAllocatedActorListView>() {
            @Override
            public String apply(PortfolioEntryResourcePlanAllocatedActorListView portfolioEntryResourcePlanAllocatedActorListView, Object value) {
                return controllers.core.routes.PortfolioEntryPlanningController.resources(portfolioEntryResourcePlanAllocatedActorListView.portfolioEntryId)
                        .url();
            }
        });

        return Pair.of(table, pagination);

    }

    /**
     * Get the activity's allocations table for the subordinates of an actor.
     * 
     * @param actorId
     *            the actor id
     * @param filterConfig
     *            the filter config.
     */
    private Pair<Table<TimesheetActivityAllocatedActorListView>, Pagination<TimesheetActivityAllocatedActor>> getActivityAllocationsTable(Long actorId,
            FilterConfig<TimesheetActivityAllocatedActorListView> filterConfig) {

        ExpressionList<TimesheetActivityAllocatedActor> expressionList = filterConfig
                .updateWithSearchExpression(TimesheetDao.getTimesheetActivityAllocatedActorAsExprByManager(actorId, true));
        filterConfig.updateWithSortExpression(expressionList);

        Pagination<TimesheetActivityAllocatedActor> pagination = new Pagination<TimesheetActivityAllocatedActor>(expressionList);
        pagination.setCurrentPage(filterConfig.getCurrentPage());

        List<TimesheetActivityAllocatedActorListView> listView = new ArrayList<TimesheetActivityAllocatedActorListView>();
        for (TimesheetActivityAllocatedActor portfolioEntryResourcePlanAllocatedActor : pagination.getListOfObjects()) {
            listView.add(new TimesheetActivityAllocatedActorListView(portfolioEntryResourcePlanAllocatedActor));
        }

        Set<String> columnsToHide = filterConfig.getColumnsToHide();
        columnsToHide.add("editActionLink");
        columnsToHide.add("removeActionLink");

        Table<TimesheetActivityAllocatedActorListView> table = TimesheetActivityAllocatedActorListView.templateTable.fillForFilterConfig(listView,
                columnsToHide);

        table.setLineAction(new IColumnFormatter<TimesheetActivityAllocatedActorListView>() {
            @Override
            public String apply(TimesheetActivityAllocatedActorListView timesheetActivityAllocatedActorListView, Object value) {
                return controllers.core.routes.ActorController.allocationDetails(timesheetActivityAllocatedActorListView.actor.id, 0, 0, false).url();
            }
        });

        return Pair.of(table, pagination);

    }

    /**
     * Get the user session manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    /**
     * Get the security service.
     */
    private ISecurityService getSecurityService() {
        return securityService;
    }

    /**
     * The menu item type.
     * 
     * @author Johann Kohler
     * 
     */
    public static enum MenuItemType {
        MY_INITIATIVES, MY_PORTFOLIOS, MY_EMPLOYEES, MY_ORG_UNITS, MY_BUDGET_BUCKETS, MY_EMPLOYEE_CARD;
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getMessagesPlugin() {
        return messagesPlugin;
    }

    /**
     * Get the Play configuration service.
     */
    private Configuration getConfiguration() {
        return configuration;
    }
}
