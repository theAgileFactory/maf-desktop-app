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

import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.CustomConstraints.MultiLanguagesStringMaxLength;
import framework.utils.CustomConstraints.MultiLanguagesStringRequired;
import framework.utils.MultiLanguagesString;
import models.finance.PortfolioEntryBudgetLineType;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;

/**
 * A portfolio entry budget line type form data is used to manage the fields
 * when adding/editing a budget line type for a budget.
 * 
 * @author Marc Schaer
 */
public class PortfolioEntryBudgetLineTypeFormData {

    // the portfolioEntry id
    public Long id;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String refId;

    @MultiLanguagesStringRequired
    @MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString name;

    @MultiLanguagesStringMaxLength(value = IModelConstants.VLARGE_STRING)
    public MultiLanguagesString description;

    @Required
    public boolean selectable;

    /**
     * Default constructor.
     */
    public PortfolioEntryBudgetLineTypeFormData() {
    }

    /**
     * Form validation.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param budgetLineType
     *            the budget line in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public PortfolioEntryBudgetLineTypeFormData(PortfolioEntryBudgetLineType budgetLineType, II18nMessagesPlugin i18nMessagesPlugin) {
        this.id = budgetLineType.id;
        this.refId = budgetLineType.refId;
        this.name = MultiLanguagesString.getByKey(budgetLineType.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(budgetLineType.description, i18nMessagesPlugin);
        this.selectable = budgetLineType.selectable;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param budgetLineType
     *            the budget line in the DB
     */
    public void fill(PortfolioEntryBudgetLineType budgetLineType) {
        budgetLineType.refId = this.refId;
        budgetLineType.name = this.name.getKeyIfValue();
        budgetLineType.description = this.description.getKeyIfValue();
        budgetLineType.selectable = this.selectable;
    }

}
