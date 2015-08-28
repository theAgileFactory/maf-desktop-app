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
import dao.datasyndication.DataSyndicationDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import models.datasyndication.DataSyndication;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the Data syndication.
 * 
 * @author Johann Kohler
 */
@Api(value = "/api/core/data-syndication", description = "Operations on syndicated data")
public class DataSyndicationApiController extends ApiController {

    /**
     * Post a syndicated data.
     * 
     * @param agreementLinkId
     *            the agreement link id
     * @param agreementItemId
     *            the agreement item id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Post syndicated data", notes = "Post a syndicated data for an item of an agreement link.", httpMethod = "POST")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "A syndicated data", required = true, dataType = "Json", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 204, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result postData(@ApiParam(value = "Agreement link ID", required = true) @PathParam("agreementLinkId") Long agreementLinkId,
            @ApiParam(value = "Agreement item ID", required = true) @PathParam("agreementItemId") Long agreementItemId) {

        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            DataSyndication dataSyndication = DataSyndicationDao.getDataSyndicationByLinkAndItem(agreementLinkId, agreementItemId);
            if (dataSyndication == null) {
                // edit case
                dataSyndication = new DataSyndication();
                dataSyndication.dataSyndicationAgreementLinkId = agreementLinkId;
                dataSyndication.dataSyndicationAgreementItemId = agreementItemId;
            }

            dataSyndication.data = new ObjectMapper().writeValueAsString(json);
            dataSyndication.save();

            // return json success
            return noContent();

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
