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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import dao.timesheet.TimesheetDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Msg;
import framework.utils.Utilities;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryPlanningPackage;
import models.timesheet.TimesheetActivity;
import models.timesheet.TimesheetActivityType;
import models.timesheet.TimesheetEntry;
import models.timesheet.TimesheetLog;
import models.timesheet.TimesheetReport;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckTimesheetReportExists;
import utils.form.TimesheetReportApprovalFormData;

/**
 * The controller which allows to manage the timesheeting.
 * 
 * @author Johann Kohler
 */
public class TimesheetController extends Controller {
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;

    private static Logger.ALogger log = Logger.of(TimesheetController.class);

    private static Form<TimesheetReportApprovalFormData> timesheetReportApprovalFormTemplate = Form.form(TimesheetReportApprovalFormData.class);

    /**
     * Page to fill a timesheet by week.
     * 
     * @param stringDate
     *            a date in the format yyyy-MM-dd: the system gets the weekly
     *            report including this date, if empty it uses the current date.
     */
    @Restrict({ @Group(IMafConstants.TIMESHEET_ENTRY_PERMISSION) })
    public Result weeklyFill(String stringDate) {

        // get the current actor
        Actor actor = getCurrentActor();
        if (actor == null) {
            return redirect(controllers.routes.Application.index());
        }

        // get the report
        TimesheetReport report = getTimesheetReport(stringDate, actor);

        // get the available initiatives for the actor
        // the actor is either manager, direct stakeholder, portfolio manager,
        // portfolio stakeholder, delivery unit manager or delivery unit member
        List<PortfolioEntry> portfolioEntries = PortfolioEntryDao.getPEAsListByMember(actor.id);

        // get the activity types with at least one activity
        List<TimesheetActivityType> activityTypes = new ArrayList<TimesheetActivityType>();
        for (TimesheetActivityType activityType : TimesheetDao.getTimesheetActivityTypeAsList()) {
            if (activityType.timesheetActivities != null && activityType.timesheetActivities.size() > 0) {
                activityTypes.add(activityType);
            }

        }

        return ok(views.html.core.timesheet.timesheet_weekly_fill.render(report, portfolioEntries, activityTypes));
    }

