package controllers.api.core;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import dao.timesheet.TimesheetDao;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import javax.ws.rs.PathParam;
import models.timesheet.TimesheetEntry;
import models.timesheet.TimesheetReport;
import play.mvc.Result;

/**
 * The API controller for the {@link models.timesheet.TimesheetEntry}.
 *
 * @author Guillaume Petit
 */
@Api(value = "/api/core/timesheet-entry", description = "Operations on Timesheet Entries")
public class TimesheetApiController extends ApiController {

    /**
     * Get a timesheet entry by id.
     *
     * @param id
     *            the timesheet entry id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Timesheet Entry ", notes = "Return the Timesheet Entry with the specified id", response = TimesheetEntry.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getTimesheetEntryById(@ApiParam(value = "Timesheet entry id", required = true) @PathParam("id") Long id) {

        try {
            TimesheetEntry timesheetEntryById = TimesheetDao.getTimesheetEntryById(id);
            if (timesheetEntryById == null) {
                return getJsonErrorResponse(new ApiError(404, "The Timesheet Entry with the specified id is not found"));
            }
            return getJsonSuccessResponse(timesheetEntryById);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get a timesheet report by id.
     *
     * @param id
     *            the timesheet report id
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get the specified Timesheet report ", notes = "Return the Timesheet report with the specified id", response = TimesheetReport.class,
            httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getTimesheetReportById(@ApiParam(value = "Timesheet report id", required = true) @PathParam("id") Long id) {

        try {
            TimesheetReport timesheetReportById = TimesheetDao.getTimesheetReportById(id);
            if (timesheetReportById == null) {
                return getJsonErrorResponse(new ApiError(404, "The Timesheet Report with the specified id is not found"));
            }
            return getJsonSuccessResponse(timesheetReportById);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
