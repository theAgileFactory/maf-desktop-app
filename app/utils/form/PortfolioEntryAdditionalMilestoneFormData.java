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

import dao.governance.LifeCycleMilestoneDao;
import dao.pmo.ActorDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.CustomConstraints;
import framework.utils.MultiLanguagesString;
import models.framework_models.parent.IModelConstants;
import models.governance.LifeCycleMilestone;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Guillaume Petit
 */
public class PortfolioEntryAdditionalMilestoneFormData {

    /**
     * The milestone id
     */
    public Long id;

    /**
     * The previous milestone id (to set the correct order)
     */
    @Constraints.Required
    public Long previousMilestone;

    @CustomConstraints.MultiLanguagesStringRequired
    @CustomConstraints.MultiLanguagesStringMaxLength(value = IModelConstants.SMALL_STRING)
    public MultiLanguagesString shortName;

    @CustomConstraints.MultiLanguagesStringRequired
    @CustomConstraints.MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString name;

    @CustomConstraints.MultiLanguagesStringMaxLength(value = IModelConstants.VLARGE_STRING)
    public MultiLanguagesString description;

    public boolean isReviewRequired;

    @Constraints.Required
    public Long defaultStatusType;

    public List<Long> approvers = new ArrayList<>();

    public PortfolioEntryAdditionalMilestoneFormData() {
    }

    /**
     * Init the form with a db entry
     *
     * @param lifeCycleMilestone the additional lifecycle milestone
     * @param previousMilestoneId the id of the previous lifecycle milestone
     * @param ii18nMessagesPlugin the i18n messages plugin
     */
    public PortfolioEntryAdditionalMilestoneFormData(LifeCycleMilestone lifeCycleMilestone, Long previousMilestoneId, II18nMessagesPlugin ii18nMessagesPlugin) {
        this.id = lifeCycleMilestone.id;
        this.previousMilestone = previousMilestoneId;
        this.shortName = MultiLanguagesString.getByKey(lifeCycleMilestone.shortName, ii18nMessagesPlugin);
        this.name = MultiLanguagesString.getByKey(lifeCycleMilestone.name, ii18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(lifeCycleMilestone.description, ii18nMessagesPlugin);
        this.isReviewRequired = lifeCycleMilestone.isReviewRequired;
        this.defaultStatusType = lifeCycleMilestone.defaultLifeCycleMilestoneInstanceStatusType.id;
        if (lifeCycleMilestone.approvers != null) {
            this.approvers.addAll(lifeCycleMilestone.approvers.stream().map(approver -> approver.id).collect(Collectors.toList()));
        }
    }

    /**
     * Fill the DB entry with the form values.
     *
     * @param lifeCycleMilestone
     *            the life cycle milestone in the DB
     */
    public void fill(LifeCycleMilestone lifeCycleMilestone) {
        lifeCycleMilestone.isActive = true;
        lifeCycleMilestone.shortName = this.shortName.getKeyIfValue();
        lifeCycleMilestone.name = this.name.getKeyIfValue();
        lifeCycleMilestone.description = this.description.getKeyIfValue();
        lifeCycleMilestone.isReviewRequired = this.isReviewRequired;
        lifeCycleMilestone.type = null;
        lifeCycleMilestone.defaultLifeCycleMilestoneInstanceStatusType = LifeCycleMilestoneDao.getLCMilestoneInstanceStatusTypeById(this.defaultStatusType);
        lifeCycleMilestone.approvers = new ArrayList<>();
        lifeCycleMilestone.approvers.addAll(this.approvers.stream().map(ActorDao::getActorById).collect(Collectors.toList()));
    }

}