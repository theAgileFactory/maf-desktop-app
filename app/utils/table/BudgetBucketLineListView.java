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

import constants.IMafConstants;
import controllers.core.routes;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.finance.BudgetBucketLine;
import models.finance.Currency;

import java.math.BigDecimal;
import java.text.MessageFormat;

/**
 * A budget bucket line list view is used to display a line of a budget bucket
 * in a table.
 * 
 * @author Johann Kohler
 */
public class BudgetBucketLineListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<BudgetBucketLineListView> templateTable;

        /**
         * Default constructor.
         * 
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */
        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the table.
         * 
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */

        public Table<BudgetBucketLineListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<BudgetBucketLineListView>() {
                {
                    setIdFieldName("id");

                    addColumn("refId", "refId", "object.budget_bucket_line.ref_id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("refId", new ObjectFormatter<>());

                    addColumn("name", "name", "object.budget_bucket_line.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<>());

                    addColumn("isOpex", "isOpex", "object.budget_bucket_line.expenditure_type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isOpex", (budgetBucketLineListView, value) -> views.html.modelsparts.display_is_opex.render(budgetBucketLineListView.isOpex).body());

                    addColumn("currency", "currency", "object.budget_bucket_line.currency.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("currency", new ObjectFormatter<>());

                    addSummableColumn("amount", "amount", "object.budget_bucket_line.amount.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("amount", new NumberFormatter<>(NumberFormatter.CURRENCY_PATTERN));
                    setColumnHeaderCssClass("amount", "text-right");
                    setColumnValueCssClass("amount", "text-right");

                    addCustomAttributeColumns(i18nMessagesPlugin, BudgetBucketLine.class);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<>(IMafConstants.EDIT_URL_FORMAT,
                            (StringFormatFormatter.Hook<BudgetBucketLineListView>) budgetBucketLineListView -> routes.BudgetBucketController
                                    .manageLine(budgetBucketLineListView.budgetBucketId, budgetBucketLineListView.id).url()));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", (budgetBucketLineListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = routes.BudgetBucketController
                                .deleteLine(budgetBucketLineListView.budgetBucketId, budgetBucketLineListView.id).url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    setEmptyMessageKey("object.budget_bucket_line.table.empty");
                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public BudgetBucketLineListView() {
    }

    public Long id;

    public Long budgetBucketId;

    public String name;

    public String refId;

    public Boolean isOpex;

    public Currency currency;

    public BigDecimal amount;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param line
     *            the budget bucket line in the DB
     */
    public BudgetBucketLineListView(BudgetBucketLine line) {

        this.id = line.id;
        this.budgetBucketId = line.budgetBucket.id;
        this.name = line.name;
        this.refId = line.refId;
        this.isOpex = line.isOpex;
        this.currency = line.currency;
        this.amount = line.amount;
    }
}
