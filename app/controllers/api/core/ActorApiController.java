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

import models.pmo.Actor;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import controllers.api.request.ActorListRequest;
import controllers.api.request.post.ActorRequest;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link Actor}.
 * 
 * @author Pierre-Yves Cloux
 * 
 */
@Api(value = "/api/core/actor", description = "Operations on Actors")
public class ActorApiController extends ApiController {

    public static Form<ActorListRequest> actorListRequestFormTemplate = Form.form(ActorListRequest.class);
    public static Form<ActorRequest> actorRequestFormTemplate = Form.form(ActorRequest.class);
    public static ObjectMapper actorMapper = new ObjectMapper();

    /**
     * Get the actors list with filters.
     * 
     * @param isActive
     *            true to return only active actors, false only non-active, null
     *            all.
     * @param managerId
     *            if not null then return only actors with the given manager.
     * @param actorTypeId
     *            if not null then return only actors with the given type.
     * @param competencyId
     *            if not null then return only actors with the given competency.
     * @param orgUnitId
     *            if not null then return only actors with the given org unit.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Actors", notes = "Return the list of Actors in the system", response = Actor.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getActorsList(@ApiParam(value = "isActive", required = false) @QueryParam("isActive") Boolean isActive, @ApiParam(
            value = "managerId", required = false) @QueryParam("managerId") Long managerId,
            @ApiParam(value = "actorTypeId", required = false) @QueryParam("actorTypeId") Long actorTypeId, @ApiParam(value = "competencyId",
                    required = false) @QueryParam("competencyId") Long competencyId,
            @ApiParam(value = "orgUnitId", required = false) @QueryParam("orgUnitId") Long orgUnitId) {

        try {

            // Validation form
            ActorListRequest actorListRequest = new ActorListRequest(isActive, managerId, actorTypeId, competencyId, orgUnitId);

            // object to jsonNode
            JsonNode node = actorMapper.valueToTree(actorListRequest);

            // fill a play form
            Form<ActorListRequest> actorListRequestForm = actorListRequestFormTemplate.bind(node);

            if (actorListRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = actorListRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            return getJsonSuccessResponse(ActorDao.getActorAsListByFilter(isActive, managerId, actorTypeId, competencyId, orgUnitId));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get an actor by id.
     * 
     * @param id
     *            the actor id.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Actor", notes = "Return the Actor with the specified id", response = Actor.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getActorById(@ApiParam(value = "actor's id", required = true) @PathParam("id") Long id) {

        try {
            if (ActorDao.getActorById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Actor with the specified id is not found"));
            }
            return getJsonSuccessResponse(ActorDao.getActorById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Get an actor by uid.
     * 
     * @param uid
     *            the actor uid
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get a specified actor", notes = "Return the Actor with the specified uid", response = Actor.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getActorByUid(@ApiParam(value = "actor's uid", required = true) @PathParam("uid") String uid) {

        try {
            if (ActorDao.getActorByUid(uid) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Actor with the specified uid is not found"));
            }
            return getJsonSuccessResponse(ActorDao.getActorByUid(uid));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }

    }

    /**
     * Create an actor.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create an Actor", notes = "Create an Actor", response = ActorRequest.class, httpMethod = "POST")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "An actor", required = true, dataType = "ActorRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createActor() {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<ActorRequest> actorRequestForm = actorRequestFormTemplate.bind(json);

            // if errors
            if (actorRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = actorRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            ActorRequest actorRequest = actorRequestForm.get();

            // Save actor
            Actor actor = new Actor();

            // fill to match with DB
            actor.firstName = actorRequest.firstName;
            actor.employeeId = actorRequest.employeeId;
            actor.lastName = actorRequest.lastName;
            actor.uid = actorRequest.uid;
            actor.title = actorRequest.title;
            actor.isActive = actorRequest.isActive;
            actor.mail = actorRequest.mail;
            actor.mobilePhone = actorRequest.mobilePhone;
            actor.fixPhone = actorRequest.fixPhone;
            actor.actorType = ActorDao.getActorTypeById(actorRequest.actorTypeId);
            actor.erpRefId = actorRequest.erpRefId;
            actor.orgUnit = OrgUnitDao.getOrgUnitById(actorRequest.orgUnitId);
            actor.manager = ActorDao.getActorById(actorRequest.managerId);

            actor.save();

            // return json success
            return getJsonSuccessCreatedResponse(actor);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Update an actor.
     * 
     * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the actor id
     * 
     * @return the JSON object of the corresponding actor.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update the specified Actor, default for empty fields : null", notes = "Update an Actor", response = ActorRequest.class,
            httpMethod = "PUT")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "A input person object", required = true, dataType = "ActorRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updateActor(@ApiParam(value = "An actor id", required = true) @PathParam("id") Long id) {
        try {

            // check if actor exist
            if (ActorDao.getActorById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Actor with the specified id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<ActorRequest> actorRequestForm = actorRequestFormTemplate.bind(json);

            // if errors
            if (actorRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = actorRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(404, errorMsg));
            }

            // Validation Form
            ActorRequest actorRequest = actorRequestForm.get();

            // Save actor
            Actor actor = ActorDao.getActorById(id);

            // fill to match with DB
            actor.firstName = actorRequest.firstName;
            actor.employeeId = actorRequest.employeeId;
            actor.lastName = actorRequest.lastName;
            actor.uid = actorRequest.uid;
            actor.title = actorRequest.title;
            actor.isActive = actorRequest.isActive;
            actor.mail = actorRequest.mail;
            actor.mobilePhone = actorRequest.mobilePhone;
            actor.fixPhone = actorRequest.fixPhone;
            actor.actorType = ActorDao.getActorTypeById(actorRequest.actorTypeId);
            actor.erpRefId = actorRequest.erpRefId;
            actor.orgUnit = OrgUnitDao.getOrgUnitById(actorRequest.orgUnitId);
            actor.manager = ActorDao.getActorById(actorRequest.managerId);

            actor.save();

            // json success
            return getJsonSuccessResponse(actor);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
