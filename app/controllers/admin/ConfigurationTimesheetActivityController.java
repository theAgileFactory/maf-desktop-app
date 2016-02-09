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
import dao.timesheet.TimesheetDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.timesheet.TimesheetActivity;
import models.timesheet.TimesheetActivityType;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import services.tableprovider.ITableProvider;
import utils.form.TimesheetActivityFormData;
import utils.form.TimesheetActivityTypeFormData;
import utils.table.TimesheetActivityListView;
import utils.table.TimesheetActivityTypeListView;

/**
 * Manage the timesheet activities reference data.
 * 
 * @author Johann Kohler
 * 
 */
@Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
public class ConfigurationTimesheetActivityController extends Controller {

    private static Form<TimesheetActivityFormData> timesheetActivityFormTemplate = Form.form(TimesheetActivityFormData.class);

    private static Form<TimesheetActivityTypeFormData> timesheetActivityTypeFormTemplate = Form.form(TimesheetActivityTypeFormData.class);

    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;

    @Inject
    private ITableProvider tableProvider;

    /**
     * Reference data: timesheet activities.
     */
    public Result list() {

        List<TimesheetActivityType> activityTypes = TimesheetDao.getTimesheetActivityTypeAsList();

        List<TimesheetActivityTypeListView> activityTypesListView = new ArrayList<TimesheetActivityTypeListView>();
        for (TimesheetActivityType activityType : activityTypes) {
            activityTypesListView.add(new TimesheetActivityTypeListView(activityType));
        }

        Table<TimesheetActivityTypeListView> activityTypesFilledTable = this.getTableProvider().get().timesheetActivityType.templateTable
                .fill(activityTypesListView);

        List<TimesheetActivity> activities = TimesheetDao.getTimesheetActivityAsList();

        List<TimesheetActivityListView> activitiesListView = new ArrayList<TimesheetActivityListView>();
        for (TimesheetActivity activity : activities) {
            activitiesListView.add(new TimesheetActivityListView(activity));
        }

        Table<TimesheetActivityListView> activitiesFilledTable = this.getTableProvider().get().timesheetActivity.templateTable.fill(activitiesListView);

        return ok(views.html.admin.config.datareference.timesheetactivity.timesheet_activities.render(activityTypesFilledTable, activitiesFilledTable));
    }

    /**
     * Edit or create a timesheet activity type.
     * 
     * @param timesheetActivityTypeId
     *            the timesheet activity type id (set 0 for create case)
     */
    public Result manageTimesheetActivityType(Long timesheetActivityTypeId) {

        // initiate the form with the template
        Form<TimesheetActivityTypeFormData> timesheetActivityTypeForm = timesheetActivityTypeFormTemplate;

        // edit case: inject values
        if (!timesheetActivityTypeId.equals(Long.valueOf(0))) {

            TimesheetActivityType timesheetActivityType = TimesheetDao.getTimesheetActivityTypeById(timesheetActivityTypeId);

            timesheetActivityTypeForm = timesheetActivityTypeFormTemplate
                    .fill(new TimesheetActivityTypeFormData(timesheetActivityType, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.timesheetactivity.timesheet_activity_type_manage.render(timesheetActivityTypeForm));

    }

    /**
     * Process the edit/create form of a timesheet activity type.
     */
    public Result saveTimesheetActivityType() {

        // bind the form
        Form<TimesheetActivityTypeFormData> boundForm = timesheetActivityTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.timesheetactivity.timesheet_activity_type_manage.render(boundForm));
        }

        TimesheetActivityTypeFormData timesheetActivityTypeFormData = boundForm.get();

        TimesheetActivityType timesheetActivityType = null;

        if (timesheetActivityTypeFormData.id == null) { // create case

            timesheetActivityType = new TimesheetActivityType();

            timesheetActivityTypeFormData.fill(timesheetActivityType);
            timesheetActivityType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.timesheetactivitytype.add.successful"));

        } else { // edit case

            timesheetActivityType = TimesheetDao.getTimesheetActivityTypeById(timesheetActivityTypeFormData.id);

            timesheetActivityTypeFormData.fill(timesheetActivityType);
            timesheetActivityType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.timesheetactivitytype.edit.successful"));
        }

        timesheetActivityTypeFormData.description.persist(getI18nMessagesPlugin());
        timesheetActivityTypeFormData.name.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationTimesheetActivityController.list());

    }

    /**
     * Delete a timesheet activity type.
     * 
     * @param timesheetActivityTypeId
     *            the timesheet activity type id
     */
    public Result deleteTimesheetActivityType(Long timesheetActivityTypeId) {

        TimesheetActivityType timesheetActivityType = TimesheetDao.getTimesheetActivityTypeById(timesheetActivityTypeId);

        timesheetActivityType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.timesheetactivitytype.delete.successful"));

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationTimesheetActivityController.list());

    }

    /**
     * Edit or create a timesheet activity.
     * 
     * @param timesheetActivityId
     *            the timesheet activity id (set 0 for create case)
     */
    public Result manageTimesheetActivity(Long timesheetActivityId) {

        // initiate the form with the template
        Form<TimesheetActivityFormData> timesheetActivityForm = timesheetActivityFormTemplate;

        // edit case: inject values
        if (!timesheetActivityId.equals(Long.valueOf(0))) {

            TimesheetActivity timesheetActivity = TimesheetDao.getTimesheetActivityById(timesheetActivityId);

            timesheetActivityForm = timesheetActivityFormTemplate.fill(new TimesheetActivityFormData(timesheetActivity, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.timesheetactivity.timesheet_activity_manage.render(timesheetActivityForm,
                TimesheetDao.getTimesheetActivityTypeAsVH()));

    }

    /**
     * Process the edit/create form of a timesheet activity.
     */
    public Result saveTimesheetActivity() {

        // bind the form
        Form<TimesheetActivityFormData> boundForm = timesheetActivityFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.timesheetactivity.timesheet_activity_manage.render(boundForm,
                    TimesheetDao.getTimesheetActivityTypeAsVH()));
        }

        TimesheetActivityFormData timesheetActivityFormData = boundForm.get();

        TimesheetActivity timesheetActivity = null;

        if (timesheetActivityFormData.id == null) { // create case

            timesheetActivity = new TimesheetActivity();

            timesheetActivityFormData.fill(timesheetActivity);
            timesheetActivity.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.timesheetactivity.add.successful"));

        } else { // edit case

            timesheetActivity = TimesheetDao.getTimesheetActivityById(timesheetActivityFormData.id);

            timesheetActivityFormData.fill(timesheetActivity);
            timesheetActivity.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.timesheetactivity.edit.successful"));
        }

        timesheetActivityFormData.description.persist(getI18nMessagesPlugin());
        timesheetActivityFormData.name.persist(getI18nMessagesPlugin());

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationTimesheetActivityController.list());

    }

    /**
     * Delete a timesheet activity.
     * 
     * @param timesheetActivityId
     *            the timesheet activity id
     */
    public Result deleteTimesheetActivity(Long timesheetActivityId) {

        TimesheetActivity timesheetActivity = TimesheetDao.getTimesheetActivityById(timesheetActivityId);

        timesheetActivity.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.timesheetactivity.delete.successful"));

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationTimesheetActivityController.list());

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
