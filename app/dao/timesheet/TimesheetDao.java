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
package dao.timesheet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Finder;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import constants.IMafConstants;
import framework.services.account.IPreferenceManagerPlugin;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;
import java.util.Map;
import java.util.stream.Collectors;
import models.pmo.Actor;
import models.pmo.PortfolioEntryPlanningPackage;
import models.sql.TotalHours;
import models.timesheet.TimesheetActivity;
import models.timesheet.TimesheetActivityAllocatedActor;
import models.timesheet.TimesheetActivityType;
import models.timesheet.TimesheetEntry;
import models.timesheet.TimesheetLog;
import models.timesheet.TimesheetReport;
import models.timesheet.TimesheetReport.Status;
import models.timesheet.TimesheetReport.Type;
import models.timesheet.TimesheetSummary;
import models.timesheet.TimesheetSummaryForActor;
import play.Logger;
import play.Play;

/**
 * DAO for the {@link TimesheetActivity} and
 * {@link TimesheetActivityAllocatedActor} and {@link TimesheetActivityType} and
 * {@link TimesheetEntry} and {@link TimesheetLog} and {@link TimesheetReport}
 * objects.
 * 
 * @author Johann Kohler
 */
public abstract class TimesheetDao {

    public static Finder<Long, TimesheetActivity> findTimesheetActivity = new Finder<>(TimesheetActivity.class);

    public static Finder<Long, TimesheetActivityAllocatedActor> findTimesheetActivityAllocatedActor = new Finder<>(TimesheetActivityAllocatedActor.class);

    public static Finder<Long, TimesheetActivityType> findTimesheetActivityType = new Finder<>(TimesheetActivityType.class);

    public static Finder<Long, TimesheetEntry> findTimesheetEntry = new Finder<>(TimesheetEntry.class);

    public static Finder<Long, TimesheetLog> findTimesheetLog = new Finder<>(TimesheetLog.class);

    public static Finder<Long, TimesheetReport> findTimesheetReport = new Finder<>(TimesheetReport.class);

    private static Logger.ALogger log = Logger.of(TimesheetDao.class);

    /**
     * Default constructor.
     */
    public TimesheetDao() {
    }

