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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Triple;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import constants.IMafConstants;
import constants.MafDataType;
import controllers.ControllersUtils;
import dao.governance.LifeCycleMilestoneDao;
import dao.governance.LifeCyclePlanningDao;
import dao.governance.LifeCycleProcessDao;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import framework.security.ISecurityService;
import framework.services.account.AccountManagementException;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.plugins.IPluginManagerService;
import framework.services.plugins.IPluginManagerService.IPluginInfo;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.utils.CustomAttributeFormAndDisplayHandler;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.FileAttachmentHelper;
import framework.utils.ISelectableValueHolder;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Menu.ClickableMenuItem;
import framework.utils.Menu.HeaderMenuItem;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.SideBar;
import framework.utils.Table;
import framework.utils.Utilities;
import models.finance.PortfolioEntryBudget;
import models.finance.PortfolioEntryResourcePlan;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.framework_models.common.Attachment;
import models.governance.LifeCycleInstance;
import models.governance.LifeCycleInstancePlanning;
import models.governance.LifeCycleMilestone;
import models.governance.LifeCycleMilestoneInstance;
import models.governance.LifeCycleProcess;
import models.governance.PlannedLifeCycleMilestoneInstance;
import models.pmo.Actor;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryDependency;
import models.pmo.PortfolioEntryType;
import play.Configuration;
import play.Logger;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioEntryExists;
import security.dynamic.PortfolioEntryDynamicHelper;
import services.licensesmanagement.ILicensesManagementService;
import utils.MilestonesTrend;
import utils.form.AttachmentFormData;
import utils.form.EmptyEditFormData;
import utils.form.PortfolioEntryCreateFormData;
import utils.form.PortfolioEntryDependencyFormData;
import utils.form.PortfolioEntryEditFormData;
import utils.form.PortfolioEntryPortfoliosFormData;
import utils.table.AttachmentListView;
import utils.table.GovernanceListView;
import utils.table.PortfolioEntryDependencyListView;
import utils.table.PortfolioListView;

/**
 * The controller which manages a portfolio entry.
 * 
 * @author Pierre-Yves Cloux
 * @author Johann Kohler
 */

public class PortfolioEntryController extends Controller {
    @Inject
    private ILicensesManagementService licensesManagementService;
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private IPluginManagerService pluginManagerService;
    @Inject
    private IAttachmentManagerPlugin attachmentManagerPlugin;
    @Inject
    private ISecurityService securityService;
    @Inject
    private II18nMessagesPlugin messagesPlugin;
    @Inject
    private Configuration configuration;

    private static Logger.ALogger log = Logger.of(PortfolioEntryController.class);

    private static Form<PortfolioEntryCreateFormData> portfolioEntryCreateFormDataStep1 = Form.form(PortfolioEntryCreateFormData.class,
            PortfolioEntryCreateFormData.Step1Group.class);
    private static Form<PortfolioEntryCreateFormData> portfolioEntryCreateFormDataStep2 = Form.form(PortfolioEntryCreateFormData.class,
            PortfolioEntryCreateFormData.Step2Group.class);
    private static Form<PortfolioEntryEditFormData> portfolioEntryEditFormData = Form.form(PortfolioEntryEditFormData.class);
    private static Form<PortfolioEntryPortfoliosFormData> portfoliosFormTemplate = Form.form(PortfolioEntryPortfoliosFormData.class);
    private static Form<EmptyEditFormData> emptyFormTemplate = Form.form(EmptyEditFormData.class);
    private static Form<AttachmentFormData> attachmentFormTemplate = Form.form(AttachmentFormData.class);
    public static Form<PortfolioEntryDependencyFormData> portfolioEntryDependencyFormTemplate = Form.form(PortfolioEntryDependencyFormData.class);

    /**
     * Form to create a new portfolio entry.
     */
    @Restrict({ @Group(IMafConstants.PORTFOLIO_ENTRY_SUBMISSION_PERMISSION) })
    public Result createStep1() {

        if (!getLicensesManagementService().canCreatePortfolioEntry()) {
            Utilities.sendErrorFlashMessage(Msg.get("licenses_management.cannot_create_portfolio_entry"));
        }

        Actor actor = ActorDao.getActorByUidOrCreateDefaultActor(getUserSessionManagerPlugin().getUserSessionId(ctx()));
        Form<PortfolioEntryCreateFormData> filledForm = portfolioEntryCreateFormDataStep1.fill(new PortfolioEntryCreateFormData(actor.id));
        return ok(views.html.core.portfolioentry.portfolio_entry_create_step1.render(filledForm));
    }

