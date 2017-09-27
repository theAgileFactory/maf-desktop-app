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

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import com.google.common.base.Strings;
import models.framework_models.parent.IModelConstants;
import models.governance.LifeCycleInstancePlanning;
import models.governance.LifeCycleMilestoneInstance;
import models.governance.PlannedLifeCycleMilestoneInstance;
import models.governance.ProcessTransitionRequest;
import models.governance.ProcessTransitionRequest.RequestType;
import models.pmo.Actor;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import dao.governance.LifeCycleMilestoneDao;
import dao.governance.LifeCyclePlanningDao;
import dao.pmo.PortfolioEntryDao;
import framework.utils.FileField;
import framework.utils.FileFieldOptionalValidator;
import framework.utils.Utilities;

/**
 * A request milestone form data is used to manage the fields when requesting a
 * new milestone.
 * 
 * @author Johann Kohler
 */
public class RequestMilestoneFormData implements Serializable {

    private static final long serialVersionUID = 1L;

    // portfolio entry id
    public Long id;

    public Long milestoneId;

    @Required
    public String passedDate;

    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String comments;

    @ValidateWith(value = FileFieldOptionalValidator.class, message = "form.input.file_field.error")
    public FileField descriptionDocument;

    /**
     * Default constructor.
     */
    public RequestMilestoneFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param milestoneId
     *            the milestone id
     */
    public RequestMilestoneFormData(Long portfolioEntryId, Long milestoneId) {
        this.id = portfolioEntryId;
        this.milestoneId = milestoneId;
        LifeCycleInstancePlanning planning = LifeCyclePlanningDao.getLCInstancePlanningAsLastByPE(id);
        PlannedLifeCycleMilestoneInstance  plannedLifeCycleMilestoneInstance = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceByLCInstancePlanningAndLCMilestone(planning.id,
                milestoneId);
        if (Strings.isNullOrEmpty(this.passedDate))
        {
        	try
        	{
        		this.passedDate = Utilities.getDateFormat(null).format(plannedLifeCycleMilestoneInstance.plannedDate);
        	}
        	catch (Exception e) {
                this.passedDate = null;
            }
        }
    }

    /**
     * Fill a process transition request with the form data.
     * 
     * @param processTransitionRequest
     *            the process transition request to fill.
     * @param actor
     *            the actor which makes the request
     */
    public void create(ProcessTransitionRequest processTransitionRequest, Actor actor) {
        processTransitionRequest.comments = comments;
        processTransitionRequest.creationDate = new Date();
        processTransitionRequest.requester = actor;
        processTransitionRequest.requestType = RequestType.MILESTONE_APPROVAL.name();
        processTransitionRequest.accepted = null;
        processTransitionRequest.title = "";

    }

    /**
     * Fill a life cycle milestone instance with the form data.
     * 
     * @param lifeCycleMilestoneInstance
     *            the life cycle milestone instance to fill
     * @param hasAttachment
     *            set to true if the request has an attachment
     */
    public void create(LifeCycleMilestoneInstance lifeCycleMilestoneInstance, boolean hasAttachment) {

        lifeCycleMilestoneInstance.gateComments = comments;
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
