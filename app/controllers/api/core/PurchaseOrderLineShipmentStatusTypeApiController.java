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
import controllers.api.request.post.PurchaseOrderLineShipmentStatusTypeRequest;
import dao.finance.PurchaseOrderDAO;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.finance.PurchaseOrderLineShipmentStatusType;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link PurchaseOrderLineShipmentStatusType}.
 * 
 * @author Marc Schaer
 * 
 */
@Api(value = "/api/core/purchase-order-shipment-status-type", description = "Operations on Purchase Order Line Shipment Status type")
public class PurchaseOrderLineShipmentStatusTypeApiController extends ApiController {

    public static Form<PurchaseOrderLineShipmentStatusTypeRequest> purchaseOrderLineShipmentStatusTypeRequestFormTemplate = Form
            .form(PurchaseOrderLineShipmentStatusTypeRequest.class);

    /**
     * Get the purchase orders line shipment status type list with filter.
     * 
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the active purchase orders line shipment status type", notes = "Return list of purchase orders line shipment status type in the system", response = PurchaseOrderLineShipmentStatusType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPurchaseOrdersLineShipmentStatusTypeList() {

        try {
            return getJsonSuccessResponse(PurchaseOrderDAO.getPurchaseOrderLineShipmentStatusTypeAsList());
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a purchase order line Shipment status type by id.
     * 
     * @param id
     *            the purchase order id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified purchase order line shipment status type", notes = "Return a purchase order line shipment status type with a specified id", response = PurchaseOrderLineShipmentStatusType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPurchaseOrderLineShipmentStatusTypeById(
            @ApiParam(value = "purchaseOrderLineShipmentStatusType's id", required = true) @PathParam("id") Long id) {

        try {
            if (PurchaseOrderDAO.getPurchaseOrderLineShipmentStatutsTypeById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The purchaseOrderLineShipmentStatusType with the specified id is not found"));
            }
            return getJsonSuccessResponse(PurchaseOrderDAO.getPurchaseOrderLineShipmentStatutsTypeById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Create a Purchase Order Line Shipment Status Type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a Purchase Order Line Shipment Status Type", notes = "Create a Purchase Order Line Shipment Status Type", response = PurchaseOrderLineShipmentStatusTypeRequest.class, httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A Purchase Order Line Shipment Status Type", required = true, dataType = "PurchaseOrderLineShipmentStatusTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createPurchaseOrderLineShipmentStatusType() {
        try {
            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PurchaseOrderLineShipmentStatusTypeRequest> purchaseOrderLineShipmentStatusTypeRequestForm = purchaseOrderLineShipmentStatusTypeRequestFormTemplate
                    .bind(json);

            // if errors
            if (purchaseOrderLineShipmentStatusTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = purchaseOrderLineShipmentStatusTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            PurchaseOrderLineShipmentStatusTypeRequest purchaseOrderLineShipmentStatusTypeRequest = purchaseOrderLineShipmentStatusTypeRequestForm.get();

            // Save purchase order line shipment status type
            PurchaseOrderLineShipmentStatusType purchaseOrderLineShipmentStatusType = new PurchaseOrderLineShipmentStatusType();

            // fill to match with DB
            purchaseOrderLineShipmentStatusType.refId = purchaseOrderLineShipmentStatusTypeRequest.refId;
            purchaseOrderLineShipmentStatusType.name = purchaseOrderLineShipmentStatusTypeRequest.name;
            purchaseOrderLineShipmentStatusType.description = purchaseOrderLineShipmentStatusTypeRequest.description;
            purchaseOrderLineShipmentStatusType.isAmountExpanded = purchaseOrderLineShipmentStatusTypeRequest.isAmountExpanded;
            purchaseOrderLineShipmentStatusType.save();

            // return json success
            return getJsonSuccessCreatedResponse(purchaseOrderLineShipmentStatusType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a purchase order line shipment status type.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the purchase order line shipment status type id
     * 
     * @return the JSON object of the corresponding purchase order.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Purchase Order, default for empty fields : null", notes = "Update a Purchase Order", response = PurchaseOrderLineShipmentStatusTypeRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "An input purchase order line shipment status type object", required = true, dataType = "PurchaseOrderLineShipmentStatusTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updatePurchaseOrderLineShipmentStatusType(
            @ApiParam(value = "A purchase order line shipment status type id", required = true) @PathParam("id") Long id) {
        try {

            PurchaseOrderLineShipmentStatusType purchaseOrderLineShipmentStatusType = PurchaseOrderDAO.getPurchaseOrderLineShipmentStatutsTypeById(id);
            // check if purchase order line shipment status type exist
            if (purchaseOrderLineShipmentStatusType == null) {
                return getJsonErrorResponse(new ApiError(404, "The purchase order line shipment status type with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<PurchaseOrderLineShipmentStatusTypeRequest> purchaseOrderLineShipmentStatusTypeRequestForm = purchaseOrderLineShipmentStatusTypeRequestFormTemplate
                    .bind(json);

            // if errors
            if (purchaseOrderLineShipmentStatusTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = purchaseOrderLineShipmentStatusTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            PurchaseOrderLineShipmentStatusTypeRequest purchaseOrderLineShipmentStatusTypeRequest = purchaseOrderLineShipmentStatusTypeRequestForm.get();

            // Save purchase order line shipment status type
            purchaseOrderLineShipmentStatusType.refId = purchaseOrderLineShipmentStatusTypeRequest.refId;
            purchaseOrderLineShipmentStatusType.name = purchaseOrderLineShipmentStatusTypeRequest.name;
            purchaseOrderLineShipmentStatusType.description = purchaseOrderLineShipmentStatusTypeRequest.description;
            purchaseOrderLineShipmentStatusType.isAmountExpanded = purchaseOrderLineShipmentStatusTypeRequest.isAmountExpanded;
            purchaseOrderLineShipmentStatusType.save();

            // json success
            return getJsonSuccessResponse(purchaseOrderLineShipmentStatusType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }
}
