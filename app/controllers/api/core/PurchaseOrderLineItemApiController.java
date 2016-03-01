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

import java.util.List;
import java.util.Map;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import controllers.api.request.post.PurchaseOrderLineItemRequest;
import dao.finance.CostCenterDAO;
import dao.finance.CurrencyDAO;
import dao.finance.PurchaseOrderDAO;
import dao.finance.SupplierDAO;
import dao.pmo.ActorDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.finance.PurchaseOrderLineItem;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link PurchaseOrderLineItem}.
 * 
 * @author Marc Schaer
 */
@Api(value = "/api/core/purchase-order", description = "Operations on Purchase Orders Line Items")
public class PurchaseOrderLineItemApiController extends ApiController {

    public static Form<PurchaseOrderLineItemRequest> purchaseOrderLineItemRequestFormTemplate = Form.form(PurchaseOrderLineItemRequest.class);

    /**
     * Get the line item list of a purchase order with filters.
     * 
     * @param poId
     *            the purchase order id
     * @param isCancelled
     *            the flag to define if the purchase order is cancelled
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified purchase order line item list for a PO", notes = "Return a purchase order line item list with a specified PO's id", response = PurchaseOrderLineItem.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPurchaseOrderLinesList(@ApiParam(value = "purchaseOrder's id", required = true) @PathParam("po_id") Long poId,
            @ApiParam(value = "isCancelled", required = false) @QueryParam("isCancelled") Boolean isCancelled) {

        try {
            if (PurchaseOrderDAO.getPurchaseOrderById(poId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The purchaseOrder with the specified id is not found"));
            }
            return getJsonSuccessResponse(PurchaseOrderDAO.getPurchaseOrderLineItemActiveAsListByPO(poId, isCancelled));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create a Purchase Order Line Item.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a Purchase Order Line Item", notes = "Create a Purchase Order Line Item", response = PurchaseOrderLineItemRequest.class, httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A Purchase Order Line Item", required = true, dataType = "PurchaseOrderLineItemRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createPurchaseOrderLineItem(@ApiParam(value = "purchaseOrder's id", required = true) @PathParam("po_id") Long poId) {
        try {
            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PurchaseOrderLineItemRequest> purchaseOrderLineItemRequestForm = purchaseOrderLineItemRequestFormTemplate.bind(json);

            // check if purchase order exist
            if (PurchaseOrderDAO.getPurchaseOrderById(poId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The purchase order with the specified id is not found"));
            }

            // if errors
            if (purchaseOrderLineItemRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = purchaseOrderLineItemRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            PurchaseOrderLineItemRequest purchaseOrderLineItemRequest = purchaseOrderLineItemRequestForm.get();

            // Save Purchase Order Line item
            PurchaseOrderLineItem purchaseOrderLineItem = new PurchaseOrderLineItem();

            // fill to match with DB
            purchaseOrderLineItem.isCancelled = purchaseOrderLineItemRequest.isCancelled;
            purchaseOrderLineItem.refId = purchaseOrderLineItemRequest.refId;
            purchaseOrderLineItem.description = purchaseOrderLineItemRequest.description;
            purchaseOrderLineItem.currency = CurrencyDAO.getCurrencyById(purchaseOrderLineItemRequest.currencyId);
            purchaseOrderLineItem.currencyRate = purchaseOrderLineItemRequest.currencyRate;
            purchaseOrderLineItem.lineId = purchaseOrderLineItemRequest.lineId;
            purchaseOrderLineItem.supplier = SupplierDAO.getSupplierById(purchaseOrderLineItemRequest.supplierId);
            purchaseOrderLineItem.quantity = purchaseOrderLineItemRequest.quantity;
            purchaseOrderLineItem.quantityTotalReceived = purchaseOrderLineItemRequest.quantityTotalReceived;
            purchaseOrderLineItem.quantityBilled = purchaseOrderLineItemRequest.quantityBilled;
            purchaseOrderLineItem.amount = purchaseOrderLineItemRequest.amount;
            purchaseOrderLineItem.amountReceived = purchaseOrderLineItemRequest.amountReceived;
            purchaseOrderLineItem.amountBilled = purchaseOrderLineItemRequest.amountBilled;
            purchaseOrderLineItem.unitPrice = purchaseOrderLineItemRequest.unitPrice;
            purchaseOrderLineItem.materialCode = purchaseOrderLineItemRequest.materialCode;
            purchaseOrderLineItem.glAccount = purchaseOrderLineItemRequest.glAccount;
            purchaseOrderLineItem.isOpex = purchaseOrderLineItemRequest.isOpex;
            purchaseOrderLineItem.creationDate = purchaseOrderLineItemRequest.creationDate;
            purchaseOrderLineItem.dueDate = purchaseOrderLineItemRequest.dueDate;
            purchaseOrderLineItem.shipmentType = PurchaseOrderDAO.getPurchaseOrderLineShipmentStatutsTypeById(purchaseOrderLineItemRequest.shipmentTypeId);
            purchaseOrderLineItem.requester = ActorDao.getActorById(purchaseOrderLineItemRequest.requesterId);
            purchaseOrderLineItem.costCenter = CostCenterDAO.getCostCenterById(purchaseOrderLineItemRequest.costCenterId);
            // TODO : purchaseOrderLineItem.goodsReceipts =
            // purchaseOrderLineItemRequest.goodsReceipts;
            purchaseOrderLineItem.purchaseOrder = PurchaseOrderDAO.getPurchaseOrderById(purchaseOrderLineItemRequest.purchaseOrderId);
            purchaseOrderLineItem.workOrders = purchaseOrderLineItemRequest.workOrders;
            purchaseOrderLineItem.save();

            // return json success
            return getJsonSuccessCreatedResponse(purchaseOrderLineItem);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a purchase order line item.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param poId
     *            the purchase order id
     * @param lineId
     *            the purchase order line item id
     * 
     * @return the JSON object of the corresponding purchase order.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Purchase Order Line Item, default for empty fields : null", notes = "Update a Purchase Order Line Item", response = PurchaseOrderLineItemRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "An input purchase order line item object", required = true, dataType = "PurchaseOrderLineItemRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updatePurchaseOrderLineItem(@ApiParam(value = "A purchase order line item id", required = true) @PathParam("line_id") Long lineId,
            @ApiParam(value = "A purchase order id", required = true) @PathParam("po_id") Long poId) {
        try {

            // check if purchase order exist
            if (PurchaseOrderDAO.getPurchaseOrderById(poId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The purchase order with the specified id is not found"));
            }
            // check if purchase order line item exist
            if (PurchaseOrderDAO.getPurchaseOrderLineItemById(lineId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The purchase order line item with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PurchaseOrderLineItemRequest> purchaseOrderLineItemRequestForm = purchaseOrderLineItemRequestFormTemplate.bind(json);

            // if errors
            if (purchaseOrderLineItemRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = purchaseOrderLineItemRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            PurchaseOrderLineItemRequest purchaseOrderLineItemRequest = purchaseOrderLineItemRequestForm.get();

            // Save Purchase Order Line Item
            PurchaseOrderLineItem purchaseOrderLineItem = PurchaseOrderDAO.getPurchaseOrderLineItemById(lineId);

            purchaseOrderLineItem.isCancelled = purchaseOrderLineItemRequest.isCancelled;
            purchaseOrderLineItem.refId = purchaseOrderLineItemRequest.refId;
            purchaseOrderLineItem.description = purchaseOrderLineItemRequest.description;
            purchaseOrderLineItem.currency = CurrencyDAO.getCurrencyById(purchaseOrderLineItemRequest.currencyId);
            purchaseOrderLineItem.currencyRate = purchaseOrderLineItemRequest.currencyRate;
            purchaseOrderLineItem.lineId = purchaseOrderLineItemRequest.lineId;
            purchaseOrderLineItem.supplier = SupplierDAO.getSupplierById(purchaseOrderLineItemRequest.supplierId);
            purchaseOrderLineItem.quantity = purchaseOrderLineItemRequest.quantity;
            purchaseOrderLineItem.quantityTotalReceived = purchaseOrderLineItemRequest.quantityTotalReceived;
            purchaseOrderLineItem.quantityBilled = purchaseOrderLineItemRequest.quantityBilled;
            purchaseOrderLineItem.amount = purchaseOrderLineItemRequest.amount;
            purchaseOrderLineItem.amountReceived = purchaseOrderLineItemRequest.amountReceived;
            purchaseOrderLineItem.amountBilled = purchaseOrderLineItemRequest.amountBilled;
            purchaseOrderLineItem.unitPrice = purchaseOrderLineItemRequest.unitPrice;
            purchaseOrderLineItem.materialCode = purchaseOrderLineItemRequest.materialCode;
            purchaseOrderLineItem.glAccount = purchaseOrderLineItemRequest.glAccount;
            purchaseOrderLineItem.isOpex = purchaseOrderLineItemRequest.isOpex;
            purchaseOrderLineItem.creationDate = purchaseOrderLineItemRequest.creationDate;
            purchaseOrderLineItem.dueDate = purchaseOrderLineItemRequest.dueDate;
            purchaseOrderLineItem.shipmentType = PurchaseOrderDAO.getPurchaseOrderLineShipmentStatutsTypeById(purchaseOrderLineItemRequest.shipmentTypeId);
            purchaseOrderLineItem.requester = ActorDao.getActorById(purchaseOrderLineItemRequest.requesterId);
            purchaseOrderLineItem.costCenter = CostCenterDAO.getCostCenterById(purchaseOrderLineItemRequest.costCenterId);
            // TODO : purchaseOrderLineItem.goodsReceipts =
            // purchaseOrderLineItemRequest.goodsReceipts;
            purchaseOrderLineItem.purchaseOrder = PurchaseOrderDAO.getPurchaseOrderById(purchaseOrderLineItemRequest.purchaseOrderId);
            purchaseOrderLineItem.workOrders = purchaseOrderLineItemRequest.workOrders;
            purchaseOrderLineItem.save();

            // json success
            return getJsonSuccessResponse(purchaseOrderLineItem);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }
}
