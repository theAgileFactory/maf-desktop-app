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

import java.math.BigDecimal;
import java.text.MessageFormat;

import constants.IMafConstants;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.finance.BudgetBucket;
import models.finance.Currency;
import models.finance.PortfolioEntryBudgetLine;
import models.pmo.PortfolioEntry;

/**
 * A portfolio entry budget line list view is used to display an portfolio entry
 * budget line in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryBudgetLineListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioEntryBudgetLineListView> templateTable;

        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryBudgetLineListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryBudgetLineListView>() {
                {
                    setIdFieldName("id");

                    addColumn("refId", "refId", "object.portfolio_entry_budget_line.ref_id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("refId", new ObjectFormatter<PortfolioEntryBudgetLineListView>());

                    addColumn("name", "name", "object.portfolio_entry_budget_line.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<PortfolioEntryBudgetLineListView>());

                    addColumn("portfolioEntryName", "portfolioEntryName", "object.portfolio_entry_budget_line.portfolio_entry.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryName", new ObjectFormatter<PortfolioEntryBudgetLineListView>());

                    addColumn("isOpex", "isOpex", "object.portfolio_entry_budget_line.expenditure_type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isOpex", new IColumnFormatter<PortfolioEntryBudgetLineListView>() {
                        @Override
                        public String apply(PortfolioEntryBudgetLineListView portfolioEntryBudgetLineListView, Object value) {
                            return views.html.modelsparts.display_is_opex.render(portfolioEntryBudgetLineListView.isOpex).body();
                        }
                    });

                    addColumn("currency", "currency", "object.portfolio_entry_budget_line.currency.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("currency", new ObjectFormatter<PortfolioEntryBudgetLineListView>());

                    addColumn("amount", "amount", "object.portfolio_entry_budget_line.amount.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("amount", new NumberFormatter<PortfolioEntryBudgetLineListView>());

                    addColumn("budgetBucket", "budgetBucket", "object.portfolio_entry_budget_line.budget_bucket.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("budgetBucket", new IColumnFormatter<PortfolioEntryBudgetLineListView>() {
                        @Override
                        public String apply(PortfolioEntryBudgetLineListView portfolioEntryBudgetLineListView, Object value) {
                            return views.html.modelsparts.display_budget_bucket.render(portfolioEntryBudgetLineListView.budgetBucket).body();
                        }
                    });
                    setColumnValueCssClass("budgetBucket", "rowlink-skip");

                    addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntryBudgetLine.class);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryBudgetLineListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<PortfolioEntryBudgetLineListView>() {
                        @Override
                        public String convert(PortfolioEntryBudgetLineListView portfolioEntryBudgetLineListView) {
                            return controllers.core.routes.PortfolioEntryFinancialController
                                    .manageBudgetLine(portfolioEntryBudgetLineListView.portfolioEntryId, portfolioEntryBudgetLineListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", new IColumnFormatter<PortfolioEntryBudgetLineListView>() {
                        @Override
                        public String apply(PortfolioEntryBudgetLineListView portfolioEntryBudgetLineListView, Object value) {
                            if (!portfolioEntryBudgetLineListView.fromResource) {
                                String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                        Msg.get("default.delete.confirmation.message"));
                                String url = controllers.core.routes.PortfolioEntryFinancialController
                                        .deleteBudgetLine(portfolioEntryBudgetLineListView.portfolioEntryId, portfolioEntryBudgetLineListView.id).url();
                                return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                            } else {
                                return null;
                            }
                        }
                    });
                    setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    this.setLineAction(new IColumnFormatter<PortfolioEntryBudgetLineListView>() {
                        @Override
                        public String apply(PortfolioEntryBudgetLineListView portfolioEntryBudgetLineListView, Object value) {
                            return controllers.core.routes.PortfolioEntryFinancialController
                                    .viewBudgetLine(portfolioEntryBudgetLineListView.portfolioEntryId, portfolioEntryBudgetLineListView.id).url();
                        }
                    });

                    setEmptyMessageKey("object.portfolio_entry_budget_line.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryBudgetLineListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public String name;

    public String portfolioEntryName;

    public String refId;

    public Boolean isOpex;

    public Currency currency;

    public BigDecimal amount;

    public BudgetBucket budgetBucket;

    public boolean fromResource;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param budgetLine
     *            the portfolio entry budget line in the DB
     */
    public PortfolioEntryBudgetLineListView(PortfolioEntryBudgetLine budgetLine) {

        PortfolioEntry portfolioEntry = budgetLine.portfolioEntryBudget.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry;

        this.id = budgetLine.id;
        this.portfolioEntryId = portfolioEntry.id;
        this.name = budgetLine.name;
        this.portfolioEntryName = portfolioEntry.getName();
        this.refId = budgetLine.refId;
        this.isOpex = budgetLine.isOpex;
        this.currency = budgetLine.currency;
        this.amount = budgetLine.amount;
        this.budgetBucket = budgetLine.budgetBucket;
        this.fromResource = budgetLine.resourceObjectType != null;
    }
}
