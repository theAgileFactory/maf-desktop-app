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

import javax.inject.Inject;
import javax.ws.rs.PathParam;

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
import controllers.api.request.post.KpiDataRequest;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import framework.services.kpi.IKpiService;
import framework.services.kpi.KpiServiceImpl.KpiServiceException;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * The API controller for the KPI.
 * 
 * @author Johann Kohler
 */
@Api(value = "/api/core/kpi", description = "Operations on KPIs")
public class KpiApiController extends ApiController {
    @Inject
    private IKpiService kpiService;
    public static Form<KpiDataRequest> kpiDataRequestFormTemplate = Form.form(KpiDataRequest.class);

    /**
     * Add a KPI data.
     * 
     * @param uid
     *            the KPI definition uid
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Add KPI data", notes = "Add data (for main, additional 1 and additional 2 values) for a KPI", response = KpiDataRequest.class,
            httpMethod = "POST")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "A KPI data", required = true, dataType = "KpiDataRequest", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 204, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result addKpiData(@ApiParam(value = "KPI uid", required = true) @PathParam("uid") String uid) {
        try {

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<KpiDataRequest> kpiDataRequestForm = kpiDataRequestFormTemplate.bind(json);

            // if errors
            if (kpiDataRequestForm.hasErrors()) {
                Map<String, List<ValidationError>> allErrors = kpiDataRequestForm.errors();
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            KpiDataRequest kpiDataRequest = kpiDataRequestForm.get();

            try {
                getKpiService().addData(uid, kpiDataRequest.objectId, kpiDataRequest.timestamp, kpiDataRequest.mainValue, kpiDataRequest.additional1Value,
                        kpiDataRequest.additional2Value);
            } catch (KpiServiceException kpiE) {
                return getJsonErrorResponse(new ApiError(kpiE.getHttpCode(), kpiE.getMessage()));
            }

            // return json success
            return noContent();

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Get the KPI service.
     */
    private IKpiService getKpiService() {
        return kpiService;
    }

}
