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
package controllers.admin;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import dao.governance.LifeCycleMilestoneDao;
import dao.governance.LifeCyclePlanningDao;
import dao.governance.LifeCycleProcessDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.governance.LifeCycleInstance;
import models.governance.LifeCycleInstancePlanning;
import models.governance.LifeCycleMilestone;
import models.governance.LifeCycleMilestoneInstanceStatusType;
import models.governance.LifeCyclePhase;
import models.governance.LifeCycleProcess;
import models.governance.PlannedLifeCycleMilestoneInstance;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import services.tableprovider.ITableProvider;
import utils.form.LifeCycleMilestoneFormData;
import utils.form.LifeCycleMilestoneInstanceStatusTypeFormData;
import utils.form.LifeCyclePhaseFormData;
import utils.form.LifeCycleProcessFormData;
import utils.table.LifeCycleMilestoneInstanceStatusTypeListView;
import utils.table.LifeCycleMilestoneListView;
import utils.table.LifeCyclePhaseListView;
import utils.table.LifeCycleProcessListView;

/**
 * Manage the portfolios and portfolio entries reference data.
 * 
 * @author Johann Kohler
 * 
 */
@Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
public class ConfigurationGovernanceController extends Controller {

    private static Form<LifeCycleProcessFormData> lifeCycleProcessFormTemplate = Form.form(LifeCycleProcessFormData.class);
    private static Form<LifeCycleMilestoneInstanceStatusTypeFormData> statusTypeFormTemplate = Form.form(LifeCycleMilestoneInstanceStatusTypeFormData.class);
    private static Form<LifeCycleMilestoneFormData> lifeCycleMilestoneFormTemplate = Form.form(LifeCycleMilestoneFormData.class);
    private static Form<LifeCyclePhaseFormData> lifeCyclePhaseFormTemplate = Form.form(LifeCyclePhaseFormData.class);

    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;

    @Inject
    private ITableProvider tableProvider;

    /**
     * Display the lists of data (LifeCycleProcess,
     * LifeCycleMilestoneInstanceStatusType).
     */
    public Result list() {

        // life cycle processes
        List<LifeCycleProcess> lifeCycleProcesses = LifeCycleProcessDao.getLCProcessAsList();

        List<LifeCycleProcessListView> lifeCycleProcessListView = new ArrayList<LifeCycleProcessListView>();
        for (LifeCycleProcess lifeCycleProcess : lifeCycleProcesses) {
            lifeCycleProcessListView.add(new LifeCycleProcessListView(lifeCycleProcess));
        }

        Table<LifeCycleProcessListView> lifeCycleProcessesTable = this.getTableProvider().get().lifeCycleProcess.templateTable.fill(lifeCycleProcessListView);

        // status types
        List<LifeCycleMilestoneInstanceStatusType> statusTypes = LifeCycleMilestoneDao.getLCMilestoneInstanceStatusTypeAsList();

        List<LifeCycleMilestoneInstanceStatusTypeListView> statusTypeListView = new ArrayList<LifeCycleMilestoneInstanceStatusTypeListView>();
        for (LifeCycleMilestoneInstanceStatusType statusType : statusTypes) {
            statusTypeListView.add(new LifeCycleMilestoneInstanceStatusTypeListView(statusType));
        }

        Table<LifeCycleMilestoneInstanceStatusTypeListView> statusTypesTable = this.getTableProvider()
                .get().lifeCycleMilestoneInstanceStatusType.templateTable.fill(statusTypeListView);

        return ok(views.html.admin.config.datareference.governance.list.render(lifeCycleProcessesTable, statusTypesTable));
    }

