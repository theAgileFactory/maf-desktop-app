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

import models.governance.LifeCycleProcess;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.governance.LifeCycleProcessDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link LifeCycleProcess}.
 * 
 * @author Oury Diallo
 * 
 */
@Api(value = "/api/core/life-cycle-process", description = "Operations on Life Cycle Processes")
public class LifeCycleProcessApiController extends ApiController {

    /**
     * Get all life cycle processes.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list Life Cycle Processes", notes = "Return the list of Life Cycle Processes in the system", response = LifeCycleProcess.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getLifeCycleProcessesList() {
        try {
            return getJsonSuccessResponse(LifeCycleProcessDao.getLCProcessAsList());
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a life cycle process by id.
     * 
     * @param id
     *            the life cycle process id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Life Cycle Process", notes = "Return the Life Cycle Process with the specified id",
            response = LifeCycleProcess.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request"),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getLifeCycleProcessById(@ApiParam(value = "life cycle process id", required = true) @PathParam("id") Long id) {
        try {
            if (LifeCycleProcessDao.getLCProcessById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Life Cycle Process with the specified id is not found"));
            }
            return getJsonSuccessResponse(LifeCycleProcessDao.getLCProcessById(id));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
