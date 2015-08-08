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

import models.pmo.OrgUnitType;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.pmo.OrgUnitDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link OrgUnitType}.
 * 
 * @author Oury Diallo
 * 
 */
@Api(value = "/api/core/org-unit-type", description = "Operations on Org Unit Types")
public class OrgUnitTypeApiController extends ApiController {

    /**
     * Get the org unit types list with filters.
     * 
     * @param selectable
     *            true to return only active org units, false only non-active,
     *            null all.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Org Unit Types", notes = "Return the list of the Org Unit Types in the system", response = OrgUnitType.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getOrgUnitTypesList(@ApiParam(value = "selectable", required = false) @QueryParam("selectable") Boolean selectable) {
        try {
            return getJsonSuccessResponse(OrgUnitDao.getOrgUnitTypeAsListByFilter(selectable));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get an org unit type by id.
     * 
     * @param id
     *            the org unit id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Org Unit Type", notes = "Return the Org Unit Type with the specified id", response = OrgUnitType.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getOrgUnitTypeById(@ApiParam(value = "org unit id", required = true) @PathParam("id") Long id) {
        try {
            if (OrgUnitDao.getOrgUnitTypeById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Org Unit Type with the specified id is not found"));
            }
            return getJsonSuccessResponse(OrgUnitDao.getOrgUnitTypeById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
