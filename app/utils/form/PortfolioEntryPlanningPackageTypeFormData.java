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

import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.CustomConstraints.MultiLanguagesStringMaxLength;
import framework.utils.CustomConstraints.MultiLanguagesStringRequired;
import framework.utils.MultiLanguagesString;
import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntryPlanningPackageType;
import play.data.validation.Constraints.Required;

/**
 * A portfolio entry planning package type form data is used to manage the
 * fields when adding/editing a type for a planning package.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPlanningPackageTypeFormData {

    public Long id;

    public boolean isActive;

    @MultiLanguagesStringRequired
    @MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString name;

    @Required
    public String cssClass;

    /**
     * Default constructor.
     */
    public PortfolioEntryPlanningPackageTypeFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryPlanningPackageType
     *            the portfolio entry planning package type in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public PortfolioEntryPlanningPackageTypeFormData(PortfolioEntryPlanningPackageType portfolioEntryPlanningPackageType,
            II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = portfolioEntryPlanningPackageType.id;
        this.isActive = portfolioEntryPlanningPackageType.isActive;
        this.name = MultiLanguagesString.getByKey(portfolioEntryPlanningPackageType.name, i18nMessagesPlugin);
        this.cssClass = portfolioEntryPlanningPackageType.cssClass;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryPlanningPackageType
     *            the portfolio entry planning package type in the DB
     */
    public void fill(PortfolioEntryPlanningPackageType portfolioEntryPlanningPackageType) {

        portfolioEntryPlanningPackageType.isActive = this.isActive;
        portfolioEntryPlanningPackageType.name = this.name.getKeyIfValue();
        portfolioEntryPlanningPackageType.cssClass = this.cssClass;

    }
}
