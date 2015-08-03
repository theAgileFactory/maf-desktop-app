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

import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.framework_models.account.Principal;
import models.framework_models.common.Attachment;
import models.governance.LifeCycleMilestone;
import models.governance.LifeCycleMilestoneInstance;
import models.governance.LifeCycleMilestoneInstanceApprover;
import models.governance.LifeCycleMilestoneInstanceStatusType;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import utils.form.ProcessMilestoneApprovalFormData;
import utils.form.ProcessMilestoneDecisionFormData;
import utils.table.MilestoneApprovalListView;
import utils.table.MilestoneApproverListView;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.governance.LifeCycleMilestoneDao;
import dao.pmo.ActorDao;
import framework.security.DeadboltUtils;
import framework.services.ServiceManager;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.FileAttachmentHelper;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.Table;
import framework.utils.Utilities;

/**
 * The controller which allows to approve / decide a milestone instance.
 * 
 * Approve a milestone simply represents a vote by a user and has no impact
 * about the governance (except the vote itself).
 * 
 * Decide a milestone represents the final decision (also called final
 * approval). So the milestone is passed and possibly approved. The following
 * operations are done:<br/>
 * -the current budget is closed and assigned to the milestone, a new budget
 * (based on the existing) is created<br/>
 * -the current planning is frozen and a new one (based on the existing) is
 * created<br/>
 * -if the portfolio entry is a concept and the milestone is approved then the
 * flag is concept is settled to false
 * 
 * @author Johann Kohler
 */
public class MilestoneApprovalController extends Controller {

    private static Logger.ALogger log = Logger.of(MilestoneApprovalController.class);

    private static Form<ProcessMilestoneApprovalFormData> processMilestoneApprovalFormTemplate = Form.form(ProcessMilestoneApprovalFormData.class);
    private static Form<ProcessMilestoneDecisionFormData> processMilestoneDecisionFormTemplate = Form.form(ProcessMilestoneDecisionFormData.class);

    /**
     * The possible vote values.
     */
    public static DefaultSelectableValueHolderCollection<Boolean> getVoteValues() {
        DefaultSelectableValueHolderCollection<Boolean> voteValues = new DefaultSelectableValueHolderCollection<Boolean>();
        voteValues.add(new DefaultSelectableValueHolder<Boolean>(true, "<span class=\"a label label-success\">" + Msg.get("button.approve") + "</span>"));
        voteValues.add(new DefaultSelectableValueHolder<Boolean>(false, "<span class=\"b label label-danger\">" + Msg.get("button.reject") + "</span>"));
        return voteValues;
    }

    /**
     * Display the milestones' planning.
     */
    @Restrict({ @Group(IMafConstants.MILESTONE_OVERVIEW_PERMISSION) })
    public static Result overview() {

        List<LifeCycleMilestoneInstance> milestoneInstances = LifeCycleMilestoneDao.getLCMilestoneInstanceActiveAndPublicPEAsList();

        List<MilestoneInstanceEvent> events = new ArrayList<MilestoneInstanceEvent>();
        for (LifeCycleMilestoneInstance milestoneInstance : milestoneInstances) {
            events.add(new MilestoneInstanceEvent(milestoneInstance));
        }

        String source = "";
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            source = ow.writeValueAsString(events);
            source = source.replaceAll("\"statusClass\"", "\"class\"");
        } catch (JsonProcessingException e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

        return ok(views.html.core.milestoneapproval.overview.render(source));
    }

