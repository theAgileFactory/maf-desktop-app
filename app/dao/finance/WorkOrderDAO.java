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
package dao.finance;

import java.util.List;

import com.avaje.ebean.Model.Finder;

import models.finance.WorkOrder;

/**
 * DAO for the {@link WorkOrder} object.
 * 
 * @author Pierre-Yves Cloux
 */
public abstract class WorkOrderDAO {

    /**
     * Default finder for the entity class.
     */
    public static Finder<Long, WorkOrder> findWorkOrder = new Finder<>(WorkOrder.class);

    /**
     * Default constructor.
     */
    public WorkOrderDAO() {
    }

    /**
     * Get a work order by id.
     * 
     * @param id
     *            the work order id
     */
    public static WorkOrder getWorkOrderById(Long id) {
        return WorkOrderDAO.findWorkOrder.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get the work orders of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     **/
    public static List<WorkOrder> getWorkOrderAsList(Long portfolioEntryId) {
        return WorkOrderDAO.findWorkOrder.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId).findList();
    }

    /**
     * Get the work order of a portfolio entry that has been generated from an
     * allocated resource.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param resourceObjectType
     *            the type of the allocated resource
     * @param resourceObjectId
     *            the id of the allocated resource
     * @param isEngaged
     *            true to get the engaged work order, else the cost to complete
     * @param usePurchaseOrder
     *            true if the system is configured to use the purchase order,
     *            else false
     */
    public static WorkOrder getWorkOrderByPEAndResource(Long portfolioEntryId, String resourceObjectType, Long resourceObjectId, boolean isEngaged,
            boolean usePurchaseOrder) {
        List<WorkOrder> workOrders = WorkOrderDAO.findWorkOrder.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId)
                .eq("resourceObjectType", resourceObjectType).eq("resourceObjectId", resourceObjectId).findList();
        for (WorkOrder workOrder : workOrders) {
            if (workOrder.getComputedIsEngaged(usePurchaseOrder) == isEngaged) {
                return workOrder;
            }
        }
        return null;
    }

    /**
     * Get the work orders of a portfolio entry that has been generated from an
     * allocated resource.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<WorkOrder> getWorkOrderAsListAndResourceByPE(Long portfolioEntryId) {
        return WorkOrderDAO.findWorkOrder.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId).isNotNull("resourceObjectType").findList();
    }

    /**
     * Return true if there is at least one work order for the currency.
     * 
     * @param currency
     *            the currency code
     */
    public static boolean hasWorkOrderByCurrency(String currency) {
        return WorkOrderDAO.findWorkOrder.where().eq("deleted", false).eq("currency.code", currency).findRowCount() > 0;
    }

}
