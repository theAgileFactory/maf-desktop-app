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

import models.finance.Currency;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.finance.CurrencyDAO;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link Currency}.
 * 
 * @author Oury Diallo
 * @author Pierre-Yves Cloux
 */
@Api(value = "/api/core/currency", description = "Operations on Currencies")
public class CurrencyApiController extends ApiController {

    /**
     * Get the currencies list with filter.
     * 
     * @param isActive
     *            the flag isActive
     **/
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Currencies", notes = "Return the list of Currencies in the system", response = Currency.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getCurrenciesList(@ApiParam(value = "isActive", required = false) @QueryParam("isActive") Boolean isActive) {

        try {
            return getJsonSuccessResponse(CurrencyDAO.getCurrencyAsListByActive(isActive));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a currency by code.
     * 
     * @param code
     *            the currency code (ISO)
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Currency", notes = "Return the Currency with the specified code", response = Currency.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getCurrencyByCode(@ApiParam(value = "code", required = true) @PathParam("code") String code) {
        try {
            if (CurrencyDAO.getCurrencyByCode(code) == null) {
                return getJsonErrorResponse(new ApiError(404, "The currency with the specified code is not found"));
            }
            return getJsonSuccessResponse(CurrencyDAO.getCurrencyByCode(code));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }
}
