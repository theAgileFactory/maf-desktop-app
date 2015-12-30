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
import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntryPlanningPackagePattern;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

/**
 * An portfolio entry planning package pattern form data is used to manage the
 * fields when adding/editing a package pattern.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPlanningPackagePatternFormData {

    public Long id;

    public Long packageGroupId;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Required
    public boolean isOpex;

    @MaxLength(value = IModelConstants.VLARGE_STRING)
    public String description;

    @Required
    public Long type;

    /**
     * Default constructor.
     */
    public PortfolioEntryPlanningPackagePatternFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param packagePattern
     *            the package pattern in the DB
     */
    public PortfolioEntryPlanningPackagePatternFormData(PortfolioEntryPlanningPackagePattern packagePattern) {

        this.id = packagePattern.id;
        this.packageGroupId = packagePattern.portfolioEntryPlanningPackageGroup.id;

        this.name = packagePattern.name;
        this.isOpex = packagePattern.isOpex;
        this.description = packagePattern.description;
        this.type = packagePattern.portfolioEntryPlanningPackageType.id;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param packagePattern
     *            the package pattern in the DB
     */
    public void fill(PortfolioEntryPlanningPackagePattern packagePattern) {

        packagePattern.name = this.name;
        packagePattern.isOpex = this.isOpex;
        packagePattern.description = this.description;
        packagePattern.portfolioEntryPlanningPackageType = PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeById(this.type);

    }
}