    /**
     * Display a life cycle process: LifeCycleMilestones, LifeCyclePhases.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     */
    public Result viewLifeCycleProcess(Long lifeCycleProcessId) {

        LifeCycleProcess lifeCycleProcess = LifeCycleProcessDao.getLCProcessById(lifeCycleProcessId);

        // life cycle milestones
        List<LifeCycleMilestone> lifeCycleMilestones = LifeCycleMilestoneDao.getLCMilestoneAsListByLCProcess(lifeCycleProcessId);

        List<LifeCycleMilestoneListView> lifeCycleMilestoneListView = new ArrayList<LifeCycleMilestoneListView>();
        for (LifeCycleMilestone lifeCycleMilestone : lifeCycleMilestones) {
            lifeCycleMilestoneListView.add(new LifeCycleMilestoneListView(lifeCycleMilestone));
        }

        Table<LifeCycleMilestoneListView> lifeCycleMilestonesTable = this.getTableProvider().get().lifeCycleMilestone.templateTable
                .fill(lifeCycleMilestoneListView);

        // life cycle phases
        List<LifeCyclePhase> lifeCyclePhases = LifeCycleMilestoneDao.getLCPhaseAsListByLCProcess(lifeCycleProcessId);

        List<LifeCyclePhaseListView> lifeCyclePhaseListView = new ArrayList<LifeCyclePhaseListView>();
        for (LifeCyclePhase lifeCyclePhase : lifeCyclePhases) {
            lifeCyclePhaseListView.add(new LifeCyclePhaseListView(lifeCyclePhase));
        }

        Table<LifeCyclePhaseListView> lifeCyclePhasesTable = this.getTableProvider().get().lifeCyclePhase.templateTable.fill(lifeCyclePhaseListView);

        return ok(views.html.admin.config.datareference.governance.life_cycle_process_view.render(lifeCycleProcess, lifeCycleMilestonesTable,
                lifeCyclePhasesTable));
    }

    /**
     * Edit or create a life cycle process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id (set 0 for create case)
     */
    public Result manageLifeCycleProcess(Long lifeCycleProcessId) {

        // initiate the form with the template
        Form<LifeCycleProcessFormData> lifeCycleProcessForm = lifeCycleProcessFormTemplate;

        // edit case: inject values
        if (!lifeCycleProcessId.equals(0L)) {

            LifeCycleProcess lifeCycleProcess = LifeCycleProcessDao.getLCProcessById(lifeCycleProcessId);

            lifeCycleProcessForm = lifeCycleProcessFormTemplate.fill(new LifeCycleProcessFormData(lifeCycleProcess, getI18nMessagesPlugin()));

        }

        this.getTableProvider().flushFilterConfig();

        return ok(views.html.admin.config.datareference.governance.life_cycle_process_manage.render(lifeCycleProcessForm));

    }

