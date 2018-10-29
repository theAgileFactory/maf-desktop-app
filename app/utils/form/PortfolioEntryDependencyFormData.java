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

import dao.pmo.PortfolioEntryDao;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryDependency;
import models.pmo.PortfolioEntryDependencyType;
import play.data.validation.Constraints.Required;

/**
 * Form to add a dependency between 2 portfolio entries.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryDependencyFormData extends AbstractFormData<PortfolioEntryDependency> {

    // the portfolio entry id of the current portfolio entry
    public Long id;

    // the portfolio entry id of the depending portfolio entry
    @Required
    public Long dependingId;

    /**
     * String formatted by: {portfolioEntryDependencyTypeId}#{isContrary}.
     * 
     * Examples:<br/>
     * 4#true<br/>
     * 8#false
     */
    @Required
    public String directedType;

    /**
     * Default constructor.
     */
    public PortfolioEntryDependencyFormData() {
    }

    @Override
    public void fillEntity(PortfolioEntryDependency entity) {
        String[] elems = this.directedType.split("#");

        Long portfolioEntryDependencyTypeId = Long.valueOf(elems[0]);
        boolean isContrary = Boolean.valueOf(elems[1]);

        PortfolioEntry sourcePortfolioEntry;
        PortfolioEntry destinationPortfolioEntry;
        PortfolioEntryDependencyType portfolioEntryDependencyType = PortfolioEntryDao.getPEDependencyTypeById(portfolioEntryDependencyTypeId);

        if (isContrary) {
            sourcePortfolioEntry = PortfolioEntryDao.getPEById(this.dependingId);
            destinationPortfolioEntry = PortfolioEntryDao.getPEById(this.id);
        } else {
            sourcePortfolioEntry = PortfolioEntryDao.getPEById(this.id);
            destinationPortfolioEntry = PortfolioEntryDao.getPEById(this.dependingId);
        }

        entity.sourcePortfolioEntry = sourcePortfolioEntry;
        entity.destinationPortfolioEntry = destinationPortfolioEntry;
        entity.portfolioEntryDependencyType = portfolioEntryDependencyType;
        entity.id.sourcePortfolioEntryId = sourcePortfolioEntry.id;
        entity.id.destinationPortfolioEntryId = destinationPortfolioEntry.id;
        entity.id.portfolioEntryDependencyTypeId = portfolioEntryDependencyType.id;
    }

    /**
     * Construct with a portfolio entry id.
     * 
     * @param portfolioEntryId
     *            the portfolioEntryId
     */
    public PortfolioEntryDependencyFormData(Long portfolioEntryId) {
        super();
        this.id = portfolioEntryId;
    }
}
