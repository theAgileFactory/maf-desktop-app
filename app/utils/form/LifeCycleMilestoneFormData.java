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

import dao.governance.LifeCycleMilestoneDao;
import dao.pmo.ActorDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.CustomConstraints.MultiLanguagesStringMaxLength;
import framework.utils.CustomConstraints.MultiLanguagesStringRequired;
import framework.utils.Msg;
import framework.utils.MultiLanguagesString;
import models.framework_models.parent.IModelConstants;
import models.governance.LifeCycleMilestone;
import models.pmo.Actor;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;

/**
 * A life cycle process form data is used to manage the fields when managing a
 * life cycle process.
 * 
 * @author Johann Kohler
 */
public class LifeCycleMilestoneFormData {

    public Long id;

    public Long lifeCycleProcessId;

    @MultiLanguagesStringRequired
    @MultiLanguagesStringMaxLength(value = IModelConstants.SMALL_STRING)
    public MultiLanguagesString shortName;

    @MultiLanguagesStringRequired
    @MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString name;

    @MultiLanguagesStringMaxLength(value = IModelConstants.VLARGE_STRING)
    public MultiLanguagesString description;

    public boolean isReviewRequired;

    @Required
    public Long defaultStatusType;

    public String type;

    public boolean isActive;

    public List<Long> approvers = new ArrayList<Long>();

    /**
     * Default constructor.
     */
    public LifeCycleMilestoneFormData() {
    }

    /**
     * Form validation.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        // check the type is not already used by another milestone of the
        // process
        if (this.type != null && !this.type.equals("")) {
            LifeCycleMilestone otherMilestone = LifeCycleMilestoneDao.getLCMilestoneByProcessAndType(this.lifeCycleProcessId,
                    LifeCycleMilestone.Type.valueOf(this.type));
            if (otherMilestone != null) {
                if (this.id != null) {
                    // edit case
                    if (!this.id.equals(otherMilestone.id)) {
                        errors.add(new ValidationError("type", Msg.get("object.life_cycle_milestone.type.invalid")));
                    }
                } else {
                    // create case
                    errors.add(new ValidationError("type", Msg.get("object.life_cycle_milestone.type.invalid")));
                }
            }
        }

        return errors.isEmpty() ? null : errors;

    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param lifeCycleMilestone
     *            the life cycle milestone in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public LifeCycleMilestoneFormData(LifeCycleMilestone lifeCycleMilestone, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = lifeCycleMilestone.id;
        this.shortName = MultiLanguagesString.getByKey(lifeCycleMilestone.shortName, i18nMessagesPlugin);
        this.name = MultiLanguagesString.getByKey(lifeCycleMilestone.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(lifeCycleMilestone.description, i18nMessagesPlugin);
        this.isReviewRequired = lifeCycleMilestone.isReviewRequired;
        this.defaultStatusType = lifeCycleMilestone.defaultLifeCycleMilestoneInstanceStatusType.id;
        this.isActive = lifeCycleMilestone.isActive;
        this.type = lifeCycleMilestone.type != null ? lifeCycleMilestone.type.name() : null;
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
        lifeCycleMilestone.type = this.type != null && !this.type.equals("") ? LifeCycleMilestone.Type.valueOf(this.type) : null;
        lifeCycleMilestone.defaultLifeCycleMilestoneInstanceStatusType = LifeCycleMilestoneDao.getLCMilestoneInstanceStatusTypeById(this.defaultStatusType);
        lifeCycleMilestone.approvers = new ArrayList<Actor>();
        for (Long approver : this.approvers) {
            lifeCycleMilestone.approvers.add(ActorDao.getActorById(approver));
        }

    }

}