    /**
     * Process the edit/create form of a life cycle process.
     */
    public Result processManageLifeCycleProcess() {

        // bind the form
        Form<LifeCycleProcessFormData> boundForm = lifeCycleProcessFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.governance.life_cycle_process_manage.render(boundForm));
        }

        LifeCycleProcessFormData lifeCycleProcessFormData = boundForm.get();

        LifeCycleProcess lifeCycleProcess = null;

        if (lifeCycleProcessFormData.id == null) { // create case

            lifeCycleProcess = new LifeCycleProcess();

            lifeCycleProcessFormData.fill(lifeCycleProcess);
            lifeCycleProcess.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_process.add.successful"));

        } else { // edit case

            lifeCycleProcess = LifeCycleProcessDao.getLCProcessById(lifeCycleProcessFormData.id);

            lifeCycleProcessFormData.fill(lifeCycleProcess);
            lifeCycleProcess.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_process.edit.successful"));
        }

        lifeCycleProcessFormData.description.persist(getI18nMessagesPlugin());
        lifeCycleProcessFormData.name.persist(getI18nMessagesPlugin());
        lifeCycleProcessFormData.shortName.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationGovernanceController.list());

    }

    /**
     * Delete a life cycle process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     */
    public Result deleteLifeCycleProcess(Long lifeCycleProcessId) {

        LifeCycleProcess lifeCycleProcess = LifeCycleProcessDao.getLCProcessById(lifeCycleProcessId);

        if (lifeCycleProcess.lifeCycleInstances != null && lifeCycleProcess.lifeCycleInstances.size() > 0) {
            Utilities.sendErrorFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_process.delete.error"));
        } else {
            lifeCycleProcess.doDelete();
            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_process.delete.successful"));
            this.getTableProvider().flushFilterConfig();
        }

        return redirect(controllers.admin.routes.ConfigurationGovernanceController.list());

    }

    /**
     * Edit or create a life cycle milestone instance status type.
     * 
     * @param statusTypeId
     *            the status type id (set 0 for create case)
     */
    public Result manageStatusType(Long statusTypeId) {

        // initiate the form with the template
        Form<LifeCycleMilestoneInstanceStatusTypeFormData> statusTypeForm = statusTypeFormTemplate;

        // edit case: inject values
        if (!statusTypeId.equals(Long.valueOf(0))) {

            LifeCycleMilestoneInstanceStatusType statusType = LifeCycleMilestoneDao.getLCMilestoneInstanceStatusTypeById(statusTypeId);

            statusTypeForm = statusTypeFormTemplate.fill(new LifeCycleMilestoneInstanceStatusTypeFormData(statusType, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.governance.status_type_manage.render(statusTypeForm));

    }

    /**
     * Process the edit/create form of a life cycle milestone instance status
     * type.
     */
    public Result processManageStatusType() {

        // bind the form
        Form<LifeCycleMilestoneInstanceStatusTypeFormData> boundForm = statusTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.governance.status_type_manage.render(boundForm));
        }

        LifeCycleMilestoneInstanceStatusTypeFormData statusTypeFormData = boundForm.get();

        LifeCycleMilestoneInstanceStatusType statusType = null;

        if (statusTypeFormData.id == null) { // create case

            statusType = new LifeCycleMilestoneInstanceStatusType();

            statusTypeFormData.fill(statusType);
            statusType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_milestone_instance_status_type.add.successful"));

        } else { // edit case

            statusType = LifeCycleMilestoneDao.getLCMilestoneInstanceStatusTypeById(statusTypeFormData.id);

            statusTypeFormData.fill(statusType);
            statusType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_milestone_instance_status_type.edit.successful"));
        }

        statusTypeFormData.description.persist(getI18nMessagesPlugin());
        statusTypeFormData.name.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationGovernanceController.list());

    }

    /**
     * Delete a life cycle milestone instance status type.
     * 
     * @param statusTypeId
     *            the status type id
     */
    public Result deleteStatusType(Long statusTypeId) {

        LifeCycleMilestoneInstanceStatusType statusType = LifeCycleMilestoneDao.getLCMilestoneInstanceStatusTypeById(statusTypeId);

        if ((statusType.lifeCycleMilestoneInstances != null && statusType.lifeCycleMilestoneInstances.size() > 0)
                || (statusType.lifeCycleMilestones != null && statusType.lifeCycleMilestones.size() > 0)) {
            Utilities.sendErrorFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_milestone_instance_status_type.delete.error"));
        } else {
            statusType.doDelete();
            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_milestone_instance_status_type.delete.successful"));
            this.getTableProvider().flushFilterConfig();
        }

        return redirect(controllers.admin.routes.ConfigurationGovernanceController.list());

    }

    /**
     * Change the order of a life cycle milestone.
     * 
     * @param milestoneId
     *            the life cycle milestone id.
     * @param isDecrement
     *            if true then we decrement the order, else we increment it.
     */
    public Result changeMilestoneOrder(Long milestoneId, Boolean isDecrement) {

        LifeCycleMilestone lifeCycleMilestone = LifeCycleMilestoneDao.getLCMilestoneById(milestoneId);

        LifeCycleMilestone lifeCycleMilestoneToReverse = null;
        if (isDecrement) {
            lifeCycleMilestoneToReverse = LifeCycleMilestoneDao.getLCMilestoneAsPreviousByLCProcess(lifeCycleMilestone.lifeCycleProcess.id,
                    lifeCycleMilestone.order);
        } else {
            lifeCycleMilestoneToReverse = LifeCycleMilestoneDao.getLCMilestoneAsNextByLCProcess(lifeCycleMilestone.lifeCycleProcess.id,
                    lifeCycleMilestone.order);
        }

        if (lifeCycleMilestoneToReverse != null) {

            Integer newOrder = lifeCycleMilestoneToReverse.order;

            lifeCycleMilestoneToReverse.order = lifeCycleMilestone.order;
            lifeCycleMilestoneToReverse.save();

            lifeCycleMilestone.order = newOrder;
            lifeCycleMilestone.save();

        }

        return redirect(controllers.admin.routes.ConfigurationGovernanceController.viewLifeCycleProcess(lifeCycleMilestone.lifeCycleProcess.id));

    }

    /**
     * Change the order of a life cycle phase.
     * 
     * @param phaseId
     *            the life cycle phase id.
     * @param isDecrement
     *            if true then we decrement the order, else we increment it.
     */
    public Result changePhaseOrder(Long phaseId, Boolean isDecrement) {

        LifeCyclePhase lifeCyclePhase = LifeCycleMilestoneDao.getLCPhaseById(phaseId);

        LifeCyclePhase lifeCyclePhaseToReverse = null;
        if (isDecrement) {
            lifeCyclePhaseToReverse = LifeCycleMilestoneDao.getLCPhaseAsPreviousByLCProcess(lifeCyclePhase.lifeCycleProcess.id, lifeCyclePhase.order);
        } else {
            lifeCyclePhaseToReverse = LifeCycleMilestoneDao.getLCPhaseAsNextByLCProcess(lifeCyclePhase.lifeCycleProcess.id, lifeCyclePhase.order);
        }

        if (lifeCyclePhaseToReverse != null) {

            Integer newOrder = lifeCyclePhaseToReverse.order;

            lifeCyclePhaseToReverse.order = lifeCyclePhase.order;
            lifeCyclePhaseToReverse.save();

            lifeCyclePhase.order = newOrder;
            lifeCyclePhase.save();

        }

        return redirect(controllers.admin.routes.ConfigurationGovernanceController.viewLifeCycleProcess(lifeCyclePhase.lifeCycleProcess.id));

    }

    /**
     * Edit or create a life cycle milestone.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id (useful only for create case)
     * @param milestoneId
     *            the milestone id (set 0 for create case)
     */
    public Result manageMilestone(Long lifeCycleProcessId, Long milestoneId) {

        // get the process
        LifeCycleProcess lifeCycleProcess = LifeCycleProcessDao.getLCProcessById(lifeCycleProcessId);

        // initiate the form with the template
        Form<LifeCycleMilestoneFormData> milestoneTypeForm = lifeCycleMilestoneFormTemplate;

        // edit case: inject values
        if (!milestoneId.equals(0L)) {

            LifeCycleMilestone milestone = LifeCycleMilestoneDao.getLCMilestoneById(milestoneId);

            milestoneTypeForm = lifeCycleMilestoneFormTemplate.fill(new LifeCycleMilestoneFormData(milestone, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.governance.milestone_manage.render(lifeCycleProcess, milestoneTypeForm,
                LifeCycleMilestoneDao.getLCMilestoneInstanceStatusTypeActiveAsVH()));

    }

    /**
     * Process the edit/create form of a life cycle milestone.
     */
    public Result processManageMilestone() {

        // bind the form
        Form<LifeCycleMilestoneFormData> boundForm = lifeCycleMilestoneFormTemplate.bindFromRequest();

        // get the life cycle process
        Long lifeCycleProcessId = Long.valueOf(request().body().asFormUrlEncoded().get("lifeCycleProcessId")[0]);
        LifeCycleProcess lifeCycleProcess = LifeCycleProcessDao.getLCProcessById(lifeCycleProcessId);

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.governance.milestone_manage.render(lifeCycleProcess, boundForm,
                    LifeCycleMilestoneDao.getLCMilestoneInstanceStatusTypeActiveAsVH()));
        }

        LifeCycleMilestoneFormData milestoneFormData = boundForm.get();

        LifeCycleMilestone milestone = null;

        if (milestoneFormData.id == null) { // create case

            milestone = new LifeCycleMilestone();
            milestone.lifeCycleProcess = lifeCycleProcess;
            milestone.order = LifeCycleMilestoneDao.getLCMilestoneAsLastOrderByLCProcess(lifeCycleProcess.id) + 1;

            milestoneFormData.fill(milestone);
            milestone.save();
            // milestone.saveManyToManyAssociations("approvers");

            // create a planned date for each planning of the life cycle process
            for (LifeCycleInstance processInstance : lifeCycleProcess.lifeCycleInstances) {
                for (LifeCycleInstancePlanning planning : processInstance.lifeCycleInstancePlannings) {
                    PlannedLifeCycleMilestoneInstance plannedDate = new PlannedLifeCycleMilestoneInstance();
                    plannedDate.lifeCycleInstancePlanning = planning;
                    plannedDate.lifeCycleMilestone = milestone;
                    plannedDate.save();
                }
            }

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_milestone.add.successful"));

        } else { // edit case

            milestone = LifeCycleMilestoneDao.getLCMilestoneById(milestoneFormData.id);

            milestoneFormData.fill(milestone);
            milestone.update();
            // milestone.saveManyToManyAssociations("approvers");

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_milestone.edit.successful"));
        }

        milestoneFormData.description.persist(getI18nMessagesPlugin());
        milestoneFormData.name.persist(getI18nMessagesPlugin());
        milestoneFormData.shortName.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationGovernanceController.viewLifeCycleProcess(lifeCycleProcess.id));

    }

    /**
     * Delete a life cycle milestone.
     * 
     * @param milestoneId
     *            the milestone id (set 0 for create case)
     */
    public Result deleteMilestone(Long milestoneId) {

        LifeCycleMilestone milestone = LifeCycleMilestoneDao.getLCMilestoneById(milestoneId);

        if ((milestone.lifeCycleMilestoneInstances != null && milestone.lifeCycleMilestoneInstances.size() > 0)
                || (milestone.startLifeCyclePhases != null && milestone.startLifeCyclePhases.size() > 0)
                || (milestone.endLifeCyclePhases != null && milestone.endLifeCyclePhases.size() > 0)) {
            Utilities.sendErrorFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_milestone.delete.error"));
        } else {
            milestone.doDelete();

            // remove the planned date
            for (PlannedLifeCycleMilestoneInstance plannedDate : LifeCyclePlanningDao.getPlannedLCMilestoneInstanceAsListByLCMilestone(milestone.id)) {
                plannedDate.doDelete();
            }

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_milestone.delete.successful"));

            this.getTableProvider().flushFilterConfig();
        }

        return redirect(controllers.admin.routes.ConfigurationGovernanceController.viewLifeCycleProcess(milestone.lifeCycleProcess.id));

    }

    /**
     * Edit or create a life cycle phase.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id (useful only for create case)
     * @param phaseId
     *            the phase id (set 0 for create case)
     */
    public Result managePhase(Long lifeCycleProcessId, Long phaseId) {

        // get the process
        LifeCycleProcess lifeCycleProcess = LifeCycleProcessDao.getLCProcessById(lifeCycleProcessId);

        // initiate the form with the template
        Form<LifeCyclePhaseFormData> phaseTypeForm = lifeCyclePhaseFormTemplate;

        // edit case: inject values
        if (!phaseId.equals(0L)) {

            LifeCyclePhase phase = LifeCycleMilestoneDao.getLCPhaseById(phaseId);

            phaseTypeForm = lifeCyclePhaseFormTemplate.fill(new LifeCyclePhaseFormData(phase, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.governance.phase_manage.render(lifeCycleProcess, phaseTypeForm,
                LifeCycleMilestoneDao.getLCMilestoneAsVHByLCProcess(lifeCycleProcessId)));
    }

    /**
     * Process the edit/create form of a life cycle phase.
     */
    public Result processManagePhase() {

        // bind the form
        Form<LifeCyclePhaseFormData> boundForm = lifeCyclePhaseFormTemplate.bindFromRequest();

        // get the life cycle process
        Long lifeCycleProcessId = Long.valueOf(request().body().asFormUrlEncoded().get("lifeCycleProcessId")[0]);
        LifeCycleProcess lifeCycleProcess = LifeCycleProcessDao.getLCProcessById(lifeCycleProcessId);

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.governance.phase_manage.render(lifeCycleProcess, boundForm,
                    LifeCycleMilestoneDao.getLCMilestoneAsVHByLCProcess(lifeCycleProcessId)));
        }

        LifeCyclePhaseFormData phaseFormData = boundForm.get();

        LifeCyclePhase phase = null;

        if (phaseFormData.id == null) { // create case

            phase = new LifeCyclePhase();
            phase.lifeCycleProcess = lifeCycleProcess;
            phase.order = LifeCycleMilestoneDao.getLCPhaseAsLastOrderByLCProcess(lifeCycleProcess.id) + 1;

            phaseFormData.fill(phase);
            phase.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_phase.add.successful"));

        } else { // edit case

            phase = LifeCycleMilestoneDao.getLCPhaseById(phaseFormData.id);

            phaseFormData.fill(phase);
            phase.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_phase.edit.successful"));
        }

        phaseFormData.name.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationGovernanceController.viewLifeCycleProcess(lifeCycleProcess.id));

    }

    /**
     * Delete a life cycle phase.
     * 
     * @param phaseId
     *            the phase id (set 0 for create case)
     */
    public Result deletePhase(Long phaseId) {

        LifeCyclePhase phase = LifeCycleMilestoneDao.getLCPhaseById(phaseId);

        phase.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.life_cycle_phase.delete.successful"));

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationGovernanceController.viewLifeCycleProcess(phase.lifeCycleProcess.id));
    }

    /**
     * Get the life cycle milestone types as a value holder collection.
     * 
     * Note: return null if there is no type.
     * 
     * @param isRelease
     *            true to get type for release process, false for initiative
     *            process.
     */
    public static DefaultSelectableValueHolderCollection<String> getLifeCycleMilestoneTypeAsVHC(boolean isRelease) {
        DefaultSelectableValueHolderCollection<String> types = new DefaultSelectableValueHolderCollection<String>();
        for (LifeCycleMilestone.Type type : LifeCycleMilestone.Type.values()) {
            if (type.isRelease == isRelease) {
                types.add(new DefaultSelectableValueHolder<String>(type.name(), type.getLabel()));
            }
        }
        return types.getSortedValues().size() > 0 ? types : null;
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }

}
