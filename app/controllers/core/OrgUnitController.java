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

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import constants.IMafConstants;
import controllers.ControllersUtils;
import controllers.api.core.RootApiController;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioEntryDao;
import dao.timesheet.TimesheetDao;
import framework.security.ISecurityService;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.CustomAttributeFormAndDisplayHandler;
import framework.utils.FilterConfig;
import framework.utils.IColumnFormatter;
import framework.utils.JqueryGantt;
import framework.utils.Menu.ClickableMenuItem;
import framework.utils.Menu.HeaderMenuItem;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.SideBar;
import framework.utils.Table;
import framework.utils.Utilities;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.PortfolioEntry;
import models.timesheet.TimesheetActivityAllocatedActor;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckActorExists;
import security.CheckOrgUnitExists;
import utils.SortableCollection;
import utils.SortableCollection.DateSortableObject;
import utils.form.OrgUnitFormData;
import utils.form.TimesheetActivityAllocatedActorFormData;
import utils.gantt.SourceDataValue;
import utils.gantt.SourceItem;
import utils.gantt.SourceValue;
import utils.table.ActorListView;
import utils.table.OrgUnitListView;
import utils.table.PortfolioEntryListView;
import utils.table.PortfolioEntryResourcePlanAllocatedActorListView;
import utils.table.PortfolioEntryResourcePlanAllocatedOrgUnitListView;
import utils.table.TimesheetActivityAllocatedActorListView;

/**
 * The controller which displays / allows to edit an org unit.
 * 
 * @author Johann Kohler
 */
public class OrgUnitController extends Controller {
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;

    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;

    private static Logger.ALogger log = Logger.of(OrgUnitController.class);

    public static Form<OrgUnitFormData> formTemplate = Form.form(OrgUnitFormData.class);

    private static Form<TimesheetActivityAllocatedActorFormData> allocatedActivityFormTemplate = Form.form(TimesheetActivityAllocatedActorFormData.class);

    /**
     * Display the details of an org unit with the list of portfolio entries.
     * 
     * @param id
     *            the org unit id
     * @param page
     *            the current page for the portfolio entries table
     */
    @With(CheckOrgUnitExists.class)
    @SubjectPresent
    public Result view(Long id, Integer page) {

        // get the orgUnit
        OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(id);

        // construct the corresponding form data (for the custom attributes)
        OrgUnitFormData orgUnitFormData = new OrgUnitFormData(orgUnit);

        // construct the children table
        List<OrgUnitListView> orgUnitListView = new ArrayList<OrgUnitListView>();
        for (OrgUnit child : OrgUnitDao.getOrgUnitActiveAsListByParent(id)) {
            orgUnitListView.add(new OrgUnitListView(child));
        }
        Set<String> columnsToHideForChildren = new HashSet<>();
        columnsToHideForChildren.add("isActive");
        Table<OrgUnitListView> childrenTable = OrgUnitListView.templateTable.fill(orgUnitListView, columnsToHideForChildren);

        // construct the actors table
        List<ActorListView> actorListView = new ArrayList<ActorListView>();
        for (Actor actor : ActorDao.getActorActiveAsListByOrgUnit(id)) {
            actorListView.add(new ActorListView(actor));
        }
        Set<String> columnsToHideForActors = new HashSet<>();
        columnsToHideForActors.add("orgUnit");
        columnsToHideForActors.add("isActive");
        Table<ActorListView> actorsTable = ActorListView.templateTable.fill(actorListView, columnsToHideForActors);

        return ok(views.html.core.orgunit.org_unit_view.render(orgUnit, orgUnitFormData, childrenTable, actorsTable));
    }

    /**
     * Form to create a new org unit (available from Admin menu).
     */
    @Restrict({ @Group(IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION) })
    public Result create() {

        // construct the form
        Form<OrgUnitFormData> orgUnitForm = formTemplate;

        // add the custom attributes default values
        CustomAttributeFormAndDisplayHandler.fillWithValues(orgUnitForm, OrgUnit.class, null);

        return ok(views.html.core.orgunit.org_unit_new.render(orgUnitForm, OrgUnitDao.getOrgUnitTypeActiveAsVH()));
    }

