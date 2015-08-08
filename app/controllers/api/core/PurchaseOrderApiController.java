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
package controllers.api.core;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import models.finance.PurchaseOrder;
import models.finance.PurchaseOrderLineItem;
import models.pmo.ActorCapacity;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.finance.PurchaseOrderDAO;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link ActorCapacity}.
 * 
 * @author Oury Diallo
 * @author Pierre-Yves Cloux
 */
@Api(value = "/api/core/purchase-order", description = "Operations on Purchase Orders")
public class PurchaseOrderApiController extends ApiController {

    /**
     * Get the purchase orders list with filter.
     * 
     * @param isCancelled
     *            true to return only cancelled purchase order, false only not
     *            cancelled, null all.
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the active purchase orders", notes = "Return list of purchase orders in the system", response = PurchaseOrder.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPurchaseOrdersList(@ApiParam(value = "isCancelled", required = false) @QueryParam("isCancelled") Boolean isCancelled) {

        try {
            return getJsonSuccessResponse(PurchaseOrderDAO.getPurchaseOrderAsListByFilter(isCancelled));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a purchase order by id.
     * 
     * @param id
     *            the purchase order id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified purchase order", notes = "Return an purchase order with a specified id", response = PurchaseOrder.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPurchaseOrderById(@ApiParam(value = "purchaseOrder's id", required = true) @PathParam("id") Long id) {

        try {
            if (PurchaseOrderDAO.getPurchaseOrderById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The purchaseOrder with the specified id is not found"));
            }
            return getJsonSuccessResponse(PurchaseOrderDAO.getPurchaseOrderById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Get the line item of a purchase order with filters.
     * 
     * @param id
     *            the purchase order id
     * @param isCancelled
     *            the flag to define if the purchase order is cancelled
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified purchase order line item", notes = "Return an purchase order line item with a specified id",
            response = PurchaseOrderLineItem.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPurchaseOrderLinesList(@ApiParam(value = "purchaseOrder's id", required = true) @PathParam("id") Long id, @ApiParam(
            value = "isCancelled", required = false) @QueryParam("isCancelled") Boolean isCancelled) {

        try {
            if (PurchaseOrderDAO.getPurchaseOrderById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The purchaseOrderLineItem with the specified id is not found"));
            }
            return getJsonSuccessResponse(PurchaseOrderDAO.getPurchaseOrderLineItemActiveAsListByPO(id, isCancelled));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

}
