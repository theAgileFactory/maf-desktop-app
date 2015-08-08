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

import models.finance.Supplier;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.finance.SupplierDAO;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link Supplier}.
 * 
 * @author Oury Diallo
 * @author Pierre-Yves Cloux
 */
@Api(value = "/api/core/supplier", description = "Operations on Suppliers")
public class SupplierApiController extends ApiController {

    /**
     * Get all suppliers.
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Suppliers", notes = "Return the list of Suppliers in the system", response = Supplier.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getSuppliersList() {

        try {
            return getJsonSuccessResponse(SupplierDAO.getSupplierAsList());
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a supplier by id.
     * 
     * @param id
     *            the supplier id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Supplier", notes = "Return the Supplier with the specified id", response = Supplier.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getSupplierById(@ApiParam(value = "supplier's id", required = true) @PathParam("id") Long id) {

        try {
            if (SupplierDAO.getSupplierById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Supplier with the specified id is not found"));
            }
            return getJsonSuccessResponse(SupplierDAO.getSupplierById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

}
