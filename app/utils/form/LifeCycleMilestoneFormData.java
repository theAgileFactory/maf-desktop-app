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

import models.governance.LifeCycleMilestone;
import models.pmo.Actor;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import dao.governance.LifeCycleMilestoneDao;
import dao.pmo.ActorDao;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A life cycle process form data is used to manage the fields when managing a
 * life cycle process.
 * 
 * @author Johann Kohler
 */
public class LifeCycleMilestoneFormData {

    public Long id;

    public Long lifeCycleProcessId;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString shortName;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    public boolean isReviewRequired;

    @Required
    public Long defaultStatusType;

    public boolean isActive;

    public List<Long> approvers = new ArrayList<Long>();

    /**
     * Default constructor.
     */
    public LifeCycleMilestoneFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param lifeCycleMilestone
     *            the life cycle milestone in the DB
     */
    public LifeCycleMilestoneFormData(LifeCycleMilestone lifeCycleMilestone) {

        this.id = lifeCycleMilestone.id;
        this.shortName = MultiLanguagesString.getByKey(lifeCycleMilestone.shortName);
        this.name = MultiLanguagesString.getByKey(lifeCycleMilestone.name);
        this.description = MultiLanguagesString.getByKey(lifeCycleMilestone.description);
        this.isReviewRequired = lifeCycleMilestone.isReviewRequired;
        this.defaultStatusType = lifeCycleMilestone.defaultLifeCycleMilestoneInstanceStatusType.id;
        this.isActive = lifeCycleMilestone.isActive;
        if (lifeCycleMilestone.approvers != null) {
            for (Actor approver : lifeCycleMilestone.approvers) {
                this.approvers.add(approver.id);
            }
        }

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param lifeCycleMilestone
     *            the life cycle milestone in the DB
     */
    public void fill(LifeCycleMilestone lifeCycleMilestone) {

        lifeCycleMilestone.isActive = this.isActive;
        lifeCycleMilestone.shortName = this.shortName.getKeyIfValue();
        lifeCycleMilestone.name = this.name.getKeyIfValue();
        lifeCycleMilestone.description = this.description.getKeyIfValue();
        lifeCycleMilestone.isReviewRequired = this.isReviewRequired;
        lifeCycleMilestone.defaultLifeCycleMilestoneInstanceStatusType = LifeCycleMilestoneDao.getLCMilestoneInstanceStatusTypeById(this.defaultStatusType);
        lifeCycleMilestone.approvers = new ArrayList<Actor>();
        for (Long approver : this.approvers) {
            lifeCycleMilestone.approvers.add(ActorDao.getActorById(approver));
        }

    }

}