    /**
     * Process the creation of a portfolio entry.
     * 
     * step 1: standard attributes
     */
    @Restrict({ @Group(IMafConstants.PORTFOLIO_ENTRY_SUBMISSION_PERMISSION) })
    public Result processCreateStep1() {

        if (!getLicensesManagementService().canCreatePortfolioEntry()) {
            Utilities.sendErrorFlashMessage(Msg.get("licenses_management.cannot_create_portfolio_entry"));
            return redirect(controllers.core.routes.PortfolioEntryController.createStep1());
        }

        Form<PortfolioEntryCreateFormData> boundForm = portfolioEntryCreateFormDataStep1.bindFromRequest();
        if (boundForm.hasErrors()) {
            return badRequest(views.html.core.portfolioentry.portfolio_entry_create_step1.render(boundForm));
        }
        PortfolioEntryCreateFormData newPortfolioEntryFormData = boundForm.get();
        Ebean.beginTransaction();
        PortfolioEntry portfolioEntry = new PortfolioEntry();
        Long attachmentId = null;
        Long portfolioEntryId = null;
        Portfolio portfolio = null;
        try {

            Integer lastGovernanceId = PortfolioEntryDao.getPEAsLastGovernanceId();

            // we set the portfolio entry as deleted until the creation process
            // is full-finished (all steps)
            portfolioEntry.deleted = true;

            portfolioEntry.name = newPortfolioEntryFormData.name;
            portfolioEntry.description = newPortfolioEntryFormData.description;
            portfolioEntry.creationDate = new Date();
            portfolioEntry.manager = ActorDao.getActorById(newPortfolioEntryFormData.manager);
            portfolio = PortfolioDao.getPortfolioById(newPortfolioEntryFormData.portfolio);
            portfolioEntry.portfolios = portfolio != null ? Arrays.asList(portfolio) : new ArrayList<Portfolio>();
            portfolioEntry.isPublic = !newPortfolioEntryFormData.isConfidential;
            PortfolioEntryType portfolioEntryType = PortfolioEntryDao.getPETypeById(newPortfolioEntryFormData.portfolioEntryType);
            portfolioEntry.portfolioEntryType = portfolioEntryType;
            portfolioEntry.governanceId = lastGovernanceId != null ? String.valueOf(lastGovernanceId + 1) : "1";
            log.info("Creation of the entry " + newPortfolioEntryFormData.name);

            portfolioEntry.save();
            portfolioEntryId = portfolioEntry.id;

            createLifeCycleProcessTree(LifeCycleProcessDao.getLCProcessById(newPortfolioEntryFormData.requestedLifeCycleProcess), portfolioEntry);

            // if exists, Creation of the attachment
            if (FileAttachmentHelper.hasFileField("scopeDescription")) {
                attachmentId = FileAttachmentHelper.saveAsAttachement("scopeDescription", PortfolioEntry.class, portfolioEntry.id,
                        getAttachmentManagerPlugin());
                log.info("Attachment " + attachmentId + " created for entry " + portfolioEntryId);
            }

            Ebean.commitTransaction();
        } catch (Exception e) {
            Ebean.rollbackTransaction();
            try {
                // Attempt to rollback the attachment creation
                if (attachmentId != null) {
                    FileAttachmentHelper.deleteFileAttachment(attachmentId, getAttachmentManagerPlugin(), getUserSessionManagerPlugin());
                }
            } catch (Exception exp) {
                Logger.error("impossible to rollback the attachment creation", exp);
            }
            log.error(String.format("Failure while creating the portfolio entry", newPortfolioEntryFormData.toString()));
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getMessagesPlugin());
        }

