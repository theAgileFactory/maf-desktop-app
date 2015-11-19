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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import constants.IMafConstants;
import controllers.core.TimesheetController.OptionData;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.StakeholderDao;
import dao.timesheet.TimesheetDao;
import framework.security.ISecurityService;
import framework.utils.CustomAttributeFormAndDisplayHandler;
import framework.utils.DefaultSelectableValueHolderCollection;
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
import models.pmo.Actor;
import models.pmo.ActorCapacity;
import models.pmo.Competency;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import models.timesheet.TimesheetActivity;
import models.timesheet.TimesheetActivityAllocatedActor;
import models.timesheet.TimesheetReport;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckActorExists;
import utils.SortableCollection;
import utils.SortableCollection.DateSortableObject;
import utils.form.ActorCapacityFormData;
import utils.form.ActorCompetenciesFormData;
import utils.form.ActorDefaultCompetencyFormData;
import utils.form.ActorFormData;
import utils.form.TimesheetActivityAllocatedActorFormData;
import utils.form.TimesheetReportApprovalFormData;
import utils.gantt.SourceDataValue;
import utils.gantt.SourceItem;
import utils.gantt.SourceValue;
import utils.table.ActorListView;
import utils.table.CompetencyListView;
import utils.table.PortfolioEntryListView;
import utils.table.PortfolioEntryResourcePlanAllocatedActorListView;
import utils.table.PortfolioListView;
import utils.table.TimesheetActivityAllocatedActorListView;

/**
 * The controller which displays / allows to edit an actor.
 * 
 * 
 * @author Johann Kohler
 */
public class ActorController extends Controller {
    @Inject
    private ISecurityService securityService;

    public static Form<ActorFormData> formTemplate = Form.form(ActorFormData.class);

    private static Form<TimesheetReportApprovalFormData> timesheetReportApprovalFormTemplate = Form.form(TimesheetReportApprovalFormData.class);

    private static Form<ActorCompetenciesFormData> competenciesFormTemplate = Form.form(ActorCompetenciesFormData.class);

    private static Form<ActorDefaultCompetencyFormData> defaultCompetencyFormTemplate = Form.form(ActorDefaultCompetencyFormData.class);

    private static Form<ActorCapacityFormData> capacityFormTemplate = Form.form(ActorCapacityFormData.class);

    private static Form<TimesheetActivityAllocatedActorFormData> allocatedActivityFormTemplate = Form.form(TimesheetActivityAllocatedActorFormData.class);

    /**
     * Display the details of an actor.
     * 
     * @param id
     *            the actor id
     */
    @With(CheckActorExists.class)
    @SubjectPresent
    public Result view(Long id) {

        // get the actor
        Actor actor = ActorDao.getActorById(id);

        // construct the corresponding form data (for the custom attributes)
        ActorFormData actorFormData = new ActorFormData(actor);

        // tables of actor in the same org unit
        Table<ActorListView> actorFilledTable = null;
        if (actor.orgUnit != null) {
            List<Actor> actors = ActorDao.getActorActiveAsListByOrgUnit(actor.orgUnit.id);

            List<ActorListView> actorsListView = new ArrayList<ActorListView>();
            for (Actor a : actors) {
                actorsListView.add(new ActorListView(a));
            }

            Set<String> hideColumnsForEntry = new HashSet<String>();
            hideColumnsForEntry.add("orgUnit");
            hideColumnsForEntry.add("manager");

            actorFilledTable = ActorListView.templateTable.fill(actorsListView, hideColumnsForEntry);
        }

        // competencies
        List<CompetencyListView> competenciesListView = new ArrayList<CompetencyListView>();
        for (Competency competency : actor.competencies) {
            if (competency.isActive) {
                competenciesListView.add(new CompetencyListView(competency, actor));
            }
        }

        Set<String> hideColumnsForCompetency = new HashSet<String>();
        hideColumnsForCompetency.add("isActive");
        hideColumnsForCompetency.add("editActionLink");
        hideColumnsForCompetency.add("deleteActionLink");

        Table<CompetencyListView> competenciesFilledTable = CompetencyListView.templateTable.fill(competenciesListView, hideColumnsForCompetency);

        // check if there are active competencies
        boolean existCompetencies = ActorDao.getCompetencyActiveAsList().size() > 0 ? true : false;

        return ok(views.html.core.actor.actor_view.render(actor, actorFormData, actorFilledTable, competenciesFilledTable, existCompetencies));
    }

