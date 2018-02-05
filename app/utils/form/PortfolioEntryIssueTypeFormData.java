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
import models.pmo.PortfolioEntryIssueType;

/**
 * A portfolio entry issue type form data is used to manage the fields when
 * adding/editing a portfolio entry issue type.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryIssueTypeFormData {

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
    public PortfolioEntryIssueTypeFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     *
     * @param portfolioEntryIssueType
     *            the portfolio entry issue type in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public PortfolioEntryIssueTypeFormData(PortfolioEntryIssueType portfolioEntryIssueType, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = portfolioEntryIssueType.id;
        this.selectable = portfolioEntryIssueType.selectable;
        this.name = MultiLanguagesString.getByKey(portfolioEntryIssueType.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(portfolioEntryIssueType.description, i18nMessagesPlugin);

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryIssueType
     *            the portfolio entry issue type in the DB
     */
    public void fill(PortfolioEntryIssueType portfolioEntryIssueType) {

        portfolioEntryIssueType.selectable = this.selectable;
        portfolioEntryIssueType.name = this.name.getKeyIfValue();
        portfolioEntryIssueType.description = this.description.getKeyIfValue();

    }
}
