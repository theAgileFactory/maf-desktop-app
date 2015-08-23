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

import models.pmo.Competency;
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
import controllers.api.request.CompetencyListRequest;
import dao.pmo.ActorDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link Competency}.
 * 
 * @author Oury Diallo
 * 
 */
@Api(value = "/api/core/competency", description = "Operations on Competencies")
public class CompetencyApiController extends ApiController {

    public static Form<CompetencyListRequest> competencyListRequestFormTemplate = Form.form(CompetencyListRequest.class);
    public static ObjectMapper competencyMapper = new ObjectMapper();

    /**
     * Get the competencies list with filters.
     * 
     * @param isActive
     *            true to return only active type, false only non-active, null
     *            all.
     * @param actorId
     *            if not null then return only competencies for the given actor.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Competencies", notes = "Return the list of Competencies in the system", response = Competency.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getCompetenciesList(@ApiParam(value = "isActive", required = false) @QueryParam("isActive") Boolean isActive, @ApiParam(
            value = "actorId", required = false) @QueryParam("actorId") Long actorId) {

        try {

            // Validation form
            CompetencyListRequest competencyListRequest = new CompetencyListRequest(isActive, actorId);

            // object to jsonNode
            JsonNode node = competencyMapper.valueToTree(competencyListRequest);

            // fill a play form
            Form<CompetencyListRequest> competencyListRequestForm = competencyListRequestFormTemplate.bind(node);

            if (competencyListRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = competencyListRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            return getJsonSuccessResponse(ActorDao.getCompetencyAsListByFilter(isActive, actorId));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a competency by id.
     * 
     * @param id
     *            the competency id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Competency", notes = "Return the Competency with the specified id", response = Competency.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getCompetencyById(@ApiParam(value = "competency id", required = true) @PathParam("id") Long id) {

        try {
            if (ActorDao.getCompetencyById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Competency with the specified id is not found"));
            }
            return getJsonSuccessResponse(ActorDao.getCompetencyById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
