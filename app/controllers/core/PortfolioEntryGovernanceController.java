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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.avaje.ebean.Ebean;

import be.objectify.deadbolt.java.actions.Dynamic;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.governance.LifeCycleMilestoneDao;
import dao.governance.LifeCyclePlanningDao;
import dao.governance.LifeCycleProcessDao;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryDao;
import framework.security.ISecurityService;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.utils.FileAttachmentHelper;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.finance.PortfolioEntryBudgetLine;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.framework_models.common.Attachment;
import models.governance.*;
import models.pmo.Actor;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioEntryExists;
import services.budgettracking.IBudgetTrackingService;
import services.tableprovider.ITableProvider;
import utils.SortableCollection;
import utils.SortableCollection.DateSortableObject;
import utils.form.PlannedDateFormData;
import utils.form.PlannedDatesFormData;
import utils.form.RequestMilestoneFormData;
import utils.table.GovernanceListView;
import utils.table.MilestoneApproverListView;
import utils.table.PortfolioEntryBudgetLineListView;
import utils.table.PortfolioEntryResourcePlanAllocatedResourceListView;

/**
 * The controller which allows to manage the governance of a portfolio entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryGovernanceController extends Controller {
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private IAttachmentManagerPlugin attachmentManagerPlugin;
    @Inject
    private ISecurityService securityService;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;
    @Inject
    private IBudgetTrackingService budgetTrackingService;
    @Inject
    private INotificationManagerPlugin notificationManagerService;
    @Inject
    private ITableProvider tableProvider;

    private static Logger.ALogger log = Logger.of(PortfolioEntryGovernanceController.class);

    private static Form<PlannedDatesFormData> plannedDatesFormTemplate = Form.form(PlannedDatesFormData.class);
    private static Form<ChangeLifeCycleProcessFormData> changeProcessFormTemplate = Form.form(ChangeLifeCycleProcessFormData.class);
    private static Form<RequestMilestoneFormData> requestMilestoneFormTemplate = Form.form(RequestMilestoneFormData.class);

    /**
     * Display the list of milestones with their instances for a portfolio
     * entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result index(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the active life cycle instance (the instance of the process)
        LifeCycleInstance activeLifeCycleProcessInstance = portfolioEntry.activeLifeCycleInstance;

        // construct the table
        List<GovernanceListView> governanceListView = new ArrayList<GovernanceListView>();
        List<PlannedLifeCycleMilestoneInstance> lastPlannedMilestoneInstances = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceLastAsListByPE(id);
        for (PlannedLifeCycleMilestoneInstance lastPlannedMilestoneInstance : lastPlannedMilestoneInstances) {
            governanceListView.add(new GovernanceListView(lastPlannedMilestoneInstance));
        }

        Set<String> hideColumnsForGovernance = new HashSet<String>();
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumnsForGovernance.add("actionLink");
        }

        Table<GovernanceListView> filledTable = this.getTableProvider().get().governance.templateTable.fill(governanceListView, hideColumnsForGovernance);

        // define if the initiative has a governance planning
        boolean hasPlanning = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceLastAsListByPE(id).size() > 0;

        return ok(views.html.core.portfolioentrygovernance.index.render(portfolioEntry, activeLifeCycleProcessInstance, filledTable, hasPlanning));
    }

    /**
     * Display the details of a milestone (including the instances) for a
     * portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     * @param milestoneId
     *            the milestone id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result viewMilestone(Long id, Long milestoneId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the milestone
        LifeCycleMilestone lifeCycleMilestone = LifeCycleMilestoneDao.getLCMilestoneById(milestoneId);

        // get the milestone instances
        List<LifeCycleMilestoneInstance> lifeCycleMilestoneInstances = LifeCycleMilestoneDao.getLCMilestoneInstanceAsListByPEAndLCMilestone(id, milestoneId,
                "DESC");

        // initiate the tables
        List<Table<MilestoneApproverListView>> approversTables = new ArrayList<Table<MilestoneApproverListView>>();
        List<Table<PortfolioEntryBudgetLineListView>> budgetLinesTables = new ArrayList<Table<PortfolioEntryBudgetLineListView>>();
        List<Table<PortfolioEntryResourcePlanAllocatedResourceListView>> resourcesTables;
        resourcesTables = new ArrayList<Table<PortfolioEntryResourcePlanAllocatedResourceListView>>();
        List<Attachment> attachments = new ArrayList<Attachment>();

        // columns to hide
        Set<String> columnsToHideForBudgetLine = new HashSet<>();
        columnsToHideForBudgetLine.add("refId");
        columnsToHideForBudgetLine.add("portfolioEntryName");
        columnsToHideForBudgetLine.add("budgetBucket");
        columnsToHideForBudgetLine.add("editActionLink");
        columnsToHideForBudgetLine.add("removeActionLink");

        Set<String> columnsToHideForResource = new HashSet<>();
        columnsToHideForResource.add("portfolioEntryName");
        columnsToHideForResource.add("planningPackage");
        columnsToHideForResource.add("currency");
        columnsToHideForResource.add("dailyRate");
        columnsToHideForResource.add("forecastDays");
        columnsToHideForResource.add("forecastDailyRate");
        columnsToHideForResource.add("reallocate");
        columnsToHideForResource.add("editActionLink");
        columnsToHideForResource.add("removeActionLink");

        for (LifeCycleMilestoneInstance lifeCycleMilestoneInstance : lifeCycleMilestoneInstances) {

            /** if exists, get the description document */

            List<Attachment> attachment = FileAttachmentHelper.getFileAttachmentsForDisplay(LifeCycleMilestoneInstance.class, lifeCycleMilestoneInstance.id,
                    getAttachmentManagerPlugin(), getUserSessionManagerPlugin());
            if (attachment != null && attachment.size() > 0) {
                attachments.add(attachment.get(0));
            } else {
                attachments.add(null);
            }

            /** construct the approvers table */

            // initiate the list view
            List<MilestoneApproverListView> milestoneApproverListView = new ArrayList<MilestoneApproverListView>();

            // add the approvers with vote if exists
            for (LifeCycleMilestoneInstanceApprover lifeCycleMilestoneInstanceApprover : lifeCycleMilestoneInstance.lifeCycleMilestoneInstanceApprovers) {
                milestoneApproverListView.add(new MilestoneApproverListView(lifeCycleMilestoneInstanceApprover));
            }

            // add the table
            approversTables.add(this.getTableProvider().get().milestoneApprover.templateTable.fill(milestoneApproverListView));

            /** construct the budget lines table */

            if (lifeCycleMilestoneInstance.isPassed) {

                // initiate the list view
                List<PortfolioEntryBudgetLineListView> portfolioEntryBudgetLineListView = new ArrayList<PortfolioEntryBudgetLineListView>();

                // add the lines
                for (PortfolioEntryBudgetLine budgetLine : lifeCycleMilestoneInstance.portfolioEntryBudget.portfolioEntryBudgetLines) {
                    portfolioEntryBudgetLineListView.add(new PortfolioEntryBudgetLineListView(budgetLine));
                }

                // add the table
                budgetLinesTables.add(this.getTableProvider().get().portfolioEntryBudgetLine.templateTable.fill(portfolioEntryBudgetLineListView,
                        columnsToHideForBudgetLine));

            } else {
                budgetLinesTables.add(null);
            }

            /** construct the resources table */

            if (lifeCycleMilestoneInstance.isPassed) {

                // initiate the list view
                List<PortfolioEntryResourcePlanAllocatedResourceListView> allocatedResourceListView;
                allocatedResourceListView = new ArrayList<PortfolioEntryResourcePlanAllocatedResourceListView>();

                // add the lines
                SortableCollection<DateSortableObject> sortableCollection = new SortableCollection<>();
                List<PortfolioEntryResourcePlanAllocatedActor> allocatedActors;
                allocatedActors = lifeCycleMilestoneInstance.portfolioEntryResourcePlan.portfolioEntryResourcePlanAllocatedActors;
                for (PortfolioEntryResourcePlanAllocatedActor resource : allocatedActors) {
                    sortableCollection.addObject(new DateSortableObject(resource.endDate, resource));
                }
                List<PortfolioEntryResourcePlanAllocatedCompetency> allocatedCompetencies;
                allocatedCompetencies = lifeCycleMilestoneInstance.portfolioEntryResourcePlan.portfolioEntryResourcePlanAllocatedCompetencies;
                for (PortfolioEntryResourcePlanAllocatedCompetency resource : allocatedCompetencies) {
                    sortableCollection.addObject(new DateSortableObject(resource.endDate, resource));
                }
                List<PortfolioEntryResourcePlanAllocatedOrgUnit> allocatedOrgUnits;
                allocatedOrgUnits = lifeCycleMilestoneInstance.portfolioEntryResourcePlan.portfolioEntryResourcePlanAllocatedOrgUnits;
                for (PortfolioEntryResourcePlanAllocatedOrgUnit resource : allocatedOrgUnits) {
                    sortableCollection.addObject(new DateSortableObject(resource.endDate, resource));
                }
                for (DateSortableObject dateSortableObject : sortableCollection.getSorted()) {
                    if (dateSortableObject.getObject() instanceof PortfolioEntryResourcePlanAllocatedActor) {
                        PortfolioEntryResourcePlanAllocatedActor allocatedActor = (PortfolioEntryResourcePlanAllocatedActor) dateSortableObject.getObject();
                        allocatedResourceListView.add(new PortfolioEntryResourcePlanAllocatedResourceListView(allocatedActor));
                    } else if (dateSortableObject.getObject() instanceof PortfolioEntryResourcePlanAllocatedOrgUnit) {
                        PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit = (PortfolioEntryResourcePlanAllocatedOrgUnit) dateSortableObject
                                .getObject();
                        allocatedResourceListView.add(new PortfolioEntryResourcePlanAllocatedResourceListView(allocatedOrgUnit));
                    } else if (dateSortableObject.getObject() instanceof PortfolioEntryResourcePlanAllocatedCompetency) {
                        PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency = (PortfolioEntryResourcePlanAllocatedCompetency) dateSortableObject
                                .getObject();
                        allocatedResourceListView.add(new PortfolioEntryResourcePlanAllocatedResourceListView(allocatedCompetency));
                    }
                }

                // add the table
                resourcesTables.add(this.getTableProvider().get().portfolioEntryResourcePlanAllocatedResource.templateTable.fill(allocatedResourceListView,
                        columnsToHideForResource));

            } else {
                resourcesTables.add(null);
            }

        }

        return ok(views.html.core.portfolioentrygovernance.milestone_view.render(portfolioEntry, lifeCycleMilestone, lifeCycleMilestoneInstances,
                approversTables, budgetLinesTables, resourcesTables, attachments));
    }

    /**
     * Delete a milestone instance
     *
     * @param id the portfolio entry id
     * @param milestoneId the milestone id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result deleteMilestone(Long id, Long milestoneId) {
        LifeCycleMilestoneDao.doDelete(milestoneId, getBudgetTrackingService());
        return redirect(controllers.core.routes.PortfolioEntryGovernanceController.index(id));
    }

    /**
     * Form to request a milestone approval for a milestone.
     * 
     * @param id
     *            the portfolio entry id
     * @param milestoneId
     *            the milestone id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result requestMilestone(Long id, Long milestoneId) {

        // if there is already a pending milestone instance, then this is not
        // possible to do a new request
        List<LifeCycleMilestoneInstance> milestoneInstances = LifeCycleMilestoneDao.getLCMilestoneInstanceAsListByPEAndLCMilestone(id, milestoneId);
        if (milestoneInstances != null) {
            for (LifeCycleMilestoneInstance milestone : milestoneInstances) {
                if (!milestone.isPassed) {
                    return forbidden(views.html.error.access_forbidden.render(""));
                }
            }
        }

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the milestone
        LifeCycleMilestone lifeCycleMilestone = LifeCycleMilestoneDao.getLCMilestoneById(milestoneId);

        // get the milestone instances status
        List<String> status = LifeCycleMilestoneDao.getLCMilestoneAsStatusByPEAndLCMilestone(id, lifeCycleMilestone.id);

        // get the last planned date
        LifeCycleInstancePlanning planning = LifeCyclePlanningDao.getLCInstancePlanningAsLastByPE(id);
        PlannedLifeCycleMilestoneInstance plannedDate = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceByLCInstancePlanningAndLCMilestone(planning.id,
                milestoneId);

        Form<RequestMilestoneFormData> form = requestMilestoneFormTemplate.fill(new RequestMilestoneFormData(id, milestoneId));

        return ok(views.html.core.portfolioentrygovernance.milestone_request.render(portfolioEntry, lifeCycleMilestone, form, status, plannedDate));
    }

    /**
     * Process the save of a milestone approval request for a milestone.
     * 
     * if the milestone must be reviewed, we create a process transition request
     * and we store the details as a structured document
     * 
     * if the milestone must not be reviewed, we directly create a milestone
     * instance and if it has approvers then we notify them else we set it as
     * passed
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processRequestMilestone() {

        Form<RequestMilestoneFormData> boundForm = requestMilestoneFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the milestone
        Long milestoneId = Long.valueOf(boundForm.data().get("milestoneId"));
        LifeCycleMilestone lifeCycleMilestone = LifeCycleMilestoneDao.getLCMilestoneById(milestoneId);

        if (boundForm.hasErrors()) {

            // get the milestone instances status
            List<String> status = LifeCycleMilestoneDao.getLCMilestoneAsStatusByPEAndLCMilestone(id, lifeCycleMilestone.id);

            // get the last planned date
            LifeCycleInstancePlanning planning = LifeCyclePlanningDao.getLCInstancePlanningAsLastByPE(id);
            PlannedLifeCycleMilestoneInstance plannedDate = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceByLCInstancePlanningAndLCMilestone(planning.id,
                    milestoneId);

            return badRequest(
                    views.html.core.portfolioentrygovernance.milestone_request.render(portfolioEntry, lifeCycleMilestone, boundForm, status, plannedDate));
        }

        // get the form values
        RequestMilestoneFormData requestMilestoneFormData = boundForm.get();

        String successKey = "";

        // if the milestone must be reviewed, we create a process transition
        // request and we store the details as a structured document attachment
        if (lifeCycleMilestone.isReviewRequired) {

            // get the current actor
            Actor actor = null;
            try {
                String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
                actor = ActorDao.getActorByUid(uid);
            } catch (Exception e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }

            if (actor == null) {
                return redirect(controllers.dashboard.routes.DashboardController.index(0, false));
            }

            // create the process transition request
            ProcessTransitionRequest processTransitionRequest = new ProcessTransitionRequest();
            requestMilestoneFormData.create(processTransitionRequest, actor);
            processTransitionRequest.save();

            LifeCycleMilestoneReviewRequest reviewRequest = null;
            try {
                reviewRequest = new LifeCycleMilestoneReviewRequest(portfolioEntry, lifeCycleMilestone, processTransitionRequest, Utilities.getDateFormat(null).parse(requestMilestoneFormData.passedDate));
            } catch (ParseException e) {
                return badRequest();
            }
            reviewRequest.save();

            // store the request details
            try {
                getAttachmentManagerPlugin().addStructuredDocumentAttachment(requestMilestoneFormData, "application/xml", "Milestone request",
                        ProcessTransitionRequest.class, processTransitionRequest.id);
            } catch (Exception e) {
                processTransitionRequest.doDelete();
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }

            // notification
            String url = controllers.core.routes.ProcessTransitionRequestController
                    .processMilestoneRequest(requestMilestoneFormData.id, processTransitionRequest.id).url();
            for (Portfolio portfolio : portfolioEntry.portfolios) {
                ActorDao.sendNotification(this.getNotificationManagerService(), this.getI18nMessagesPlugin(), portfolio.manager,
                        NotificationCategory.getByCode(Code.REQUEST_REVIEW), url,
                        "core.portfolio_entry_governance.milestone.request.approval.notification.title",
                        "core.portfolio_entry_governance.milestone.request.approval.notification.message", portfolioEntry.getName());
            }

            // success message
            successKey = "core.portfolio_entry_governance.milestone.request.approval.successful";

            // if exists, add the the description document file
            if (FileAttachmentHelper.hasFileField("descriptionDocument")) {
                try {
                    FileAttachmentHelper.saveAsAttachement("descriptionDocument", ProcessTransitionRequest.class, processTransitionRequest.id,
                            getAttachmentManagerPlugin());
                } catch (Exception e) {
                    Utilities.sendErrorFlashMessage(Msg.get("object.process_transition_request.description_document.error"));
                }
            }

        } else { // if the milestone must not be reviewed, we directly create a
                 // milestone instance

            LifeCycleMilestoneInstance lifeCycleMilestoneInstance;

            Ebean.beginTransaction();

            try {

                // create LifeCycleMilestoneInstance
                lifeCycleMilestoneInstance = new LifeCycleMilestoneInstance();
                requestMilestoneFormData.create(lifeCycleMilestoneInstance, FileAttachmentHelper.hasFileField("descriptionDocument"));
                lifeCycleMilestoneInstance.save();

                // if the milestone has approvers then we notify them
                if (lifeCycleMilestone.approvers.size() > 0) {

                    // add the default approvers
                    for (Actor approver : lifeCycleMilestone.approvers) {
                        LifeCycleMilestoneInstanceApprover instanceApprover = new LifeCycleMilestoneInstanceApprover(approver, lifeCycleMilestoneInstance);
                        instanceApprover.save();
                    }

                    // notification (approvers)
                    String approversUrl = controllers.core.routes.MilestoneApprovalController.process(lifeCycleMilestoneInstance.id).url();
                    ActorDao.sendNotification(this.getNotificationManagerService(), this.getI18nMessagesPlugin(), lifeCycleMilestone.approvers,
                            NotificationCategory.getByCode(Code.APPROVAL), approversUrl,
                            "core.portfolio_entry_governance.milestone.request.voterequired.notification.title",
                            "core.portfolio_entry_governance.milestone.request.voterequired.notification.message", portfolioEntry.getName());

                    // success message
                    successKey = "core.portfolio_entry_governance.milestone.request.instance.successful";

                } else { // if the milestone hasn't approvers then we set the
                         // milestone instance as passed

                    lifeCycleMilestoneInstance = LifeCycleMilestoneDao.doPassed(lifeCycleMilestoneInstance.id, this.getBudgetTrackingService());

                    // notification
                    ActorDao.sendNotification(this.getNotificationManagerService(), this.getI18nMessagesPlugin(), portfolioEntry.manager,
                            NotificationCategory.getByCode(Code.APPROVAL),
                            controllers.core.routes.PortfolioEntryGovernanceController.index(portfolioEntry.id).url(),
                            "core.portfolio_entry_governance.milestone.request.approved.notification.title",
                            "core.portfolio_entry_governance.milestone.request.approved.notification.message", portfolioEntry.getName());

                    // success message
                    successKey = "core.portfolio_entry_governance.milestone.request.approved.successful";

                }

                Ebean.commitTransaction();

            } catch (Exception e) {
                Ebean.rollbackTransaction();
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }

            // if exists, add the the description document file
            if (FileAttachmentHelper.hasFileField("descriptionDocument")) {
                try {
                    FileAttachmentHelper.saveAsAttachement("descriptionDocument", LifeCycleMilestoneInstance.class, lifeCycleMilestoneInstance.id,
                            getAttachmentManagerPlugin());
                } catch (Exception e) {
                    Utilities.sendErrorFlashMessage(Msg.get("object.process_transition_request.description_document.error"));
                }
            }
        }

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get(successKey));

        return redirect(controllers.core.routes.PortfolioEntryGovernanceController.index(requestMilestoneFormData.id));
    }

    /**
     * Form to edit the current planning of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result editPlanning(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the last planning
        LifeCycleInstancePlanning lastPlanning = LifeCyclePlanningDao.getLCInstancePlanningAsLastByPE(id);

        // get the last planned dates
        List<PlannedLifeCycleMilestoneInstance> plannedLifeCycleMilestoneInstances = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceLastAsListByPE(id);

        // construct the form
        Form<PlannedDatesFormData> plannedDatesForm = plannedDatesFormTemplate.fill(new PlannedDatesFormData(plannedLifeCycleMilestoneInstances, id));

        return ok(views.html.core.portfolioentrygovernance.planning_edit.render(portfolioEntry, lastPlanning,
                governanceListViewsAsMap(plannedLifeCycleMilestoneInstances), plannedDatesForm));
    }

    /**
     * Process the update of the current planning of a portfolio entry.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processEditPlanning() {

        // bind the form
        Form<PlannedDatesFormData> boundForm = plannedDatesFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors()) {

            // get the last planning
            LifeCycleInstancePlanning lastPlanning = LifeCyclePlanningDao.getLCInstancePlanningAsLastByPE(id);

            // get the last planned dates
            List<PlannedLifeCycleMilestoneInstance> plannedLifeCycleMilestoneInstances = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceLastAsListByPE(id);

            return ok(views.html.core.portfolioentrygovernance.planning_edit.render(portfolioEntry, lastPlanning,
                    governanceListViewsAsMap(plannedLifeCycleMilestoneInstances), boundForm));
        }

        PlannedDatesFormData plannedDatesFormData = boundForm.get();

        for (PlannedDateFormData plannedDateFormData : plannedDatesFormData.plannedDates) {
            Logger.debug(plannedDateFormData.plannedDateId + ": " + plannedDateFormData.plannedDate);

            if (plannedDateFormData.plannedDateId != null) {
                PlannedLifeCycleMilestoneInstance updPlannedLifeCycleMilestoneInstance = LifeCyclePlanningDao
                        .getPlannedLCMilestoneInstanceById(plannedDateFormData.plannedDateId);
                plannedDateFormData.fill(updPlannedLifeCycleMilestoneInstance);
                updPlannedLifeCycleMilestoneInstance.update();
            }
        }

        portfolioEntry.updateFirstLastPlannedDate();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_governance.planning.edit.successful"));

        return redirect(controllers.core.routes.PortfolioEntryGovernanceController.index(plannedDatesFormData.id));
    }

    /**
     * Form to change the life cycle process of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result changeLifeCycleProcess(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        return ok(views.html.core.portfolioentrygovernance.life_cycle_process_change.render(portfolioEntry, changeProcessFormTemplate,
                LifeCycleProcessDao.getLCProcessActiveAsVH(portfolioEntry.portfolioEntryType.isRelease)));
    }

    /**
     * Process the change of the life cycle process of a portfolio entry.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processChangeLifeCycleProcess() {

        // bind the form
        Form<ChangeLifeCycleProcessFormData> boundForm = changeProcessFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.portfolioentrygovernance.life_cycle_process_change.render(portfolioEntry, boundForm,
                    LifeCycleProcessDao.getLCProcessActiveAsVH()));
        }

        ChangeLifeCycleProcessFormData changeLifeCycleProcessFormData = boundForm.get();

        LifeCycleProcess lifeCycleProcess = LifeCycleProcessDao.getLCProcessById(changeLifeCycleProcessFormData.lifeCycleProcess);

        Ebean.beginTransaction();

        try {

            /*
             * set all processes of the portfolio entry to inactive
             */
            for (LifeCycleInstance process : portfolioEntry.lifeCycleInstances) {
                process.doInactive();
            }

            /*
             * create the life cycle instance
             */
            LifeCycleInstance lifeCycleInstance = new LifeCycleInstance(lifeCycleProcess, portfolioEntry);
            lifeCycleInstance.save();

            /*
             * assign to the portfolio entry the new life cycle as the active
             * one
             */
            portfolioEntry.activeLifeCycleInstance = lifeCycleInstance;
            portfolioEntry.lastApprovedLifeCycleMilestoneInstance = null;
            portfolioEntry.nextPlannedLifeCycleMilestoneInstance = null;
            portfolioEntry.startDate = portfolioEntry.endDate = null;

            /*
             * create the planning
             */
            LifeCycleInstancePlanning lifeCycleInstancePlanning = new LifeCycleInstancePlanning(lifeCycleInstance);
            lifeCycleInstancePlanning.save();

            /*
             * create the planned instances
             */
            for (LifeCycleMilestone milestone : lifeCycleProcess.lifeCycleMilestones) {
                PlannedLifeCycleMilestoneInstance plannedInstance = new PlannedLifeCycleMilestoneInstance(lifeCycleInstancePlanning, milestone);
                if (portfolioEntry.nextPlannedLifeCycleMilestoneInstance == null) {
                    portfolioEntry.nextPlannedLifeCycleMilestoneInstance = plannedInstance;
                }
                plannedInstance.save();
            }

            Ebean.commitTransaction();

        } catch (Exception e) {
            Ebean.rollbackTransaction();
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }

        /*
         * send notifications
         */

        List<Actor> actors = new ArrayList<Actor>();
        actors.add(portfolioEntry.manager);
        for (Portfolio portfolio : portfolioEntry.portfolios) {
            actors.add(portfolio.manager);
        }
        ActorDao.sendNotification(this.getNotificationManagerService(), this.getI18nMessagesPlugin(), actors,
                NotificationCategory.getByCode(Code.PORTFOLIO_ENTRY),
                controllers.core.routes.PortfolioEntryGovernanceController.index(portfolioEntry.id).url(),
                "core.portfolio_entry_governance.process.change.notification.title", "core.portfolio_entry_governance.process.change.notification.message",
                portfolioEntry.getName(), lifeCycleProcess.getName());

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_governance.process.change.successful"));

        return redirect(controllers.core.routes.PortfolioEntryGovernanceController.index(portfolioEntry.id));
    }

    /**
     * Get a map of governance list view (list of milestones) by planned date id
     * thanks a list of planned dates.
     * 
     * @param list
     *            the list of planned dates
     */
    private static Map<Long, GovernanceListView> governanceListViewsAsMap(List<PlannedLifeCycleMilestoneInstance> list) {

        if (list != null) {

            HashMap<Long, GovernanceListView> map = new HashMap<Long, GovernanceListView>();

            for (PlannedLifeCycleMilestoneInstance elem : list) {
                map.put(elem.id, new GovernanceListView(elem));
            }

            return map;
        }

        return null;

    }

    /**
     * A change life cycle process form data is used when we want change the
     * life cycle process of a portfolio entry.
     */
    public static class ChangeLifeCycleProcessFormData {

        /**
         * Default constructor.
         */
        public ChangeLifeCycleProcessFormData() {
        }

        @Required
        public Long lifeCycleProcess;

    }

    /**
     * Get the user session manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    /**
     * Get the attachment manager service.
     */
    private IAttachmentManagerPlugin getAttachmentManagerPlugin() {
        return attachmentManagerPlugin;
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
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    /**
     * Get the Play configuration service.
     */
    private Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Get the budget tracking service.
     */
    private IBudgetTrackingService getBudgetTrackingService() {
        return this.budgetTrackingService;
    }

    /**
     * Get the notification manager service.
     */
    private INotificationManagerPlugin getNotificationManagerService() {
        return this.notificationManagerService;
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }

}
