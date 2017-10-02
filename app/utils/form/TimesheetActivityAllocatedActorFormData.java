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
package utils.form;

import constants.IMafConstants;
import dao.pmo.ActorDao;
import dao.timesheet.TimesheetDao;
import framework.services.account.IPreferenceManagerPlugin;
import framework.utils.Utilities;
import models.timesheet.TimesheetActivityAllocatedActor;
import models.timesheet.TimesheetActivityAllocatedActorDetail;
import play.Play;
import play.data.validation.Constraints.Required;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A timesheet activity allocated actor form data is used to manage the fields
 * when adding/editing an allocated actor for a timesheet activity.
 * 
 * @author Johann Kohler
 */
public class TimesheetActivityAllocatedActorFormData extends ResourceAllocationFormData {

    IPreferenceManagerPlugin preferenceManager;

    @Required
    public Long actorId;

    @Required
    public Long timesheetActivityType;

    @Required
    public Long timesheetActivity;

    /**
     * Default constructor.
     */
    public TimesheetActivityAllocatedActorFormData() {
        super();
        this.preferenceManager = Play.application().injector().instanceOf(IPreferenceManagerPlugin.class);
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param id
     *            the current object id (actor id or org unit id)
     * @param allocatedActivity
     *            the allocated activity in the DB
     */
    public TimesheetActivityAllocatedActorFormData(Long id, TimesheetActivityAllocatedActor allocatedActivity) {

        this.id = id;
        this.allocationId = allocatedActivity.id;

        this.actorId = allocatedActivity.actor.id;
        this.timesheetActivityType = allocatedActivity.timesheetActivity.timesheetActivityType.id;
        this.timesheetActivity = allocatedActivity.timesheetActivity.id;
        this.days = allocatedActivity.days;
        this.startDate = allocatedActivity.startDate != null ? Utilities.getDateFormat(null).format(allocatedActivity.startDate) : null;
        this.endDate = allocatedActivity.endDate != null ? Utilities.getDateFormat(null).format(allocatedActivity.endDate) : null;

        this.monthlyAllocated = allocatedActivity.monthlyAllocated;
        this.monthAllocations = new ArrayList<>();
        List<TimesheetActivityAllocatedActorDetail> details = allocatedActivity.timesheetActivityAllocatedActorDetails;
        for(TimesheetActivityAllocatedActorDetail detail : details) {
            Optional<MonthAllocation> optionalMonthlyAllocation = this.monthAllocations.stream().filter(allocation -> allocation.year.equals(detail.year)).findFirst();
            MonthAllocation monthAllocation = optionalMonthlyAllocation.isPresent() ? optionalMonthlyAllocation.get() : new MonthAllocation(detail.year);
            monthAllocation.addValue(detail.month, detail.days);
            if (!optionalMonthlyAllocation.isPresent()) {
                monthAllocations.add(monthAllocation);
            }
        }
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param allocatedActivity
     *            the allocated activity in the DB
     */
    public void fill(TimesheetActivityAllocatedActor allocatedActivity) {

        allocatedActivity.timesheetActivity = TimesheetDao.getTimesheetActivityById(this.timesheetActivity);
        allocatedActivity.days = this.days;
        allocatedActivity.actor = ActorDao.getActorById(this.actorId);

        try {
            allocatedActivity.startDate = Utilities.getDateFormat(null).parse(this.startDate);
        } catch (ParseException e) {
            allocatedActivity.startDate = null;
        }

        try {
            allocatedActivity.endDate = Utilities.getDateFormat(null).parse(this.endDate);
        } catch (ParseException e) {
            allocatedActivity.endDate = null;
        }

        allocatedActivity.monthlyAllocated = this.monthlyAllocated;

        if (this.monthlyAllocated) {

            // Set allocations by month
            List<TimesheetActivityAllocatedActorDetail> details = new ArrayList<>();
            for (MonthAllocation monthAllocation : monthAllocations) {
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 0, monthAllocation.januaryAllocationValue));
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 1, monthAllocation.februaryAllocationValue));
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 2, monthAllocation.marchAllocationValue));
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 3, monthAllocation.aprilAllocationValue));
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 4, monthAllocation.mayAllocationValue));
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 5, monthAllocation.juneAllocationValue));
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 6, monthAllocation.julyAllocationValue));
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 7, monthAllocation.augustAllocationValue));
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 8, monthAllocation.septemberAllocationValue));
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 9, monthAllocation.octoberAllocationValue));
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 10, monthAllocation.novemberAllocationValue));
                details.add((TimesheetActivityAllocatedActorDetail) processMonthAllocation(allocatedActivity, monthAllocation.year, 11, monthAllocation.decemberAllocationValue));
            }

            allocatedActivity.timesheetActivityAllocatedActorDetails.clear();
            allocatedActivity.timesheetActivityAllocatedActorDetails.addAll(details.stream().filter(detail -> detail != null).collect(Collectors.toList()));

            // Set start date and end date
            Comparator<? super TimesheetActivityAllocatedActorDetail> comp = (d1, d2) -> {
                int c = Integer.compare(d1.year, d2.year);
                return c == 0 ? Integer.compare(d1.month, d2.month) : c;
            };

            TimesheetActivityAllocatedActorDetail startMonth = allocatedActivity.timesheetActivityAllocatedActorDetails.stream().min(comp).get();
            TimesheetActivityAllocatedActorDetail endMonth = allocatedActivity.timesheetActivityAllocatedActorDetails.stream().max(comp).get();

            Calendar c = GregorianCalendar.getInstance();
            c.set(startMonth.year, startMonth.month, 1);
            allocatedActivity.startDate = c.getTime();

            c.set(endMonth.year, endMonth.month, 1);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            allocatedActivity.endDate = c.getTime();

        } else if (allocatedActivity.startDate != null && allocatedActivity.endDate != null) { // If start and end dates are provided, distribute evenly the days across the months
            allocatedActivity.computeAllocationDetails(false, preferenceManager.getPreferenceValueAsBoolean(IMafConstants.RESOURCES_WEEK_DAYS_ALLOCATION_PREFERENCE));
        } else { // If no manual allocation and no start date and end date are provided, just remove the monthly distribution
            allocatedActivity.clearAllocations();
        }

    }
}
