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
import dao.delivery.RequirementDAO;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.delivery.RequirementPriority;
import models.delivery.RequirementSeverity;
import models.delivery.RequirementStatus;
import models.delivery.RequirementStatus.Type;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import services.tableprovider.ITableProvider;
import utils.form.RequirementPriorityFormData;
import utils.form.RequirementSeverityFormData;
import utils.form.RequirementStatusFormData;
import utils.table.RequirementPriorityListView;
import utils.table.RequirementSeverityListView;
import utils.table.RequirementStatusListView;

/**
 * Manage the requirements reference data.
 * 
 * @author Johann Kohler
 * 
 */
@Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
public class ConfigurationRequirementController extends Controller {

    private static Form<RequirementStatusFormData> statusFormTemplate = Form.form(RequirementStatusFormData.class);
    private static Form<RequirementPriorityFormData> priorityFormTemplate = Form.form(RequirementPriorityFormData.class);
    private static Form<RequirementSeverityFormData> severityFormTemplate = Form.form(RequirementSeverityFormData.class);

    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;

    @Inject
    private ITableProvider tableProvider;

    /**
     * Display the lists of data.
     */
    public Result list() {

        // status
        List<RequirementStatus> requirementStatus = RequirementDAO.getRequirementStatusAsList();

        List<RequirementStatusListView> requirementStatusListView = new ArrayList<RequirementStatusListView>();
        for (RequirementStatus status : requirementStatus) {
            requirementStatusListView.add(new RequirementStatusListView(status));
        }

        Table<RequirementStatusListView> requirementStatusFilledTable = RequirementStatusListView.templateTable.fill(requirementStatusListView);

        // priorities
        List<RequirementPriority> requirementPriorities = RequirementDAO.getRequirementPriorityAsList();

        List<RequirementPriorityListView> requirementPriorityListView = new ArrayList<RequirementPriorityListView>();
        for (RequirementPriority requirementPriority : requirementPriorities) {
            requirementPriorityListView.add(new RequirementPriorityListView(requirementPriority));
        }

        Table<RequirementPriorityListView> requirementPrioritiesFilledTable = RequirementPriorityListView.templateTable.fill(requirementPriorityListView);

        // severities
        List<RequirementSeverity> requirementSeverities = RequirementDAO.getRequirementSeverityAsList();

        List<RequirementSeverityListView> requirementSeverityListView = new ArrayList<RequirementSeverityListView>();
        for (RequirementSeverity requirementSeverity : requirementSeverities) {
            requirementSeverityListView.add(new RequirementSeverityListView(requirementSeverity));
        }

        Table<RequirementSeverityListView> requirementSeveritiesFilledTable = RequirementSeverityListView.templateTable.fill(requirementSeverityListView);

        return ok(views.html.admin.config.datareference.requirement.list.render(requirementStatusFilledTable, requirementPrioritiesFilledTable,
                requirementSeveritiesFilledTable));
    }

