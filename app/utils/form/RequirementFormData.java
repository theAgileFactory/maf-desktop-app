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

import dao.delivery.DeliverableDAO;
import dao.delivery.IterationDAO;
import dao.delivery.RequirementDAO;
import dao.pmo.ActorDao;
import models.delivery.Deliverable;
import models.delivery.Requirement;
import models.framework_models.parent.IModelConstants;
import models.pmo.Actor;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

/**
 * A requirement form data is used to manage the fields when editing a
 * requirement.
 * 
 * @author Johann Kohler
 */
public class RequirementFormData {

    // the portfolioEntry id
    public Long id;

    public Long requirementId;

    public boolean isDefect;

    @Required
    @MaxLength(value = IModelConstants.LARGE_STRING)
    public String name;

    public String description;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String category;

    public Long requirementStatus;

    public Long requirementPriority;

    public Long requirementSeverity;

    public Long author;

    public Integer storyPoints;

    public Double initialEstimation;

    public Double effort;

    public Double remainingEffort;

    public boolean isScoped;

    public Long iteration;

    public List<Long> deliverables = new ArrayList<Long>();

    /**
     * Default constructor.
     */
    public RequirementFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param requirement
     *            the requirement in the DB
     */
    public RequirementFormData(Requirement requirement) {
        this.id = requirement.portfolioEntry.id;
        this.requirementId = requirement.id;

        this.isDefect = requirement.isDefect;
        this.name = requirement.name;
        this.description = requirement.description;
        this.category = requirement.category;
        this.requirementStatus = requirement.requirementStatus != null ? requirement.requirementStatus.id : null;
        this.requirementPriority = requirement.requirementPriority != null ? requirement.requirementPriority.id : null;
        this.requirementSeverity = requirement.requirementSeverity != null ? requirement.requirementSeverity.id : null;
        this.author = requirement.author != null ? requirement.author.id : null;
        this.storyPoints = requirement.storyPoints;
        this.initialEstimation = requirement.initialEstimation;
        this.effort = requirement.effort;
        this.remainingEffort = requirement.remainingEffort;
        this.isScoped = requirement.isScoped != null ? requirement.isScoped : false;
        this.iteration = requirement.iteration != null ? requirement.iteration.id : null;
        for (Deliverable deliverable : DeliverableDAO.getDeliverableAsListByRequirement(requirement.id)) {
            this.deliverables.add(deliverable.id);
        }
    }

    /**
     * Construct the form data with default value.
     * 
     * @param author
     *            the requirement author
     */
    public RequirementFormData(Actor author) {
        this.author = author.id;
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param requirement
     *            the requirement in the DB
     */
    public void fill(Requirement requirement) {
        requirement.isDefect = this.isDefect;
        requirement.name = this.name;
        requirement.description = this.description;
        requirement.category = this.category;
        requirement.requirementStatus = this.requirementStatus != null ? RequirementDAO.getRequirementStatusById(this.requirementStatus) : null;
        requirement.requirementPriority = this.requirementPriority != null ? RequirementDAO.getRequirementPriorityById(this.requirementPriority) : null;
        requirement.requirementSeverity = this.requirementSeverity != null ? RequirementDAO.getRequirementSeverityById(this.requirementSeverity) : null;
        requirement.author = this.author != null ? ActorDao.getActorById(this.author) : null;
        requirement.storyPoints = this.storyPoints;
        requirement.initialEstimation = this.initialEstimation;
        requirement.effort = this.effort;
        requirement.remainingEffort = this.remainingEffort;
        requirement.isScoped = this.isScoped;
        requirement.iteration = this.iteration != null ? IterationDAO.getIterationById(this.iteration) : null;
        requirement.deliverables = new ArrayList<Deliverable>();
        for (Long deliverableId : this.deliverables) {
            requirement.deliverables.add(DeliverableDAO.getDeliverableById(deliverableId));
        }
    }
}
