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
import controllers.api.request.post.CostCenterRequest;
import dao.finance.CostCenterDAO;
import dao.pmo.ActorDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.finance.CostCenter;
import models.finance.PurchaseOrderLineItem;
import models.pmo.ActorCapacity;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link ActorCapacity}.
 * 
 * @author Oury Diallo
 * @author Pierre-Yves Cloux
 * 
 */
@Api(value = "/api/core/cost-center", description = "Operations on Cost Centers")
public class CostCenterApiController extends ApiController {

    public static Form<CostCenterRequest> costCenterRequestFormTemplate = Form.form(CostCenterRequest.class);

    /**
     * Get all cost centers.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the cost centers", notes = "Return list of cost centers in the system", response = CostCenter.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getCostCentersList() {

        try {
            return getJsonSuccessResponse(CostCenterDAO.getCostCenterAsList());
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a cost center by id.
     * 
     * @param id
     *            the cost center id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified cost center", notes = "Return an cost center with a specified id", response = CostCenter.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getCostCenterById(@ApiParam(value = "costCenter's id", required = true) @PathParam("id") Long id) {

        try {
            if (CostCenterDAO.getCostCenterById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The costCenter with the specified id is not found"));
            }
            return getJsonSuccessResponse(CostCenterDAO.getCostCenterById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the line item list of a cost center with filters.
     * 
     * @param ccId
     *            the cost center id
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified purchase order line item list for a Cost Center", notes = "Return a purchase order line item list with a specified cost center id", response = PurchaseOrderLineItem.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPurchaseOrderLinesListForCostCenter(@ApiParam(value = "costCenter's id", required = true) @PathParam("cc_id") Long ccId) {

        try {
            if (CostCenterDAO.getCostCenterById(ccId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The costCenter with the specified id is not found"));
            }
            return getJsonSuccessResponse(CostCenterDAO.getPurchaseOrderLineItemActiveAsListByCC(ccId));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a line item of a cost center with filters.
     * 
     * @param lineId
     *            the line item id
     * @param ccId
     *            the cost center id
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified purchase order line item for a Cost Center", notes = "Return a purchase order line item with a specified cost center id", response = PurchaseOrderLineItem.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getPurchaseOrderLineItemForCostCenter(@ApiParam(value = "costCenter's id", required = true) @PathParam("cc_id") Long ccId,
            @ApiParam(value = "lineItem's id", required = true) @PathParam("line_id") Long lineId) {

        try {
            if (CostCenterDAO.getCostCenterById(ccId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The costCenter with the specified id is not found"));
            }
            return getJsonSuccessResponse(CostCenterDAO.getPurchaseOrderLineItemByCC(ccId, lineId));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create a Cost Center.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a Cost Center", notes = "Create a Cost Center", response = CostCenterRequest.class, httpMethod = "POST")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "A Cost Center", required = true, dataType = "CostCenterRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createCostCenter() {
        try {
            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<CostCenterRequest> costCenterRequestForm = costCenterRequestFormTemplate.bind(json);

            // if errors
            if (costCenterRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = costCenterRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            CostCenterRequest costCenterRequest = costCenterRequestForm.get();

            // Save cost center
            CostCenter costCenter = new CostCenter();

            // fill to match with DB
            costCenter.refId = costCenterRequest.refId;
            costCenter.name = costCenterRequest.name;
            costCenter.owner = ActorDao.getActorById(costCenterRequest.ownerId);
            costCenter.save();

            // return json success
            return getJsonSuccessCreatedResponse(costCenter);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update a cost center.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the cost center id
     * 
     * @return the JSON object of the corresponding cost center.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Cost Center, default for empty fields : null", notes = "Update a Cost Center", response = CostCenterRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "An input cost center object", required = true, dataType = "CostCenterRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updateCostCenter(@ApiParam(value = "A cost center id", required = true) @PathParam("id") Long id) {
        try {

            // check if purchase order exist
            if (CostCenterDAO.getCostCenterById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The cost center with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<CostCenterRequest> costCenterRequestForm = costCenterRequestFormTemplate.bind(json);

            // if errors
            if (costCenterRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = costCenterRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            CostCenterRequest costCenterRequest = costCenterRequestForm.get();

            // Save cose center
            CostCenter costCenter = CostCenterDAO.getCostCenterById(id);

            costCenter.refId = costCenterRequest.refId;
            costCenter.name = costCenterRequest.name;
            costCenter.owner = ActorDao.getActorById(costCenterRequest.ownerId);
            costCenter.save();

            // json success
            return getJsonSuccessResponse(costCenter);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }
}
