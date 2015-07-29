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

import models.pmo.Stakeholder;
import play.data.validation.Constraints.Required;
import dao.pmo.ActorDao;
import dao.pmo.StakeholderDao;

/**
 * A stakeholder form data is used to manage the fields when adding/editing a
 * stakeholder.
 * 
 * @author Johann Kohler
 */
public class StakeholderFormData {

    // the portfolioEntry id or the portfolio id
    public Long id;

    public Long stakeholderId;

    @Required
    public Long stakeholderType;

    @Required
    public Long actor;

    /**
     * Default constructor.
     */
    public StakeholderFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param stakeholder
     *            the stakeholder in the DB
     */
    public StakeholderFormData(Stakeholder stakeholder) {

        if (stakeholder.portfolioEntry != null) {
            this.id = stakeholder.portfolioEntry.id;
        }

        if (stakeholder.portfolio != null) {
            this.id = stakeholder.portfolio.id;
        }

        this.stakeholderId = stakeholder.id;
        this.stakeholderType = stakeholder.stakeholderType.id;
        this.actor = stakeholder.actor.id;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param stakeholder
     *            the stakeholder in the DB
     */
    public void fill(Stakeholder stakeholder) {
        stakeholder.stakeholderType = StakeholderDao.getStakeholderTypeById(stakeholderType);
        stakeholder.actor = ActorDao.getActorById(actor);
    }

}