    /**
     * Save the weekly timesheet.
     */
    @Restrict({ @Group(IMafConstants.TIMESHEET_ENTRY_PERMISSION) })
    public Result weeklySave() {

        String[] dataString = request().body().asFormUrlEncoded().get("data");

        try {
            JSONObject dataJson = new JSONObject(dataString[0]);

            // get the report
            Long reportId = dataJson.getLong("reportId");
            TimesheetReport report = TimesheetDao.getTimesheetReportById(reportId);

            // create the date format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // get the actor
            Actor actor = getCurrentActor();

            // check the report belongs to the sign-in user
            if (!report.actor.id.equals(actor.id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            // check the report is editable
            if (!report.isEditable()) {
                Utilities.sendErrorFlashMessage(Msg.get("core.timesheet.fill.save.error.non_editable"));
                return redirect(controllers.core.routes.TimesheetController.weeklyFill(sdf.format(report.startDate)));
            }

            Ebean.beginTransaction();

            try {

                // get the entries
                JSONArray entriesJson = dataJson.getJSONArray("entries");

                for (int i = 0; i < entriesJson.length(); i++) {

                    JSONObject entryJson;
                    try {
                        entryJson = entriesJson.getJSONObject(i);
                    } catch (JSONException e) {
                        entryJson = null;
                    }

                    if (entryJson != null) {

                        // get the attributes
                        boolean inDB = entryJson.getBoolean("inDB");

                        Boolean toRemove;
                        try {
                            toRemove = entryJson.getBoolean("toRemove");
                        } catch (JSONException e) {
                            toRemove = null;
                        }

                        Long entryId;
                        try {
                            entryId = entryJson.getLong("entryId");
                        } catch (JSONException e) {
                            entryId = null;
                        }

                        Long portfolioEntryId;
                        try {
                            portfolioEntryId = entryJson.getLong("portfolioEntryId");
                        } catch (JSONException e) {
                            portfolioEntryId = null;
                        }

                        Long packageId;
                        try {
                            packageId = entryJson.getLong("packageId");
                        } catch (JSONException e) {
                            packageId = null;
                        }

                        Long activityId;
                        try {
                            activityId = entryJson.getLong("activityId");
                        } catch (JSONException e) {
                            activityId = null;
                        }

                        // get the logs
                        JSONArray logsJson = entryJson.getJSONArray("logs");

                        if (logsJson.length() == 7) {

                            TimesheetEntry entry = null;
                            if (inDB) {
                                entry = TimesheetDao.getTimesheetEntryById(entryId);
                            } else {
                                entry = new TimesheetEntry();
                                entry.timesheetReport = report;
                            }

                            if (inDB && toRemove) {
                                entry.doDelete();
                            } else {

                                entry.portfolioEntry = null;
                                entry.portfolioEntryPlanningPackage = null;
                                entry.timesheetActivity = null;

                                if (portfolioEntryId != null) { // initiative
                                    entry.portfolioEntry = PortfolioEntryDao.getPEById(portfolioEntryId);
                                    if (packageId != null) {
                                        entry.portfolioEntryPlanningPackage = PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(packageId);
                                    }
                                } else { // activity
                                    entry.timesheetActivity = TimesheetDao.getTimesheetActivityById(activityId);
                                }

                                entry.save();

                                for (int j = 0; j < logsJson.length(); j++) {

                                    JSONObject logJson = logsJson.getJSONObject(j);

                                    // get the attributes
                                    double hours = logJson.getDouble("hours");

                                    Long logId;
                                    try {
                                        logId = logJson.getLong("logId");
                                    } catch (JSONException e) {
                                        logId = null;
                                    }

                                    TimesheetLog log = null;
                                    if (inDB) {
                                        log = TimesheetDao.getTimesheetLogById(logId);
                                    } else {
                                        log = new TimesheetLog();
                                        log.timesheetEntry = entry;

                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(report.startDate);
                                        cal.add(Calendar.DAY_OF_YEAR, j);
                                        log.logDate = cal.getTime();
                                    }

                                    log.hours = hours;

                                    log.save();

                                }
                            }

                        }

                    }

                }

                Ebean.commitTransaction();

            } catch (Exception e) {
                Ebean.rollbackTransaction();
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }

            Utilities.sendSuccessFlashMessage(Msg.get("core.timesheet.fill.save.successful"));

            return redirect(controllers.core.routes.TimesheetController.weeklyFill(sdf.format(report.startDate)));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }

    }

    /**
     * Copy the entries (without "hours") of the previous report to the given
     * report (for a weekly).
     * 
     * @param reportId
     *            the report id
     */
    @Restrict({ @Group(IMafConstants.TIMESHEET_ENTRY_PERMISSION) })
    public Result weeklyCopy(Long reportId) {

        // get the report
        TimesheetReport report = TimesheetDao.getTimesheetReportById(reportId);

        // get the previous report
        TimesheetReport previousReport = TimesheetDao.getTimesheetReportByActorAndStartDate(report.actor.id, report.getPreviousStartDate());

        if (previousReport != null && previousReport.timesheetEntries != null && previousReport.timesheetEntries.size() > 0) {
            for (TimesheetEntry previousEntry : previousReport.timesheetEntries) {

                Ebean.beginTransaction();
                try {

                    TimesheetEntry entry = new TimesheetEntry();
                    entry.timesheetReport = report;
                    entry.portfolioEntry = previousEntry.portfolioEntry;
                    entry.portfolioEntryPlanningPackage = previousEntry.portfolioEntryPlanningPackage;
                    entry.timesheetActivity = previousEntry.timesheetActivity;
                    entry.save();

                    for (int i = 0; i < 7; i++) {

                        TimesheetLog log = new TimesheetLog();
                        log.hours = 0.0;
                        log.timesheetEntry = entry;

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(report.startDate);
                        cal.add(Calendar.DAY_OF_YEAR, i);
                        log.logDate = cal.getTime();

                        log.save();
                    }

                    Ebean.commitTransaction();

                } catch (Exception e) {
                    Ebean.rollbackTransaction();
                    return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
                }
            }
            Utilities.sendSuccessFlashMessage(Msg.get("core.timesheet.fill.weekly.copy.successful"));
        } else {
            Utilities.sendInfoFlashMessage(Msg.get("core.timesheet.fill.weekly.copy.info.empty"));
        }

        // create the date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        return redirect(controllers.core.routes.TimesheetController.weeklyFill(sdf.format(report.startDate)));
    }

    /**
     * Submit a weekly timesheet.
     * 
     * @param reportId
     *            the report id
     */
    @Restrict({ @Group(IMafConstants.TIMESHEET_ENTRY_PERMISSION) })
    public Result weeklySubmit(Long reportId) {

        // get the report
        TimesheetReport report = TimesheetDao.getTimesheetReportById(reportId);

        // create the date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // get the actor
        Actor actor = getCurrentActor();

        // check the report belongs to the sign-in user
        if (!report.actor.id.equals(actor.id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // check the report is editable
        if (!report.isEditable()) {
            Utilities.sendErrorFlashMessage(Msg.get("core.timesheet.fill.save.error.non_editable"));
            return redirect(controllers.core.routes.TimesheetController.weeklyFill(sdf.format(report.startDate)));
        }

        report.orgUnit = actor.orgUnit;

        if (TimesheetDao.getTimesheetReportMustApprove()) {
            report.status = TimesheetReport.Status.SUBMITTED;

            if (actor.manager != null) {
                ActorDao.sendNotification(actor.manager, NotificationCategory.getByCode(Code.TIMESHEET),
                        controllers.core.routes.ActorController.viewWeeklyTimesheet(report.actor.id, sdf.format(report.startDate)).url(),
                        "core.timesheet.fill.submit.notification.title.with_approval", "core.timesheet.fill.submit.notification.message.with_approval",
                        actor.getName());
            }

            Utilities.sendSuccessFlashMessage(Msg.get("core.timesheet.fill.submit.successful.with_approval"));
        } else {
            report.status = TimesheetReport.Status.APPROVED;
            Utilities.sendSuccessFlashMessage(Msg.get("core.timesheet.fill.submit.successful.without_approval"));
        }

        report.save();

        return redirect(controllers.core.routes.TimesheetController.weeklyFill(sdf.format(report.startDate)));
    }

    /**
     * Get the packages of a portfolio entry.
     */
    @Restrict({ @Group(IMafConstants.TIMESHEET_ENTRY_PERMISSION) })
    public Result getPackages() {

        JsonNode json = request().body().asJson();
        Long portfolioEntryId = json.findPath("portfolioEntryId").asLong();

        List<PortfolioEntryPlanningPackage> packages = PortfolioEntryPlanningPackageDao.getPEPlanningPackageAsListByPE(portfolioEntryId);
        List<OptionData> options = new ArrayList<OptionData>();

        for (PortfolioEntryPlanningPackage p : packages) {
            options.add(new OptionData(p.id, p.getName()));
        }

        ObjectMapper mapper = new ObjectMapper();

        return ok((JsonNode) mapper.valueToTree(options));
    }

    /**
     * Get the activities of type.
     */
    @Restrict({ @Group(IMafConstants.TIMESHEET_ENTRY_PERMISSION) })
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
     * Process (accept or reject) a timesheet.
     */
    @With(CheckTimesheetReportExists.class)
    @Dynamic(IMafConstants.TIMESHEET_APPROVAL_DYNAMIC_PERMISSION)
    public Result processTimesheet() {

        // bind the form
        Form<TimesheetReportApprovalFormData> boundForm = timesheetReportApprovalFormTemplate.bindFromRequest();
        TimesheetReportApprovalFormData formData = boundForm.get();

        // get the report
        TimesheetReport report = TimesheetDao.getTimesheetReportById(formData.id);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = Utilities.getDateFormat(null).format(report.startDate);
        String endDate = Utilities.getDateFormat(null).format(report.getEndDate());

        // check the report could be processed
        if (!report.isProcessable()) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        if (formData.action.equals("APPROVE")) {

            report.status = TimesheetReport.Status.APPROVED;
            report.save();

            if (formData.comments != null && !formData.comments.equals("")) {
                // with comments
                ActorDao.sendNotification(report.actor, NotificationCategory.getByCode(Code.TIMESHEET),
                        controllers.core.routes.TimesheetController.weeklyFill(sdf.format(report.startDate)).url(),
                        "core.timesheet.approve.notification.title", "core.timesheet.approve.notification.message.with_comments", startDate, endDate,
                        formData.comments);
            } else {
                // without comments
                ActorDao.sendNotification(report.actor, NotificationCategory.getByCode(Code.TIMESHEET),
                        controllers.core.routes.TimesheetController.weeklyFill(sdf.format(report.startDate)).url(),
                        "core.timesheet.approve.notification.title", "core.timesheet.approve.notification.message.without_comments", startDate, endDate);
            }

            Utilities.sendSuccessFlashMessage(Msg.get("core.timesheet.approve.successful"));
        }

        if (formData.action.equals("REJECT")) {

            report.status = TimesheetReport.Status.REJECTED;
            report.save();

            if (formData.comments != null && !formData.comments.equals("")) {
                // with comments
                ActorDao.sendNotification(report.actor, NotificationCategory.getByCode(Code.TIMESHEET),
                        controllers.core.routes.TimesheetController.weeklyFill(sdf.format(report.startDate)).url(),
                        "core.timesheet.reject.notification.title", "core.timesheet.reject.notification.message.with_comments", startDate, endDate,
                        formData.comments);
            } else {
                // without comments
                ActorDao.sendNotification(report.actor, NotificationCategory.getByCode(Code.TIMESHEET),
                        controllers.core.routes.TimesheetController.weeklyFill(sdf.format(report.startDate)).url(),
                        "core.timesheet.reject.notification.title", "core.timesheet.reject.notification.message.without_comments", startDate, endDate);
            }

            Utilities.sendSuccessFlashMessage(Msg.get("core.timesheet.reject.successful"));
        }

        return redirect(controllers.core.routes.CockpitController.subordinatesTimesheet());
    }

    /**
     * Send a reminder for a timesheet.
     * 
     * @param id
     *            the timesheet report id
     */
    @With(CheckTimesheetReportExists.class)
    @Dynamic(IMafConstants.TIMESHEET_APPROVAL_DYNAMIC_PERMISSION)
    public Result sendReminderTimesheet(Long id) {

        // get the report
        TimesheetReport report = TimesheetDao.getTimesheetReportById(id);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = Utilities.getDateFormat(null).format(report.startDate);
        String endDate = Utilities.getDateFormat(null).format(report.getEndDate());

        // send the notification
        ActorDao.sendNotification(report.actor, NotificationCategory.getByCode(Code.TIMESHEET),
                controllers.core.routes.TimesheetController.weeklyFill(sdf.format(report.startDate)).url(), "core.timesheet.send_reminder.notification.title",
                "core.timesheet.send_reminder.notification.message", startDate, endDate);

        Utilities.sendSuccessFlashMessage(Msg.get("core.timesheet.send_reminder.successful"));

        return redirect(controllers.core.routes.CockpitController.subordinatesTimesheet());
    }

    /**
     * Get the timesheet report of an actor for a given string date.
     * 
     * if the string date is empty: the start date corresponds to the first day
     * (monday) of the current week<br/>
     * else: the start date corresponds to the first day (monday) of the week
     * including the given date
     * 
     * Note: if the report doesn't exist, the system creates it.
     * 
     * @param stringDate
     *            a date in the format yyyy-MM-dd: the system gets the weekly
     *            report including this date, if empty it uses the current date.
     * @param actor
     *            the actor of the timesheet
     */
    public static TimesheetReport getTimesheetReport(String stringDate, Actor actor) {

        // get the date: either given as a parameter or the current
        Date date = null;
        if (!stringDate.equals("")) {
            try {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                date = formatter.parse(stringDate);
            } catch (ParseException e) {
                Logger.error(e.getMessage());
                return null;
            }
        } else {
            date = new Date();
        }

        // get the first day of the week including the date
        // we consider the first day as Monday
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date startDate = cal.getTime();

        TimesheetReport report = TimesheetDao.getTimesheetReportByActorAndStartDate(actor.id, startDate);
        if (report == null) {
            report = new TimesheetReport();
            report.actor = actor;
            report.orgUnit = actor.orgUnit;
            report.type = TimesheetReport.Type.WEEKLY;
            report.startDate = startDate;
            report.status = TimesheetReport.Status.OPEN;
            report.save();
        }

        return report;

    }

    /**
     * Get the current actor.
     */
    private Actor getCurrentActor() {

        try {

            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            return ActorDao.getActorByUid(uid);

        } catch (Exception e) {

            Logger.error("impossible to find the actor of the sign-in user");
            return null;

        }
    }

    /**
     * An option data is used in a select list.
     * 
     * @author Johann Kohler
     * 
     */
    public static class OptionData {

        public Long value;
        public String text;

        /**
         * Default constructor.
         * 
         * @param value
         *            the value of the option, correspond to an id
         * @param text
         *            the text of the option
         */
        public OptionData(Long value, String text) {
            this.value = value;
            this.text = text;
        }
    }

    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    private Configuration getConfiguration() {
        return configuration;
    }
}
