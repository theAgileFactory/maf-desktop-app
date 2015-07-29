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

import models.pmo.PortfolioEntryDependencyType;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A portfolio entry dependency type form data is used to manage the fields when
 * adding/editing a portfolio entry dependency type.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryDependencyTypeFormData {

    public Long id;

    public boolean isActive;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString contrary;

    public MultiLanguagesString description;

    /**
     * Default constructor.
     */
    public PortfolioEntryDependencyTypeFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryDependencyType
     *            the portfolio entry dependency type in the DB
     */
    public PortfolioEntryDependencyTypeFormData(PortfolioEntryDependencyType portfolioEntryDependencyType) {

        this.id = portfolioEntryDependencyType.id;
        this.isActive = portfolioEntryDependencyType.isActive;
        this.name = MultiLanguagesString.getByKey(portfolioEntryDependencyType.name);
        this.contrary = MultiLanguagesString.getByKey(portfolioEntryDependencyType.contrary);
        this.description = MultiLanguagesString.getByKey(portfolioEntryDependencyType.description);

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryDependencyType
     *            the portfolio entry dependency type in the DB
     */
    public void fill(PortfolioEntryDependencyType portfolioEntryDependencyType) {

        portfolioEntryDependencyType.isActive = this.isActive;
        portfolioEntryDependencyType.name = this.name.getKeyIfValue();
        portfolioEntryDependencyType.contrary = this.contrary.getKeyIfValue();
        portfolioEntryDependencyType.description = this.description.getKeyIfValue();

    }
}