    /**
     * Edit or create a requirement status.
     * 
     * @param statusId
     *            the requirement status id (set 0 for create case)
     */
    public Result manageRequirementStatus(Long statusId) {

        // initiate the form with the template
        Form<RequirementStatusFormData> statusForm = statusFormTemplate;

        // edit case: inject values
        if (!statusId.equals(Long.valueOf(0))) {

            RequirementStatus status = RequirementDAO.getRequirementStatusById(statusId);

            statusForm = statusFormTemplate.fill(new RequirementStatusFormData(status, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.requirement.requirement_status_manage.render(statusForm, getStatusTypesAsValueHolderCollection()));
    }

    /**
     * Process the edit/create form of a requirement status.
     */
    public Result saveRequirementStatus() {

        // bind the form
        Form<RequirementStatusFormData> boundForm = statusFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.requirement.requirement_status_manage.render(boundForm, getStatusTypesAsValueHolderCollection()));
        }

        RequirementStatusFormData statusFormData = boundForm.get();

        RequirementStatus status = null;

        if (statusFormData.id == null) { // create case

            status = new RequirementStatus();

            statusFormData.fill(status);
            status.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.requirementstatus.add.successful"));

        } else { // edit case

            status = RequirementDAO.getRequirementStatusById(statusFormData.id);

            statusFormData.fill(status);
            status.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.requirementstatus.edit.successful"));
        }

        statusFormData.description.persist(getI18nMessagesPlugin());
        statusFormData.name.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRequirementController.list());
    }

    /**
     * Delete a requirement status.
     * 
     * @param statusId
     *            the requirement status id
     */
    public Result deleteRequirementStatus(Long statusId) {
        RequirementStatus status = RequirementDAO.getRequirementStatusById(statusId);

        status.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.requirementstatus.delete.successful"));

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRequirementController.list());
    }

    /**
     * Edit or create a requirement priority.
     * 
     * @param priorityId
     *            the requirement priority id (set 0 for create case)
     */
    public Result manageRequirementPriority(Long priorityId) {
        // initiate the form with the template
        Form<RequirementPriorityFormData> priorityForm = priorityFormTemplate;

        // edit case: inject values
        if (!priorityId.equals(Long.valueOf(0))) {

            RequirementPriority priority = RequirementDAO.getRequirementPriorityById(priorityId);

            priorityForm = priorityFormTemplate.fill(new RequirementPriorityFormData(priority, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.requirement.requirement_priority_manage.render(priorityForm));
    }

    /**
     * Process the edit/create form of a requirement priority.
     */
    public Result saveRequirementPriority() {
        // bind the form
        Form<RequirementPriorityFormData> boundForm = priorityFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.requirement.requirement_priority_manage.render(boundForm));
        }

        RequirementPriorityFormData priorityFormData = boundForm.get();

        RequirementPriority priority = null;

        if (priorityFormData.id == null) { // create case

            priority = new RequirementPriority();

            priorityFormData.fill(priority);
            priority.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.requirementpriority.add.successful"));

        } else { // edit case

            priority = RequirementDAO.getRequirementPriorityById(priorityFormData.id);

            priorityFormData.fill(priority);
            priority.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.requirementpriority.edit.successful"));
        }

        priorityFormData.description.persist(getI18nMessagesPlugin());
        priorityFormData.name.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRequirementController.list());
    }

    /**
     * Delete a requirement priority.
     * 
     * @param priorityId
     *            the requirement priority id
     */
    public Result deleteRequirementPriority(Long priorityId) {
        RequirementPriority priority = RequirementDAO.getRequirementPriorityById(priorityId);

        priority.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.requirementpriority.delete.successful"));

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRequirementController.list());
    }

    /**
     * Edit or create a requirement severity.
     * 
     * @param severityId
     *            the requirement severity id (set 0 for create case)
     */
    public Result manageRequirementSeverity(Long severityId) {
        // initiate the form with the template
        Form<RequirementSeverityFormData> severityForm = severityFormTemplate;

        // edit case: inject values
        if (!severityId.equals(Long.valueOf(0))) {

            RequirementSeverity severity = RequirementDAO.getRequirementSeverityById(severityId);

            severityForm = severityFormTemplate.fill(new RequirementSeverityFormData(severity, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.requirement.requirement_severity_manage.render(severityForm));
    }

    /**
     * Process the edit/create form of a requirement severity.
     */
    public Result saveRequirementSeverity() {
        // bind the form
        Form<RequirementSeverityFormData> boundForm = severityFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.requirement.requirement_severity_manage.render(boundForm));
        }

        RequirementSeverityFormData severityFormData = boundForm.get();

        RequirementSeverity severity = null;

        if (severityFormData.id == null) { // create case

            severity = new RequirementSeverity();

            severityFormData.fill(severity);
            severity.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.requirementseverity.add.successful"));

        } else { // edit case

            severity = RequirementDAO.getRequirementSeverityById(severityFormData.id);

            severityFormData.fill(severity);
            severity.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.requirementseverity.edit.successful"));
        }

        severityFormData.description.persist(getI18nMessagesPlugin());
        severityFormData.name.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRequirementController.list());
    }

    /**
     * Delete a requirement severity.
     * 
     * @param severityId
     *            the requirement severity id
     */
    public Result deleteRequirementSeverity(Long severityId) {
        RequirementSeverity severity = RequirementDAO.getRequirementSeverityById(severityId);

        severity.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.requirementseverity.delete.successful"));

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationRequirementController.list());
    }

    /**
     * Get the status types as a value holder collection.
     */
    private static ISelectableValueHolderCollection<String> getStatusTypesAsValueHolderCollection() {
        ISelectableValueHolderCollection<String> statusTypes = new DefaultSelectableValueHolderCollection<String>();
        for (Type type : RequirementStatus.Type.values()) {
            statusTypes.add(new DefaultSelectableValueHolder<String>(type.name(), Msg.get("object.requirement_status.type." + type.name() + ".label")));
        }
        return statusTypes;
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
