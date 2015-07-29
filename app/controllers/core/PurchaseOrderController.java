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
package controllers.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.finance.GoodsReceipt;
import models.finance.PurchaseOrder;
import models.finance.PurchaseOrderLineItem;
import models.finance.WorkOrder;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import utils.form.PurchaseOrderLineItemWorkOrderFormData;
import utils.table.GoodsReceiptListView;
import utils.table.PurchaseOrderLineItemListView;
import utils.table.PurchaseOrderLineItemWorkOrderListView;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import dao.finance.PurchaseOrderDAO;
import dao.finance.WorkOrderDAO;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;

/**
 * The controller which allows to display the purchase orders.
 * 
 * @author Johann Kohler
 */
public class PurchaseOrderController extends Controller {

    public static Form<PurchaseOrderLineItemWorkOrderFormData> workOrderFormTemplate = Form.form(PurchaseOrderLineItemWorkOrderFormData.class);

    /**
     * Display the details of a purchase order with its line items.
     * 
     * @param purchaseOrderId
     *            the purchase order id
     */
    @Restrict({ @Group(IMafConstants.PURCHASE_ORDER_VIEW_ALL_PERMISSION) })
    public static Result view(Long purchaseOrderId) {

        // get the purchase order
        PurchaseOrder purchaseOrder = PurchaseOrderDAO.getPurchaseOrderById(purchaseOrderId);

        // construct the items table
        Set<String> hideColumnsForLineItemTable = new HashSet<String>();
        hideColumnsForLineItemTable.add("purchaseOrderRefId");
        hideColumnsForLineItemTable.add("selectActionLink");
        hideColumnsForLineItemTable.add("amountReceived");
        hideColumnsForLineItemTable.add("amountOpen");
        List<PurchaseOrderLineItemListView> purchaseOrderLineItemListView = new ArrayList<PurchaseOrderLineItemListView>();
        for (PurchaseOrderLineItem lineItem : purchaseOrder.purchaseOrderLineItems) {
            purchaseOrderLineItemListView.add(new PurchaseOrderLineItemListView(lineItem));
        }
        Table<PurchaseOrderLineItemListView> purchaseOrderLineItemsTable =
                PurchaseOrderLineItemListView.templateTable.fill(purchaseOrderLineItemListView, hideColumnsForLineItemTable);

        return ok(views.html.core.purchaseorder.purchase_order_view.render(purchaseOrder, purchaseOrderLineItemsTable));
    }

    /**
     * Display the details of a purchase order line item with its work orders
     * and goods receipts.
     * 
     * @param lineItemId
     *            the line item id
     */
    @Restrict({ @Group(IMafConstants.PURCHASE_ORDER_VIEW_ALL_PERMISSION) })
    public static Result viewLineItem(Long lineItemId) {

        // get the purchase order line item
        PurchaseOrderLineItem lineItem = PurchaseOrderDAO.getPurchaseOrderLineItemById(lineItemId);

        // construct the goods receipt table
        List<GoodsReceiptListView> goodsReceiptListView = new ArrayList<GoodsReceiptListView>();
        for (GoodsReceipt goodsReceipt : lineItem.goodsReceipts) {
            goodsReceiptListView.add(new GoodsReceiptListView(goodsReceipt));
        }
        Table<GoodsReceiptListView> goodsReceiptTable = GoodsReceiptListView.templateTable.fill(goodsReceiptListView);

        // construct the work orders table
        List<PurchaseOrderLineItemWorkOrderListView> workOrderListView = new ArrayList<PurchaseOrderLineItemWorkOrderListView>();
        for (WorkOrder workOrder : lineItem.workOrders) {
            workOrderListView.add(new PurchaseOrderLineItemWorkOrderListView(workOrder));
        }
        Set<String> hideColumnsForWorkOrderTable = new HashSet<String>();
        if (!lineItem.isAssociated() || lineItem.isShared() == false) {
            hideColumnsForWorkOrderTable.add("editActionLink");
        }
        Table<PurchaseOrderLineItemWorkOrderListView> workOrderTable =
                PurchaseOrderLineItemWorkOrderListView.templateTable.fill(workOrderListView, hideColumnsForWorkOrderTable);

        return ok(views.html.core.purchaseorder.purchase_order_line_item_view.render(lineItem, workOrderTable, goodsReceiptTable));
    }

    /**
     * Edit a work order in a "purchase order" context. This allows the user to
     * change the allocated amount taking from the line item to a shared work
     * order.
     * 
     * note: for the non-shared purchase orders (meaning associated to one
     * non-shared work order), the allocated amount is simply the full amount of
     * the line item and so it can't be modified
     * 
     * @param lineItemId
     *            the line item id
     * @param workOrderId
     *            the work order id
     */
    @Restrict({ @Group(IMafConstants.PURCHASE_ORDER_VIEW_ALL_PERMISSION) })
    public static Result editWorkOrder(Long lineItemId, Long workOrderId) {

        // get the purchase order line item
        PurchaseOrderLineItem lineItem = PurchaseOrderDAO.getPurchaseOrderLineItemById(lineItemId);

        // get the work order
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        // initiate the form with the template
        Form<PurchaseOrderLineItemWorkOrderFormData> workOrderForm = workOrderFormTemplate.fill(new PurchaseOrderLineItemWorkOrderFormData(workOrder));

        return ok(views.html.core.purchaseorder.purchase_order_line_item_work_order_edit.render(lineItem, workOrder, workOrderForm));
    }

    /**
     * Process the save of the work order (allocated amount).
     */
    @Restrict({ @Group(IMafConstants.PURCHASE_ORDER_VIEW_ALL_PERMISSION) })
    public static Result saveWorkOrder() {

        // bind the form
        Form<PurchaseOrderLineItemWorkOrderFormData> boundForm = workOrderFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long lineItemId = Long.valueOf(boundForm.data().get("lineItemId"));
        PurchaseOrderLineItem lineItem = PurchaseOrderDAO.getPurchaseOrderLineItemById(lineItemId);

        // get the work order
        Long workOrderId = Long.valueOf(boundForm.data().get("workOrderId"));
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.purchaseorder.purchase_order_line_item_work_order_edit.render(lineItem, workOrder, boundForm));
        }

        PurchaseOrderLineItemWorkOrderFormData workOrderFormData = boundForm.get();

        // check the amount
        if (workOrderFormData.amount.doubleValue() > lineItem.getRemainingAmount(workOrder).doubleValue() + 0.01) {
            boundForm.reject("amount", Msg.get("object.work_order.amount.invalid"));
            return ok(views.html.core.purchaseorder.purchase_order_line_item_work_order_edit.render(lineItem, workOrder, boundForm));
        }

        workOrderFormData.fill(workOrder);
        workOrder.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.purchase_order.line_item.work_order.edit.successful"));

        return redirect(controllers.core.routes.PurchaseOrderController.viewLineItem(lineItemId));
    }

}
