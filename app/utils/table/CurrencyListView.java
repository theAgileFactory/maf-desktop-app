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
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.finance.Currency;

/**
 * A currency list view is used to display a currency row in a table.
 * 
 * @author Johann Kohler
 */
public class CurrencyListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<CurrencyListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<CurrencyListView> getTable() {
            return new Table<CurrencyListView>() {
                {

                    setIdFieldName("id");

                    addColumn("code", "code", "object.currency.code.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("code", new ObjectFormatter<CurrencyListView>());

                    addColumn("isDefault", "isDefault", "object.currency.is_default.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isDefault", new BooleanFormatter<CurrencyListView>());

                    addColumn("isActive", "isActive", "object.currency.is_active.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isActive", new BooleanFormatter<CurrencyListView>());

                    addColumn("conversionRate", "conversionRate", "object.currency.conversion_rate.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("conversionRate", new NumberFormatter<CurrencyListView>());

                    addColumn("symbol", "symbol", "object.currency.symbol.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("symbol", new ObjectFormatter<CurrencyListView>());

                    addColumn("setAsDefault", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("setAsDefault", new IColumnFormatter<CurrencyListView>() {
                        @Override
                        public String apply(CurrencyListView currencyListView, Object value) {
                            if (!currencyListView.isDefault) {
                                String setAsDefaultConfirmationMessage = MessageFormat.format(
                                        "<a onclick=\"return maf_confirmAction(''{0}'');\" href=\"%s\"><span class=\"fa fa-check-square-o\"></span></a>",
                                        Msg.get("admin.configuration.reference_data.currency.set_as_default.confirm"));
                                String url = controllers.admin.routes.ConfigurationFinanceController.setCurrencyAsDefault(currencyListView.id).url();
                                return views.html.framework_views.parts.formats.display_with_format.render(url, setAsDefaultConfirmationMessage).body();
                            }
                            return null;

                        }
                    });
                    setColumnCssClass("setAsDefault", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("setAsDefault", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink",
                            new StringFormatFormatter<CurrencyListView>(IMafConstants.EDIT_URL_FORMAT, new StringFormatFormatter.Hook<CurrencyListView>() {
                        @Override
                        public String convert(CurrencyListView currencyListView) {
                            return controllers.admin.routes.ConfigurationFinanceController.manageCurrency(currencyListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<CurrencyListView>() {
                        @Override
                        public String apply(CurrencyListView currencyListView, Object value) {
                            if (!currencyListView.isDefault) {
                                String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                        Msg.get("default.delete.confirmation.message"));
                                String url = controllers.admin.routes.ConfigurationFinanceController.deleteCurrency(currencyListView.id).url();
                                return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                            }
                            return null;
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("table.empty");

                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public CurrencyListView() {
    }

    public Long id;

    public boolean isActive = true;

    public String code;

    public boolean isDefault = false;

    public BigDecimal conversionRate;

    public String symbol;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param currency
     *            the currency in the DB
     */
    public CurrencyListView(Currency currency) {

        this.id = currency.id;
        this.isActive = currency.isActive;
        this.code = currency.code;
        this.isDefault = currency.isDefault;
        if (!this.isDefault) {
            this.conversionRate = currency.conversionRate;
        }
        this.symbol = currency.symbol;

    }
}
