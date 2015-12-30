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

import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioEntryDao;
import models.framework_models.parent.IModelConstants;
import models.pmo.OrgUnit;
import models.pmo.PortfolioEntry;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

/**
 * An portfolio entry edit form data is used to manage the fields when editing
 * an portfolio entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryEditFormData {

    public Long id;

    public boolean isConfidential;

    public boolean isActive;

    public boolean defaultIsOpex;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Required
    public Long portfolioEntryType;

    @MaxLength(value = IModelConstants.SMALL_STRING)
    public String governanceId;

    @MaxLength(value = IModelConstants.SMALL_STRING)
    public String erpRefId;

    @Required
    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String description;

    @Required
    public Long manager;

    public Long sponsoringUnit;

    public List<Long> deliveryUnits = new ArrayList<Long>();

    /**
     * Default constructor.
     */
    public PortfolioEntryEditFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntry
     *            the portfolio entry in the DB
     */
    public PortfolioEntryEditFormData(PortfolioEntry portfolioEntry) {

        this.id = portfolioEntry.id;
        this.isConfidential = !portfolioEntry.isPublic;
        this.isActive = !portfolioEntry.archived;
        this.defaultIsOpex = portfolioEntry.defaultIsOpex;
        this.name = portfolioEntry.name;
        this.governanceId = portfolioEntry.governanceId;
        this.erpRefId = portfolioEntry.erpRefId;
        this.description = portfolioEntry.description;
        this.portfolioEntryType = portfolioEntry.portfolioEntryType != null ? portfolioEntry.portfolioEntryType.id : null;
        this.manager = portfolioEntry.manager != null ? portfolioEntry.manager.id : null;
        this.sponsoringUnit = portfolioEntry.sponsoringUnit != null ? portfolioEntry.sponsoringUnit.id : null;
        if (portfolioEntry.deliveryUnits != null) {
            for (OrgUnit deliveryUnit : portfolioEntry.deliveryUnits) {
                this.deliveryUnits.add(deliveryUnit.id);
            }
        }

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntry
     *            the portfolio entry in the DB
     */
    public void fill(PortfolioEntry portfolioEntry) {

        portfolioEntry.isPublic = !this.isConfidential;
        portfolioEntry.archived = !this.isActive;
        portfolioEntry.defaultIsOpex = this.defaultIsOpex;
        portfolioEntry.name = this.name;
        portfolioEntry.governanceId = this.governanceId;
        portfolioEntry.erpRefId = this.erpRefId;
        portfolioEntry.description = this.description;
        portfolioEntry.portfolioEntryType = PortfolioEntryDao.getPETypeById(this.portfolioEntryType);
        portfolioEntry.manager = ActorDao.getActorById(this.manager);
        portfolioEntry.sponsoringUnit = OrgUnitDao.getOrgUnitById(this.sponsoringUnit);
        portfolioEntry.deliveryUnits = new ArrayList<OrgUnit>();
        for (Long deliveryUnit : this.deliveryUnits) {
            portfolioEntry.deliveryUnits.add(OrgUnitDao.getOrgUnitById(deliveryUnit));
        }

    }

}
