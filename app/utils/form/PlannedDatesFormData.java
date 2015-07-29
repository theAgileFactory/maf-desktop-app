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

import java.util.ArrayList;
import java.util.List;

import models.governance.PlannedLifeCycleMilestoneInstance;

/**
 * An planned dates form data is used to manage the planned dates of the last
 * planning.
 * 
 * @author Johann Kohler
 */
public class PlannedDatesFormData {

    public Long id;

    public List<PlannedDateFormData> plannedDates = new ArrayList<PlannedDateFormData>();

    /**
     * Default constructor.
     */
    public PlannedDatesFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param plannedLifeCycleMilestoneInstances
     *            the current planned milestones of a portfolio entry
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public PlannedDatesFormData(List<PlannedLifeCycleMilestoneInstance> plannedLifeCycleMilestoneInstances, Long portfolioEntryId) {

        id = portfolioEntryId;

        for (PlannedLifeCycleMilestoneInstance plannedLifeCycleMilestoneInstance : plannedLifeCycleMilestoneInstances) {
            plannedDates.add(new PlannedDateFormData(plannedLifeCycleMilestoneInstance));
        }

    }

}
