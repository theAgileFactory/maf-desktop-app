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

import framework.utils.Utilities;
import models.governance.PlannedLifeCycleMilestoneInstance;

import java.text.ParseException;

/**
 * An planned date form data is used to update a planned date.
 * 
 * @author Johann Kohler
 */
public class PlannedDateFormData extends AbstractFormData<PlannedLifeCycleMilestoneInstance> {

    public Long plannedDateId;

    public String plannedDate;

    /**
     * Default constructor.
     */
    public PlannedDateFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param plannedLifeCycleMilestoneInstance
     *            the planned milestone instance in the DB
     */
    public PlannedDateFormData(PlannedLifeCycleMilestoneInstance plannedLifeCycleMilestoneInstance) {

        this.plannedDateId = plannedLifeCycleMilestoneInstance.id;
        this.plannedDate = plannedLifeCycleMilestoneInstance.plannedDate != null ? Utilities.getDateFormat(null).format(
                plannedLifeCycleMilestoneInstance.plannedDate) : null;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param plannedLifeCycleMilestoneInstance
     *            the planned milestone instance in the DB
     */
    public void fillEntity(PlannedLifeCycleMilestoneInstance plannedLifeCycleMilestoneInstance) {
        try {
            plannedLifeCycleMilestoneInstance.plannedDate = Utilities.getDateFormat(null).parse(plannedDate);
        } catch (ParseException e) {
            plannedLifeCycleMilestoneInstance.plannedDate = null;
        }
    }

}
