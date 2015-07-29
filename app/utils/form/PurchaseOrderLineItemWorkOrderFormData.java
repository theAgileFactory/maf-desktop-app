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
package utils.form;

import java.math.BigDecimal;

import models.finance.WorkOrder;
import play.data.validation.Constraints.Required;

/**
 * A purchase order line item work order form data is used to edit the engaged
 * amount of a shared work order engaged to a line item.
 * 
 * @author Johann Kohler
 */
public class PurchaseOrderLineItemWorkOrderFormData {

    public Long lineItemId;

    public Long workOrderId;

    @Required
    public BigDecimal amount;

    /**
     * Default constructor.
     */
    public PurchaseOrderLineItemWorkOrderFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param workOrder
     *            the work order in the DB
     */
    public PurchaseOrderLineItemWorkOrderFormData(WorkOrder workOrder) {
        this.lineItemId = workOrder.purchaseOrderLineItem.id;
        this.workOrderId = workOrder.id;
        this.amount = workOrder.amount;
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param workOrder
     *            the work order in the DB
     */
    public void fill(WorkOrder workOrder) {
        workOrder.amount = this.amount;
    }

}
