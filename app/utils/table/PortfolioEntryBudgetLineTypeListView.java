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

import java.text.MessageFormat;

import constants.IMafConstants;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.finance.PortfolioEntryBudgetLineType;

/**
 * A portfolio entry budget line type list view is used to display a portfolio
 * entry budget line type in a table.
 * 
 * @author Marc Schaer
 */
public class PortfolioEntryBudgetLineTypeListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioEntryBudgetLineTypeListView> templateTable;

        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryBudgetLineTypeListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryBudgetLineTypeListView>() {
                {
                    setIdFieldName("id");

                    addColumn("refId", "refId", "object.portfolio_entry_budget_line_type.ref_id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("refId", new ObjectFormatter<PortfolioEntryBudgetLineTypeListView>());

                    addColumn("name", "name", "object.portfolio_entry_budget_line_type.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<PortfolioEntryBudgetLineTypeListView>());

                    addColumn("description", "description", "object.portfolio_entry_budget_line_type.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<PortfolioEntryBudgetLineTypeListView>());

                    addColumn("active", "active", "object.portfolio_entry_budget_line_type.selectable.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("active", new IColumnFormatter<PortfolioEntryBudgetLineTypeListView>() {
                        @Override
                        public String apply(PortfolioEntryBudgetLineTypeListView portfolioEntryBudgetLineTypeListView, Object value) {
                            return views.html.framework_views.parts.formats.display_boolean.render(portfolioEntryBudgetLineTypeListView.selectable).body();
                        }
                    });

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryBudgetLineTypeListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<PortfolioEntryBudgetLineTypeListView>() {

                        @Override
                        public String convert(PortfolioEntryBudgetLineTypeListView portfolioEntryBudgetLineTypeListView) {
                            return controllers.admin.routes.ConfigurationCurrencyController.managePEBudgetLineType(portfolioEntryBudgetLineTypeListView.id)
                                    .url();
                        }
                    }));

                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", new IColumnFormatter<PortfolioEntryBudgetLineTypeListView>() {

                        @Override
                        public String apply(PortfolioEntryBudgetLineTypeListView portfolioEntryBudgetLineTypeListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationCurrencyController
                                    .deletePEBudgetLineType(portfolioEntryBudgetLineTypeListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    /*
                     * this.setLineAction(new
                     * IColumnFormatter<PortfolioEntryBudgetLineTypeListView>()
                     * {
                     * 
                     * @Override public String
                     * apply(PortfolioEntryBudgetLineTypeListView
                     * portfolioEntryBudgetLineTypeListView, Object value) {
                     * return
                     * controllers.core.routes.PortfolioEntryBudgetLineType
                     * .viewBudgetLine(portfolioEntryBudgetLineTypeListView.
                     * portfolioEntryId,
                     * portfolioEntryBudgetLineTypeListView.id).url(); } });
                     */

                    setEmptyMessageKey("object.portfolio_entry_budget_line_type.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryBudgetLineTypeListView() {
    }

    public Long id;

    public String name;

    public String description;

    public String refId;

    public boolean selectable;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param budgetLineType
     *            the portfolio entry budget line in the DB
     */
    public PortfolioEntryBudgetLineTypeListView(PortfolioEntryBudgetLineType budgetLineType) {

        this.id = budgetLineType.id;
        this.name = budgetLineType.name;
        this.description = budgetLineType.description;
        this.refId = budgetLineType.refId;
        this.selectable = budgetLineType.selectable;
    }
}
