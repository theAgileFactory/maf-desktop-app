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

import models.delivery.RequirementSeverity;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.delivery.RequirementDAO;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link RequirementSeverity}.
 * 
 * @author Oury Diallo
 */
@Api(value = "/api/core/requirement-severity", description = "Operations on Requirement Severities")
public class RequirementSeverityApiController extends ApiController {

    /**
     * Get all requirement severities.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Requirement Severities", notes = "Return the list of Requirement Severities in the system",
            response = RequirementSeverity.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getRequirementSeveritiesList() {
        try {
            return getJsonSuccessResponse(RequirementDAO.getRequirementSeverityAsList());
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a requirement severity by id.
     * 
     * @param id
     *            the requirement severity id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Requirement Severity", notes = "Return the Requirement Severity with the specified id",
            response = RequirementSeverity.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getRequirementSeverityById(@ApiParam(value = "Requirement Severity id", required = true) @PathParam("id") Long id) {

        try {
            if (RequirementDAO.getRequirementSeverityById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Requirement Severity with the specified id is not found"));
            }
            return getJsonSuccessResponse(RequirementDAO.getRequirementSeverityById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
