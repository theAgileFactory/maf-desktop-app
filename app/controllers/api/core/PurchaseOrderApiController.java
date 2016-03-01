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
import controllers.api.request.post.PurchaseOrderRequest;
import dao.finance.PurchaseOrderDAO;
import dao.pmo.PortfolioEntryDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.finance.PurchaseOrder;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link PurchaseOrder}.
 * 
 * @author Oury Diallo
 * @author Pierre-Yves Cloux
 * @author Marc Schaer
 */
@Api(value = "/api/core/purchase-order", description = "Operations on Purchase Orders")
public class PurchaseOrderApiController extends ApiController {

    public static Form<PurchaseOrderRequest> purchaseOrderRequestFormTemplate = Form.form(PurchaseOrderRequest.class);

    /**
     * Get the purchase orders list with filter.
     * 
     * @param isCancelled
     *            true to return only cancelled purchase order, false only not
     *            cancelled, null all.
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the active purchase orders", notes = "Return list of purchase orders in the system", response = PurchaseOrder.class, httpMethod = "GET")
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
    @ApiOperation(value = "Get the specified purchase order", notes = "Return an purchase order with a specified id", response = PurchaseOrder.class, httpMethod = "GET")
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
     * Create a Purchase Order.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a Purchase Order", notes = "Create a Purchase Order", response = PurchaseOrderRequest.class, httpMethod = "POST")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "A Purchase Order", required = true, dataType = "PurchaseOrderRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createPurchaseOrder() {
        try {
            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PurchaseOrderRequest> purchaseOrderRequestForm = purchaseOrderRequestFormTemplate.bind(json);

            // if errors
            if (purchaseOrderRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = purchaseOrderRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            PurchaseOrderRequest purchaseOrderRequest = purchaseOrderRequestForm.get();

            // Save purchase order
            PurchaseOrder purchaseOrder = new PurchaseOrder();

            // fill to match with DB
            purchaseOrder.refId = purchaseOrderRequest.refId;
            purchaseOrder.isCancelled = purchaseOrderRequest.isCancelled;
            purchaseOrder.description = purchaseOrderRequest.description;
            purchaseOrder.portfolioEntry = PortfolioEntryDao.getPEById(purchaseOrderRequest.portfolioEntryId);
            purchaseOrder.save();

            // return json success
            return getJsonSuccessCreatedResponse(purchaseOrder);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a purchase order.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the purchase order id
     * 
     * @return the JSON object of the corresponding purchase order.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Purchase Order, default for empty fields : null", notes = "Update a Purchase Order", response = PurchaseOrderRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "An input purchase order object", required = true, dataType = "PurchaseOrderRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updatePurchaseOrder(@ApiParam(value = "A purchase order id", required = true) @PathParam("id") Long id) {
        try {

            // check if purchase order exist
            if (PurchaseOrderDAO.getPurchaseOrderById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The purchase order with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PurchaseOrderRequest> purchaseOrderRequestForm = purchaseOrderRequestFormTemplate.bind(json);

            // if errors
            if (purchaseOrderRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = purchaseOrderRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            PurchaseOrderRequest purchaseOrderRequest = purchaseOrderRequestForm.get();

            // Save purchase order
            PurchaseOrder purchaseOrder = PurchaseOrderDAO.getPurchaseOrderById(id);

            purchaseOrder.refId = purchaseOrderRequest.refId;
            purchaseOrder.isCancelled = purchaseOrderRequest.isCancelled;
            purchaseOrder.description = purchaseOrderRequest.description;
            purchaseOrder.portfolioEntry = PortfolioEntryDao.getPEById(purchaseOrderRequest.portfolioEntryId);
            purchaseOrder.save();

            // json success
            return getJsonSuccessResponse(purchaseOrder);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }
}
