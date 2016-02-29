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
import controllers.api.request.post.ActorTypeRequest;
import dao.pmo.ActorDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.pmo.ActorType;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the {@link ActorType}.
 * 
 * @author Oury Diallo
 * @author Marc Schaer
 * 
 */
@Api(value = "/api/core/actor-type", description = "Operations on Actor types")
public class ActorTypeApiController extends ApiController {

    public static Form<ActorTypeRequest> actorTypeRequestFormTemplate = Form.form(ActorTypeRequest.class);

    /**
     * Get the actor types list with filters.
     * 
     * @param selectable
     *            true to return only active type, false only non-active, null
     *            all.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list Actor Types", notes = "Return the list of Actor Types in the system", response = ActorType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getActorTypesList(@ApiParam(value = "selectable", required = false) @QueryParam("selectable") Boolean selectable) {
        try {
            return getJsonSuccessResponse(ActorDao.getActorTypeAsListByFilter(selectable));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get an actor type by id.
     * 
     * @param id
     *            the actor type id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Actor Type", notes = "Return the Actor Type with the specified id", response = ActorType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getActorTypeById(@ApiParam(value = "actor type id", required = true) @PathParam("id") Long id) {

        try {
            if (ActorDao.getActorTypeById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Actor Type with the specified id is not found"));
            }
            return getJsonSuccessResponse(ActorDao.getActorTypeById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Create an actor type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create an Actor Type", notes = "Create an Actor Type", response = ActorTypeRequest.class, httpMethod = "POST")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "An actor type", required = true, dataType = "ActorTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createActorType() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<ActorTypeRequest> actorTypeRequestForm = actorTypeRequestFormTemplate.bind(json);

            // if errors
            if (actorTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = actorTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            ActorTypeRequest actorTypeRequest = actorTypeRequestForm.get();

            // Save actor type
            ActorType actorType = new ActorType();

            // fill to match with DB
            actorType.name = actorTypeRequest.name;
            actorType.description = actorTypeRequest.description;
            actorType.refId = actorTypeRequest.refId;
            actorType.selectable = actorTypeRequest.selectable;

            actorType.save();

            // return json success
            return getJsonSuccessCreatedResponse(actorType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update an actor type.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the actor type id
     * 
     * @return the JSON object of the corresponding actor type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Actor type, default for empty fields : null", notes = "Update an Actor Type", response = ActorTypeRequest.class, httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "A input Actor Type object", required = true, dataType = "ActorTypeRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updateActorType(@ApiParam(value = "An actor type id", required = true) @PathParam("id") Long id) {
        try {

            // check if actor exist
            ActorType actorType = ActorDao.getActorTypeById(id);
            if (actorType == null) {
                return getJsonErrorResponse(new ApiError(404, "The Actor Type with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<ActorTypeRequest> actorTypeRequestForm = actorTypeRequestFormTemplate.bind(json);

            // if errors
            if (actorTypeRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = actorTypeRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            ActorTypeRequest actorTypeRequest = actorTypeRequestForm.get();

            // Save actor type
            // fill to match with DB
            actorType.name = actorTypeRequest.name;
            actorType.description = actorTypeRequest.description;
            actorType.refId = actorTypeRequest.refId;
            actorType.selectable = actorTypeRequest.selectable;

            actorType.save();

            // json success
            return getJsonSuccessResponse(actorType);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
