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

import models.pmo.Stakeholder;
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
import controllers.api.request.StakeholderListRequest;
import dao.pmo.StakeholderDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;

/**
 * The API controller for the {@link Stakeholder}.
 * 
 * @author Oury Diallo
 */
@Api(value = "/api/core/stakeholder", description = "Operations on Stakeholders")
public class StakeholderApiController extends ApiController {

    public static Form<StakeholderListRequest> stakeholderListRequestFormTemplate = Form.form(StakeholderListRequest.class);
    public static ObjectMapper stakeholderMapper = new ObjectMapper();

    /**
     * Get the stakeholders list with filters.
     * 
     * @param actorId
     *            if not null then return only stakeholders associated the given
     *            actor.
     * @param portfolioId
     *            if not null then return only stakeholders associated the given
     *            portfolio.
     * @param portfolioEntryId
     *            if not null then return only stakeholders associated the given
     *            portfolio entry.
     * @param stakeholderTypeId
     *            if not null then return only stakeholders with the given type.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Stakeholders", notes = "Return the list of Stakeholders in the system", response = Stakeholder.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getStakeholdersList(@ApiParam(value = "actorId", required = false) @QueryParam("actorId") Long actorId, @ApiParam(
            value = "portfolioId", required = false) @QueryParam("portfolioId") Long portfolioId,
            @ApiParam(value = "portfolioEntryId", required = false) @QueryParam("portfolioEntryId") Long portfolioEntryId, @ApiParam(
                    value = "stakeholderTypeId", required = false) @QueryParam("stakeholderTypeId") Long stakeholderTypeId) {

        try {

            // Validation form
            StakeholderListRequest stakeholderListRequest = new StakeholderListRequest(actorId, portfolioId, portfolioEntryId, stakeholderTypeId);

            // object to jsonNode
            JsonNode node = stakeholderMapper.valueToTree(stakeholderListRequest);

            // fill a play form
            Form<StakeholderListRequest> stakeholderListRequestForm = stakeholderListRequestFormTemplate.bind(node);

            if (stakeholderListRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = stakeholderListRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            return getJsonSuccessResponse(StakeholderDao.getStakeholderAsListByFilter(actorId, portfolioId, portfolioEntryId, stakeholderTypeId));
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a stakeholder by id.
     * 
     * @param id
     *            the stakeholder id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Stakeholder", notes = "Return the Stakeholder with the specified id", response = Stakeholder.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getStakeholderById(@ApiParam(value = "stakeholder id", required = true) @PathParam("id") Long id) {

        try {
            if (StakeholderDao.getStakeholderById(id) == null) {
                return getJsonErrorResponse(new ApiError(404, "The Stakeholder with the specified id is not found"));
            }
            return getJsonSuccessResponse(StakeholderDao.getStakeholderById(id));

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
