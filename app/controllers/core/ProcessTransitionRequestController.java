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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.avaje.ebean.Ebean;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.governance.LifeCycleMilestoneDao;
import dao.governance.ProcessTransitionRequestDao;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryDao;
import framework.security.ISecurityService;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.utils.FileAttachmentHelper;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.Table;
import framework.utils.Utilities;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.framework_models.common.Attachment;
import models.governance.LifeCycleMilestone;
import models.governance.LifeCycleMilestoneInstance;
import models.governance.LifeCycleMilestoneInstanceApprover;
import models.governance.ProcessTransitionRequest;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioEntryExists;
import utils.form.ProcessMilestoneRequestFormData;
import utils.form.RequestMilestoneFormData;
import utils.table.MilestoneRequestListView;

/**
 * The controller which is to be used to review approve/reject a process
 * transition request.<br/>
 * The consequence of the approval of a process transition request is variable:
 * <ul>
 * <li>For a "MILESTONE APPROVAL" request : if the request is accepted, the
 * approval process is triggered</li>
 * </ul>
 * 
 * @author Pierre-Yves Cloux
 * @author Johann Kohler
 */
public class ProcessTransitionRequestController extends Controller {
    @Inject
    private IAttachmentManagerPlugin attachmentManagerPlugin;
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private ISecurityService securityService;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;
    
    private static Logger.ALogger log = Logger.of(ProcessTransitionRequestController.class);

    private static Form<ProcessMilestoneRequestFormData> processMilestoneRequestFormTemplate = Form.form(ProcessMilestoneRequestFormData.class);

    /**
     * Display the list of pending milestone requests. Pending means a request
     * that is neither accepted nor rejected.
     * 
     * @param page
     *            the current page
     */
    @Restrict({ @Group(IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_ALL_PERMISSION),
            @Group(IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_AS_PORTFOLIO_MANAGER_PERMISSION) })
    public Result reviewMilestoneRequestList(Integer page) {

        Pagination<ProcessTransitionRequest> pagination = ProcessTransitionRequestDao.getProcessTransitionRequestMilestoneApprovalToReviewAsPagination();
        pagination.setCurrentPage(page);

        List<MilestoneRequestListView> requestsListView = new ArrayList<MilestoneRequestListView>();
        for (ProcessTransitionRequest request : pagination.getListOfObjects()) {
            MilestoneRequestListView milestoneRequest = new MilestoneRequestListView(request);
            if (milestoneRequest.portfolioEntry != null
                    && milestoneRequest.milestone != null
                    && getSecurityService().dynamic(IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_DYNAMIC_PERMISSION, "",
                            milestoneRequest.portfolioEntry.id)) {
                requestsListView.add(milestoneRequest);
            }
        }

        Table<MilestoneRequestListView> filledTable = MilestoneRequestListView.templateTable.fill(requestsListView);
        return ok(views.html.core.processtransitionrequest.review_milestone_request_list.render(filledTable, pagination));
    }

