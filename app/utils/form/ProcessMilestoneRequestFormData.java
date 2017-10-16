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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import models.framework_models.parent.IModelConstants;
import models.governance.LifeCycleMilestone;
import models.governance.LifeCycleMilestoneInstance;
import models.pmo.Actor;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import dao.governance.LifeCycleMilestoneDao;
import dao.pmo.PortfolioEntryDao;
import framework.utils.Utilities;

/**
 * A process milestone request form data is used to manage the fields when
 * processing a milestone request.
 * 
 * @author Johann Kohler
 */
public class ProcessMilestoneRequestFormData {

    // the portfolio entry id
    public Long id;

    public Long requestId;

    public Long milestoneId;

    @Required
    public String passedDate;

    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String gateComments;

    public List<Long> approvers = new ArrayList<>();

    /**
     * Default constructor.
     */
    public ProcessMilestoneRequestFormData() {
    }

    /**
     * Construct the form data with a request milestone form data.
     * 
     * @param requestMilestoneFormData
     *            the request milestone form data
     * @param requestId
     *            the request id (DB)
     */
    public ProcessMilestoneRequestFormData(RequestMilestoneFormData requestMilestoneFormData, Long requestId) {

        this.id = requestMilestoneFormData.id;
        this.milestoneId = requestMilestoneFormData.milestoneId;
        this.requestId = requestId;
        this.passedDate = requestMilestoneFormData.passedDate;

        LifeCycleMilestone milestone = LifeCycleMilestoneDao.getLCMilestoneById(requestMilestoneFormData.milestoneId);
        for (Actor approver : milestone.approvers) {
            approvers.add(approver.id);
        }

    }

    /**
     * Fill the DB entry with the form values.
     *  @param lifeCycleMilestoneInstance
     *            the life cycle milestone instance in the DB
     * @param hasAttachment
     *             true if milestone request has attached document
     * @param requestComments
     *            the comment in the initial milestone request
     */
    public void fill(LifeCycleMilestoneInstance lifeCycleMilestoneInstance, boolean hasAttachment, String requestComments) {

        lifeCycleMilestoneInstance.gateComments = gateComments;
        lifeCycleMilestoneInstance.requestComments = requestComments;
        lifeCycleMilestoneInstance.isPassed = false;
        lifeCycleMilestoneInstance.lifeCycleInstance = PortfolioEntryDao.getPEById(id).activeLifeCycleInstance;
        lifeCycleMilestoneInstance.lifeCycleMilestone = LifeCycleMilestoneDao.getLCMilestoneById(milestoneId);
        try {
            lifeCycleMilestoneInstance.passedDate = Utilities.getDateFormat(null).parse(passedDate);
        } catch (ParseException e) {
            lifeCycleMilestoneInstance.passedDate = null;
        }
        lifeCycleMilestoneInstance.hasAttachments = hasAttachment;

    }
}
