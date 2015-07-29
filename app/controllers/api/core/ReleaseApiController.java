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

import models.delivery.Release;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import controllers.api.request.ReleaseListRequest;
import dao.delivery.ReleaseDAO;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link Release}.
 * 
 * @author Oury Diallo
 */
@Api(value = "/api/core/release", description = "Operations on Releases")
public class ReleaseApiController extends ApiController {

    public static Form<ReleaseListRequest> releaseListRequestFormTemplate = Form.form(ReleaseListRequest.class);
    public static ObjectMapper releaseMapper = new ObjectMapper();

    /**
     * Get the releases list with filters.
     * 
     * @param isActive
     *            true to return only active releases, false only non-active,
     *            null all.
     * @param managerId
     *            if not null then return only releases with the given manager.
     * @param portfolioEntryId
     *            if not null then return only releases for the given portfolio
     *            entry.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Releases", notes = "Return the list of the Releases in the system", response = Release.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public static Result getReleasesList(@ApiParam(value = "isActive", required = false) @QueryParam("isActive") Boolean isActive, @ApiParam(
            value = "managerId", required = false) @QueryParam("managerId") Long managerId,
            @ApiParam(value = "portfolioEntryId", required = false) @QueryParam("portfolioEntryId") Long portfolioEntryId) {

        try {

            // Validation form
            ReleaseListRequest releaseListRequest = new ReleaseListRequest(isActive, managerId, portfolioEntryId);

            // object to jsonNode
            JsonNode node = releaseMapper.valueToTree(releaseListRequest);

            // fill a play form
            Form<ReleaseListRequest> releaseListRequestForm = releaseListRequestFormTemplate.bind(node);

            if (releaseListRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = releaseListRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            return getJsonSuccessResponse(ReleaseDAO.getReleaseAsListByActiveAndManagerAndPE(isActive, managerId, portfolioEntryId));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a release by id.
     * 
     * @param id
     *            the release id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Release", notes = "Return the Release with the specified id", response = Release.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public static Result getReleaseById(@ApiParam(value = "Release  id", required = true) @PathParam("id") Long id) {

        try {
            if (ReleaseDAO.getReleaseById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Release with the specified id is not found"));
            }
            return getJsonSuccessResponse(ReleaseDAO.getReleaseById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