    /**
     * Display the details of a pending milestone request in order to
     * accept/reject it.
     * 
     * @param id
     *            the portfolio entry id
     * @param requestId
     *            the request id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_DYNAMIC_PERMISSION)
    public Result processMilestoneRequest(Long id, Long requestId) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the request
        ProcessTransitionRequest request = ProcessTransitionRequestDao.getProcessTransitionRequestById(requestId);

        // check the request is not yet processed
        if (request.reviewDate != null) {
            Utilities.sendInfoFlashMessage(Msg.get("core.process_transition_request.process_milestone_request.message.already_processed"));
            return redirect(controllers.core.routes.ProcessTransitionRequestController.reviewMilestoneRequestList(0));
        }

        // get the structured document (RequestMilestoneFormData)
        RequestMilestoneFormData requestMilestoneFormData = null;
        try {
            List<Attachment> structuredDocumentAttachments =
                    getAttachmentManagerPlugin().getAttachmentsFromObjectTypeAndObjectId(ProcessTransitionRequest.class, request.id, true);

            requestMilestoneFormData = (RequestMilestoneFormData) Utilities.unmarshallObject(structuredDocumentAttachments.get(0).structuredDocument.content);

            if (!requestMilestoneFormData.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }

        // get the milestone
        LifeCycleMilestone lifeCycleMilestone = LifeCycleMilestoneDao.getLCMilestoneById(requestMilestoneFormData.milestoneId);

        // get the milestone instances status
        List<String> status = LifeCycleMilestoneDao.getLCMilestoneAsStatusByPEAndLCMilestone(id, lifeCycleMilestone.id);

        // if exists, get the description document
        List<Attachment> attachments = FileAttachmentHelper.getFileAttachmentsForDisplay(ProcessTransitionRequest.class, request.id,getAttachmentManagerPlugin(),getUserSessionManagerPlugin());
        Attachment descriptionDocument = null;
        if (attachments != null && attachments.size() > 0) {
            descriptionDocument = attachments.get(0);
        }

        // construct the form
        Form<ProcessMilestoneRequestFormData> processMilestoneRequestForm =
                processMilestoneRequestFormTemplate.fill(new ProcessMilestoneRequestFormData(requestMilestoneFormData, request.id));

        return ok(views.html.core.processtransitionrequest.milestone_request_process.render(request, descriptionDocument, portfolioEntry, lifeCycleMilestone,
                status, processMilestoneRequestForm));
    }

    /**
     * Process the approval of a milestone request (a user accept it).<br>
     * -create a life cycle milestone instance<br/>
     * -if exists forward the request attachment document to the milestone
     * instance<br>
     * -if the milestone instance has approvers then notify them, else
     * automatically approve it
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_DYNAMIC_PERMISSION)
    public Result acceptMilestoneRequest() {

        Form<ProcessMilestoneRequestFormData> boundForm = processMilestoneRequestFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            // this case is not possible
            Long id = Long.valueOf(boundForm.data().get("id"));
            Long requestId = Long.valueOf(boundForm.data().get("requestId"));
            return redirect(controllers.core.routes.ProcessTransitionRequestController.processMilestoneRequest(id, requestId));
        }

        // get the form values
        ProcessMilestoneRequestFormData processMilestoneRequestFormData = boundForm.get();

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(processMilestoneRequestFormData.id);

        // get the request
        ProcessTransitionRequest request = ProcessTransitionRequestDao.getProcessTransitionRequestById(processMilestoneRequestFormData.requestId);

        // if exists, get the request description document file
        List<Attachment> attachments = FileAttachmentHelper.getFileAttachmentsForDisplay(ProcessTransitionRequest.class, request.id,getAttachmentManagerPlugin(),getUserSessionManagerPlugin());
        Attachment descriptionDocument = null;
        if (attachments != null && attachments.size() > 0) {
            descriptionDocument = attachments.get(0);
        }

        String successKey = "";
        LifeCycleMilestoneInstance lifeCycleMilestoneInstance;

        Ebean.beginTransaction();

        try {

            // create LifeCycleMilestoneInstance
            lifeCycleMilestoneInstance = new LifeCycleMilestoneInstance();
            processMilestoneRequestFormData.fill(lifeCycleMilestoneInstance, descriptionDocument != null);
            lifeCycleMilestoneInstance.save();

            // if there are approvers
            if (processMilestoneRequestFormData.approvers.size() > 0) {

                // add the approvers
                List<Actor> approvers = new ArrayList<Actor>();
                for (Long approverId : processMilestoneRequestFormData.approvers) {
                    Actor actor = ActorDao.getActorById(approverId);
                    LifeCycleMilestoneInstanceApprover instanceApprover = new LifeCycleMilestoneInstanceApprover(actor, lifeCycleMilestoneInstance);
                    instanceApprover.save();
                    approvers.add(actor);
                }

                // notification (manager + requester)
                List<Actor> actors = new ArrayList<Actor>(Arrays.asList(portfolioEntry.manager, request.requester));
                ActorDao.sendNotification(actors, NotificationCategory.getByCode(Code.REQUEST_REVIEW),
                        controllers.core.routes.PortfolioEntryGovernanceController.index(portfolioEntry.id).url(),
                        "core.process_transition_request.process_milestone_request.panel.form.accept.instance.notification.title",
                        "core.process_transition_request.process_milestone_request.panel.form.accept.instance.notification.message", portfolioEntry.getName());

                // notification (approvers)
                String approversUrl = controllers.core.routes.MilestoneApprovalController.process(lifeCycleMilestoneInstance.id).url();
                ActorDao.sendNotification(approvers, NotificationCategory.getByCode(Code.APPROVAL), approversUrl,
                        "core.process_transition_request.process_milestone_request.panel.form.vote_required.notification.title",
                        "core.process_transition_request.process_milestone_request.panel.form.vote_required.notification.message", portfolioEntry.getName());

                // success message
                successKey = "core.process_transition_request.process_milestone_request.panel.form.accept.instance.successful";

            } else { // if there aren't approvers then we set the milestone
                     // instance as passed

                lifeCycleMilestoneInstance = LifeCycleMilestoneDao.doPassed(lifeCycleMilestoneInstance.id);

                // set the current actor as approver (if exists)
                Actor actor = ActorDao.getActorByUid(getUserSessionManagerPlugin().getUserSessionId(ctx()));
                if (actor != null) {
                    lifeCycleMilestoneInstance.approver = actor;
                    lifeCycleMilestoneInstance.save();
                }

                // notification
                List<Actor> actors = new ArrayList<Actor>(Arrays.asList(portfolioEntry.manager, request.requester));
                ActorDao.sendNotification(actors, NotificationCategory.getByCode(Code.APPROVAL), controllers.core.routes.PortfolioEntryGovernanceController
                        .index(portfolioEntry.id).url(),
                        "core.process_transition_request.process_milestone_request.panel.form.accept.approved.notification.title",
                        "core.process_transition_request.process_milestone_request.panel.form.accept.approved.notification.message", portfolioEntry.getName());

                // success message
                successKey = "core.process_transition_request.process_milestone_request.panel.form.accept.approved.successful";

            }

            // accept the request
            request.accepted = true;
            request.reviewDate = new Date();
            request.save();

            // forward the description document
            if (descriptionDocument != null) {
                descriptionDocument.objectId = lifeCycleMilestoneInstance.id;
                descriptionDocument.objectType = LifeCycleMilestoneInstance.class.getName();
                descriptionDocument.save();
            }

            Ebean.commitTransaction();

        } catch (Exception e) {
            Ebean.rollbackTransaction();
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get(successKey));

        return redirect(controllers.core.routes.ProcessTransitionRequestController.reviewMilestoneRequestList(0));
    }

    /**
     * Process the rejection of a milestone request by simply notify the
     * requester.
     * 
     * @param id
     *            the portfolio entry id
     * @param requestId
     *            the request id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_DYNAMIC_PERMISSION)
    public Result rejectMilestoneRequest(Long id, Long requestId) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the request
        ProcessTransitionRequest request = ProcessTransitionRequestDao.getProcessTransitionRequestById(requestId);

        // reject the request
        request.accepted = false;
        request.reviewDate = new Date();
        request.save();

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get("core.process_transition_request.process_milestone_request.panel.form.reject.successful"));

        // send notification (manager + requester)
        List<Actor> actors = new ArrayList<Actor>(Arrays.asList(portfolioEntry.manager, request.requester));
        ActorDao.sendNotification(actors, NotificationCategory.getByCode(Code.REQUEST_REVIEW), controllers.core.routes.PortfolioEntryGovernanceController
                .index(portfolioEntry.id).url(), "core.process_transition_request.process_milestone_request.panel.form.reject.notification.title",
                "core.process_transition_request.process_milestone_request.panel.form.reject.notification.message", portfolioEntry.getName());

        return redirect(controllers.core.routes.ProcessTransitionRequestController.reviewMilestoneRequestList(0));

    }

    private IAttachmentManagerPlugin getAttachmentManagerPlugin() {
        return attachmentManagerPlugin;
    }

    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    private ISecurityService getSecurityService() {
        return securityService;
    }

    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    private Configuration getConfiguration() {
        return configuration;
    }
}