    /**
     * Modal content for a milestone in the planning.
     * 
     * @param milestoneInstanceId
     *            the milestone instance id
     */
    @Restrict({ @Group(IMafConstants.MILESTONE_OVERVIEW_PERMISSION) })
    public static Result overviewModal(Long milestoneInstanceId) {

        // get the milestone instance
        LifeCycleMilestoneInstance milestoneInstance = LifeCycleMilestoneDao.getLCMilestoneInstanceById(milestoneInstanceId);

        // construct the approvers table
        List<MilestoneApproverListView> milestoneApproverListView = new ArrayList<MilestoneApproverListView>();
        for (LifeCycleMilestoneInstanceApprover lifeCycleMilestoneInstanceApprover : milestoneInstance.lifeCycleMilestoneInstanceApprovers) {
            milestoneApproverListView.add(new MilestoneApproverListView(lifeCycleMilestoneInstanceApprover));
        }
        Table<MilestoneApproverListView> filledTable = MilestoneApproverListView.templateTable.fill(milestoneApproverListView);

        return ok(views.html.core.milestoneapproval.overview_modal.render(milestoneInstance, filledTable));
    }

    /**
     * Display the list of life cycle milestone instance for which an
     * vote/decision is needed.
     * 
     * @param page
     *            the current page
     */
    @Restrict({ @Group(IMafConstants.MILESTONE_APPROVAL_PERMISSION), @Group(IMafConstants.MILESTONE_DECIDE_PERMISSION) })
    public static Result list(Integer page) {

        // get the current user
        IUserAccount userAccount;
        try {
            IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionManagerPlugin.getUserSessionId(ctx()));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

        // get the current actor
        Actor actor = ActorDao.getActorByUid(userAccount.getUid());

        // get the milestone instances required for a vote/decision
        Pagination<LifeCycleMilestoneInstance> pagination;
        if (DeadboltUtils.hasRole(userAccount, IMafConstants.MILESTONE_DECIDE_PERMISSION)) {
            pagination = LifeCycleMilestoneDao.getLCMilestoneInstanceAsPagination();
        } else {
            // if the sign in user hasn't the permission
            // MILESTONE_DECIDE_PERMISSION and is not related to an actor, then
            // the list of milestone to approve doesn't make sense
            if (actor == null) {
                return redirect(controllers.routes.Application.index());
            }
            pagination = LifeCycleMilestoneDao.getLCMilestoneInstanceAsPaginationByApprover(actor.id);
        }

        pagination.setCurrentPage(page);

        List<MilestoneApprovalListView> milestoneApprovalListView = new ArrayList<MilestoneApprovalListView>();
        for (LifeCycleMilestoneInstance lifeCycleMilestoneInstance : pagination.getListOfObjects()) {
            milestoneApprovalListView.add(new MilestoneApprovalListView(lifeCycleMilestoneInstance));
        }

        Table<MilestoneApprovalListView> filledTable = MilestoneApprovalListView.templateTable.fill(milestoneApprovalListView);

        return ok(views.html.core.milestoneapproval.milestone_approval_list.render(filledTable, pagination));
    }

