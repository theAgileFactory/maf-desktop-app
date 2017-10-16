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
import java.util.stream.Collectors;

import javax.inject.Inject;

import framework.services.account.AccountManagementException;
import org.apache.commons.lang3.tuple.Pair;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.OrderBy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.governance.LifeCycleMilestoneDao;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryEventDao;
import framework.security.ISecurityService;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.FileAttachmentHelper;
import framework.utils.FilterConfig;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.Table;
import framework.utils.Utilities;
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
import models.pmo.PortfolioEntryEvent;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioEntryExists;
import security.dynamic.PortfolioEntryDynamicHelper;
import services.budgettracking.IBudgetTrackingService;
import services.tableprovider.ITableProvider;
import utils.form.ProcessMilestoneApprovalFormData;
import utils.form.ProcessMilestoneDecisionFormData;
import utils.table.MilestoneApprovalListView;
import utils.table.MilestoneApproverListView;
import utils.table.PortfolioEntryEventListView;

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
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private IAccountManagerPlugin accountManagerPlugin;
    @Inject
    private IAttachmentManagerPlugin attachmentPluginManager;
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
    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;

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
    public Result overview() {

        List<LifeCycleMilestoneInstance> milestoneInstances = LifeCycleMilestoneDao.getLCMilestoneInstancePublicPEAsList();

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
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
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
    public Result overviewModal(Long milestoneInstanceId) {

        // get the milestone instance
        LifeCycleMilestoneInstance milestoneInstance = LifeCycleMilestoneDao.getLCMilestoneInstanceById(milestoneInstanceId);

        // construct the approvers table
        List<MilestoneApproverListView> milestoneApproverListView = new ArrayList<MilestoneApproverListView>();
        for (LifeCycleMilestoneInstanceApprover lifeCycleMilestoneInstanceApprover : milestoneInstance.lifeCycleMilestoneInstanceApprovers) {
            milestoneApproverListView.add(new MilestoneApproverListView(lifeCycleMilestoneInstanceApprover));
        }
        Table<MilestoneApproverListView> filledTable = this.getTableProvider().get().milestoneApprover.templateTable.fill(milestoneApproverListView);

        return ok(views.html.core.milestoneapproval.overview_modal.render(milestoneInstance, filledTable));
    }

    
    private Pair<Table<MilestoneApprovalListView>, Pagination<LifeCycleMilestoneInstance>> getMilestoneApprovalListTable(FilterConfig<MilestoneApprovalListView> filterConfig) throws AccountManagementException {
        // get the current user
        IUserAccount userAccount;
        userAccount = getAccountManagerPlugin().getUserAccountFromUid(getUserSessionManagerPlugin().getUserSessionId(ctx()));

        // get the current actor
        Actor actor = ActorDao.getActorByUid(userAccount.getUid());

        OrderBy<LifeCycleMilestoneInstance> orderBy = filterConfig.getSortExpression();

    	ExpressionList<LifeCycleMilestoneInstance> expressionList;
    	 if (getSecurityService().restrict(IMafConstants.MILESTONE_DECIDE_PERMISSION, userAccount)) {
    		 expressionList = filterConfig.updateWithSearchExpression(LifeCycleMilestoneDao.getLCMilestoneInstanceAsExpr());
    	 }
    	 else
    	 {
    		 expressionList = filterConfig.updateWithSearchExpression(LifeCycleMilestoneDao.getLCMilestoneInstanceAsExprByApprover(actor.id));
    	 }
        
        Utilities.updateExpressionListWithOrderBy(orderBy, expressionList);
        
        Pagination<LifeCycleMilestoneInstance> pagination = new Pagination<>(this.getPreferenceManagerPlugin(), expressionList.findList().size(), expressionList);
        pagination.setCurrentPage(filterConfig.getCurrentPage());

        List<MilestoneApprovalListView> milestoneApprovalListView = pagination.getListOfObjects().stream().map(MilestoneApprovalListView::new).collect(Collectors.toList());

        Table<MilestoneApprovalListView> table = this.getTableProvider().get().milestoneApproval.templateTable
                .fillForFilterConfig(milestoneApprovalListView, filterConfig.getColumnsToHide());

        return Pair.of(table, pagination);

    }
    
    /**

     */
    @Restrict({ @Group(IMafConstants.MILESTONE_APPROVAL_PERMISSION), @Group(IMafConstants.MILESTONE_DECIDE_PERMISSION) })
    public Result listFilter() {

        try {
            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            
            FilterConfig<MilestoneApprovalListView> filterConfig = this.getTableProvider().get().milestoneApproval.filterConfig.persistCurrentInDefault(uid, request());

            if (filterConfig == null) {
                return ok(views.html.framework_views.parts.table.dynamic_tableview_no_more_compatible.render());
            } else {
                // get the table
                Pair<Table<MilestoneApprovalListView>, Pagination<LifeCycleMilestoneInstance>> t = getMilestoneApprovalListTable(filterConfig);

                return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));
            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }
    
    /**
     * Display the list of life cycle milestone instance for which an
     * vote/decision is needed.
     * 
     * @param page
     *            the current page
     */
    @Restrict({ @Group(IMafConstants.MILESTONE_APPROVAL_PERMISSION), @Group(IMafConstants.MILESTONE_DECIDE_PERMISSION) })
    public Result list() {
        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<MilestoneApprovalListView> filterConfig = this.getTableProvider().get().milestoneApproval.filterConfig.getCurrent(uid, request());

            // get the table
            Pair<Table<MilestoneApprovalListView>, Pagination<LifeCycleMilestoneInstance>> t = getMilestoneApprovalListTable(filterConfig);

            return ok(views.html.core.milestoneapproval.milestone_approval_list.render(t.getLeft(), t.getRight(), filterConfig));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
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
    public Result process(Long milestoneInstanceId) {

        // get the milestone instance
        LifeCycleMilestoneInstance milestoneInstance = LifeCycleMilestoneDao.getLCMilestoneInstanceById(milestoneInstanceId);

        // check the milestone instance is not already passed (decided)
        if (milestoneInstance.isPassed) {
            Utilities.sendInfoFlashMessage(Msg.get("core.milestone.approval.process.alreadydecided"));
            return redirect(controllers.core.routes.MilestoneApprovalController.list());
        }

        // get the current user
        IUserAccount userAccount;
        try {
            userAccount = getAccountManagerPlugin().getUserAccountFromUid(getUserSessionManagerPlugin().getUserSessionId(ctx()));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
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
        if (!getSecurityService().restrict(IMafConstants.MILESTONE_DECIDE_PERMISSION, userAccount)) {
            if (approverInstance == null) {
                return forbidden(views.html.error.access_forbidden.render(""));
            } else {
                // check the approver has not already vote
                if (approverInstance.hasApproved != null) {
                    Utilities.sendInfoFlashMessage(Msg.get("core.milestone.approval.process.alreadyvoted"));
                    return redirect(controllers.core.routes.MilestoneApprovalController.list());
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
        Table<MilestoneApproverListView> filledTable = this.getTableProvider().get().milestoneApprover.templateTable.fill(milestoneApproverListView,
                hideColumnsForApprover);

        /*
         * end of table construction
         */

        // get the portfolio entry
        PortfolioEntry portfolioEntry = milestoneInstance.lifeCycleInstance.portfolioEntry;

        // get the milestone instances status
        List<String> status = LifeCycleMilestoneDao.getLCMilestoneAsStatusByPEAndLCMilestone(portfolioEntry.id, milestoneInstance.lifeCycleMilestone.id);

        // if exists, get the description document
        List<Attachment> attachments = FileAttachmentHelper.getFileAttachmentsForDisplay(LifeCycleMilestoneInstance.class, milestoneInstance.id,
                getAttachmentPluginManager(), getUserSessionManagerPlugin());
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
        if (getSecurityService().restrict(IMafConstants.MILESTONE_DECIDE_PERMISSION, userAccount)) {
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
    public Result vote() {

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

                ActorDao.sendNotification(getNotificationManagerService(), getI18nMessagesPlugin(), principal.uid,
                        NotificationCategory.getByCode(Code.APPROVAL), url, "core.milestone.approval.process.panel.vote.decisionrequired.notification.title",
                        "core.milestone.approval.process.panel.vote.decisionrequired.notification.message", portfolioEntry.getName());
            }

        }

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get("core.milestone.approval.process.panel.vote.successfull"));

        return redirect(controllers.core.routes.MilestoneApprovalController.list());
    }

    /**
     * Process a decision.
     */
    @Restrict({ @Group(IMafConstants.MILESTONE_DECIDE_PERMISSION) })
    public Result decide() {

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

            milestoneInstance = LifeCycleMilestoneDao.doPassed(milestoneInstance.id, status, processMilestoneDecisionFormData.comments,
                    this.getBudgetTrackingService());

            // save the passed date
            milestoneInstance.passedDate = Utilities.getDateFormat(null).parse(processMilestoneDecisionFormData.passedDate);
            milestoneInstance.save();

            // set the current actor as approver (if exists)

            Actor actor = ActorDao.getActorByUid(getUserSessionManagerPlugin().getUserSessionId(ctx()));
            if (actor != null) {
                milestoneInstance.approver = actor;
                milestoneInstance.save();
            }

            Ebean.commitTransaction();

            // notification
            PortfolioEntry portfolioEntry = milestoneInstance.lifeCycleInstance.portfolioEntry;
            String url = controllers.core.routes.PortfolioEntryGovernanceController.index(portfolioEntry.id).url();
            if (status.isApproved) {
                ActorDao.sendNotification(getNotificationManagerService(), getI18nMessagesPlugin(), portfolioEntry.manager,
                        NotificationCategory.getByCode(Code.APPROVAL), url, "core.milestone.approval.process.panel.decide.approve.notification.title",
                        "core.milestone.approval.process.panel.decide.approve.notification.message", portfolioEntry.getName());
            } else {
                ActorDao.sendNotification(getNotificationManagerService(), getI18nMessagesPlugin(), portfolioEntry.manager,
                        NotificationCategory.getByCode(Code.APPROVAL), url, "core.milestone.approval.process.panel.decide.reject.notification.title",
                        "core.milestone.approval.process.panel.decide.reject.notification.message", portfolioEntry.getName());
            }

        } catch (Exception e) {
            Ebean.rollbackTransaction();
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get("core.milestone.approval.process.panel.decide.successfull"));

        return redirect(controllers.core.routes.MilestoneApprovalController.list());
    }

    /**
     * Delete the milestone instance.
     * 
     * @param milestoneInstanceId
     *            the milestone instance id
     */
    @Restrict({ @Group(IMafConstants.MILESTONE_DECIDE_PERMISSION) })
    public Result delete(Long milestoneInstanceId) {

        LifeCycleMilestoneInstance milestoneInstance = LifeCycleMilestoneDao.getLCMilestoneInstanceById(milestoneInstanceId);

        milestoneInstance.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.milestone.approval.process.panel.decide.delete.successful"));

        return redirect(controllers.core.routes.MilestoneApprovalController.list());
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

            this.goToUrl = controllers.core.routes.PortfolioEntryGovernanceController
                    .viewMilestone(portfolioEntry.id, milestoneInstance.lifeCycleMilestone.id).url();

            this.statusClass = statusClass;

            this.start = milestoneInstance.passedDate;
            this.end = milestoneInstance.passedDate;

        }

    }

    /**
     * Get the use session manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    /**
     * Get the account manager service.
     */
    private IAccountManagerPlugin getAccountManagerPlugin() {
        return accountManagerPlugin;
    }

    /**
     * Get the attachment plugin service.
     */
    private IAttachmentManagerPlugin getAttachmentPluginManager() {
        return attachmentPluginManager;
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

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return this.preferenceManagerPlugin;
    }

}
