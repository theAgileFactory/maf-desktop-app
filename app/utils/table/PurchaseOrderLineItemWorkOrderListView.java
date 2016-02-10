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
import dao.finance.PurchaseOrderDAO;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.finance.WorkOrder;
import models.pmo.PortfolioEntry;

/**
 * A purchase order line item work order list view is used to display an engaged
 * work order in the details of a purchase order line item.
 * 
 * @author Johann Kohler
 */
public class PurchaseOrderLineItemWorkOrderListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PurchaseOrderLineItemWorkOrderListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the table.
         */
        public Table<PurchaseOrderLineItemWorkOrderListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PurchaseOrderLineItemWorkOrderListView>() {
                {
                    setIdFieldName("id");

                    addColumn("name", "name", "object.work_order.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<PurchaseOrderLineItemWorkOrderListView>());

                    addColumn("portfolioEntry", "portfolioEntry", "object.work_order.portfolio_entry.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntry", new IColumnFormatter<PurchaseOrderLineItemWorkOrderListView>() {
                        @Override
                        public String apply(PurchaseOrderLineItemWorkOrderListView purchaseOrderLineItemWorkOrderListView, Object value) {
                            return views.html.modelsparts.display_portfolio_entry.render(purchaseOrderLineItemWorkOrderListView.portfolioEntry, true).body();
                        }
                    });
                    this.setColumnValueCssClass("portfolioEntry", "rowlink-skip");

                    addColumn("amount", "amount", "object.work_order.amount.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("amount", new NumberFormatter<PurchaseOrderLineItemWorkOrderListView>());

                    addColumn("amountReceived", "amountReceived", "object.work_order.amount_received.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("amountReceived", new NumberFormatter<PurchaseOrderLineItemWorkOrderListView>());

                    addColumn("amountOpen", "amountOpen", "object.work_order.amount_open.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("amountOpen", new NumberFormatter<PurchaseOrderLineItemWorkOrderListView>());

                    addCustomAttributeColumns(i18nMessagesPlugin, WorkOrder.class);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PurchaseOrderLineItemWorkOrderListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<PurchaseOrderLineItemWorkOrderListView>() {
                        @Override
                        public String convert(PurchaseOrderLineItemWorkOrderListView workOrderListView) {
                            return controllers.core.routes.PurchaseOrderController.editWorkOrder(workOrderListView.lineItemId, workOrderListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.work_order.table.empty");

                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public PurchaseOrderLineItemWorkOrderListView() {
    }

    public Long id;

    public Long lineItemId;

    public String name;

    public PortfolioEntry portfolioEntry;

    public BigDecimal amount;

    public BigDecimal amountOpen;

    public BigDecimal amountReceived;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param workOrder
     *            the work order in the DB.
     */
    public PurchaseOrderLineItemWorkOrderListView(IPreferenceManagerPlugin preferenceManagerPlugin, WorkOrder workOrder) {

        this.id = workOrder.id;
        this.lineItemId = workOrder.purchaseOrderLineItem.id;
        this.name = workOrder.name;
        this.portfolioEntry = workOrder.portfolioEntry;
        this.amount = workOrder.getComputedAmount(PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(preferenceManagerPlugin));
        this.amountReceived = workOrder.getComputedAmountReceived(PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(preferenceManagerPlugin));
        this.amountOpen = workOrder.getAmountOpen(PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(preferenceManagerPlugin));

    }
}
