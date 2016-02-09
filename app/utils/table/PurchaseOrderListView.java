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
import java.util.List;

import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ListOfValuesFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import models.finance.PurchaseOrder;
import models.finance.PurchaseOrderLineItem;

/**
 * A purchase order list view is used to display a purchase order row in a
 * table.
 * 
 * @author Johann Kohler
 */
public class PurchaseOrderListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PurchaseOrderListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<PurchaseOrderListView> getTable() {
            return new Table<PurchaseOrderListView>() {
                {
                    setIdFieldName("id");

                    addColumn("refId", "refId", "object.purchase_order.ref_id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("refId", new ObjectFormatter<PurchaseOrderListView>());

                    addColumn("amount", "amount", "object.purchase_order.amount.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("amount", new NumberFormatter<PurchaseOrderListView>());

                    addColumn("lineItems", "lineItems", "object.purchase_order.line_items.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lineItems", new ListOfValuesFormatter<PurchaseOrderListView>());

                    addColumn("isCancelled", "isCancelled", "object.purchase_order.is_cancelled.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isCancelled", new BooleanFormatter<PurchaseOrderListView>());
                    setColumnCssClass("isCancelled", IMafConstants.BOOTSTRAP_COLUMN_2);

                    this.setLineAction(new IColumnFormatter<PurchaseOrderListView>() {
                        @Override
                        public String apply(PurchaseOrderListView purchaseOrderListView, Object value) {
                            return controllers.core.routes.PurchaseOrderController.view(purchaseOrderListView.id).url();
                        }
                    });

                    setEmptyMessageKey("object.purchase_order.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PurchaseOrderListView() {
    }

    public Long id;

    public String refId;

    public Boolean isCancelled;

    public BigDecimal amount = BigDecimal.ZERO;

    public List<PurchaseOrderLineItem> lineItems;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param purchaseOrder
     *            the purchase order in the DB
     */
    public PurchaseOrderListView(PurchaseOrder purchaseOrder) {

        this.id = purchaseOrder.id;
        this.refId = purchaseOrder.refId;
        this.isCancelled = purchaseOrder.isCancelled;
        this.lineItems = purchaseOrder.purchaseOrderLineItems;

        for (PurchaseOrderLineItem item : this.lineItems) {
            this.amount = this.amount.add(item.amount);
        }
    }

}
