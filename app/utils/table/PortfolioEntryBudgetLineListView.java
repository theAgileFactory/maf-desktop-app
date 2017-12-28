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
package utils.table;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import controllers.core.routes;
import dao.finance.PortfolioEntryBudgetDAO;
import dao.pmo.PortfolioEntryDao;
import models.finance.PortfolioEntryBudgetLine;
import utils.table.common.BudgetBucketLink;
import utils.table.common.FinancialNumber;
import utils.table.serializer.FinancialSerializer;

import java.math.BigDecimal;

/**
 * A portfolio entry budget line list view is used to display an portfolio entry
 * budget line in a table.
 *
 * @author Guillaume petit
 */
@JsonAutoDetect()
@JsonRootName("data")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class PortfolioEntryBudgetLineListView {

    public PortfolioEntryBudgetLineListView() {
    }

    public Long id;

    public String link;

    public String name;

    public String refId;

    public String expenditureType;

    public String currency;

    public FinancialNumber amount;

    public BudgetBucketLink budgetBucket;

    public String portfolioEntryBudgetLineType;

    /**
     * Construct a list view with a DB entry.
     *
     * @param budgetLine
     *            the portfolio entry budget line in the DB
     */
    public PortfolioEntryBudgetLineListView(PortfolioEntryBudgetLine budgetLine) {
        this.id = budgetLine.id;
        this.link = routes.PortfolioEntryFinancialController.viewBudgetLine(budgetLine.portfolioEntryBudget.lifeCycleInstancePlannings.stream().filter(planning -> !planning.isFrozen).findFirst().get().lifeCycleInstance.portfolioEntry.id, budgetLine.id).url();
        this.name = budgetLine.name;
        this.refId = budgetLine.refId;
        this.expenditureType = budgetLine.isOpex ? "OPEX" : "CAPEX";
        this.currency = budgetLine.currency == null ? "" : budgetLine.currency.code;
        this.amount = new FinancialNumber(budgetLine.amount.doubleValue());
        this.budgetBucket = new BudgetBucketLink(budgetLine.budgetBucket);
        this.portfolioEntryBudgetLineType = budgetLine.portfolioEntryBudgetLineType == null ? "" : budgetLine.portfolioEntryBudgetLineType.name;
    }
}