    /**
     * Get a timesheet activity by id.
     * 
     * @param id
     *            the timesheet activity id
     */
    public static TimesheetActivity getTimesheetActivityById(Long id) {
        return findTimesheetActivity.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all timesheet activities.
     * 
     */
    public static List<TimesheetActivity> getTimesheetActivityAsList() {
        return findTimesheetActivity.where().eq("deleted", false).findList();
    }

    /**
     * Get the timesheet activities of a type.
     * 
     * @param activityTypeId
     *            the activity type id
     */
    public static List<TimesheetActivity> getTimesheetActivityAsListByActivityType(Long activityTypeId) {
        return findTimesheetActivity.where().eq("deleted", false).eq("timesheetActivityType.id", activityTypeId).findList();
    }

    /**
     * get an allocated actor by id.
     * 
     * @param id
     *            the allocated actor id
     */
    public static TimesheetActivityAllocatedActor getTimesheetActivityAllocatedActorById(Long id) {
        return findTimesheetActivityAllocatedActor.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all allocation of an actor as an expression list.
     * 
     * @param actorId
     *            the actor id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static ExpressionList<TimesheetActivityAllocatedActor> getTimesheetActivityAllocatedActorAsExprByActor(Long actorId, boolean activeOnly) {

        ExpressionList<TimesheetActivityAllocatedActor> expr = findTimesheetActivityAllocatedActor.orderBy("endDate").where().eq("deleted", false)
                .eq("actor.id", actorId).eq("timesheetActivity.deleted", false);

        if (activeOnly) {
            expr = expr.add(Expr.or(Expr.isNull("endDate"), Expr.gt("endDate", new Date())));
        }

        return expr;
    }

    /**
     * Get all allocation of an actor.
     * 
     * @param actorId
     *            the actor id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static List<TimesheetActivityAllocatedActor> getTimesheetActivityAllocatedActorAsListByActor(Long actorId, boolean activeOnly) {
        return getTimesheetActivityAllocatedActorAsExprByActor(actorId, activeOnly).findList();
    }

    /**
     * Get all allocation of an actor as a pagination object.
     * 
     * @param actorId
     *            the actor id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static Pagination<TimesheetActivityAllocatedActor> getTimesheetActivityAllocatedActorAsPaginationByActor(Long actorId, boolean activeOnly) {
        return new Pagination<>(getTimesheetActivityAllocatedActorAsExprByActor(actorId, activeOnly), 5,
                Play.application().configuration().getInt("maf.number_page_links"));
    }

    /**
     * Get all allocations of the actors of an org unit as an expression list.
     * 
     * @param orgUnitId
     *            the org unit id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static ExpressionList<TimesheetActivityAllocatedActor> getTimesheetActivityAllocatedActorAsExprByOrgUnit(Long orgUnitId, boolean activeOnly) {

        ExpressionList<TimesheetActivityAllocatedActor> expr = findTimesheetActivityAllocatedActor.where().eq("deleted", false).eq("actor.isActive", true)
                .eq("actor.deleted", false).eq("actor.orgUnit.id", orgUnitId).eq("timesheetActivity.deleted", false);

        if (activeOnly) {
            expr = expr.add(Expr.or(Expr.isNull("endDate"), Expr.gt("endDate", new Date())));
        }

        return expr;
    }

    /**
     * Get all allocations of the actors of an org unit.
     * 
     * @param orgUnitId
     *            the org unit id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static List<TimesheetActivityAllocatedActor> getTimesheetActivityAllocatedActorAsListByOrgUnit(Long orgUnitId, boolean activeOnly) {
        return getTimesheetActivityAllocatedActorAsExprByOrgUnit(orgUnitId, activeOnly).findList();
    }

    /**
     * Get all allocations of the active subordinates of an actor as an
     * expression list.
     * 
     * @param actorId
     *            id the actor id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static ExpressionList<TimesheetActivityAllocatedActor> getTimesheetActivityAllocatedActorAsExprByManager(Long actorId, boolean activeOnly) {

        ExpressionList<TimesheetActivityAllocatedActor> expr = findTimesheetActivityAllocatedActor.where().eq("deleted", false).eq("actor.isActive", true)
                .eq("actor.deleted", false).eq("actor.manager.id", actorId).eq("timesheetActivity.deleted", false);

        if (activeOnly) {
            expr = expr.add(Expr.or(Expr.isNull("endDate"), Expr.gt("endDate", new Date())));
        }

        return expr;
    }

    /**
     * Get all allocations of the active subordinates of an actor.
     * 
     * @param actorId
     *            the actor id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static List<TimesheetActivityAllocatedActor> getTimesheetActivityAllocatedActorAsListByManager(Long actorId, boolean activeOnly) {
        return getTimesheetActivityAllocatedActorAsExprByManager(actorId, activeOnly).findList();
    }

    /**
     * Get the allocated activities for an org unit according to some filters.
     * 
     * @param orgUnitId
     *            the org unit id
     * @param start
     *            the startDate or the endDate should be after this date
     * @param end
     *            the startDate or the endDate should be before this date
     */
    public static List<TimesheetActivityAllocatedActor> getTimesheetActivityAllocatedActorAsListByOrgUnitAndPeriod(Long orgUnitId, Date start, Date end) {
        return findTimesheetActivityAllocatedActor.where().eq("deleted", false).isNotNull("startDate").isNotNull("endDate").le("startDate", end)
                .ge("endDate", start).eq("actor.deleted", false).eq("actor.orgUnit.id", orgUnitId).findList();

    }

    /**
     * Get the allocated activities for a competency according to some filters.
     * 
     * @param competencyId
     *            the competency id
     * @param start
     *            the startDate or the endDate should be after this date
     * @param end
     *            the startDate or the endDate should be before this date
     */
    public static List<TimesheetActivityAllocatedActor> getTimesheetActivityAllocatedActorAsListByCompetencyAndPeriod(Long competencyId, Date start,
            Date end) {
        return findTimesheetActivityAllocatedActor.where().eq("deleted", false).isNotNull("startDate").isNotNull("endDate").le("startDate", end)
                .ge("endDate", start).eq("actor.deleted", false).eq("actor.defaultCompetency.id", competencyId).findList();

    }

    /**
     * Get the allocated activities for an actor according to some filters.
     * 
     * @param actorId
     *            the actor id
     * @param start
     *            the startDate or the endDate should be after this date
     * @param end
     *            the startDate or the endDate should be before this date
     */
    public static List<TimesheetActivityAllocatedActor> getTimesheetActivityAllocatedActorAsListByActorAndPeriod(Long actorId, Date start, Date end) {
        return findTimesheetActivityAllocatedActor.where().eq("deleted", false).isNotNull("startDate").isNotNull("endDate").le("startDate", end)
                .ge("endDate", start).eq("actor.id", actorId).findList();
    }

    /**
     * Get a timesheet activity type by id.
     * 
     * @param id
     *            the timesheet activity type id
     */
    public static TimesheetActivityType getTimesheetActivityTypeById(Long id) {
        return findTimesheetActivityType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all timesheet activity types.
     * 
     */
    public static List<TimesheetActivityType> getTimesheetActivityTypeAsList() {
        return findTimesheetActivityType.where().eq("deleted", false).findList();
    }

    /**
     * Get all timesheet activity types as a value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getTimesheetActivityTypeAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getTimesheetActivityTypeAsList());
    }

    /**
     * Get a timesheet entry by id.
     * 
     * @param id
     *            the timesheet entry id
     */
    public static TimesheetEntry getTimesheetEntryById(Long id) {
        return findTimesheetEntry.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get a timesheet log by id.
     * 
     * @param id
     *            the timesheet log id
     */
    public static TimesheetLog getTimesheetLogById(Long id) {
        return findTimesheetLog.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get the total timesheeted hours of a planning package.
     * 
     * @param planningPackage
     *            the planning package
     */
    public static BigDecimal getTimesheetLogAsTotalHoursByPEPlanningPackage(PortfolioEntryPlanningPackage planningPackage) {

        String sql = "SELECT SUM(tl.hours) AS totalHours FROM timesheet_log tl " + "JOIN timesheet_entry te ON tl.timesheet_entry_id = te.id "
                + "JOIN timesheet_report tr ON te.timesheet_report_id = tr.id "
                + "WHERE tl.deleted = false AND te.deleted = false AND tr.deleted = false AND te.portfolio_entry_planning_package_id = '" + planningPackage.id
                + "'";

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalHours> query = Ebean.find(TotalHours.class);

        BigDecimal totalHours = query.setRawSql(rawSql).findUnique().totalHours;

        if (totalHours == null) {
            return BigDecimal.ZERO;
        }

        return totalHours;
    }

    /**
     * Get the total timesheeted hours of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry
     */
    public static BigDecimal getTimesheetLogAsTotalHoursByPE(Long portfolioEntryId) {

        String sql = "SELECT SUM(tl.hours) AS totalHours FROM timesheet_log tl " + "JOIN timesheet_entry te ON tl.timesheet_entry_id = te.id "
                + "JOIN timesheet_report tr ON te.timesheet_report_id = tr.id "
                + "WHERE tl.deleted = false AND te.deleted = false AND tr.deleted = false AND te.portfolio_entry_id = '" + portfolioEntryId
                + "' AND (tr.status = '" + Status.APPROVED.name() + "' OR tr.status = '" + Status.LOCKED.name() + "' )";

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalHours> query = Ebean.find(TotalHours.class);

        BigDecimal totalHours = query.setRawSql(rawSql).findUnique().totalHours;

        if (totalHours == null) {
            return BigDecimal.ZERO;
        }

        return totalHours;
    }

    /**
     * Get the timesheet logs of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry
     */
    public static ExpressionList<TimesheetLog> getTimesheetLogAsExprByPortfolioEntry(Long portfolioEntryId) {
        return findTimesheetLog.where().eq("deleted", false).ne("hours", 0).eq("timesheetEntry.deleted", false)
                .eq("timesheetEntry.portfolioEntry.id", portfolioEntryId).eq("timesheetEntry.timesheetReport.deleted", false);
    }

    /**
     * Get the active timesheet logs of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry
     */
    public static ExpressionList<TimesheetLog> getTimesheetLogActiveAsExprByPortfolioEntry(Long portfolioEntryId) {
        return findTimesheetLog.where().eq("deleted", false).ne("hours", 0).eq("timesheetEntry.deleted", false)
                .eq("timesheetEntry.portfolioEntry.id", portfolioEntryId).eq("timesheetEntry.timesheetReport.deleted", false).disjunction()
                .eq("timesheetEntry.timesheetReport.status", Status.APPROVED).eq("timesheetEntry.timesheetReport.status", Status.LOCKED);
    }

    /**
     * Get a timesheet report by id.
     * 
     * @param id
     *            the timesheet report id
     */
    public static TimesheetReport getTimesheetReportById(Long id) {
        return findTimesheetReport.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get a timesheet report of an actor for a start date.
     * 
     * @param actorId
     *            the actor id
     * @param startDate
     *            the start date
     */
    public static TimesheetReport getTimesheetReportByActorAndStartDate(Long actorId, Date startDate) {
        return findTimesheetReport.where().eq("deleted", false).eq("actor.id", actorId).eq("startDate", startDate).findUnique();
    }

    public static List<TimesheetSummary> getTimesheetLogSummary(Long portfolioEntryId, String groupBy, Long actorId, String startDateString, String endDateString) {
        Date startDate = null;
        Date endDate = null;
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            if (startDateString != null) {
                startDate = formatter.parse(startDateString);
            }
            if (endDateString != null) {
                endDate = formatter.parse(endDateString);
            }
        } catch (ParseException e) {
            log.error(e.getMessage());
        }

        ExpressionList<TimesheetLog> expressionList = findTimesheetLog
                .fetch("timesheetEntry.timesheetReport.actor")
                .where()
                .eq("timesheetEntry.portfolioEntry.id", portfolioEntryId)
                .ne("hours", 0);
        if (actorId != 0) {
            expressionList = expressionList.eq("timesheetEntry.timesheetReport.actor.id", actorId);
        }
        if (startDate != null) {
            expressionList = expressionList.gt("logDate", startDate);
        }
        if (endDate != null) {
            expressionList = expressionList.lt("logDate", endDate);
        }
        List<TimesheetLog> logs = expressionList.findList();
        Map<Long, Actor> actorMap = new HashMap<>();
        List<TimesheetSummary> summaries = new ArrayList<>();
        DateFormat monthFormatter = new SimpleDateFormat("yyyy-MM");
        DateFormat weekFormatter = new SimpleDateFormat("yyyy-w");

        Map<Long, List<TimesheetLog>> logsByActor = logs.stream().collect(Collectors.groupingBy(log -> {
            actorMap.put(log.timesheetEntry.timesheetReport.actor.id, log.timesheetEntry.timesheetReport.actor);
            return log.timesheetEntry.timesheetReport.actor.id;
        }));

        Map<Long, Map<String, Double>> hoursByActorGroupByPeriod = new HashMap<>();
        logsByActor.keySet().forEach(aId -> {
            Map<String, Double> groupedHours = logsByActor.get(aId).stream().collect(Collectors.toMap(
                    timesheetLog -> {
                        if ("month".equalsIgnoreCase(groupBy)) {
                            return monthFormatter.format(timesheetLog.logDate);
                        }
                        if ("week".equalsIgnoreCase(groupBy)) {
                            return weekFormatter.format(timesheetLog.logDate);
                        }
                        return "";
                    },
                    timesheetLog -> timesheetLog.hours,
                    Double::sum
            ));
            hoursByActorGroupByPeriod.put(aId, groupedHours);
        });

        hoursByActorGroupByPeriod.keySet().forEach(aId -> {
            TimesheetSummary timesheetSummary = new TimesheetSummary();
            List<TimesheetSummaryForActor> timesheetSummaryForActors = new ArrayList<>();
            timesheetSummary.setActor(actorMap.get(aId));
            for (String period : hoursByActorGroupByPeriod.get(aId).keySet()) {
                TimesheetSummaryForActor timesheetSummaryForActor = new TimesheetSummaryForActor();
                if ("month".equalsIgnoreCase(groupBy)) {
                    timesheetSummaryForActor.setMonth(period);
                }
                if ("week".equalsIgnoreCase(groupBy)) {
                    timesheetSummaryForActor.setWeek(period);
                }
                timesheetSummaryForActor.setHours(BigDecimal.valueOf(hoursByActorGroupByPeriod.get(aId).get(period)).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                timesheetSummaryForActors.add(timesheetSummaryForActor);
            }
            timesheetSummary.setTimesheetSummary(timesheetSummaryForActors);
            summaries.add(timesheetSummary);
        });

        return summaries;
    }

    /**
     * Look for a timesheet entry with the specified characteristics
     * @param timeSheetReportId a report id
     * @param peId a portfolioentryId
     * @param pePlanningPackageId a planning package Id (optional, can be null)
     * @return a timesheet entry
     */
    public static TimesheetEntry getTimesheetEntryByPEandPEPlanningPackage(Long timeSheetReportId, Long peId, Long pePlanningPackageId){
        List<TimesheetEntry> list=null;
        if(pePlanningPackageId==null){
            list=findTimesheetEntry.where().eq("deleted", false).eq("timesheetReport.id", timeSheetReportId).eq("portfolioEntry.id",peId).findList();
        }else{
            list=findTimesheetEntry.where().eq("deleted", false).eq("timesheetReport.id", timeSheetReportId).eq("portfolioEntry.id",peId).eq("portfolioEntryPlanningPackage.id",pePlanningPackageId).findList();
        }
        return (list!=null&&list.size()!=0)?list.get(0):null;
    }

    /**
     * Get the submitted timesheets of the subordinates of a manager.
     * 
     * @param managerId
     *            the manager id
     */
    public static List<TimesheetReport> getTimesheetReportSubmittedAsListByManager(Long managerId) {
        return findTimesheetReport.where().eq("deleted", false).eq("actor.manager.id", managerId).eq("status", Status.SUBMITTED).findList();
    }

    /**
     * Get the late timesheets of the subordinates of a manager.
     * 
     * For each subordinate we return only late timesheets included in the
     * reminder limit.
     * 
     * @param managerId
     *            the manager id
     * @param preferenceManagerPlugin
     *            the preference manager service
     */
    public static List<TimesheetReport> getTimesheetReportLateAsListByManager(Long managerId, IPreferenceManagerPlugin preferenceManagerPlugin) {

        // compute the period aggregate
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        Date periodEndDate = cal.getTime();
        cal.add(Calendar.WEEK_OF_YEAR, -1 * getTimesheetReportReminderLimit(preferenceManagerPlugin) + 1);
        Date periodStartDate = cal.getTime();

        return findTimesheetReport.orderBy("startDate, actor.id").where().eq("deleted", false).eq("actor.manager.id", managerId)
                .le("startDate", periodEndDate).ge("startDate", periodStartDate).disjunction().eq("status", Status.OPEN).eq("status", Status.REJECTED)
                .eq("status", Status.UNLOCKED).findList();
    }

    /**
     * Create the missing reports for an actor.
     * 
     * A report is missing if it is in a past period (computed with the given
     * type) and doesn't exist in the DB. We consider only the last past periods
     * defined by the reminder limit.
     * 
     * @param type
     *            the report type
     * @param actor
     *            the actor
     * @param preferenceManagerPlugin
     *            the preference manager service
     */
    public static void createMissingTimesheetReport(Type type, Actor actor, IPreferenceManagerPlugin preferenceManagerPlugin) {

        switch (type) {
        case WEEKLY:

            // compute the period aggregate
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            Date periodEndDate = cal.getTime();
            cal.add(Calendar.WEEK_OF_YEAR, -1 * getTimesheetReportReminderLimit(preferenceManagerPlugin) + 1);
            Date periodStartDate = cal.getTime();

            // compute the number of existing reports
            Integer n = findTimesheetReport.where().eq("deleted", false).eq("actor.id", actor.id).le("startDate", periodEndDate)
                    .ge("startDate", periodStartDate).findRowCount();

            // create the reports only if at least one is missing
            if (n != getTimesheetReportReminderLimit(preferenceManagerPlugin)) {

                Logger.debug("some reports are missing for the actor " + actor.uid);

                for (int i = 0; i < getTimesheetReportReminderLimit(preferenceManagerPlugin); i++) {

                    // at start the current date is equal to periodStartDate
                    Date currentDate = cal.getTime();

                    TimesheetReport report = getTimesheetReportByActorAndStartDate(actor.id, currentDate);
                    if (report == null) {

                        Logger.debug("the report for the start date '" + currentDate + "' is missing");

                        report = new TimesheetReport();
                        report.actor = actor;
                        report.orgUnit = actor.orgUnit;
                        report.type = TimesheetReport.Type.WEEKLY;
                        report.startDate = currentDate;
                        report.status = TimesheetReport.Status.OPEN;
                        report.save();
                    }

                    // add a week to the current date
                    cal.add(Calendar.WEEK_OF_YEAR, 1);

                }

            }

            break;

        default:
            break;
        }

    }

    /**
     * Return true if the timesheets should be approved by the managers.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     */
    public static boolean getTimesheetReportMustApprove(IPreferenceManagerPlugin preferenceManagerPlugin) {
        return preferenceManagerPlugin.getPreferenceValueAsBoolean(IMafConstants.TIMESHEET_MUST_APPROVE_PREFERENCE);
    }

    /**
     * Return the reminder limit (number of reports of the past for an actor).
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     */
    public static Integer getTimesheetReportReminderLimit(IPreferenceManagerPlugin preferenceManagerPlugin) {
        return preferenceManagerPlugin.getPreferenceValueAsInteger(IMafConstants.TIMESHEET_REMINDER_LIMIT_PREFERENCE);
    }

    /**
     * Return the expected number of hours for a day.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     */
    public static BigDecimal getTimesheetReportHoursPerDay(IPreferenceManagerPlugin preferenceManagerPlugin) {
        return preferenceManagerPlugin.getPreferenceValueAsDecimal(IMafConstants.TIMESHEET_HOURS_PER_DAY);
    }

}