    /**
     * Display the page to vote/decide for a milestone instance. This page
     * contains the details of the milestone (and its portfolio entry), the
     * approvers with their vote, the panel to vote (if approver), the panel to
     * decide (if decider).
     * 
     * @param milestoneInstanceId
     *            the milestone instance id
     */
    @Restrict({ @Group(IMafConstants.MILESTONE_APPROVAL_PERMISSION), @Group(IMafConstants.MILESTONE_DECIDE_PERMISSION) })
    public static Result process(Long milestoneInstanceId) {

        // get the milestone instance
        LifeCycleMilestoneInstance milestoneInstance = LifeCycleMilestoneDao.getLCMilestoneInstanceById(milestoneInstanceId);

        // check the milestone instance is not already passed (decided)
        if (milestoneInstance.isPassed == true) {
            Utilities.sendInfoFlashMessage(Msg.get("core.milestone.approval.process.alreadydecided"));
            return redirect(controllers.core.routes.MilestoneApprovalController.list(0));
        }

        // get the current user
        IUserAccount userAccount;
        try {
            IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionManagerPlugin.getUserSessionId(ctx()));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

        // get the current actor
        Actor actor = ActorDao.getActorByUid(userAccount.getUid());

        // if exists, get the approver instance
        LifeCycleMilestoneInstanceApprover approverInstance = null;
        if (actor != null) {
            approverInstance = LifeCycleMilestoneDao.getLCMilestoneInstanceApproverByActorAndLCMilestoneInstance(actor.id, milestoneInstance.id);
        }

        // if the user hasn't the permission MILESTONE_DECIDE_PERMISSION, then
        // he must be an approver of the milestone instance
        if (!DeadboltUtils.hasRole(userAccount, IMafConstants.MILESTONE_DECIDE_PERMISSION)) {
            if (approverInstance == null) {
                return forbidden(views.html.error.access_forbidden.render(""));
            } else {
                // check the approver has not already vote
                if (approverInstance.hasApproved != null) {
                    Utilities.sendInfoFlashMessage(Msg.get("core.milestone.approval.process.alreadyvoted"));
                    return redirect(controllers.core.routes.MilestoneApprovalController.list(0));
                }
            }
        }

        /*
         * construct the current vote table
         */

        // initiate the list view
        List<MilestoneApproverListView> milestoneApproverListView = new ArrayList<MilestoneApproverListView>();

        // add the approvers with vote if exists
        for (LifeCycleMilestoneInstanceApprover lifeCycleMilestoneInstanceApprover : milestoneInstance.lifeCycleMilestoneInstanceApprovers) {
            milestoneApproverListView.add(new MilestoneApproverListView(lifeCycleMilestoneInstanceApprover));
        }

        // hide the date column
        Set<String> hideColumnsForApprover = new HashSet<String>();
        hideColumnsForApprover.add("approvalDate");

        // fill the table
        Table<MilestoneApproverListView> filledTable = MilestoneApproverListView.templateTable.fill(milestoneApproverListView, hideColumnsForApprover);

        /*
         * end of table construction
         */

        // get the portfolio entry
        PortfolioEntry portfolioEntry = milestoneInstance.lifeCycleInstance.portfolioEntry;

        // get the milestone instances status
        List<String> status = LifeCycleMilestoneDao.getLCMilestoneAsStatusByPEAndLCMilestone(portfolioEntry.id, milestoneInstance.lifeCycleMilestone.id);

        // if exists, get the description document
        List<Attachment> attachments = FileAttachmentHelper.getFileAttachmentsForDisplay(LifeCycleMilestoneInstance.class, milestoneInstance.id);
        Attachment descriptionDocument = null;
        if (attachments != null && attachments.size() > 0) {
            descriptionDocument = attachments.get(0);
        }

        // construct the approval (vote) form
        Form<ProcessMilestoneApprovalFormData> processMilestoneApprovalForm = null;
        if (approverInstance != null) {
            processMilestoneApprovalForm = processMilestoneApprovalFormTemplate.fill(new ProcessMilestoneApprovalFormData(approverInstance));
        }

        // construct the decision form
        Form<ProcessMilestoneDecisionFormData> processMilestoneDecisionForm = null;
        if (DeadboltUtils.hasRole(userAccount, IMafConstants.MILESTONE_DECIDE_PERMISSION)) {
            processMilestoneDecisionForm = processMilestoneDecisionFormTemplate.fill(new ProcessMilestoneDecisionFormData(milestoneInstance));
        }

        return ok(views.html.core.milestoneapproval.milestone_approval_process.render(milestoneInstance, descriptionDocument, approverInstance, filledTable,
                portfolioEntry, status, processMilestoneApprovalForm, processMilestoneDecisionForm,
                LifeCycleMilestoneDao.getLCMilestoneInstanceStatusTypeActiveAsVH()));
    }

