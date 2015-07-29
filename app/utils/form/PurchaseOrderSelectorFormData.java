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

import models.finance.WorkOrder;
import play.data.validation.Constraints.Required;

/**
 * An purchase order selector form data is used to select a purchase order.
 * 
 * @author Johann Kohler
 */
public class PurchaseOrderSelectorFormData {

    // portfolio entry id
    public Long id;

    public Long workOrderId;

    @Required
    public String purchaseOrderRefId;

    /**
     * Default constructor.
     */
    public PurchaseOrderSelectorFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param workOrder
     *            the work order in the DB
     */
    public PurchaseOrderSelectorFormData(WorkOrder workOrder) {
        this.id = workOrder.portfolioEntry.id;
        this.workOrderId = workOrder.id;
    }
}
