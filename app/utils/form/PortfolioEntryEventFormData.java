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

import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntryEvent;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import dao.pmo.PortfolioEntryEventDao;

/**
 * An portfolio entry event form data is used to manage the fields when
 * adding/editing a portfolio entry event.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryEventFormData {

    // the portfolioEntry id
    public Long id;

    public Long eventId;

    @Required
    public Long type;

    @Required
    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String message;

    /**
     * Default constructor.
     */
    public PortfolioEntryEventFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryEvent
     *            the portfolio entry event in the DB
     */
    public PortfolioEntryEventFormData(PortfolioEntryEvent portfolioEntryEvent) {
        this.id = portfolioEntryEvent.portfolioEntry.id;
        this.eventId = portfolioEntryEvent.id;
        this.type = portfolioEntryEvent.portfolioEntryEventType.id;
        this.message = portfolioEntryEvent.message;
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryEvent
     *            the portfolio entry event in the DB
     */
    public void fill(PortfolioEntryEvent portfolioEntryEvent) {
        portfolioEntryEvent.portfolioEntryEventType = PortfolioEntryEventDao.getPEEventTypeById(this.type);
        portfolioEntryEvent.message = this.message;
    }
}
