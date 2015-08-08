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
package controllers.api.system;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import play.mvc.Result;

/**
 * A controller which gathers some system methods.
 * 
 * @author Pierre-Yves Cloux
 * 
 */
@Api(value = "/api/system", description = "Operations on System")
public class SystemApiController extends ApiController {

    /**
     * Default constructor.
     */
    public SystemApiController() {
    }

    /**
     * Return the current time of the system.<br/>
     * This value is required for subsequent calls in case there is a time
     * different between the server and the calling system.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class, allowTimeDifference = true)
    @ApiOperation(value = "Get system current time", notes = "Return the system current time to be used for subsequent API calls",
            response = TimeHolder.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = ApiController.SUCCESS_API_RESPONSE_CODE, message = ApiController.SUCCESS_API_RESPONSE_NAME),
            @ApiResponse(code = ApiController.ERROR_API_RESPONSE_CODE, message = ApiController.ERROR_API_RESPONSE_NAME, response = ApiError.class) })
    public Result getSystemCurrentTime() {
        return getJsonSuccessResponse(new TimeHolder(System.currentTimeMillis(), new Date().toString()));
    }

    /**
     * Time holder.
     * 
     * @author Pierre-Yves Cloux
     * 
     */
    public static class TimeHolder {
        @JsonProperty
        @ApiModelProperty(value = "The difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC", required = true)
        public long timeStamp;
        @JsonProperty
        @ApiModelProperty(value = "The current date of the system as String", required = true)
        public String currentDateAsString;

        /**
         * Constructor.
         * 
         * @param timeStamp
         *            the current timestamp
         * @param currentDateAsString
         *            the current date as a string
         */
        public TimeHolder(long timeStamp, String currentDateAsString) {
            super();
            this.timeStamp = timeStamp;
            this.currentDateAsString = currentDateAsString;
        }
    }
}