    /**
     * Form to edit an org unit (available from the details of an org unit).
     * 
     * @param id
     *            the org unit id
     */
    @With(CheckOrgUnitExists.class)
    @Restrict({ @Group(IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION) })
    public Result edit(Long id) {

        // get the orgUnit
        OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(id);

        // construct the form
        Form<OrgUnitFormData> orgUnitForm = formTemplate.fill(new OrgUnitFormData(orgUnit));

        // add the custom attributes values
        CustomAttributeFormAndDisplayHandler.fillWithValues(orgUnitForm, OrgUnit.class, id);

        return ok(views.html.core.orgunit.org_unit_edit.render(orgUnit, orgUnitForm, OrgUnitDao.getOrgUnitTypeActiveAsVH()));
    }

    /**
     * Process the save of an org unit (create and edit cases).
     */
    @Restrict({ @Group(IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION) })
    public Result save() {

        // bind the form
        Form<OrgUnitFormData> boundForm = formTemplate.bindFromRequest();

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, OrgUnit.class)) {

            // edit case
            if (boundForm.data().get("id") != null) {
                // get the orgUnit
                Long id = Long.valueOf(boundForm.data().get("id"));
                OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(id);
                return ok(views.html.core.orgunit.org_unit_edit.render(orgUnit, boundForm, OrgUnitDao.getOrgUnitTypeActiveAsVH()));
            } else { // new case
                return ok(views.html.core.orgunit.org_unit_new.render(boundForm, OrgUnitDao.getOrgUnitTypeActiveAsVH()));
            }
        }

        OrgUnitFormData orgUnitFormData = boundForm.get();

        OrgUnit orgUnit = null;

        // edit case
        if (orgUnitFormData.id != null) {

            orgUnit = OrgUnitDao.getOrgUnitById(orgUnitFormData.id);
            orgUnitFormData.fill(orgUnit);
            orgUnit.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.org_unit.edit.successful"));

        } else { // new case

            orgUnit = new OrgUnit();
            orgUnitFormData.fill(orgUnit);
            orgUnit.save();

            Utilities.sendSuccessFlashMessage(Msg.get("core.org_unit.new.successful"));

        }

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, OrgUnit.class, orgUnit.id);

        RootApiController.flushFilters();

        return redirect(controllers.core.routes.OrgUnitController.view(orgUnit.id, 0));
    }

    /**
     * Delete an org unit.
     * 
     * @param id
     *            the org unit id
     */
    @With(CheckOrgUnitExists.class)
    @Restrict({ @Group(IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION) })
    public Result delete(Long id) {

        // get the org unit
        OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(id);

        // delete the org unit
        orgUnit.doDelete();

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get("core.org_unit.delete.successful"));

        RootApiController.flushFilters();

        return redirect(controllers.core.routes.SearchController.index());
    }

    /**
     * Display the active portfolio entries of the org unit. direct stakeholder.
     * 
     * @param id
     *            the org unit id
     * @param page
     *            the current page
     */
    @With(CheckOrgUnitExists.class)
    @Dynamic(IMafConstants.ORG_UNIT_VIEW_DYNAMIC_PERMISSION)
    public Result listPortfolioEntries(Long id, Integer page) {

        // get the org unit
        OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(id);

        // get the portfolio entries
        Pagination<PortfolioEntry> pagination = PortfolioEntryDao.getPEActiveAsPaginationByOrgUnit(id);
        pagination.setCurrentPage(page);

        List<PortfolioEntryListView> portfolioEntriesView = new ArrayList<PortfolioEntryListView>();
        for (PortfolioEntry portfolioEntry : pagination.getListOfObjects()) {
            portfolioEntriesView.add(new PortfolioEntryListView(portfolioEntry));
        }

        Table<PortfolioEntryListView> filledTable = PortfolioEntryListView.templateTable.fill(portfolioEntriesView,
                PortfolioEntryListView.getHideNonDefaultColumns(true, true));

        return ok(views.html.core.orgunit.org_unit_portfolio_entry_list.render(orgUnit, filledTable, pagination));
    }

    /**
     * Display the gantt of actors' allocations of the org unit.
     * 
     * @param id
     *            the org unit id
     */
    @With(CheckOrgUnitExists.class)
    @Dynamic(IMafConstants.ORG_UNIT_VIEW_DYNAMIC_PERMISSION)
    public Result allocation(Long id) {

        // get the org unit
        OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(id);

        // prepare the data (to order them)
        SortableCollection<DateSortableObject> sortableCollection = new SortableCollection<>();
        for (PortfolioEntryResourcePlanAllocatedActor allocatedActor : PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsListByOrgUnitAndActive(id,
                true)) {
            if (allocatedActor.endDate != null) {
                sortableCollection.addObject(new DateSortableObject(allocatedActor.endDate, allocatedActor));
            }
        }
        for (TimesheetActivityAllocatedActor allocatedActivity : TimesheetDao.getTimesheetActivityAllocatedActorAsListByOrgUnit(id, true)) {
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

        return ok(views.html.core.orgunit.org_unit_allocation.render(orgUnit, ganttSource));

    }

    /**
     * Display the allocations (direct for portfolio entry, of actors for
     * portfolio entry and activity) of the org unit.
     * 
     * @param id
     *            the org unit id
     * @param page
     *            the current page for portfolio entry allocations (of the org
     *            unit)
     */
    @With(CheckOrgUnitExists.class)
    @Dynamic(IMafConstants.ORG_UNIT_VIEW_DYNAMIC_PERMISSION)
    public Result allocationDetails(Long id, Integer page) {

        // get the org unit
        OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(id);

        // construct the portfolio entry table

        Pagination<PortfolioEntryResourcePlanAllocatedOrgUnit> portfolioEntryPagination = PortfolioEntryResourcePlanDAO
                .getPEResourcePlanAllocatedOrgUnitAsPaginationByOrgUnit(id, true);
        portfolioEntryPagination.setCurrentPage(page);

        List<PortfolioEntryResourcePlanAllocatedOrgUnitListView> allocationListView = new ArrayList<PortfolioEntryResourcePlanAllocatedOrgUnitListView>();
        for (PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit : portfolioEntryPagination.getListOfObjects()) {
            allocationListView.add(new PortfolioEntryResourcePlanAllocatedOrgUnitListView(allocatedOrgUnit));
        }

        Set<String> columnsToHide = new HashSet<String>();
        columnsToHide.add("editActionLink");
        columnsToHide.add("removeActionLink");
        columnsToHide.add("orgUnit");

        Table<PortfolioEntryResourcePlanAllocatedOrgUnitListView> portfolioEntryTable = PortfolioEntryResourcePlanAllocatedOrgUnitListView.templateTable
                .fill(allocationListView, columnsToHide);

        portfolioEntryTable.setLineAction(new IColumnFormatter<PortfolioEntryResourcePlanAllocatedOrgUnitListView>() {
            @Override
            public String apply(PortfolioEntryResourcePlanAllocatedOrgUnitListView portfolioEntryResourcePlanAllocatedOrgUnitListView, Object value) {
                return controllers.core.routes.PortfolioEntryPlanningController.resources(portfolioEntryResourcePlanAllocatedOrgUnitListView.portfolioEntryId)
                        .url();
            }
        });

        // construct the actors portfolio entry table

        String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
        FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> actorsPortfolioEntryFilter = PortfolioEntryResourcePlanAllocatedActorListView.filterConfig
                .getCurrent(uid, request());

        Pair<Table<PortfolioEntryResourcePlanAllocatedActorListView>, Pagination<PortfolioEntryResourcePlanAllocatedActor>> actorsPortfolioEntryTable;
        actorsPortfolioEntryTable = getActorsPEAllocTable(id, actorsPortfolioEntryFilter);

        // construct the actors activity table

        FilterConfig<TimesheetActivityAllocatedActorListView> actorsActivityFilter = TimesheetActivityAllocatedActorListView.filterConfig.getCurrent(uid,
                request());

        Pair<Table<TimesheetActivityAllocatedActorListView>, Pagination<TimesheetActivityAllocatedActor>> actorsActivityTable;
        actorsActivityTable = getActorsActivityAllocTable(id, actorsActivityFilter);

        return ok(views.html.core.orgunit.org_unit_allocation_details.render(orgUnit, portfolioEntryTable, portfolioEntryPagination,
                actorsPortfolioEntryTable.getLeft(), actorsPortfolioEntryTable.getRight(), actorsPortfolioEntryFilter, actorsActivityTable.getLeft(),
                actorsActivityTable.getRight(), actorsActivityFilter));
    }

    /**
     * Filter the porfolio entry allocations for actors.
     * 
     * @param id
     *            the actor id
     */
    @With(CheckOrgUnitExists.class)
    @Dynamic(IMafConstants.ORG_UNIT_VIEW_DYNAMIC_PERMISSION)
    public Result actorsPortfolioEntryAllocationsFilter(Long id) {

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> filterConfig = PortfolioEntryResourcePlanAllocatedActorListView.filterConfig
                    .persistCurrentInDefault(uid, request());

            if (filterConfig == null) {
                return ok(views.html.framework_views.parts.table.dynamic_tableview_no_more_compatible.render());
            } else {

                // get the table
                Pair<Table<PortfolioEntryResourcePlanAllocatedActorListView>, Pagination<PortfolioEntryResourcePlanAllocatedActor>> t = getActorsPEAllocTable(
                        id, filterConfig);

                return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));

            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }

    }

    /**
     * Filter the activity allocations for actors.
     * 
     * @param id
     *            the actor id
     */
    @With(CheckOrgUnitExists.class)
    @Dynamic(IMafConstants.ORG_UNIT_VIEW_DYNAMIC_PERMISSION)
    public Result actorsActivityAllocationsFilter(Long id) {

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<TimesheetActivityAllocatedActorListView> filterConfig = TimesheetActivityAllocatedActorListView.filterConfig
                    .persistCurrentInDefault(uid, request());

            if (filterConfig == null) {
                return ok(views.html.framework_views.parts.table.dynamic_tableview_no_more_compatible.render());
            } else {

                // get the table
                Pair<Table<TimesheetActivityAllocatedActorListView>, Pagination<TimesheetActivityAllocatedActor>> t = getActorsActivityAllocTable(id,
                        filterConfig);

                return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));

            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Form to create an actor allocation with an activity.
     * 
     * @param id
     *            the actor id
     */
    @With(CheckActorExists.class)
    @Restrict({ @Group(IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION) })
    public Result manageActorAllocatedActivity(Long id) {

        // get the org unit
        OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(id);

        // initiate the form with the template
        Form<TimesheetActivityAllocatedActorFormData> allocatedActivityForm = allocatedActivityFormTemplate;

        // add the custom attributes default values
        CustomAttributeFormAndDisplayHandler.fillWithValues(allocatedActivityForm, TimesheetActivityAllocatedActor.class, null);

        return ok(views.html.core.orgunit.actor_allocated_activity_manage.render(orgUnit, allocatedActivityForm));

    }

    /**
     * Process the form to create/edit an allocation with an activity.
     */
    @With(CheckActorExists.class)
    @Restrict({ @Group(IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION) })
    public Result processManageActorAllocatedActivity() {

        // bind the form
        Form<TimesheetActivityAllocatedActorFormData> boundForm = allocatedActivityFormTemplate.bindFromRequest();

        // get the org unit
        Long id = Long.valueOf(boundForm.data().get("id"));
        OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(id);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, TimesheetActivityAllocatedActor.class)) {
            return ok(views.html.core.orgunit.actor_allocated_activity_manage.render(orgUnit, boundForm));
        }

        TimesheetActivityAllocatedActorFormData allocatedActivityFormData = boundForm.get();

        TimesheetActivityAllocatedActor allocatedActivity = new TimesheetActivityAllocatedActor();
        allocatedActivityFormData.fill(allocatedActivity);
        allocatedActivity.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.org_unit.actor_allocated_activity.add.successful"));

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, TimesheetActivityAllocatedActor.class, allocatedActivity.id);

        return redirect(controllers.core.routes.OrgUnitController.allocationDetails(id, 0));

    }

    /**
     * Construct the side bar.
     * 
     * @param id
     *            the org unit id
     * @param currentType
     *            the current menu item type, useful to select the correct item
     * @param securityService
     *            the security service
     */
    public static SideBar getSideBar(Long id, MenuItemType currentType, ISecurityService securityService) {

        SideBar sideBar = new SideBar();

        sideBar.addMenuItem(new ClickableMenuItem("core.org_unit.sidebar.overview", controllers.core.routes.OrgUnitController.view(id, 0),
                "fa fa-search-plus", currentType.equals(MenuItemType.OVERVIEW)));

        if (securityService.dynamic(IMafConstants.ORG_UNIT_VIEW_DYNAMIC_PERMISSION, "")) {

            sideBar.addMenuItem(
                    new ClickableMenuItem("core.org_unit.sidebar.portfolio_entries", controllers.core.routes.OrgUnitController.listPortfolioEntries(id, 0),
                            "fa fa-sticky-note", currentType.equals(MenuItemType.INITIATIVES)));

            HeaderMenuItem allocationMenu = new HeaderMenuItem("core.org_unit.sidebar.allocation", "fa fa-book",
                    currentType.equals(MenuItemType.ALLOCATION));

            allocationMenu.addSubMenuItem(new ClickableMenuItem("core.org_unit.sidebar.allocation.overview",
                    controllers.core.routes.OrgUnitController.allocation(id), "fa fa-tachometer", false));

            allocationMenu.addSubMenuItem(new ClickableMenuItem("core.org_unit.sidebar.allocation.details",
                    controllers.core.routes.OrgUnitController.allocationDetails(id, 0), "fa fa-search-plus", false));

            sideBar.addMenuItem(allocationMenu);

        }

        return sideBar;

    }

    /**
     * Get the initiative's allocations table for the actors of an org unit.
     * 
     * @param orgUnitId
     *            the org unit id
     * @param filterConfig
     *            the filter config.
     */
    private static Pair<Table<PortfolioEntryResourcePlanAllocatedActorListView>, Pagination<PortfolioEntryResourcePlanAllocatedActor>> getActorsPEAllocTable(
            Long orgUnitId, FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> filterConfig) {

        ExpressionList<PortfolioEntryResourcePlanAllocatedActor> expressionList = filterConfig
                .updateWithSearchExpression(PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsExprByOrgUnitAndActive(orgUnitId, true));
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

        Table<PortfolioEntryResourcePlanAllocatedActorListView> table = PortfolioEntryResourcePlanAllocatedActorListView.templateTable
                .fillForFilterConfig(listView, columnsToHide);

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
     * Get the activity's allocations table for the actors of an org unit.
     * 
     * @param orgUnitId
     *            the org unit id
     * @param filterConfig
     *            the filter config.
     */
    private static Pair<Table<TimesheetActivityAllocatedActorListView>, Pagination<TimesheetActivityAllocatedActor>> getActorsActivityAllocTable(
            Long orgUnitId, FilterConfig<TimesheetActivityAllocatedActorListView> filterConfig) {

        ExpressionList<TimesheetActivityAllocatedActor> expressionList = filterConfig
                .updateWithSearchExpression(TimesheetDao.getTimesheetActivityAllocatedActorAsExprByOrgUnit(orgUnitId, true));
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
     * The menu item type.
     * 
     * @author Johann Kohler
     * 
     */
    public static enum MenuItemType {
        OVERVIEW, INITIATIVES, ALLOCATION;
    }

    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    private Configuration getConfiguration() {
        return configuration;
    }
}