    /**
     * Form to create a new actor (available from Admin menu).
     */
    @Restrict({ @Group(IMafConstants.ACTOR_EDIT_ALL_PERMISSION) })
    public Result create() {

        // construct the form
        Form<ActorFormData> actorForm = formTemplate;

        // add the custom attributes default values
        CustomAttributeFormAndDisplayHandler.fillWithValues(actorForm, Actor.class, null);

        return ok(views.html.core.actor.actor_new.render(actorForm, ActorDao.getActorTypeActiveAsVH()));
    }

    /**
     * Form to edit an actor (available from the details of an actor).
     * 
     * @param id
     *            the actor id
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_EDIT_DYNAMIC_PERMISSION)
    public Result edit(Long id) {

        // get the actor
        Actor actor = ActorDao.getActorById(id);

        // construct the form
        Form<ActorFormData> actorForm = formTemplate.fill(new ActorFormData(actor));

        // add the custom attributes values
        CustomAttributeFormAndDisplayHandler.fillWithValues(actorForm, Actor.class, id);

        return ok(views.html.core.actor.actor_edit.render(actor, actorForm, ActorDao.getActorTypeActiveAsVH()));
    }

    /**
     * Process the save of an actor (create and edit cases).
     */
    @Dynamic(IMafConstants.ACTOR_EDIT_DYNAMIC_PERMISSION)
    public Result save() {

        // bind the form
        Form<ActorFormData> boundForm = formTemplate.bindFromRequest();

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, Actor.class)) {

            if (boundForm.data().get("id") != null) { // edit case
                // get the actor
                Long id = Long.valueOf(boundForm.data().get("id"));
                Actor actor = ActorDao.getActorById(id);
                return ok(views.html.core.actor.actor_edit.render(actor, boundForm, ActorDao.getActorTypeActiveAsVH()));
            } else { // new case
                return ok(views.html.core.actor.actor_new.render(boundForm, ActorDao.getActorTypeActiveAsVH()));
            }
        }

        ActorFormData actorFormData = boundForm.get();

        // check the uid is not already used by another actor
        if (actorFormData.uid != null && !actorFormData.uid.equals("")) {
            Actor testActor = ActorDao.getActorByUid(actorFormData.uid);
            if (testActor != null) { // edit case

                if (actorFormData.id != null) {
                    if (!testActor.id.equals(actorFormData.id)) {
                        boundForm.reject("uid", Msg.get("object.actor.uid.invalid"));
                        Actor actor = ActorDao.getActorById(actorFormData.id);
                        return ok(views.html.core.actor.actor_edit.render(actor, boundForm, ActorDao.getActorTypeActiveAsVH()));
                    }
                } else { // new case
                    boundForm.reject("uid", Msg.get("object.actor.uid.invalid"));
                    return ok(views.html.core.actor.actor_new.render(boundForm, ActorDao.getActorTypeActiveAsVH()));
                }
            }
        }

        Actor actor = null;

        if (actorFormData.id != null) { // edit case

            actor = ActorDao.getActorById(actorFormData.id);
            actorFormData.fill(actor);
            actor.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.actor.edit.successful"));

        } else { // new case

            actor = new Actor();
            actorFormData.fill(actor);
            actor.save();

            Utilities.sendSuccessFlashMessage(Msg.get("core.actor.new.successful"));

        }

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, Actor.class, actor.id);

        return redirect(controllers.core.routes.ActorController.view(actor.id));
    }

    /**
     * Delete an actor.
     * 
     * @param id
     *            the actor id
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_DELETE_DYNAMIC_PERMISSION)
    public Result delete(Long id) {

        // get the actor
        Actor actor = ActorDao.getActorById(id);

        // delete the actor
        actor.doDelete();

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get("core.actor.delete.successful"));

        return redirect(controllers.core.routes.SearchController.index());
    }

    /**
     * Form to select the competencies of the actor.
     * 
     * @param id
     *            the actor id
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_EDIT_DYNAMIC_PERMISSION)
    public Result editCompetencies(Long id) {

        // get the actor
        Actor actor = ActorDao.getActorById(id);

        // construct the form
        Form<ActorCompetenciesFormData> competenciesForm = competenciesFormTemplate.fill(new ActorCompetenciesFormData(actor));

        return ok(views.html.core.actor.competencies_edit.render(actor, ActorDao.getCompetencyActiveAsVH(), competenciesForm));

    }

    /**
     * Process the form to select the competencies of the actor.
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_EDIT_DYNAMIC_PERMISSION)
    public Result processEditCompetencies() {

        // bind the form
        Form<ActorCompetenciesFormData> boundForm = competenciesFormTemplate.bindFromRequest();

        // get the actor
        Long id = Long.valueOf(boundForm.data().get("id"));
        Actor actor = ActorDao.getActorById(id);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.actor.competencies_edit.render(actor, ActorDao.getCompetencyActiveAsVH(), boundForm));
        }

        ActorCompetenciesFormData competenciesFormData = boundForm.get();

        competenciesFormData.fill(actor);
        // actor.saveManyToManyAssociations("competencies");

        if (actor.competencies.size() == 0) {
            actor.defaultCompetency = null;
            actor.save();
        } else if (actor.competencies.size() == 1) {
            actor.defaultCompetency = actor.competencies.get(0);
            actor.save();
        } else {
            Form<ActorDefaultCompetencyFormData> defaultCompetencyForm = defaultCompetencyFormTemplate.fill(new ActorDefaultCompetencyFormData(actor));
            // force the default competency to an existing one
            actor.defaultCompetency = actor.competencies.get(0);
            actor.save();
            return ok(views.html.core.actor.default_competency_edit.render(actor, new DefaultSelectableValueHolderCollection<Long>(actor.competencies),
                    defaultCompetencyForm));
        }

        Utilities.sendSuccessFlashMessage(Msg.get("core.actor.competencies.edit.successful"));

        return redirect(controllers.core.routes.ActorController.view(actor.id));

    }

    /**
     * Process the form to edit the default competency of an actor.
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_EDIT_DYNAMIC_PERMISSION)
    public Result processEditDefaultCompetency() {

        // bind the form
        Form<ActorDefaultCompetencyFormData> boundForm = defaultCompetencyFormTemplate.bindFromRequest();

        // get the actor
        Long id = Long.valueOf(boundForm.data().get("id"));
        Actor actor = ActorDao.getActorById(id);

        ActorDefaultCompetencyFormData defaultCompetencyFormData = boundForm.get();

        defaultCompetencyFormData.fill(actor);
        actor.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.actor.competencies.edit.successful"));

        return redirect(controllers.core.routes.ActorController.view(actor.id));

    }

    /**
     * Display the active portfolio entries for which the actor is manager or
     * direct stakeholder.
     * 
     * @param id
     *            the actor id
     * @param page
     *            the current page
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_VIEW_DYNAMIC_PERMISSION)
    public Result listPortfolioEntries(Long id, Integer page) {

        // get the actor
        Actor actor = ActorDao.getActorById(id);

        Pagination<PortfolioEntry> pagination = PortfolioEntryDao.getPEActiveAsPaginationByManagerOrDirectStakeholder(id);
        pagination.setCurrentPage(page);

        List<PortfolioEntryListView> portfolioEntriesView = new ArrayList<PortfolioEntryListView>();
        for (PortfolioEntry portfolioEntry : pagination.getListOfObjects()) {
            portfolioEntriesView.add(new PortfolioEntryListView(portfolioEntry, StakeholderDao.getStakeholderAsListByActorAndPE(id, portfolioEntry.id)));
        }

        Table<PortfolioEntryListView> filledTable = PortfolioEntryListView.templateTable.fill(portfolioEntriesView,
                PortfolioEntryListView.getHideNonDefaultColumns(false, false));

        return ok(views.html.core.actor.actor_portfolio_entry_list.render(actor, filledTable, pagination));
    }

    /**
     * Display the active portfolios for which the actor is manager or
     * stakeholder.
     * 
     * @param id
     *            the actor id
     * @param page
     *            the current page
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_VIEW_DYNAMIC_PERMISSION)
    public Result listPortfolios(Long id, Integer page) {

        // get the actor
        Actor actor = ActorDao.getActorById(id);

        Pagination<Portfolio> pagination = PortfolioDao.getPortfolioActiveAsPaginationByStakeholderOrManager(id);
        pagination.setCurrentPage(page);

        List<PortfolioListView> portfoliosView = new ArrayList<PortfolioListView>();
        for (Portfolio portfolio : pagination.getListOfObjects()) {
            portfoliosView.add(new PortfolioListView(portfolio, StakeholderDao.getStakeholderAsListByActorAndPortfolio(id, portfolio.id)));
        }

        Table<PortfolioListView> filledTable = PortfolioListView.templateTable.fill(portfoliosView);

        return ok(views.html.core.actor.actor_portfolio_list.render(actor, filledTable, pagination));
    }

    /**
     * Display the gantt of allocations of the actor.
     * 
     * @param id
     *            the actor id
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_VIEW_DYNAMIC_PERMISSION)
    public Result allocation(Long id) {

        // get the actor
        Actor actor = ActorDao.getActorById(id);

        // prepare the data (to order them)
        SortableCollection<DateSortableObject> sortableCollection = new SortableCollection<>();
        for (PortfolioEntryResourcePlanAllocatedActor allocatedActor : PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsListByActorAndActive(id,
                true)) {
            if (allocatedActor.endDate != null) {
                sortableCollection.addObject(new DateSortableObject(allocatedActor.endDate, allocatedActor));
            }
        }
        for (TimesheetActivityAllocatedActor allocatedActivity : TimesheetDao.getTimesheetActivityAllocatedActorAsListByActor(id, true)) {
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

                String packageName = allocatedActor.portfolioEntryPlanningPackage != null ? allocatedActor.portfolioEntryPlanningPackage.getName() : "";

                SourceItem item = new SourceItem(portfolioEntry.getName(), packageName);

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
                        views.html.framework_views.parts.formats.display_number.render(allocatedActor.days, null, false).body(), cssClass, dataValue));

                items.add(item);

            }

            if (dateSortableObject.getObject() instanceof TimesheetActivityAllocatedActor) {

                TimesheetActivityAllocatedActor allocatedActivity = (TimesheetActivityAllocatedActor) dateSortableObject.getObject();

                // get the from date
                Date from = allocatedActivity.startDate;

                // get the to date
                Date to = allocatedActivity.endDate;

                SourceItem item = new SourceItem(allocatedActivity.timesheetActivity.getName(), "");

                String cssClass = null;

                if (from != null) {

                    to = JqueryGantt.cleanToDate(from, to);
                    cssClass = "";

                } else {

                    from = to;
                    cssClass = "diamond diamond-";

                }

                cssClass += "info";

                SourceDataValue dataValue = new SourceDataValue(controllers.core.routes.ActorController.allocationDetails(actor.id, 0, 0, false).url(), null,
                        null, null, null);

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

        return ok(views.html.core.actor.actor_allocation.render(actor, ganttSource));
    }

    /**
     * Display the allocations (portfolio entry and activity) of the actor.
     * 
     * @param id
     *            the actor id
     * @param pagePortfolioEntry
     *            the current page for the portfolio entry allocations
     * @param pageActivity
     *            the current page for the activity allocations
     * @param viewAllActivities
     *            set to true if all activities (including the past ones) must
     *            be displayed
     * 
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_VIEW_DYNAMIC_PERMISSION)
    public Result allocationDetails(Long id, Integer pagePortfolioEntry, Integer pageActivity, Boolean viewAllActivities) {

        // get the actor
        Actor actor = ActorDao.getActorById(id);

        // construct the "portfolio entry" allocations tables

        Pagination<PortfolioEntryResourcePlanAllocatedActor> portfolioEntryPagination = PortfolioEntryResourcePlanDAO
                .getPEPlanAllocatedActorAsPaginationByActorAndActive(id, true);
        portfolioEntryPagination.setCurrentPage(pagePortfolioEntry);
        portfolioEntryPagination.setPageQueryName("pagePortfolioEntry");

        List<PortfolioEntryResourcePlanAllocatedActorListView> allocationListView = new ArrayList<PortfolioEntryResourcePlanAllocatedActorListView>();
        for (PortfolioEntryResourcePlanAllocatedActor allocatedActor : portfolioEntryPagination.getListOfObjects()) {
            allocationListView.add(new PortfolioEntryResourcePlanAllocatedActorListView(allocatedActor));
        }

        Set<String> columnsToHide = new HashSet<String>();
        columnsToHide.add("editActionLink");
        columnsToHide.add("removeActionLink");
        columnsToHide.add("followPackageDates");
        columnsToHide.add("actor");

        Table<PortfolioEntryResourcePlanAllocatedActorListView> portfolioEntryTable = PortfolioEntryResourcePlanAllocatedActorListView.templateTable
                .fill(allocationListView, columnsToHide);

        portfolioEntryTable.setLineAction(new IColumnFormatter<PortfolioEntryResourcePlanAllocatedActorListView>() {
            @Override
            public String apply(PortfolioEntryResourcePlanAllocatedActorListView portfolioEntryResourcePlanAllocatedActorListView, Object value) {
                return controllers.core.routes.PortfolioEntryPlanningController.resources(portfolioEntryResourcePlanAllocatedActorListView.portfolioEntryId)
                        .url();
            }
        });

        // construct the "activity" allocations tables

        Pagination<TimesheetActivityAllocatedActor> activityPagination = TimesheetDao.getTimesheetActivityAllocatedActorAsPaginationByActor(id,
                !viewAllActivities);
        activityPagination.setCurrentPage(pageActivity);
        activityPagination.setPageQueryName("pageActivity");

        List<TimesheetActivityAllocatedActorListView> activityAllocationListView = new ArrayList<TimesheetActivityAllocatedActorListView>();
        for (TimesheetActivityAllocatedActor allocatedActor : activityPagination.getListOfObjects()) {
            activityAllocationListView.add(new TimesheetActivityAllocatedActorListView(allocatedActor));
        }

        Set<String> columnsToHideForActivity = new HashSet<String>();
        columnsToHideForActivity.add("actor");

        Table<TimesheetActivityAllocatedActorListView> activityTable = TimesheetActivityAllocatedActorListView.templateTable.fill(activityAllocationListView,
                columnsToHideForActivity);

        return ok(views.html.core.actor.actor_allocation_details.render(actor, portfolioEntryTable, portfolioEntryPagination, activityTable,
                activityPagination, viewAllActivities));
    }

    /**
     * Form to create/edit an allocation with an activity.
     * 
     * @param id
     *            the actor id
     * @param allocatedActivityId
     *            the allocated activity id (0 for create)
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_EDIT_DYNAMIC_PERMISSION)
    public Result manageAllocatedActivity(Long id, Long allocatedActivityId) {

        // get the actor
        Actor actor = ActorDao.getActorById(id);

        // initiate the form with the template
        Form<TimesheetActivityAllocatedActorFormData> allocatedActivityForm = allocatedActivityFormTemplate;

        // edit case: inject values
        if (!allocatedActivityId.equals(Long.valueOf(0))) {

            TimesheetActivityAllocatedActor allocatedActivity = TimesheetDao.getTimesheetActivityAllocatedActorById(allocatedActivityId);

            // security: the actor must be related to the object
            if (!allocatedActivity.actor.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            allocatedActivityForm = allocatedActivityFormTemplate.fill(new TimesheetActivityAllocatedActorFormData(id, allocatedActivity));

            // add the custom attributes values
            CustomAttributeFormAndDisplayHandler.fillWithValues(allocatedActivityForm, TimesheetActivityAllocatedActor.class, allocatedActivityId);
        } else {
            // add the custom attributes default values
            CustomAttributeFormAndDisplayHandler.fillWithValues(allocatedActivityForm, TimesheetActivityAllocatedActor.class, null);
        }

        return ok(views.html.core.actor.allocated_activity_manage.render(actor, allocatedActivityForm));

    }

    /**
     * Get the activities of a type.
     */
    @SubjectPresent
    public Result getActivities() {

        JsonNode json = request().body().asJson();
        Long activityTypeId = json.findPath("activityTypeId").asLong();

        List<TimesheetActivity> activities = TimesheetDao.getTimesheetActivityAsListByActivityType(activityTypeId);
        List<OptionData> options = new ArrayList<OptionData>();

        for (TimesheetActivity activity : activities) {
            options.add(new OptionData(activity.id, activity.getName()));
        }

        ObjectMapper mapper = new ObjectMapper();

        return ok((JsonNode) mapper.valueToTree(options));
    }

    /**
     * Process the form to create/edit an allocation with an activity.
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_EDIT_DYNAMIC_PERMISSION)
    public Result processManageAllocatedActivity() {

        // bind the form
        Form<TimesheetActivityAllocatedActorFormData> boundForm = allocatedActivityFormTemplate.bindFromRequest();

        // get the actor
        Long id = Long.valueOf(boundForm.data().get("id"));
        Actor actor = ActorDao.getActorById(id);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, TimesheetActivityAllocatedActor.class)) {
            return ok(views.html.core.actor.allocated_activity_manage.render(actor, boundForm));
        }

        TimesheetActivityAllocatedActorFormData allocatedActivityFormData = boundForm.get();

        TimesheetActivityAllocatedActor allocatedActivity = null;

        if (allocatedActivityFormData.allocatedActivityId == null) { // create
                                                                     // case

            allocatedActivity = new TimesheetActivityAllocatedActor();
            allocatedActivityFormData.fill(allocatedActivity);
            allocatedActivity.save();

            Utilities.sendSuccessFlashMessage(Msg.get("core.actor.allocated_activity.add.successful"));

        } else { // edit case

            allocatedActivity = TimesheetDao.getTimesheetActivityAllocatedActorById(allocatedActivityFormData.allocatedActivityId);

            // security: the actor must be related to the object
            if (!allocatedActivity.actor.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            allocatedActivityFormData.fill(allocatedActivity);
            allocatedActivity.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.actor.allocated_activity.edit.successful"));

        }

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, TimesheetActivityAllocatedActor.class, allocatedActivity.id);

        return redirect(controllers.core.routes.ActorController.allocationDetails(id, 0, 0, false));

    }

    /**
     * Delete an allocated activity.
     * 
     * @param id
     *            the actor id
     * @param allocatedActivityId
     *            the allocated activity id
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_EDIT_DYNAMIC_PERMISSION)
    public Result deleteAllocatedActivity(Long id, Long allocatedActivityId) {

        // get the allocated activity
        TimesheetActivityAllocatedActor allocatedActivity = TimesheetDao.getTimesheetActivityAllocatedActorById(allocatedActivityId);

        // security: the actor must be related to the object
        if (!allocatedActivity.actor.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // set the delete flag to true
        allocatedActivity.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.actor.allocated_activity.delete.successful"));

        return redirect(controllers.core.routes.ActorController.allocationDetails(id, 0, 0, false));
    }

    /**
     * Display the capacity of the actor with management capability if enough
     * perm.
     * 
     * @param id
     *            the actor id
     * @param year
     *            the year (0 for the current)
     * 
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_VIEW_DYNAMIC_PERMISSION)
    public Result capacity(Long id, Integer year) {

        if (year.equals(0)) {
            year = Calendar.getInstance().get(Calendar.YEAR);
        }

        // get the actor
        Actor actor = ActorDao.getActorById(id);

        Form<ActorCapacityFormData> capacityForm = capacityFormTemplate.fill(new ActorCapacityFormData(actor, year));

        // can edit
        boolean canEdit = getSecurityService().dynamic(IMafConstants.ACTOR_EDIT_DYNAMIC_PERMISSION, "");

        return ok(views.html.core.actor.actor_capacity.render(actor, year, capacityForm, canEdit));
    }

    /**
     * Save the capacity.
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_EDIT_DYNAMIC_PERMISSION)
    public Result saveCapacity() {

        // bind the form
        Form<ActorCapacityFormData> boundForm = capacityFormTemplate.bindFromRequest();

        // get the actor
        Long id = Long.valueOf(boundForm.data().get("id"));
        Actor actor = ActorDao.getActorById(id);

        // get the year
        Integer year = Integer.valueOf(boundForm.data().get("year"));

        if (boundForm.hasErrors()) {
            return ok(views.html.core.actor.actor_capacity.render(actor, year, boundForm, true));
        }

        ActorCapacityFormData capacityFormData = boundForm.get();

        for (ActorCapacity capacity : capacityFormData.getFilledCapacities()) {
            capacity.save();
        }

        Utilities.sendSuccessFlashMessage(Msg.get("core.actor.capacity.save.successful"));

        return redirect(controllers.core.routes.ActorController.capacity(capacityFormData.id, capacityFormData.year));
    }

    /**
     * Display the details of a timesheet.
     * 
     * @param id
     *            the actor id
     * @param stringDate
     *            a date in the format yyyy-MM-dd: the system gets the weekly
     *            report including this date, if empty it uses the current date.
     */
    @With(CheckActorExists.class)
    @Dynamic(IMafConstants.ACTOR_VIEW_DYNAMIC_PERMISSION)
    public Result viewWeeklyTimesheet(Long id, String stringDate) {

        // get the actor
        Actor actor = ActorDao.getActorById(id);

        // get the report
        TimesheetReport report = TimesheetController.getTimesheetReport(stringDate, actor);

        return ok(views.html.core.actor.actor_view_weekly_timesheet.render(actor, report, timesheetReportApprovalFormTemplate));
    }

    /**
     * Construct the side bar.
     * 
     * @param id
     *            the actor id
     * @param currentType
     *            the current menu item type, useful to select the correct item
     * @param securityService
     *            the security service
     */
    public static SideBar getSideBar(Long id, MenuItemType currentType, ISecurityService securityService) {

        SideBar sideBar = new SideBar();

        sideBar.addMenuItem(new ClickableMenuItem("core.actor.sidebar.overview", controllers.core.routes.ActorController.view(id),
                "glyphicons glyphicons-zoom-in", currentType.equals(MenuItemType.OVERVIEW)));

        if (securityService.dynamic(IMafConstants.ACTOR_VIEW_DYNAMIC_PERMISSION, "")) {

            sideBar.addMenuItem(
                    new ClickableMenuItem("core.actor.sidebar.portfolio_entries", controllers.core.routes.ActorController.listPortfolioEntries(id, 0),
                            "glyphicons glyphicons-wallet", currentType.equals(MenuItemType.INITIATIVES)));

            sideBar.addMenuItem(new ClickableMenuItem("core.actor.sidebar.portfolios", controllers.core.routes.ActorController.listPortfolios(id, 0),
                    "glyphicons glyphicons-sort", currentType.equals(MenuItemType.PORTFOLIOS)));

            HeaderMenuItem allocationMenu = new HeaderMenuItem("core.actor.sidebar.allocation", "glyphicons glyphicons-address-book",
                    currentType.equals(MenuItemType.ALLOCATION));

            allocationMenu.addSubMenuItem(new ClickableMenuItem("core.actor.sidebar.allocation.overview",
                    controllers.core.routes.ActorController.allocation(id), "glyphicons glyphicons-radar", false));

            allocationMenu.addSubMenuItem(new ClickableMenuItem("core.actor.sidebar.allocation.details",
                    controllers.core.routes.ActorController.allocationDetails(id, 0, 0, false), "glyphicons glyphicons-zoom-in", false));

            allocationMenu.addSubMenuItem(new ClickableMenuItem("core.actor.sidebar.allocation.capacity",
                    controllers.core.routes.ActorController.capacity(id, 0), "glyphicons glyphicons-equalizer", false));

            sideBar.addMenuItem(allocationMenu);

            sideBar.addMenuItem(new ClickableMenuItem("core.actor.sidebar.timesheet", controllers.core.routes.ActorController.viewWeeklyTimesheet(id, ""),
                    "glyphicons glyphicons-clock", currentType.equals(MenuItemType.TIMESHEET)));

        }

        return sideBar;

    }

    /**
     * The menu item type.
     * 
     * @author Johann Kohler
     * 
     */
    public static enum MenuItemType {
        OVERVIEW, INITIATIVES, PORTFOLIOS, TIMESHEET, ALLOCATION;
    }

    private ISecurityService getSecurityService() {
        return securityService;
    }

}
