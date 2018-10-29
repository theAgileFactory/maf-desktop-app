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

import java.math.BigDecimal;

import dao.finance.BudgetBucketDAO;
import dao.finance.CurrencyDAO;
import dao.finance.PortfolioEntryBudgetDAO;
import models.finance.PortfolioEntryBudgetLine;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

/**
 * A portfolio entry budget line form data is used to manage the fields when
 * adding/editing a budget line for a budget.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryBudgetLineFormData extends AbstractFormData<PortfolioEntryBudgetLine> {

    // the portfolioEntry id
    public Long id;

    public boolean fromResource;

    public Long budgetLineId;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String refId;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Required
    public boolean isOpex;

    @Required
    public String currencyCode;

    @Required
    public BigDecimal currencyRate;

    @Required
    public BigDecimal amount;

    public Long portfolioEntryBudgetLineType;

    public Long budgetBucket;

    /**
     * Default constructor.
     */
    public PortfolioEntryBudgetLineFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param budgetLine
     *            the budget line in the DB
     */
    public PortfolioEntryBudgetLineFormData(PortfolioEntryBudgetLine budgetLine) {

        this.id = budgetLine.portfolioEntryBudget.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.budgetLineId = budgetLine.id;

        this.refId = budgetLine.refId;
        this.name = budgetLine.name;
        this.isOpex = budgetLine.isOpex != null ? budgetLine.isOpex : false;
        this.currencyCode = budgetLine.currency != null ? budgetLine.currency.code : null;
        this.currencyRate = budgetLine.currencyRate;
        this.amount = budgetLine.amount;
        this.portfolioEntryBudgetLineType = budgetLine.portfolioEntryBudgetLineType != null ? budgetLine.portfolioEntryBudgetLineType.id : null;
        this.budgetBucket = budgetLine.budgetBucket != null ? budgetLine.budgetBucket.id : null;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param budgetLine
     *            the budget line in the DB
     */
    public void fillEntity(PortfolioEntryBudgetLine budgetLine) {
        budgetLine.refId = this.refId;
        budgetLine.name = this.name;
        budgetLine.isOpex = this.isOpex;
        budgetLine.currency = CurrencyDAO.getCurrencyByCode(this.currencyCode);
        budgetLine.currencyRate = this.currencyRate;
        budgetLine.amount = this.amount;
        budgetLine.portfolioEntryBudgetLineType = this.portfolioEntryBudgetLineType != null
                ? PortfolioEntryBudgetDAO.getPEBudgetLineTypeById(this.portfolioEntryBudgetLineType) : null;
        budgetLine.budgetBucket = this.budgetBucket != null ? BudgetBucketDAO.getBudgetBucketById(this.budgetBucket) : null;

    }

}