    /**
     * Process a vote for a milestone instance.
     */
    @Restrict({ @Group(IMafConstants.MILESTONE_APPROVAL_PERMISSION), @Group(IMafConstants.MILESTONE_DECIDE_PERMISSION) })
    public static Result vote() {

        Form<ProcessMilestoneApprovalFormData> boundForm = processMilestoneApprovalFormTemplate.bindFromRequest();

        // get the approver instance
        Long approverInstanceId = Long.valueOf(boundForm.data().get("approverInstanceId"));
        LifeCycleMilestoneInstanceApprover approverInstance = LifeCycleMilestoneDao.getLCMilestoneInstanceApproverById(approverInstanceId);

        if (boundForm.hasErrors()) {
            // this case is not possible
            return redirect(controllers.core.routes.MilestoneApprovalController.process(approverInstance.lifeCycleMilestoneInstance.id));
        }

        // get the form values
        ProcessMilestoneApprovalFormData processMilestoneApprovalFormData = boundForm.get();

        // register the vote
        processMilestoneApprovalFormData.fill(approverInstance);
        approverInstance.save();

        // if last vote, send notification to all deciders (has the permission
        // MILESTONE_DECIDE_PERMISSION)
        Long milestoneInstanceId = approverInstance.lifeCycleMilestoneInstance.id;
        if (LifeCycleMilestoneDao.hasLCMilestoneInstanceAllApproversVoted(milestoneInstanceId)) {

            PortfolioEntry portfolioEntry = approverInstance.lifeCycleMilestoneInstance.lifeCycleInstance.portfolioEntry;

            String url = controllers.core.routes.MilestoneApprovalController.process(milestoneInstanceId).url();

            List<Principal> principals = Principal.find.where().eq("deleted", false).eq("systemLevelRoles.isEnabled", true)
                    .eq("systemLevelRoles.systemLevelRoleType.deleted", false).eq("systemLevelRoles.systemLevelRoleType.selectable", true)
                    .eq("systemLevelRoles.systemLevelRoleType.systemPermissions.deleted", false)
                    .eq("systemLevelRoles.systemLevelRoleType.systemPermissions.selectable", true)
                    .eq("systemLevelRoles.systemLevelRoleType.systemPermissions.name", IMafConstants.MILESTONE_DECIDE_PERMISSION).findList();

            for (Principal principal : principals) {

                ActorDao.sendNotification(principal.uid, NotificationCategory.getByCode(Code.APPROVAL), url,
                        "core.milestone.approval.process.panel.vote.decisionrequired.notification.title",
                        "core.milestone.approval.process.panel.vote.decisionrequired.notification.message", portfolioEntry.getName());
            }

        }

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get("core.milestone.approval.process.panel.vote.successfull"));

