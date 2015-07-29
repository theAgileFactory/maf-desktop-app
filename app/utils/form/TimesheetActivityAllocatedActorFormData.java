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

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import dao.pmo.ActorDao;
import dao.timesheet.TimesheetDao;
import framework.utils.Utilities;
import models.timesheet.TimesheetActivityAllocatedActor;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.i18n.Messages;

/**
 * A timesheet activity allocated actor form data is used to manage the fields
 * when adding/editing an allocated actor for a timesheet activity.
 * 
 * @author Johann Kohler
 */
public class TimesheetActivityAllocatedActorFormData {

    // the current object id (actor id or org unit id)
    public Long id;

    public Long allocatedActivityId;

    @Required
    public Long actorId;

    @Required
    public Long timesheetActivityType;

    @Required
    public Long timesheetActivity;

    @Required
    public BigDecimal days;

    public String startDate;

    public String endDate;

    /**
     * Validate the dates.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        try {

            if (!this.startDate.equals("") && this.endDate.equals("")) {
                // the start date cannot be filled alone
                errors.add(new ValidationError("startDate", Messages.get("object.allocated_resource.start_date.invalid")));
            }

            if (!this.startDate.equals("") && !this.endDate.equals("")
                    && Utilities.getDateFormat(null).parse(this.startDate).after(Utilities.getDateFormat(null).parse(this.endDate))) {
                // the end date should be after the start date
                errors.add(new ValidationError("endDate", Messages.get("object.allocated_resource.end_date.invalid")));
            }

        } catch (Exception e) {
            Logger.warn("impossible to parse the allocation dates when testing the formats");
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Default constructor.
     */
    public TimesheetActivityAllocatedActorFormData() {
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
        this.allocatedActivityId = allocatedActivity.id;

        this.actorId = allocatedActivity.actor.id;
        this.timesheetActivityType = allocatedActivity.timesheetActivity.timesheetActivityType.id;
        this.timesheetActivity = allocatedActivity.timesheetActivity.id;
        this.days = allocatedActivity.days;
        this.startDate = allocatedActivity.startDate != null ? Utilities.getDateFormat(null).format(allocatedActivity.startDate) : null;
        this.endDate = allocatedActivity.endDate != null ? Utilities.getDateFormat(null).format(allocatedActivity.endDate) : null;

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

    }
}
