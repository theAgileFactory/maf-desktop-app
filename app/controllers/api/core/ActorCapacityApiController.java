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

import javax.ws.rs.QueryParam;

import models.pmo.ActorCapacity;
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
import controllers.api.request.ActorCapacityListRequest;
import dao.pmo.ActorDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link ActorCapacity}.
 * 
 * @author Oury Diallo
 */
@Api(value = "/api/core/actor-capacity", description = "Operations on Actor's Capacities")
public class ActorCapacityApiController extends ApiController {

    public static Form<ActorCapacityListRequest> actorCapacityListRequestFormTemplate = Form.form(ActorCapacityListRequest.class);
    public static ObjectMapper actorCapacityMapper = new ObjectMapper();

    /**
     * Get the actor capacities list with filters.
     * 
     * @param actorId
     *            if not null then return only actor capacities for the given
     *            actor.
     * @param year
     *            if not null then return only actor capacities with the given
     *            year.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list Actor's Capacities", notes = "Return the list of Actor's Capacities in the system", response = ActorCapacity.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getActorCapacitiesList(@ApiParam(value = "actorId", required = false) @QueryParam("actorId") Long actorId, @ApiParam(value = "year",
            required = false) @QueryParam("year") Integer year) {

        try {

            // Validation form
            ActorCapacityListRequest actorCapacityListRequest = new ActorCapacityListRequest(actorId, year);

            // object to jsonNode
            JsonNode node = actorCapacityMapper.valueToTree(actorCapacityListRequest);

            // fill a play form
            Form<ActorCapacityListRequest> actorCapacityListRequestForm = actorCapacityListRequestFormTemplate.bind(node);

            if (actorCapacityListRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = actorCapacityListRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            return getJsonSuccessResponse(ActorDao.getActorCapacityAsListByFilter(actorId, year));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }
}
