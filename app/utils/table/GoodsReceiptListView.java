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

import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import models.finance.Currency;
import models.finance.GoodsReceipt;

/**
 * A goods receipt list view is used to display a goods receipt in a table.
 * 
 * @author Johann Kohler
 */
public class GoodsReceiptListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<GoodsReceiptListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<GoodsReceiptListView> getTable() {
            return new Table<GoodsReceiptListView>() {
                {
                    setIdFieldName("id");

                    addColumn("refId", "refId", "object.goods_receipt.ref_id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("refId", new ObjectFormatter<GoodsReceiptListView>());

                    addColumn("currency", "currency", "object.goods_receipt.currency.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("currency", new ObjectFormatter<GoodsReceiptListView>());

                    addColumn("currencyRate", "currencyRate", "object.currency.conversion_rate.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("currencyRate", new IColumnFormatter<GoodsReceiptListView>() {
                        @Override
                        public String apply(GoodsReceiptListView goodsReceiptListView, Object value) {
                            if (!goodsReceiptListView.currency.isDefault) {
                                return views.html.modelsparts.display_currency_rate.render(goodsReceiptListView.currencyRate).body();
                            }
                            return IMafConstants.DEFAULT_VALUE_EMPTY_DATA;
                        }
                    });

                    addColumn("quantityReceived", "quantityReceived", "object.goods_receipt.quantity_received.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("quantityReceived", new NumberFormatter<GoodsReceiptListView>());

                    addColumn("amountReceived", "amountReceived", "object.goods_receipt.amount_received.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("amountReceived", new NumberFormatter<GoodsReceiptListView>());

                    setEmptyMessageKey("object.goods_receipt.table.empty");

                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public GoodsReceiptListView() {
    }

    public Long id;

    public String refId;

    public BigDecimal quantityReceived;

    public BigDecimal amountReceived;

    public Currency currency;

    public BigDecimal currencyRate;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param goodsReceipt
     *            the goods receipt in the DB
     */
    public GoodsReceiptListView(GoodsReceipt goodsReceipt) {

        this.id = goodsReceipt.id;

        this.refId = goodsReceipt.refId;
        this.quantityReceived = goodsReceipt.quantityReceived;
        this.amountReceived = goodsReceipt.amountReceived;
        this.currency = goodsReceipt.currency;
        this.currencyRate = goodsReceipt.currencyRate;
    }
}