        return redirect(controllers.core.routes.MilestoneApprovalController.list(0));
    }

    /**
     * Process a decision.
     */
    @Restrict({ @Group(IMafConstants.MILESTONE_DECIDE_PERMISSION) })
    public static Result decide() {

        Form<ProcessMilestoneDecisionFormData> boundForm = processMilestoneDecisionFormTemplate.bindFromRequest();

        // get the milestone instance
        Long milestoneInstanceId = Long.valueOf(boundForm.data().get("milestoneInstanceId"));
        LifeCycleMilestoneInstance milestoneInstance = LifeCycleMilestoneDao.getLCMilestoneInstanceById(milestoneInstanceId);

        if (boundForm.hasErrors()) {
            // this case is possible only if the user empty the date
            return redirect(controllers.core.routes.MilestoneApprovalController.process(milestoneInstance.id));
        }

        // get the form values
        ProcessMilestoneDecisionFormData processMilestoneDecisionFormData = boundForm.get();

        Ebean.beginTransaction();

        try {
            LifeCycleMilestoneInstanceStatusType status = LifeCycleMilestoneDao
                    .getLCMilestoneInstanceStatusTypeById(processMilestoneDecisionFormData.lifeCycleMilestoneInstanceStatusType);

            milestoneInstance = LifeCycleMilestoneDao.doPassed(milestoneInstance.id, status, processMilestoneDecisionFormData.comments);

            // save the passed date
            milestoneInstance.passedDate = Utilities.getDateFormat(null).parse(processMilestoneDecisionFormData.passedDate);
            milestoneInstance.save();

            // set the current actor as approver (if exists)
            IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            Actor actor = ActorDao.getActorByUid(userSessionManagerPlugin.getUserSessionId(ctx()));
            if (actor != null) {
                milestoneInstance.approver = actor;
                milestoneInstance.save();
            }

            Ebean.commitTransaction();

            // notification
            PortfolioEntry portfolioEntry = milestoneInstance.lifeCycleInstance.portfolioEntry;
            String url = controllers.core.routes.PortfolioEntryGovernanceController.index(portfolioEntry.id).url();
            if (status.isApproved) {
                ActorDao.sendNotification(portfolioEntry.manager, NotificationCategory.getByCode(Code.APPROVAL), url,
                        "core.milestone.approval.process.panel.decide.approve.notification.title",
                        "core.milestone.approval.process.panel.decide.approve.notification.message", portfolioEntry.getName());
            } else {
                ActorDao.sendNotification(portfolioEntry.manager, NotificationCategory.getByCode(Code.APPROVAL), url,
                        "core.milestone.approval.process.panel.decide.reject.notification.title",
                        "core.milestone.approval.process.panel.decide.reject.notification.message", portfolioEntry.getName());
            }

        } catch (Exception e) {
            Ebean.rollbackTransaction();
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get("core.milestone.approval.process.panel.decide.successfull"));

        return redirect(controllers.core.routes.MilestoneApprovalController.list(0));
    }

    /**
     * A milestone instance event represents an entry (for the display) in the
     * milestones' planning.
     * 
     * @author Johann Kohler
     */
    public static class MilestoneInstanceEvent {

        /**
         * The milestone instance id.
         */
        public Long id;

        /**
         * The title, that is composed by:<br/>
         * "PE governanceId - PE name: milestone short name - status". If
         * governanceId is null, simply "PE name: ..."
         */
        public String title;

        /**
         * The modal URL.
         */
        public String url;

        /**
         * The URL to the governance page of the corresponding portfolio entry.
         */
        public String goToUrl;

        /**
         * The CSS class for the status.<br/>
         * event-success: passed<br/>
         * event-important: rejected<br/>
         * event-warning: pending<br/>
         * event-info: unknown
         */
        public String statusClass;

        /**
         * The start date.
         */
        public Date start;

        /**
         * The end date.
         */
        public Date end;

        /**
         * Construct an event thanks a milestone instance in the DB.
         * 
         * @param milestoneInstance
         *            the milestone instance
         */
        public MilestoneInstanceEvent(LifeCycleMilestoneInstance milestoneInstance) {

            PortfolioEntry portfolioEntry = milestoneInstance.lifeCycleInstance.portfolioEntry;
            LifeCycleMilestone milestone = milestoneInstance.lifeCycleMilestone;

            // compute the status
            String statusLabel = null;
            String statusClass = null;
            switch (milestoneInstance.getStatus()) {
            case APPROVED:
                statusLabel = milestoneInstance.lifeCycleMilestoneInstanceStatusType.getName();
                statusClass = "event-success";
                break;
            case PENDING:
                statusLabel = Msg.get("object.life_cycle_milestone_instance.status." + milestoneInstance.getStatus() + ".label");
                statusClass = "event-warning";
                break;
            case REJECTED:
                statusLabel = milestoneInstance.lifeCycleMilestoneInstanceStatusType.getName();
                statusClass = "event-important";
                break;
            case UNKNOWN:
                statusLabel = Msg.get("object.life_cycle_milestone_instance.status." + milestoneInstance.getStatus() + ".label");
                statusClass = "event-info";
                break;
            }

            this.id = milestoneInstance.id;

            this.title = portfolioEntry.getName() + " - " + milestone.getShortName() + " - " + statusLabel;

            this.url = controllers.core.routes.MilestoneApprovalController.overviewModal(milestoneInstance.id).url();

            this.goToUrl = controllers.core.routes.PortfolioEntryGovernanceController.index(portfolioEntry.id).url();

            this.statusClass = statusClass;

            this.start = milestoneInstance.passedDate;
            this.end = milestoneInstance.passedDate;

        }

    }

}
