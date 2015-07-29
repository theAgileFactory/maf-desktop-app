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

import java.util.Date;

import models.framework_models.parent.IModelConstants;
import models.governance.LifeCycleMilestoneInstanceApprover;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

/**
 * A process milestone approval form data is used to manage the fields when
 * processing a milestone approval (vote).
 * 
 * @author Johann Kohler
 */
public class ProcessMilestoneApprovalFormData {

    public Long approverInstanceId;

    @Required
    public boolean hasApproved;

    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String comments;

    /**
     * Default constructor.
     */
    public ProcessMilestoneApprovalFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param lifeCycleMilestoneInstanceApprover
     *            the life cycle milestone instance approver in the DB
     */
    public ProcessMilestoneApprovalFormData(LifeCycleMilestoneInstanceApprover lifeCycleMilestoneInstanceApprover) {
        approverInstanceId = lifeCycleMilestoneInstanceApprover.id;
        hasApproved = true;
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param lifeCycleMilestoneInstanceApprover
     *            the cycle milestone instance approver in the DB
     */
    public void fill(LifeCycleMilestoneInstanceApprover lifeCycleMilestoneInstanceApprover) {
        lifeCycleMilestoneInstanceApprover.hasApproved = hasApproved;
        lifeCycleMilestoneInstanceApprover.comments = comments;
        lifeCycleMilestoneInstanceApprover.approvalDate = new Date();
    }
}
