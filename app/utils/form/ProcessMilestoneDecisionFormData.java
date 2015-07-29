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

import models.framework_models.parent.IModelConstants;
import models.governance.LifeCycleMilestoneInstance;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import framework.utils.Utilities;

/**
 * A process milestone decision form data is used to manage the fields when
 * processing a milestone decision (final approval).
 * 
 * @author Johann Kohler
 */
public class ProcessMilestoneDecisionFormData {

    public Long milestoneInstanceId;

    @Required
    public Long lifeCycleMilestoneInstanceStatusType;

    @Required
    public String passedDate;

    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String comments;

    /**
     * Default constructor.
     */
    public ProcessMilestoneDecisionFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param lifeCycleMilestoneInstance
     *            the life cycle milestone instance in the DB
     */
    public ProcessMilestoneDecisionFormData(LifeCycleMilestoneInstance lifeCycleMilestoneInstance) {
        this.milestoneInstanceId = lifeCycleMilestoneInstance.id;
        this.lifeCycleMilestoneInstanceStatusType = lifeCycleMilestoneInstance.lifeCycleMilestone.defaultLifeCycleMilestoneInstanceStatusType.id;
        this.passedDate = Utilities.getDateFormat(null).format(lifeCycleMilestoneInstance.passedDate);
    }
}
