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

import models.finance.CostCenter;
import models.finance.Currency;
import models.finance.PurchaseOrderLineItem;
import models.finance.WorkOrder;
import constants.IMafConstants;
import dao.finance.WorkOrderDAO;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * A purchase order line item list view is used to display a purchase order line
 * item in a table.
 * 
 * @author Johann Kohler
 */
public class PurchaseOrderLineItemListView {

    public static Table<PurchaseOrderLineItemListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<PurchaseOrderLineItemListView> getTable() {
        return new Table<PurchaseOrderLineItemListView>() {
            {
                setIdFieldName("id");

                addColumn("refId", "refId", "object.purchase_order_line_item.ref_id.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("refId", new ObjectFormatter<PurchaseOrderLineItemListView>());

                addColumn("purchaseOrderRefId", "purchaseOrderRefId", "object.purchase_order_line_item.purchase_order.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("purchaseOrderRefId", new ObjectFormatter<PurchaseOrderLineItemListView>());

                addColumn("isAssociated", "isAssociated", "object.purchase_order_line_item.is_associated.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isAssociated", new BooleanFormatter<PurchaseOrderLineItemListView>());

                addColumn("shared", "shared", "object.purchase_order_line_item.shared.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("shared", new BooleanFormatter<PurchaseOrderLineItemListView>());

                addColumn("isOpex", "isOpex", "object.purchase_order_line_item.expenditure_type.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isOpex", new IColumnFormatter<PurchaseOrderLineItemListView>() {
                    @Override
                    public String apply(PurchaseOrderLineItemListView purchaseOrderLineItemListView, Object value) {
                        return views.html.modelsparts.display_is_opex.render(purchaseOrderLineItemListView.isOpex).body();
                    }
                });

                addColumn("currency", "currency", "object.purchase_order_line_item.currency.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("currency", new ObjectFormatter<PurchaseOrderLineItemListView>());

                addColumn("amount", "amount", "object.purchase_order_line_item.amount.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("amount", new NumberFormatter<PurchaseOrderLineItemListView>());

                addColumn("remainingAmount", "remainingAmount", "object.purchase_order_line_item.remaining_amount.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("remainingAmount", new NumberFormatter<PurchaseOrderLineItemListView>());

                addColumn("amountReceived", "amountReceived", "object.purchase_order_line_item.amount_received.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("amountReceived", new NumberFormatter<PurchaseOrderLineItemListView>());

                addColumn("amountOpen", "amountOpen", "object.purchase_order_line_item.amount_open.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("amountOpen", new NumberFormatter<PurchaseOrderLineItemListView>());

                addColumn("costCenter", "costCenter", "object.purchase_order_line_item.cost_center.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("costCenter", new IColumnFormatter<PurchaseOrderLineItemListView>() {
                    @Override
                    public String apply(PurchaseOrderLineItemListView purchaseOrderLineItemListView, Object value) {
                        return views.html.modelsparts.display_cost_center.render(purchaseOrderLineItemListView.costCenter).body();
                    }
                });
                setColumnValueCssClass("costCenter", "rowlink-skip");

                addColumn("isCancelled", "isCancelled", "object.purchase_order_line_item.is_cancelled.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isCancelled", new BooleanFormatter<PurchaseOrderLineItemListView>());

                addColumn("selectActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("selectActionLink", new StringFormatFormatter<PurchaseOrderLineItemListView>(
                        "<a href=\"%s\"><span class=\"fa fa-lock\"></span></a>",
                        new StringFormatFormatter.Hook<PurchaseOrderLineItemListView>() {
                            @Override
                            public String convert(PurchaseOrderLineItemListView purchaseOrderLineItemListView) {
                                return controllers.core.routes.PortfolioEntryFinancialController.selectWorkOrderLineItemStep3(
                                        purchaseOrderLineItemListView.portfolioEntryId, purchaseOrderLineItemListView.workOrderId,
                                        purchaseOrderLineItemListView.id).url();
                            }
                        }));
                setColumnCssClass("selectActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("selectActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                this.setLineAction(new IColumnFormatter<PurchaseOrderLineItemListView>() {
                    @Override
                    public String apply(PurchaseOrderLineItemListView purchaseOrderLineItemListView, Object value) {
                        return controllers.core.routes.PurchaseOrderController.viewLineItem(purchaseOrderLineItemListView.id).url();
                    }
                });

                setEmptyMessageKey("object.purchase_order_line_item.table.empty");

            }
        };

    }

    /**
     * Default constructor.
     */
    public PurchaseOrderLineItemListView() {
    }

    public Long id;

    public Long portfolioEntryId;
    public Long workOrderId;

    public Boolean isAssociated;

    public Boolean shared;

    public String refId;

    public String purchaseOrderRefId;

    public Currency currency;

    public BigDecimal amount;

    public Double remainingAmount;

    public BigDecimal amountOpen;

    public BigDecimal amountReceived;

    public Boolean isOpex;

    public Boolean isCancelled;

    public CostCenter costCenter;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param lineItem
     *            the line item in the DB
     */
    public PurchaseOrderLineItemListView(PurchaseOrderLineItem lineItem) {
        this(lineItem, null, null);
    }

    /**
     * Construct a list view line item that is engaged by a work order with a DB
     * entry.
     * 
     * @param lineItem
     *            the line item in the DB
     * @param portfolioEntryId
     *            the portfolio entry id for which the line item is engaged
     * @param workOrderId
     *            the work order id for which the line item is engaged
     */
    public PurchaseOrderLineItemListView(PurchaseOrderLineItem lineItem, Long portfolioEntryId, Long workOrderId) {

        this.id = lineItem.id;

        if (workOrderId != null) {
            this.workOrderId = workOrderId;
        }

        if (portfolioEntryId != null) {
            this.portfolioEntryId = portfolioEntryId;
        }

        this.shared = lineItem.isShared();
        this.isAssociated = lineItem.isAssociated();

        this.refId = lineItem.refId;
        this.purchaseOrderRefId = lineItem.purchaseOrder.refId;
        this.currency = lineItem.currency;
        this.amount = lineItem.amount;
        this.isOpex = lineItem.isOpex;
        this.isCancelled = lineItem.isCancelled;
        this.costCenter = lineItem.costCenter;

        this.amountReceived = lineItem.amountReceived;

        if (this.amountReceived != null && this.amount != null) {
            this.amountOpen = this.amount.subtract(this.amountReceived);
        }

        if (workOrderId != null) {
            WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);
            this.remainingAmount = lineItem.getRemainingAmount(workOrder);
        } else {
            this.remainingAmount = lineItem.getRemainingAmount();
        }
    }
}