        // Check if the PortfolioEntry object has some custom attributes (if yes
        // go to step 2, else end)
        if (CustomAttributeFormAndDisplayHandler.hasCustomAttributes(PortfolioEntry.class)) {

            if (log.isDebugEnabled()) {
                log.debug("The PortFolioEntry object has some custom attributes, forward to step2");
            }

            newPortfolioEntryFormData.id = portfolioEntryId;
            Form<PortfolioEntryCreateFormData> filledForm = portfolioEntryCreateFormDataStep2.fill(newPortfolioEntryFormData);
            CustomAttributeFormAndDisplayHandler.fillWithValues(filledForm, PortfolioEntry.class, newPortfolioEntryFormData.id);
            return ok(views.html.core.portfolioentry.portfolio_entry_create_step2.render(filledForm));

        } else {

            if (log.isDebugEnabled()) {
                log.debug("No custom attribute defined for entry creation");
            }

            return finalizeCreateProcess(portfolioEntry);
        }
    }

    /**
     * Create the tree of appropriate LifeCycleObjects.
     * <ul>
     * <li>A process instance {@link LifeCycleInstance}</li>
     * <li>A planning {@link LifeCycleInstancePlanning} (the first one)</li>
     * <li>A list of {@link LifeCycleMilestoneInstance} (one for each possible
     * milestone)</li>
     * <li>A list of {@link PlannedLifeCycleMilestoneInstance} (one for each
     * milestone)</li>
     * </ul>
     * 
     * @param lifeCycleProcess
     *            the life cycle process
     * @param portfolioEntry
     *            the portfolio entry
     */
    public static void createLifeCycleProcessTree(LifeCycleProcess lifeCycleProcess, PortfolioEntry portfolioEntry) {

        // Creation of the life cycle tree
        LifeCycleInstance lifeCycleInstance = new LifeCycleInstance();
        lifeCycleInstance.defaults();
        lifeCycleInstance.portfolioEntry = portfolioEntry;
        LifeCycleProcess requestedLifeCycleProcess = lifeCycleProcess;
        lifeCycleInstance.lifeCycleProcess = requestedLifeCycleProcess;

        if (log.isDebugEnabled()) {
            log.debug("Selected lifeCycleProcess is " + requestedLifeCycleProcess);
        }

        // Creation of the instance planning
        List<LifeCycleInstancePlanning> plannings = new ArrayList<LifeCycleInstancePlanning>();
        LifeCycleInstancePlanning planning = new LifeCycleInstancePlanning();
        planning.version = 1;
        planning.isFrozen = false;
        planning.creationDate = new Date();
        planning.lifeCycleInstance = lifeCycleInstance;
        planning.plannedLifeCycleMilestoneInstance = new ArrayList<PlannedLifeCycleMilestoneInstance>();
        planning.portfolioEntryBudget = new PortfolioEntryBudget();
        planning.portfolioEntryResourcePlan = new PortfolioEntryResourcePlan();
        planning.save();
        plannings.add(planning);

        // Creation of the milestone instances & plannings for each milestone
        // instance
        for (LifeCycleMilestone milestone : requestedLifeCycleProcess.lifeCycleMilestones) {
            // Planned instance
            PlannedLifeCycleMilestoneInstance plannedLifeCycleMilestoneInstance = new PlannedLifeCycleMilestoneInstance();
            plannedLifeCycleMilestoneInstance.lifeCycleMilestone = milestone;
            plannedLifeCycleMilestoneInstance.lifeCycleInstancePlanning = planning;
            plannedLifeCycleMilestoneInstance.save();
            if (log.isDebugEnabled()) {
                log.debug("Added planned life cycle milestone for " + milestone);
            }
        }

        lifeCycleInstance.lifeCycleInstancePlannings = plannings;
        lifeCycleInstance.save();

        portfolioEntry.activeLifeCycleInstance = lifeCycleInstance;
        portfolioEntry.save();
    }

    /**
     * Process the creation of a portfolio entry.
     * 
     * step 1: custom attributes
     */
    @Restrict({ @Group(IMafConstants.PORTFOLIO_ENTRY_SUBMISSION_PERMISSION) })
    public Result processCreateStep2() {

        if (!getLicensesManagementService().canCreatePortfolioEntry()) {
            Utilities.sendErrorFlashMessage(Msg.get("licenses_management.cannot_create_portfolio_entry"));
            return redirect(controllers.core.routes.PortfolioEntryController.createStep1());
        }

        // bind the form
        Form<PortfolioEntryCreateFormData> boundForm = portfolioEntryCreateFormDataStep2.bindFromRequest();

        // check the custom attribute values
        if (CustomAttributeFormAndDisplayHandler.validateValues(boundForm, PortfolioEntry.class)) {
            return badRequest(views.html.core.portfolioentry.portfolio_entry_create_step2.render(boundForm));
        }

        PortfolioEntryCreateFormData newPortfolioEntryFormData = boundForm.get();

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, PortfolioEntry.class, newPortfolioEntryFormData.id);

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEDeletedById(newPortfolioEntryFormData.id);

        return finalizeCreateProcess(portfolioEntry);
    }

    /**
     * Finalize the portfolio entry create process (if success).
     * 
     * @param portfolioEntry
     *            the created portfolio entry
     */
    private Result finalizeCreateProcess(PortfolioEntry portfolioEntry) {

        // we set the deleted flag to false
        portfolioEntry.deleted = false;
        portfolioEntry.save();

        getLicensesManagementService().updateConsumedPortfolioEntries();

        // send a notification to the portfolio manager (if it exists)
        if (portfolioEntry.portfolios != null && portfolioEntry.portfolios.size() > 0) {
            Portfolio portfolio = portfolioEntry.portfolios.get(0);
            ActorDao.sendNotification(portfolio.manager, NotificationCategory.getByCode(Code.PORTFOLIO_ENTRY),
                    controllers.core.routes.PortfolioEntryController.view(portfolioEntry.id, 0).url(), "core.portfolio_entry.create.notification.title",
                    "core.portfolio_entry.create.notification.message", portfolio.name);
        }

        Utilities.sendSuccessFlashMessage(Messages.get("core.portfolio_entry.create.success.message"));

        // send a notification to the initiative manager (if he is not the
        // current one)
        Actor actor = ActorDao.getActorByUid(getUserSessionManagerPlugin().getUserSessionId(ctx()));
        if (!actor.id.equals(portfolioEntry.manager.id)) {
            ActorDao.sendNotification(portfolioEntry.manager, NotificationCategory.getByCode(Code.PORTFOLIO_ENTRY),
                    controllers.core.routes.PortfolioEntryController.view(portfolioEntry.id, 0).url(), "core.portfolio_entry.create.notification.title",
                    "core.portfolio_entry.create.notification.manager.message");

        }

        return redirect(controllers.core.routes.PortfolioEntryController.view(portfolioEntry.id, 0));

    }

    /**
     * Display the overview (cockpit) of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_VIEW_DYNAMIC_PERMISSION)
    public Result overview(Long id) {

        // if the user is not permitted to see the details of the PE, then
        // redirect him to the view page
        if (!getSecurityService().dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION, "")) {
            return redirect(controllers.core.routes.PortfolioEntryController.view(id, 0));
        }

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the next milestones
        List<GovernanceListView> governanceListView = new ArrayList<GovernanceListView>();
        List<PlannedLifeCycleMilestoneInstance> lastPlannedMilestoneInstances = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceNotApprovedAsListOfPE(id);
        for (PlannedLifeCycleMilestoneInstance lastPlannedMilestoneInstance : lastPlannedMilestoneInstances) {
            governanceListView.add(new GovernanceListView(lastPlannedMilestoneInstance));
        }
        Set<String> hideColumnsForGovernance = new HashSet<String>();
        hideColumnsForGovernance.add("requestActionLink");
        Table<GovernanceListView> milestonesTable = GovernanceListView.templateTable.fill(governanceListView, hideColumnsForGovernance);

        // Milestones trend
        LifeCycleInstance activeLifeCycleProcessInstance = portfolioEntry.activeLifeCycleInstance;
        List<LifeCycleMilestoneInstance> milestoneInstances = LifeCycleMilestoneDao
                .getLCMilestoneInstanceAsListByLCInstance(activeLifeCycleProcessInstance.id);
        MilestonesTrend milestonesTrend = new MilestonesTrend(activeLifeCycleProcessInstance.lifeCycleProcess.lifeCycleMilestones, milestoneInstances,
                getMessagesPlugin(), getSecurityService());

        return ok(views.html.core.portfolioentry.portfolio_entry_overview.render(portfolioEntry, milestonesTable, milestonesTrend));
    }

    /**
     * Display the details of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     * @param attachmentPage
     *            the current page for the attachment table
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_VIEW_DYNAMIC_PERMISSION)
    public Result view(Long id, Integer attachmentPage) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the last milestone
        LifeCycleMilestoneInstance lastMilestone = portfolioEntry.lastApprovedLifeCycleMilestoneInstance;

        // get the portfolios
        List<Portfolio> portfolios = portfolioEntry.portfolios;

        List<PortfolioListView> portfoliosView = new ArrayList<PortfolioListView>();
        for (Portfolio portfolio : portfolios) {
            portfoliosView.add(new PortfolioListView(portfolio));
        }

        Table<PortfolioListView> portfolioFilledTable = PortfolioListView.templateTable.fill(portfoliosView, PortfolioListView.hideStakeholderTypeColumn);

        // get the dependencies
        List<PortfolioEntryDependency> dependencies = PortfolioEntryDao.getPEDependencyAsList(id);

        List<PortfolioEntryDependencyListView> portfolioEntryDependenciesListView = new ArrayList<PortfolioEntryDependencyListView>();
        for (PortfolioEntryDependency dependency : dependencies) {
            portfolioEntryDependenciesListView.add(new PortfolioEntryDependencyListView(id, dependency));
        }

        Set<String> columnsToHideForDependencies = new HashSet<String>();
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
            columnsToHideForDependencies.add("deleteActionLink");
        }

        Table<PortfolioEntryDependencyListView> dependenciesFilledTable = PortfolioEntryDependencyListView.templateTable
                .fill(portfolioEntryDependenciesListView, columnsToHideForDependencies);

        /*
         * Get the attachments
         */

        // authorize the attachments
        FileAttachmentHelper.getFileAttachmentsForDisplay(PortfolioEntry.class, id, getAttachmentManagerPlugin(), getUserSessionManagerPlugin());

        // create the table
        Pagination<Attachment> attachmentPagination = new Pagination<Attachment>(
                Attachment.getAttachmentsFromObjectTypeAndObjectIdAsExpressionList(PortfolioEntry.class, id), 5,
                getConfiguration().getInt("maf.number_page_links"));
        attachmentPagination.setCurrentPage(attachmentPage);
        attachmentPagination.setPageQueryName("attachmentPage");

        List<AttachmentListView> attachmentsListView = new ArrayList<AttachmentListView>();
        for (Attachment attachment : attachmentPagination.getListOfObjects()) {
            attachmentsListView
                    .add(new AttachmentListView(attachment, controllers.core.routes.PortfolioEntryController.deleteAttachment(id, attachment.id).url()));
        }

        Set<String> hideColumns = new HashSet<String>();
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumns.add("removeActionLink");
        }

        Table<AttachmentListView> attachmentFilledTable = AttachmentListView.templateTable.fill(attachmentsListView, hideColumns);

        return ok(views.html.core.portfolioentry.portfolio_entry_view.render(portfolioEntry, lastMilestone, portfolioFilledTable, dependenciesFilledTable,
                attachmentFilledTable, attachmentPagination));

    }

    /**
     * Form to edit the standard attributes of portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result edit(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // construct the form
        Form<PortfolioEntryEditFormData> portfolioEntryForm = portfolioEntryEditFormData.fill(new PortfolioEntryEditFormData(portfolioEntry));

        return ok(views.html.core.portfolioentry.portfolio_entry_edit.render(portfolioEntry, portfolioEntryForm, PortfolioEntryDao.getPETypeActiveAsVH()));
    }

    /**
     * Form to edit the custom attributes of portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result editCustomAttr(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // construct the form
        Form<EmptyEditFormData> customAttributeForm = emptyFormTemplate.fill(new EmptyEditFormData(portfolioEntry.id));

        // add the custom attributes
        CustomAttributeFormAndDisplayHandler.fillWithValues(customAttributeForm, PortfolioEntry.class, id);

        return ok(views.html.core.portfolioentry.portfolio_entry_custom_attr_edit.render(portfolioEntry, customAttributeForm));
    }

    /**
     * Process the update of the standard attributes of a portfolio entry.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processEdit() {

        // bind the form
        Form<PortfolioEntryEditFormData> boundForm = portfolioEntryEditFormData.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.portfolioentry.portfolio_entry_edit.render(portfolioEntry, boundForm, PortfolioEntryDao.getPETypeActiveAsVH()));
        }

        PortfolioEntryEditFormData portfolioEntryFormData = boundForm.get();

        // save the portfolio entry
        PortfolioEntry updPortfolioEntry = PortfolioEntryDao.getPEById(portfolioEntryFormData.id);
        portfolioEntryFormData.fill(updPortfolioEntry);
        updPortfolioEntry.update();
        // updPortfolioEntry.saveManyToManyAssociations("deliveryUnits");

        // update the licenses number (because the flag is archived is used for
        // the computation and could be modified)
        getLicensesManagementService().updateConsumedPortfolioEntries();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.edit.successful"));

        return redirect(controllers.core.routes.PortfolioEntryController.view(portfolioEntryFormData.id, 0));
    }

    /**
     * Process the update of the custom attributes of a portfolio entry.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processEditCustomAttr() {

        // bind the form
        Form<EmptyEditFormData> boundForm = emptyFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, PortfolioEntry.class)) {
            return ok(views.html.core.portfolioentry.portfolio_entry_custom_attr_edit.render(portfolioEntry, boundForm));
        }

        EmptyEditFormData emptyEditFormData = boundForm.get();

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, PortfolioEntry.class, emptyEditFormData.id);

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.edit.successful"));

        return redirect(controllers.core.routes.PortfolioEntryController.view(emptyEditFormData.id, 0));
    }

    /**
     * Edit the plugin configuration for the portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result pluginConfig(Long id) {
        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the plugins which are registered for portfolio entry
        List<Triple<Long, String, IPluginInfo>> pluginInfos = getPluginManagerService()
                .getPluginSupportingRegistrationForDataType(MafDataType.getPortfolioEntry());

        return ok(views.html.core.portfolioentry.portfolio_entry_plugin_config.render(portfolioEntry, pluginInfos));
    }

    /**
     * Delete a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DELETE_DYNAMIC_PERMISSION)
    public Result delete(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // set the delete flag to true
        portfolioEntry.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.delete.successful", portfolioEntry.portfolioEntryType.getName().toLowerCase()));

        // update the licenses number
        getLicensesManagementService().updateConsumedPortfolioEntries();

        return redirect(controllers.core.routes.RoadmapController.index());
    }

    /**
     * Form to edit the selected portfolios of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result editPortfolios(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // construct the form
        Form<PortfolioEntryPortfoliosFormData> portfolioEntryPortfoliosForm = portfoliosFormTemplate
                .fill(new PortfolioEntryPortfoliosFormData(portfolioEntry));

        // get the portfolios value holders
        DefaultSelectableValueHolderCollection<Long> portfolios = PortfolioDao.getPortfolioActiveAsVH();

        return ok(views.html.core.portfolioentry.portfolios_edit.render(portfolioEntry, portfolios, portfolioEntryPortfoliosForm));
    }

    /**
     * Process the update of the selected portfolios of a portfolio entry.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processEditPortfolios() {

        // bind the form
        Form<PortfolioEntryPortfoliosFormData> boundForm = portfoliosFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {

            // get the portfolioEntry
            Long id = Long.valueOf(boundForm.data().get("id"));
            PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

            // get the portfolios value holders
            DefaultSelectableValueHolderCollection<Long> portfolios = PortfolioDao.getPortfolioActiveAsVH();

            return ok(views.html.core.portfolioentry.portfolios_edit.render(portfolioEntry, portfolios, boundForm));
        }

        PortfolioEntryPortfoliosFormData portfolioEntryPortfoliosFormData = boundForm.get();

        PortfolioEntry updPortfolioEntry = PortfolioEntryDao.getPEById(portfolioEntryPortfoliosFormData.id);
        portfolioEntryPortfoliosFormData.fill(updPortfolioEntry);
        updPortfolioEntry.save();
        // updPortfolioEntry.saveManyToManyAssociations("portfolios");

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.editportfolios.successful"));

        return redirect(controllers.core.routes.PortfolioEntryController.view(portfolioEntryPortfoliosFormData.id, 0));
    }

    /**
     * Form to create (add) a new attachment.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result createAttachment(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // construct the form
        Form<AttachmentFormData> attachmentForm = attachmentFormTemplate.fill(new AttachmentFormData(portfolioEntry.id));

        return ok(views.html.core.portfolioentry.portfolio_entry_attachment_create.render(portfolioEntry, attachmentForm));
    }

    /**
     * Process the creation of the attachment.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processCreateAttachment() {

        Form<AttachmentFormData> boundForm = attachmentFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors()) {
            return badRequest(views.html.core.portfolioentry.portfolio_entry_attachment_create.render(portfolioEntry, boundForm));
        }

        // store the document
        try {
            FileAttachmentHelper.saveAsAttachement("document", PortfolioEntry.class, portfolioEntry.id, getAttachmentManagerPlugin());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getMessagesPlugin());
        }

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.attachment.new.successful"));

        return redirect(controllers.core.routes.PortfolioEntryController.view(portfolioEntry.id, 0));
    }

    /**
     * Delete an attachment.
     * 
     * @param id
     *            the portfolio entry id
     * @param attachmentId
     *            the attachment id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result deleteAttachment(Long id, Long attachmentId) {

        // get the attachment
        Attachment attachment = Attachment.getAttachmentFromId(attachmentId);

        // security: the portfolioEntry must be related to the object
        if (!attachment.objectId.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // delete the attachment
        FileAttachmentHelper.deleteFileAttachment(attachmentId, getAttachmentManagerPlugin(), getUserSessionManagerPlugin());

        attachment.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.attachment.delete"));

        return redirect(controllers.core.routes.PortfolioEntryController.view(id, 0));
    }

    /**
     * Form to add a dependency.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result addDependency(Long id) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        return ok(views.html.core.portfolioentry.portfolio_entry_add_dependency.render(portfolioEntry, portfolioEntryDependencyFormTemplate));
    }

    /**
     * Process the form to add a dependency.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processAddDependency() {

        // bind the form
        Form<PortfolioEntryDependencyFormData> boundForm = portfolioEntryDependencyFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.portfolioentry.portfolio_entry_add_dependency.render(portfolioEntry, boundForm));
        }

        PortfolioEntryDependencyFormData portfolioEntryDependencyFormData = boundForm.get();

        PortfolioEntryDependency portfolioEntryDependency = portfolioEntryDependencyFormData.get();

        // check the depending PE is not the current
        if (portfolioEntryDependency.getSourcePortfolioEntry().id.equals(portfolioEntryDependency.getDestinationPortfolioEntry().id)) {
            boundForm.reject("dependingId", Msg.get("object.portfolio_entry.dependency.same.error"));
            return ok(views.html.core.portfolioentry.portfolio_entry_add_dependency.render(portfolioEntry, boundForm));
        }

        // check the release is not already assigned.
        if (PortfolioEntryDao.getPEDependencyById(portfolioEntryDependency.getSourcePortfolioEntry().id,
                portfolioEntryDependency.getDestinationPortfolioEntry().id, portfolioEntryDependency.getPortfolioEntryDependencyType().id) != null) {
            boundForm.reject("dependingId", Msg.get("object.portfolio_entry.dependency.already.error"));
            return ok(views.html.core.portfolioentry.portfolio_entry_add_dependency.render(portfolioEntry, boundForm));
        }

        portfolioEntryDependency.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.dependency.add.successful"));

        return redirect(controllers.core.routes.PortfolioEntryController.view(portfolioEntry.id, 0));
    }

    /**
     * Delete a dependency.
     * 
     * @param id
     *            the portfolio entry id
     * @param peDepSourceId
     *            the portfolio entry dependency id (source part)
     * @param peDepDestinationId
     *            the portfolio entry dependency id (destination part)
     * @param peDepTypeId
     *            the portfolio entry dependency id (type part)
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result deleteDependency(Long id, Long peDepSourceId, Long peDepDestinationId, Long peDepTypeId) {

        PortfolioEntryDependency dependency = PortfolioEntryDao.getPEDependencyById(peDepSourceId, peDepDestinationId, peDepTypeId);

        // security: the portfolioEntry must be related to the object
        if (!dependency.getSourcePortfolioEntry().id.equals(id) && !dependency.getDestinationPortfolioEntry().id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        dependency.delete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.dependency.delete"));

        return redirect(controllers.core.routes.PortfolioEntryController.view(id, 0));
    }

    /**
     * Search from portfolio entries.
     */
    @SubjectPresent
    public Result search() {

        try {

            String query = request().queryString().get("query") != null ? request().queryString().get("query")[0] : null;
            String value = request().queryString().get("value") != null ? request().queryString().get("value")[0] : null;

            if (query != null) {

                ISelectableValueHolderCollection<Long> portfolioEntries = new DefaultSelectableValueHolderCollection<Long>();

                Expression expression = Expr.or(Expr.ilike("name", query + "%"), Expr.ilike("governanceId", query + "%"));

                for (PortfolioEntry portfolioEntry : PortfolioEntryDynamicHelper.getPortfolioEntriesViewAllowedAsQuery(expression, getSecurityService())
                        .findList()) {
                    portfolioEntries.add(new DefaultSelectableValueHolder<Long>(portfolioEntry.id, portfolioEntry.getName()));
                }

                return ok(Utilities.marshallAsJson(portfolioEntries.getValues()));
            }

            if (value != null) {
                PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(Long.valueOf(value));
                ISelectableValueHolder<Long> portfolioEntryAsVH = new DefaultSelectableValueHolder<Long>(portfolioEntry.id, portfolioEntry.getName());
                return ok(Utilities.marshallAsJson(portfolioEntryAsVH, 0));
            }

            return ok(Json.newObject());

        } catch (AccountManagementException e) {
            return badRequest();
        }

    }

    /**
     * Construct the portfolio entry icons bar depending of the sign-in user
     * permissions.
     * 
     * @param isDataSyndicationActive
     *            true if the data syndication is active (conf)
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param currentType
     *            the current menu item type, useful to select the correct item
     * @param securityService
     *            the security service
     */
    public static SideBar getIconsBar(Boolean isDataSyndicationActive, Long portfolioEntryId, MenuItemType currentType, ISecurityService securityService) {

        SideBar sideBar = new SideBar();

        if (securityService.dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION, "", portfolioEntryId)) {
            sideBar.addMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.overview.label",
                    controllers.core.routes.PortfolioEntryController.overview(portfolioEntryId), "glyphicons glyphicons-radar",
                    currentType.equals(MenuItemType.OVERVIEW)));
        }

        sideBar.addMenuItem(
                new ClickableMenuItem("core.portfolio_entry.sidebar.view.label", controllers.core.routes.PortfolioEntryController.view(portfolioEntryId, 0),
                        "glyphicons glyphicons-zoom-in", currentType.equals(MenuItemType.VIEW)));

        if (securityService.dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION, "", portfolioEntryId)) {

            HeaderMenuItem financialMenu = new HeaderMenuItem("core.portfolio_entry.sidebar.financial.label", "glyphicons glyphicons-coins",
                    currentType.equals(MenuItemType.FINANCIAL));

            financialMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.financial.details.label",
                    controllers.core.routes.PortfolioEntryFinancialController.details(portfolioEntryId), "glyphicons glyphicons-zoom-in", false));

            financialMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.financial.status.label",
                    controllers.core.routes.PortfolioEntryFinancialController.status(portfolioEntryId), "glyphicons glyphicons-charts", false));

            sideBar.addMenuItem(financialMenu);
        }

        sideBar.addMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.stakeholders.label",
                controllers.core.routes.PortfolioEntryStakeholderController.index(portfolioEntryId), "glyphicons glyphicons-group",
                currentType.equals(MenuItemType.STAKEHOLDERS)));

        if (securityService.dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION, "", portfolioEntryId)) {

            sideBar.addMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.governance.label",
                    controllers.core.routes.PortfolioEntryGovernanceController.index(portfolioEntryId), "glyphicons glyphicons-cluster",
                    currentType.equals(MenuItemType.GOVERNANCE)));

            HeaderMenuItem deliveryMenu = new HeaderMenuItem("core.portfolio_entry.sidebar.delivery.label", "glyphicons glyphicons-cargo",
                    currentType.equals(MenuItemType.DELIVERY));

            deliveryMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.delivery.requirements.label",
                    controllers.core.routes.PortfolioEntryDeliveryController.requirements(portfolioEntryId), "glyphicons glyphicons-log-book", false));

            deliveryMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.delivery.requirements_status.label",
                    controllers.core.routes.PortfolioEntryDeliveryController.requirementsStatus(portfolioEntryId), "glyphicons glyphicons-charts", false));

            deliveryMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.delivery.iterations.label",
                    controllers.core.routes.PortfolioEntryDeliveryController.iterations(portfolioEntryId), "glyphicons glyphicons-history", false));

            deliveryMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.delivery.releases.label",
                    controllers.core.routes.PortfolioEntryDeliveryController.releases(portfolioEntryId), "glyphicons glyphicons-git-branch", false));

            sideBar.addMenuItem(deliveryMenu);

            HeaderMenuItem planningMenu = new HeaderMenuItem("core.portfolio_entry.sidebar.planning.label", "glyphicons glyphicons-calendar",
                    currentType.equals(MenuItemType.PLANNING));

            planningMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.planning.overview.label",
                    controllers.core.routes.PortfolioEntryPlanningController.overview(portfolioEntryId), "glyphicons glyphicons-radar", false));

            planningMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.planning.packages.label",
                    controllers.core.routes.PortfolioEntryPlanningController.packages(portfolioEntryId), "glyphicons glyphicons-package", false));

            planningMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.planning.resources.label",
                    controllers.core.routes.PortfolioEntryPlanningController.resources(portfolioEntryId), "glyphicons glyphicons-address-book", false));

            sideBar.addMenuItem(planningMenu);

            HeaderMenuItem reportingMenu = new HeaderMenuItem("core.portfolio_entry.sidebar.status_reporting.label", "glyphicons glyphicons-notes",
                    currentType.equals(MenuItemType.REPORTING));

            reportingMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.status_reporting.registers.label",
                    controllers.core.routes.PortfolioEntryStatusReportingController.registers(portfolioEntryId, 0, 0, 0, false, false),
                    "glyphicons glyphicons-inbox", false));

            reportingMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.status_reporting.events.label",
                    controllers.core.routes.PortfolioEntryStatusReportingController.events(portfolioEntryId), "glyphicons glyphicons-bullhorn", false));

            reportingMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.status_reporting.timesheets.label",
                    controllers.core.routes.PortfolioEntryStatusReportingController.timesheets(portfolioEntryId), "glyphicons glyphicons-clock", false));

            sideBar.addMenuItem(reportingMenu);

        }

        if (securityService.dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION, "", portfolioEntryId)) {

            HeaderMenuItem integrationMenu = new HeaderMenuItem("core.portfolio_entry.sidebar.integration.label", "glyphicons glyphicons-cloud",
                    currentType.equals(MenuItemType.INTEGRATION));
            integrationMenu.setIsImportant(true);
            sideBar.addMenuItem(integrationMenu);

            integrationMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.integration.plugins.label",
                    controllers.core.routes.PortfolioEntryController.pluginConfig(portfolioEntryId), "glyphicons glyphicons-remote-control", false));

            if (isDataSyndicationActive) {
                integrationMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.integration.data_syndication.label",
                        controllers.core.routes.PortfolioEntryDataSyndicationController.index(portfolioEntryId), "glyphicons glyphicons-share-alt", false));
            }

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
        OVERVIEW, VIEW, FINANCIAL, STAKEHOLDERS, GOVERNANCE, PLANNING, DELIVERY, REPORTING, INTEGRATION;
    }

    /**
     * Get the licenses management service.
     */
    private ILicensesManagementService getLicensesManagementService() {
        return licensesManagementService;
    }

    /**
     * Get the user session manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    /**
     * Get the plugin manager service.
     */
    private IPluginManagerService getPluginManagerService() {
        return pluginManagerService;
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
    private II18nMessagesPlugin getMessagesPlugin() {
        return messagesPlugin;
    }
    private Configuration getConfiguration() {
        return configuration;
    }
}
