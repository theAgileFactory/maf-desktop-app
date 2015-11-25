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

import dao.delivery.RequirementDAO;
import models.delivery.Deliverable;
import models.delivery.PortfolioEntryDeliverable;
import models.delivery.Requirement;

/**
 * Form used to edit the requirements assignments of a deliverable.
 * 
 * @author Johann Kohler
 */
public class DeliverableRequirementsFormData {

    // the portfolio entry id
    public Long id;

    public Long deliverableId;

    public List<Long> requirements = new ArrayList<Long>();

    /**
     * Default constructor.
     */
    public DeliverableRequirementsFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryDeliverable
     *            the portfolio entry deliverable relation in the DB
     */
    public DeliverableRequirementsFormData(PortfolioEntryDeliverable portfolioEntryDeliverable) {

        this.id = portfolioEntryDeliverable.id.portfolioEntryId;

        Deliverable deliverable = portfolioEntryDeliverable.getDeliverable();

        this.deliverableId = deliverable.id;

        if (deliverable.requirements != null) {
            for (Requirement requirement : deliverable.requirements) {
                this.requirements.add(requirement.id);
            }
        }

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param deliverable
     *            the deliverable in the DB
     */
    public void fill(Deliverable deliverable) {

        deliverable.requirements = new ArrayList<Requirement>();
        for (Long requirementId : this.requirements) {
            if (requirementId != null) {
                deliverable.requirements.add(RequirementDAO.getRequirementById(requirementId));
            }
        }

    }

}
