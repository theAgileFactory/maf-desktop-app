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

import java.util.Date;
import java.util.List;
import java.util.Map;

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
import framework.services.ServiceManager;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import framework.services.kpi.IKpiService;
import framework.services.kpi.Kpi;
import models.framework_models.kpi.KpiData;
import models.framework_models.kpi.KpiDefinition;
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
    public static Result addKpiData(@ApiParam(value = "KPI uid", required = true) @PathParam("uid") String uid) {
        try {

            uid = uid.trim();

            KpiDefinition kpiDefinition = KpiDefinition.getByUid(uid);
            if (kpiDefinition == null) {
                return getJsonErrorResponse(new ApiError(404, "The KPI with the specified uid is not found"));
            }

            Kpi kpi = ServiceManager.getService(IKpiService.NAME, IKpiService.class).getKpi(uid);
            if (kpi == null) {
                return getJsonErrorResponse(new ApiError(400, "Impossible to add a data for an inactive KPI"));
            }

            if (kpi.isStandard()) {
                return getJsonErrorResponse(new ApiError(400, "Impossible to add a data for a standard KPI"));
            }

            if (!kpi.isExternal()) {
                return getJsonErrorResponse(new ApiError(400, "Impossible to add a data for a KPI with data provided by BizDock"));
            }

            if (!kpi.hasBoxDisplay()) {
                return getJsonErrorResponse(new ApiError(400, "Impossible to add a data for a KPI without box display."));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<KpiDataRequest> kpiDataRequestForm = kpiDataRequestFormTemplate.bind(json);

            // if errors
            if (kpiDataRequestForm.hasErrors()) {
                Map<String, List<ValidationError>> allErrors = kpiDataRequestForm.errors();
                String errorMsg = ApiError.getValidationErrorsMessage(allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            KpiDataRequest kpiDataRequest = kpiDataRequestForm.get();

            // If the timestamp is not given then get the current date
            Date timestamp = kpiDataRequest.timestamp != null ? kpiDataRequest.timestamp : new Date();

            // Check the object id (should exist for the given object type)
            if (kpi.getKpiObjectsContainer().getObjectByIdForKpi(kpiDataRequest.objectId) == null) {
                return getJsonErrorResponse(new ApiError(400, "Impossible to find the corresponding object for the given objectId"));
            }

            KpiData mainKpiData = new KpiData();
            mainKpiData.kpiValueDefinition = kpiDefinition.mainKpiValueDefinition;
            mainKpiData.objectId = kpiDataRequest.objectId;
            mainKpiData.timestamp = timestamp;
            mainKpiData.value = kpiDataRequest.mainValue;
            mainKpiData.save();

            KpiData additional1KpiData = new KpiData();
            additional1KpiData.kpiValueDefinition = kpiDefinition.additional1KpiValueDefinition;
            additional1KpiData.objectId = kpiDataRequest.objectId;
            additional1KpiData.timestamp = timestamp;
            additional1KpiData.value = kpiDataRequest.additional1Value;
            additional1KpiData.save();

            KpiData additional2KpiData = new KpiData();
            additional2KpiData.kpiValueDefinition = kpiDefinition.additional2KpiValueDefinition;
            additional2KpiData.objectId = kpiDataRequest.objectId;
            additional2KpiData.timestamp = timestamp;
            additional2KpiData.value = kpiDataRequest.additional2Value;
            additional2KpiData.save();

            // return json success
            return noContent();

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
