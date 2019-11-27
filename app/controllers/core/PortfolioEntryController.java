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

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
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
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.custom_attribute.ICustomAttributeManagerService;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.plugins.IPluginManagerService;
import framework.services.plugins.IPluginManagerService.IPluginInfo;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.utils.*;
import framework.utils.Menu.ClickableMenuItem;
import framework.utils.Menu.HeaderMenuItem;
import models.common.BizDockModel;
import models.finance.PortfolioEntryBudget;
import models.finance.PortfolioEntryResourcePlan;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.framework_models.common.Attachment;
import models.governance.*;
import models.pmo.Actor;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryDependency;
import org.apache.commons.lang3.tuple.Triple;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioEntryExists;
import security.dynamic.PortfolioEntryDynamicHelper;
import services.licensesmanagement.ILicensesManagementService;
import services.tableprovider.ITableProvider;
import utils.MilestonesTrend;
import utils.form.*;
import utils.table.AttachmentListView;
import utils.table.GovernanceListView;
import utils.table.PortfolioEntryDependencyListView;
import utils.table.PortfolioListView;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

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
    @Inject
    private IAccountManagerPlugin accountManagerPlugin;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private INotificationManagerPlugin notificationManagerService;
    @Inject
    private ITableProvider tableProvider;
    @Inject
    private ICustomAttributeManagerService customAttributeManagerService;
    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;

    private static Logger.ALogger log = Logger.of(PortfolioEntryController.class);

    private static Form<PortfolioEntryCreateFormData> portfolioEntryCreateFormTemplate = Form.form(PortfolioEntryCreateFormData.class);
    private static Form<PortfolioEntryEditFormData> portfolioEntryEditFormData = Form.form(PortfolioEntryEditFormData.class);
    private static Form<PortfolioEntryPortfoliosFormData> portfoliosFormTemplate = Form.form(PortfolioEntryPortfoliosFormData.class);
    private static Form<AttachmentFormData> attachmentFormTemplate = Form.form(AttachmentFormData.class);
    private static Form<PortfolioEntryDependencyFormData> portfolioEntryDependencyFormTemplate = Form.form(PortfolioEntryDependencyFormData.class);

    /**
     * Form to create a new portfolio entry.
     * 
     * @param isRelease
     *            true if the portfolio entry is a release, else an initiative
     */
    @Restrict({ @Group(IMafConstants.PORTFOLIO_ENTRY_SUBMISSION_PERMISSION), @Group(IMafConstants.RELEASE_SUBMISSION_PERMISSION) })
    public Result create(boolean isRelease) {

        try {
            if (isRelease && !securityService.restrict(IMafConstants.RELEASE_SUBMISSION_PERMISSION)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }
            if (!isRelease && !securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_SUBMISSION_PERMISSION)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getMessagesPlugin());
        }

        if (!getLicensesManagementService().canCreatePortfolioEntry()) {
            Utilities.sendErrorFlashMessage(Msg.get("licenses_management.cannot_create_portfolio_entry"));
        }

        Actor actor = ActorDao.getActorByUidOrCreateDefaultActor(this.getAccountManagerPlugin(), getUserSessionManagerPlugin().getUserSessionId(ctx()));
        Form<PortfolioEntryCreateFormData> filledForm = portfolioEntryCreateFormTemplate.fill(new PortfolioEntryCreateFormData(isRelease, actor.id));
        return ok(views.html.core.portfolioentry.portfolio_entry_create.render(filledForm, isRelease));
    }

    /**
     * Process the creation of a portfolio entry.
     */
    @Restrict({ @Group(IMafConstants.PORTFOLIO_ENTRY_SUBMISSION_PERMISSION), @Group(IMafConstants.RELEASE_SUBMISSION_PERMISSION) })
    public Result processCreate() {

        if (!getLicensesManagementService().canCreatePortfolioEntry()) {
            Utilities.sendErrorFlashMessage(Msg.get("licenses_management.cannot_create_portfolio_entry"));
            return redirect(controllers.core.routes.PortfolioEntryController.create(false));
        }

        Form<PortfolioEntryCreateFormData> boundForm = portfolioEntryCreateFormTemplate.bindFromRequest();

        boolean isRelease = Boolean.parseBoolean(boundForm.data().get("isRelease"));

        Result resultException = checkReleasePermission(isRelease);
        if (resultException != null) return resultException;

        this.getCustomAttributeManagerService().validateValues(boundForm, PortfolioEntry.class);
        if (boundForm.hasErrors()) {
            return badRequest(views.html.core.portfolioentry.portfolio_entry_create.render(boundForm, isRelease));
        }

        String keyPrefix;
        if (isRelease) {
            keyPrefix = "core.portfolio_entry.create.release.";
        } else {
            keyPrefix = "core.portfolio_entry.create.initiative.";
        }

        PortfolioEntryCreateFormData portfolioEntryCreateFormData = boundForm.get();

        PortfolioEntry portfolioEntry = new PortfolioEntry();
        Long attachmentId = null;
        List<Portfolio> portfolios;

        Ebean.beginTransaction();
        try {

            portfolioEntryCreateFormData.fill(portfolioEntry);
            portfolios = portfolioEntryCreateFormData.portfolios == null ? null : Arrays.stream(portfolioEntryCreateFormData.portfolios).map(PortfolioDao::getPortfolioById).collect(Collectors.toList());
            portfolioEntry.save();

            // Get the request life cycle process
            LifeCycleProcess requestedLifeCycleProcess = LifeCycleProcessDao.getLCProcessById(portfolioEntryCreateFormData.requestedLifeCycleProcess);

            createLifeCycleProcessTree(portfolioEntry, requestedLifeCycleProcess);

            // if exists, Creation of the attachment
            if (FileAttachmentHelper.hasFileField("scopeDescription")) {
                attachmentId = FileAttachmentHelper.saveAsAttachement("scopeDescription", PortfolioEntry.class, portfolioEntry.id,
                        getAttachmentManagerPlugin());
            }

            Ebean.commitTransaction();

        } catch (Exception e) {

            Ebean.rollbackTransaction();

            try {
                // Attempt to rollback the attachment creation
                if (attachmentId != null) {
                    FileAttachmentHelper.deleteFileAttachment(attachmentId, getAttachmentManagerPlugin(), getUserSessionManagerPlugin());
                }
            } catch (Exception e1) {
                Logger.error("impossible to rollback the attachment creation", e1);
            }

            log.error(String.format("Failure while creating the portfolio entry %s", portfolioEntryCreateFormData.toString()));

            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getMessagesPlugin());
        }

        this.getCustomAttributeManagerService().validateAndSaveValues(boundForm, PortfolioEntry.class, portfolioEntry.id);

        getLicensesManagementService().updateConsumedPortfolioEntries();

        // send a notification to the portfolio managers (if it exists)
        if (portfolios != null) {
            portfolios.forEach(portfolio -> ActorDao.sendNotification(
                    this.getNotificationManagerService(),
                    this.getI18nMessagesPlugin(),
                    portfolio.manager,
                    NotificationCategory.getByCode(Code.PORTFOLIO_ENTRY),
                    controllers.core.routes.PortfolioEntryController.view(portfolioEntry.id, 0).url(),
                    keyPrefix + "notification.title",
                    keyPrefix + "notification.message",
                    portfolio.name
            ));
        }

        // send a notification to the initiative manager (if he is not the
        // current one)
        Actor actor = ActorDao.getActorByUid(getUserSessionManagerPlugin().getUserSessionId(ctx()));
        if (!actor.id.equals(portfolioEntry.manager.id)) {
            ActorDao.sendNotification(this.getNotificationManagerService(), this.getI18nMessagesPlugin(), portfolioEntry.manager,
                    NotificationCategory.getByCode(Code.PORTFOLIO_ENTRY), controllers.core.routes.PortfolioEntryController.view(portfolioEntry.id, 0).url(),
                    keyPrefix + "notification.title", keyPrefix + "notification.manager.message");

        }

        Utilities.sendSuccessFlashMessage(Messages.get(keyPrefix + "success.message"));

        return redirect(controllers.core.routes.PortfolioEntryController.view(portfolioEntry.id, 0));

    }

    private Result checkReleasePermission(boolean isRelease) {
        try {
            if (isRelease && !securityService.restrict(IMafConstants.RELEASE_SUBMISSION_PERMISSION)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }
            if (!isRelease && !securityService.restrict(IMafConstants.PORTFOLIO_ENTRY_SUBMISSION_PERMISSION)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getMessagesPlugin());
        }
        return null;
    }

    /**
     * Create the life cycle process tree for a portfolio entry.
     * 
     * @param portfolioEntry
     *            the portfolio entry
     * @param requestedLifeCycleProcess
     *            the request life cycle process
     */
    public static void createLifeCycleProcessTree(PortfolioEntry portfolioEntry, LifeCycleProcess requestedLifeCycleProcess) {

        // Create the life cycle tree
        LifeCycleInstance lifeCycleInstance = new LifeCycleInstance();
        lifeCycleInstance.defaults();
        lifeCycleInstance.portfolioEntry = portfolioEntry;
        lifeCycleInstance.lifeCycleProcess = requestedLifeCycleProcess;

        // Create the instance planning
        LifeCycleInstancePlanning planning = new LifeCycleInstancePlanning();
        planning.version = 1;
        planning.isFrozen = false;
        planning.creationDate = new Date();
        planning.lifeCycleInstance = lifeCycleInstance;
        planning.plannedLifeCycleMilestoneInstance = new ArrayList<>();
        planning.portfolioEntryBudget = new PortfolioEntryBudget();
        planning.portfolioEntryResourcePlan = new PortfolioEntryResourcePlan();
        planning.save();

        // Create the dates of the planning
        for (LifeCycleMilestone milestone : requestedLifeCycleProcess.lifeCycleMilestones) {
            PlannedLifeCycleMilestoneInstance plannedLifeCycleMilestoneInstance = new PlannedLifeCycleMilestoneInstance();
            plannedLifeCycleMilestoneInstance.lifeCycleMilestone = milestone;
            plannedLifeCycleMilestoneInstance.lifeCycleInstancePlanning = planning;
            plannedLifeCycleMilestoneInstance.save();
        }

        // Assign the planning to the life cycle process instance
        List<LifeCycleInstancePlanning> plannings = new ArrayList<>();
        plannings.add(planning);
        lifeCycleInstance.lifeCycleInstancePlannings = plannings;
        lifeCycleInstance.save();

        // Set the life cycle process instance as the active one of the
        // portfolio entry
        portfolioEntry.activeLifeCycleInstance = lifeCycleInstance;
        portfolioEntry.save();
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
        List<GovernanceListView> governanceListView = new ArrayList<>();
        List<PlannedLifeCycleMilestoneInstance> lastPlannedMilestoneInstances = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceNotApprovedAsListOfPE(id);
        for (PlannedLifeCycleMilestoneInstance lastPlannedMilestoneInstance : lastPlannedMilestoneInstances) {
            governanceListView.add(new GovernanceListView(lastPlannedMilestoneInstance));
        }
        Set<String> hideColumnsForGovernance = new HashSet<>();
        hideColumnsForGovernance.add("actionLink");
        Table<GovernanceListView> milestonesTable = this.getTableProvider().get().governance.templateTable.fill(governanceListView, hideColumnsForGovernance);

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

        // construct the corresponding form data (for the custom attributes)
        PortfolioEntryEditFormData editFormData = new PortfolioEntryEditFormData(portfolioEntry);

        // get the last milestone
        LifeCycleMilestoneInstance lastMilestone = portfolioEntry.lastApprovedLifeCycleMilestoneInstance;

        // get the portfolios
        List<Portfolio> portfolios = portfolioEntry.portfolios;

        List<PortfolioListView> portfoliosView = new ArrayList<>();
        for (Portfolio portfolio : portfolios) {
            portfoliosView.add(new PortfolioListView(portfolio));
        }

        Table<PortfolioListView> portfolioFilledTable = this.getTableProvider().get().portfolio.templateTable.fill(portfoliosView,
                PortfolioListView.hideStakeholderTypeColumn);

        // get the dependencies
        List<PortfolioEntryDependency> dependencies = PortfolioEntryDao.getPEDependencyAsList(id);

        List<PortfolioEntryDependencyListView> portfolioEntryDependenciesListView = new ArrayList<>();
        for (PortfolioEntryDependency dependency : dependencies) {
            portfolioEntryDependenciesListView.add(new PortfolioEntryDependencyListView(id, dependency));
        }

        Set<String> columnsToHideForDependencies = new HashSet<>();
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
            columnsToHideForDependencies.add("deleteActionLink");
        }

        Table<PortfolioEntryDependencyListView> dependenciesFilledTable = this.getTableProvider().get().portfolioEntryDependency.templateTable
                .fill(portfolioEntryDependenciesListView, columnsToHideForDependencies);

        /*
         * Get the attachments
         */

        // authorize the attachments
        FileAttachmentHelper.getFileAttachmentsForDisplay(PortfolioEntry.class, id, getAttachmentManagerPlugin(), getUserSessionManagerPlugin());

        // create the table
        Pagination<Attachment> attachmentPagination = new Pagination<>(
                Attachment.getAttachmentsFromObjectTypeAndObjectIdAsExpressionList(PortfolioEntry.class, id), 5,
                getConfiguration().getInt("maf.number_page_links"));
        attachmentPagination.setCurrentPage(attachmentPage);
        attachmentPagination.setPageQueryName("attachmentPage");

        List<AttachmentListView> attachmentsListView = new ArrayList<>();
        for (Attachment attachment : attachmentPagination.getListOfObjects()) {
            attachmentsListView
                    .add(new AttachmentListView(attachment, controllers.core.routes.PortfolioEntryController.deleteAttachment(id, attachment.id).url()));
        }

        Set<String> hideColumns = new HashSet<>();
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumns.add("removeActionLink");
        }

        Table<AttachmentListView> attachmentFilledTable = this.getTableProvider().get().attachment.templateTable.fill(attachmentsListView, hideColumns);

        Map<Date, String[]> updates = new HashMap<>();

        updates.put(portfolioEntry.lastUpdate, new String[]{portfolioEntry.updatedBy == null ? "-" : portfolioEntry.updatedBy, Msg.get("core.portfolio_entry.view.details.update.details")});
        LifeCycleInstancePlanning currentPlanning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        this.addUpdatedSection(updates, currentPlanning.portfolioEntryBudget.getLastUpdatedBudgetLine(), "core.portfolio_entry.view.details.update.budget");
        this.addUpdatedSection(updates, portfolioEntry.getLastUpdatedWorkOrder(), "core.portfolio_entry.view.details.update.work_orders");
        this.addUpdatedSection(updates, currentPlanning.getLastUpdatedMilestoneInstance(), "core.portfolio_entry.view.details.update.planning");
        this.addUpdatedSection(updates, portfolioEntry.getLastUpdatedPackage(), "core.portfolio_entry.view.details.update.packages");
        this.addUpdatedSection(updates, currentPlanning.portfolioEntryResourcePlan.getLastUpdatedResource(), "core.portfolio_entry.view.details.update.resources");
        this.addUpdatedSection(updates, portfolioEntry.lastPortfolioEntryReport, "core.portfolio_entry.view.details.update.reports");
        this.addUpdatedSection(updates, portfolioEntry.getLastUpdatedRisk(), "core.portfolio_entry.view.details.update.risks");
        this.addUpdatedSection(updates, portfolioEntry.getLastUpdatedIssue(), "core.portfolio_entry.view.details.update.issues");
        this.addUpdatedSection(updates, portfolioEntry.getLastUpdatedEvent(), "core.portfolio_entry.view.details.update.events");

        Date lastUpdatedDate = updates.keySet().stream().max(Date::compareTo).orElse(new Date());

        String updatedByActorUid = updates.get(lastUpdatedDate)[0];
        Actor updatedByActor = ActorDao.getActorByUid(updatedByActorUid);
        String updatedByNameHumanReadable = "-";
        if (updatedByActorUid.equals(Actor.TECHNICAL_ACTOR_UID)) {
            updatedByNameHumanReadable = updatedByActorUid;
        }
        if (updatedByActor != null) {
            updatedByNameHumanReadable = updatedByActor.getNameHumanReadable();
        }

        return ok(views.html.core.portfolioentry.portfolio_entry_view.render(portfolioEntry, editFormData, lastMilestone, portfolioFilledTable,
                dependenciesFilledTable, attachmentFilledTable, attachmentPagination, updatedByNameHumanReadable, lastUpdatedDate, updates.get(lastUpdatedDate)[1]));

    }

    private void addUpdatedSection(Map<Date, String[]> updates, BizDockModel field, String translationKey) {
        if (field != null) {
            updates.put(field.lastUpdate, new String[]{field.updatedBy == null ? "-" : field.updatedBy, Msg.get(translationKey)});
        }
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

        // add the custom attributes
        this.getCustomAttributeManagerService().fillWithValues(portfolioEntryForm, PortfolioEntry.class, id);

        return ok(views.html.core.portfolioentry.portfolio_entry_edit.render(portfolioEntry, portfolioEntryForm,
                PortfolioEntryDao.getPETypeActiveAsVH(portfolioEntry.portfolioEntryType.isRelease),
                getPreferenceManagerPlugin().getPreferenceValueAsBoolean(IMafConstants.READONLY_GOVERNANCE_ID_PREFERENCE)));
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

        if (boundForm.hasErrors() || this.getCustomAttributeManagerService().validateValues(boundForm, PortfolioEntry.class)) {
            return ok(views.html.core.portfolioentry.portfolio_entry_edit.render(portfolioEntry, boundForm, PortfolioEntryDao.getPETypeActiveAsVH(),
            		getPreferenceManagerPlugin().getPreferenceValueAsBoolean(IMafConstants.READONLY_GOVERNANCE_ID_PREFERENCE)));
        }

        PortfolioEntryEditFormData portfolioEntryFormData = boundForm.get();

        Ebean.beginTransaction();
        try {

            // save the portfolio entry
            PortfolioEntry updPortfolioEntry = PortfolioEntryDao.getPEById(portfolioEntryFormData.id);
            portfolioEntryFormData.fill(updPortfolioEntry);
            updPortfolioEntry.update();

            // update the licenses number (because the flag is archived is used
            // for the computation and could be modified)
            getLicensesManagementService().updateConsumedPortfolioEntries();

            // save the custom attributes
            this.getCustomAttributeManagerService().validateAndSaveValues(boundForm, PortfolioEntry.class, id);

            Ebean.commitTransaction();
            Ebean.endTransaction();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.edit.successful"));

            return redirect(controllers.core.routes.PortfolioEntryController.view(portfolioEntryFormData.id, 0));

        } catch (Exception e) {

            Ebean.rollbackTransaction();
            Ebean.endTransaction();
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());

        }
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

        PortfolioEntryDependency portfolioEntryDependency = new PortfolioEntryDependency();
        portfolioEntryDependencyFormData.fill(portfolioEntryDependency);

        // check the depending PE is not the current
        if (portfolioEntryDependency.getSourcePortfolioEntry().id.equals(portfolioEntryDependency.getDestinationPortfolioEntry().id)) {
            boundForm.reject("dependingId", Msg.get("object.portfolio_entry.dependency.same.error"));
            return ok(views.html.core.portfolioentry.portfolio_entry_add_dependency.render(portfolioEntry, boundForm));
        }

        // check the portfolio entry is not already assigned.
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

                ISelectableValueHolderCollection<Long> portfolioEntries = new DefaultSelectableValueHolderCollection<>();

                Expression expression = Expr.and(Expr.eq("archived", false),
                        Expr.or(Expr.ilike("name", query + "%"), Expr.ilike("governanceId", query + "%")));

                for (PortfolioEntry portfolioEntry : PortfolioEntryDynamicHelper.getPortfolioEntriesViewAllowedAsQuery(getSecurityService()).add(expression)
                        .findList()) {
                    portfolioEntries.add(new DefaultSelectableValueHolder<Long>(portfolioEntry.id, portfolioEntry.getName()));
                }

                return ok(Utilities.marshallAsJson(portfolioEntries.getValues()));
            }

            if (value != null) {
                PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(Long.valueOf(value));
                ISelectableValueHolder<Long> portfolioEntryAsVH = new DefaultSelectableValueHolder<>(portfolioEntry.id, portfolioEntry.getName());
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

        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(portfolioEntryId);

        if (securityService.dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION, "", portfolioEntryId)) {
            sideBar.addMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.overview.label",
                    controllers.core.routes.PortfolioEntryController.overview(portfolioEntryId), "fa fa-tachometer",
                    currentType.equals(MenuItemType.OVERVIEW)));
        }

        sideBar.addMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.view.label",
                controllers.core.routes.PortfolioEntryController.view(portfolioEntryId, 0), "fa fa-search-plus", currentType.equals(MenuItemType.VIEW)));

        if (securityService.dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION, "", portfolioEntryId)) {

            HeaderMenuItem financialMenu = new HeaderMenuItem("core.portfolio_entry.sidebar.financial.label", "fa fa-money",
                    currentType.equals(MenuItemType.FINANCIAL));

            financialMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.financial.details.label",
                    controllers.core.routes.PortfolioEntryFinancialController.details(portfolioEntryId), "fa fa-search-plus", false));

            financialMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.financial.status.label",
                    controllers.core.routes.PortfolioEntryFinancialController.status(portfolioEntryId), "fa fa-bar-chart", false));

            sideBar.addMenuItem(financialMenu);
        }

        sideBar.addMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.stakeholders.label",
                controllers.core.routes.PortfolioEntryStakeholderController.index(portfolioEntryId), "fa fa-users",
                currentType.equals(MenuItemType.STAKEHOLDERS)));

        if (securityService.dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION, "", portfolioEntryId)) {

            sideBar.addMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.governance.label",
                    controllers.core.routes.PortfolioEntryGovernanceController.index(portfolioEntryId), "fa fa-university",
                    currentType.equals(MenuItemType.GOVERNANCE)));

            HeaderMenuItem deliveryMenu = new HeaderMenuItem("core.portfolio_entry.sidebar.delivery.label", "fa fa-industry",
                    currentType.equals(MenuItemType.DELIVERY));

            deliveryMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.delivery.deliverables.label",
                    controllers.core.routes.PortfolioEntryDeliveryController.deliverables(portfolioEntryId), "fa fa-cubes", false));

            deliveryMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.delivery.requirements.label",
                    controllers.core.routes.PortfolioEntryDeliveryController.requirements(portfolioEntryId), "fa fa-newspaper-o", false));

            deliveryMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.delivery.requirements_status.label",
                    controllers.core.routes.PortfolioEntryDeliveryController.requirementsStatus(portfolioEntryId), "fa fa-bar-chart", false));

            if (portfolioEntry.portfolioEntryType != null && !portfolioEntry.portfolioEntryType.isRelease) {
                deliveryMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.delivery.iterations.label",
                        controllers.core.routes.PortfolioEntryDeliveryController.iterations(portfolioEntryId), "fa fa-history", false));
            }

            sideBar.addMenuItem(deliveryMenu);

            HeaderMenuItem planningMenu = new HeaderMenuItem("core.portfolio_entry.sidebar.planning.label", "fa fa-calendar",
                    currentType.equals(MenuItemType.PLANNING));

            planningMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.planning.overview.label",
                    controllers.core.routes.PortfolioEntryPlanningController.overview(portfolioEntryId), "fa fa-tachometer", false));

            planningMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.planning.packages.label",
                    controllers.core.routes.PortfolioEntryPlanningController.packages(portfolioEntryId), "fa fa-calendar-check-o", false));

            planningMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.planning.resources.label",
                    controllers.core.routes.PortfolioEntryPlanningController.resources(portfolioEntryId), "fa fa-book", false));

            sideBar.addMenuItem(planningMenu);

            HeaderMenuItem reportingMenu = new HeaderMenuItem("core.portfolio_entry.sidebar.status_reporting.label", "fa fa-file-text",
                    currentType.equals(MenuItemType.REPORTING));

            reportingMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.status_reporting.registers.label",
                    controllers.core.routes.PortfolioEntryStatusReportingController.registers(portfolioEntryId, 0, 0, 0, false, false), "fa fa-inbox",
                    false));

            reportingMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.status_reporting.events.label",
                    controllers.core.routes.PortfolioEntryStatusReportingController.events(portfolioEntryId), "fa fa-bullhorn", false));

            reportingMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.status_reporting.timesheets.label",
                    controllers.core.routes.PortfolioEntryStatusReportingController.timesheets(portfolioEntryId), "fa fa-clock-o", false));
            
            reportingMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.status_reporting.documents.label",
                    controllers.core.routes.PortfolioEntryStatusReportingController.docs(portfolioEntryId), "fa fa-file", false));

            sideBar.addMenuItem(reportingMenu);

        }

        if (securityService.dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION, "", portfolioEntryId)) {

            HeaderMenuItem integrationMenu = new HeaderMenuItem("core.portfolio_entry.sidebar.integration.label", "fa fa-cloud",
                    currentType.equals(MenuItemType.INTEGRATION));
            integrationMenu.setIsImportant(true);
            sideBar.addMenuItem(integrationMenu);

            integrationMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.integration.plugins.label",
                    controllers.core.routes.PortfolioEntryController.pluginConfig(portfolioEntryId), "fa fa-rss", false));

            if (isDataSyndicationActive) {
                integrationMenu.addSubMenuItem(new ClickableMenuItem("core.portfolio_entry.sidebar.integration.data_syndication.label",
                        controllers.core.routes.PortfolioEntryDataSyndicationController.index(portfolioEntryId), "fa fa-share-alt", false));
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
    public enum MenuItemType {
        OVERVIEW, VIEW, FINANCIAL, STAKEHOLDERS, GOVERNANCE, PLANNING, DELIVERY, REPORTING, INTEGRATION
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

    /**
     * Get the Play configuration service.
     */
    private Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Get the account manager service.
     */
    private IAccountManagerPlugin getAccountManagerPlugin() {
        return this.accountManagerPlugin;
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
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
     * Get the custom attribute manager service.
     */
    private ICustomAttributeManagerService getCustomAttributeManagerService() {
        return this.customAttributeManagerService;
    }

	private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
		return preferenceManagerPlugin;
	}
}
