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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.PathParam;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.api.ApiController;
import controllers.api.request.UserListRequest;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import play.mvc.Result;

/**
 * The API controller for a User.
 * 
 * @author Marc Schaer
 * 
 */
@Api(value = "/api/core/user", description = "Operations on Users")
public class UserApiController extends ApiController {

    @Inject
    IAccountManagerPlugin accountManager;

    /**
     * Get the users list.
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list all the Users", notes = "Return the list of all Users in the system", response = UserListRequest.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getUsersList() {
        try {
            List<IUserAccount> userAccounts = accountManager.getUserAccountsFromName("*");
            if (userAccounts.isEmpty()) {
                return getJsonErrorResponse(new ApiError(404, "There is no users."));
            }
            List<UserListRequest> userListRequest = new ArrayList<UserListRequest>();
            for (IUserAccount userAccount : userAccounts) {
                userListRequest.add(new UserListRequest(userAccount));
            }
            return getJsonSuccessResponse(userListRequest);
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the users list for a given name.
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Users for a given name", notes = "Return the list of Users for a given name in the system", response = UserListRequest.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getUsersFromName(@ApiParam(value = "User name", required = true) @PathParam("name") String name) {
        try {
            List<IUserAccount> userAccounts = accountManager.getUserAccountsFromName(name);
            if (userAccounts.isEmpty()) {
                return getJsonErrorResponse(new ApiError(404, "There is no user with the given name."));
            }
            List<UserListRequest> userListRequest = new ArrayList<UserListRequest>();
            for (IUserAccount userAccount : userAccounts) {
                userListRequest.add(new UserListRequest(userAccount));
            }
            return getJsonSuccessResponse(userListRequest);
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the user for a given mail.
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get a user for a given mail", notes = "Return a user for a given mail in the system", response = UserListRequest.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getUsersFromMail(@ApiParam(value = "User mail", required = true) @PathParam("mail") String mail) {
        try {
            IUserAccount userAccount = accountManager.getUserAccountFromEmail(mail);
            if (userAccount == null) {
                return getJsonErrorResponse(new ApiError(404, "There is no user with the given mail."));
            }
            UserListRequest userListRequest = new UserListRequest(userAccount);
            return getJsonSuccessResponse(userListRequest);
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the user for a given mafId.
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get a user for a given mafId", notes = "Return a user for a given mafId in the system", response = UserListRequest.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getUsersFromMafId(@ApiParam(value = "User mafId", required = true) @PathParam("maf_id") Long mafId) {
        try {
            IUserAccount userAccount = accountManager.getUserAccountFromMafUid(mafId);
            if (userAccount == null) {
                return getJsonErrorResponse(new ApiError(404, "There is no user with the given mafId."));
            }
            UserListRequest userListRequest = new UserListRequest(userAccount);
            return getJsonSuccessResponse(userListRequest);
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Get the user for a given uid.
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get a user for a given uid", notes = "Return a user for a given uid in the system", response = UserListRequest.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getUsersFromUid(@ApiParam(value = "User mafId", required = true) @PathParam("uid") String uid) {
        try {
            IUserAccount userAccount = accountManager.getUserAccountFromUid(uid);
            if (userAccount == null) {
                return getJsonErrorResponse(new ApiError(404, "There is no user with the given uid."));
            }
            UserListRequest userListRequest = new UserListRequest(userAccount);
            return getJsonSuccessResponse(userListRequest);
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

}
