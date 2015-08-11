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

import models.governance.LifeCyclePhase;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import dao.governance.LifeCycleMilestoneDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A life cycle process form data is used to manage the fields when managing a
 * life cycle process.
 * 
 * @author Johann Kohler
 */
public class LifeCyclePhaseFormData {

    public Long id;

    public Long lifeCycleProcessId;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    @Required
    public Long startMilestone;

    @Required
    public Long endMilestone;

    @Required
    public Integer gapDaysStart;

    @Required
    public Integer gapDaysEnd;

    public boolean isRoadmapPhase;

    /**
     * Default constructor.
     */
    public LifeCyclePhaseFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param lifeCyclePhase
     *            the life cycle phase in the DB
     * @param i18nMessagesPlugin 
     *            the i18n manager
     */
    public LifeCyclePhaseFormData(LifeCyclePhase lifeCyclePhase, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = lifeCyclePhase.id;
        this.name = MultiLanguagesString.getByKey(lifeCyclePhase.name, i18nMessagesPlugin);
        this.startMilestone = lifeCyclePhase.startLifeCycleMilestone.id;
        this.endMilestone = lifeCyclePhase.endLifeCycleMilestone.id;
        this.gapDaysStart = lifeCyclePhase.gapDaysStart;
        this.gapDaysEnd = lifeCyclePhase.gapDaysEnd;
        this.isRoadmapPhase = lifeCyclePhase.isRoadmapPhase;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param lifeCyclePhase
     *            the life cycle phase in the DB
     */
    public void fill(LifeCyclePhase lifeCyclePhase) {

        lifeCyclePhase.name = this.name.getKeyIfValue();
        lifeCyclePhase.startLifeCycleMilestone = LifeCycleMilestoneDao.getLCMilestoneById(this.startMilestone);
        lifeCyclePhase.endLifeCycleMilestone = LifeCycleMilestoneDao.getLCMilestoneById(this.endMilestone);
        lifeCyclePhase.gapDaysStart = this.gapDaysStart;
        lifeCyclePhase.gapDaysEnd = this.gapDaysEnd;
        lifeCyclePhase.isRoadmapPhase = this.isRoadmapPhase;

    }

}
