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

import dao.pmo.PortfolioEntryPlanningPackageDao;
import models.delivery.Deliverable;
import models.delivery.PortfolioEntryDeliverable;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

/**
 * An portfolio entry event form data is used to manage the fields when
 * adding/editing a portfolio entry event.
 * 
 * @author Johann Kohler
 */
public class DeliverableFormData {

    // the portfolioEntry id
    public Long id;

    public Long deliverableId;

    public boolean isOwner;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @MaxLength(value = IModelConstants.VLARGE_STRING)
    public String description;

    public Long planningPackage;

    /**
     * Default constructor.
     */
    public DeliverableFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param deliverable
     *            the deliverable in the DB
     * @param portfolioEntryDeliverable
     *            the relation
     * 
     * 
     */
    public DeliverableFormData(Deliverable deliverable, PortfolioEntryDeliverable portfolioEntryDeliverable) {
        this.id = portfolioEntryDeliverable.id.portfolioEntryId;
        this.deliverableId = deliverable.id;
        this.name = deliverable.name;
        this.description = deliverable.description;
        this.planningPackage = portfolioEntryDeliverable.portfolioEntryPlanningPackage != null ? portfolioEntryDeliverable.portfolioEntryPlanningPackage.id
                : null;
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param deliverable
     *            the deliverable in the DB
     */
    public void fillDeliverable(Deliverable deliverable) {
        deliverable.name = this.name;
        deliverable.description = this.description;
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryDeliverable
     *            the relation
     */
    public void fillPortfolioEntryDeliverable(PortfolioEntryDeliverable portfolioEntryDeliverable) {
        portfolioEntryDeliverable.portfolioEntryPlanningPackage = this.planningPackage != null
                ? PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(this.planningPackage) : null;
    }
}
