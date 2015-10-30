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
import models.pmo.PortfolioEntryRiskType;

/**
 * A portfolio entry risk type form data is used to manage the fields when
 * adding/editing a portfolio entry risk type.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryRiskTypeFormData {

    public Long id;

    public boolean selectable;

    @MultiLanguagesStringRequired
    @MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString name;

    @MultiLanguagesStringMaxLength(value = IModelConstants.VLARGE_STRING)
    public MultiLanguagesString description;

    /**
     * Default constructor.
     */
    public PortfolioEntryRiskTypeFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryRiskType
     *            the portfolio entry risk type in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public PortfolioEntryRiskTypeFormData(PortfolioEntryRiskType portfolioEntryRiskType, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = portfolioEntryRiskType.id;
        this.selectable = portfolioEntryRiskType.selectable;
        this.name = MultiLanguagesString.getByKey(portfolioEntryRiskType.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(portfolioEntryRiskType.description, i18nMessagesPlugin);

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryRiskType
     *            the portfolio entry risk type in the DB
     */
    public void fill(PortfolioEntryRiskType portfolioEntryRiskType) {

        portfolioEntryRiskType.selectable = this.selectable;
        portfolioEntryRiskType.name = this.name.getKeyIfValue();
        portfolioEntryRiskType.description = this.description.getKeyIfValue();

    }
}
