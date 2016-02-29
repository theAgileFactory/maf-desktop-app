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
import controllers.api.request.UserListRequest;
import controllers.api.request.post.UserRequestPost;
import controllers.api.request.post.UserRequestPut;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.api.ApiError;
import framework.services.api.server.ApiAuthentication;
import framework.services.configuration.II18nMessagesPlugin;
import models.framework_models.account.SystemLevelRoleType;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
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

    @Inject
    II18nMessagesPlugin messages;

    public static Form<UserRequestPost> userRequestPostFormTemplate = Form.form(UserRequestPost.class);
    public static Form<UserRequestPut> userRequestPutFormTemplate = Form.form(UserRequestPut.class);

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
     * @param name
     *            the user name
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "list the Users for a given name", notes = "Return the list of Users for a given name in the system", response = UserListRequest.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
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
     * @param mail
     *            the user mail
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get a user for a given mail", notes = "Return a user for a given mail in the system", response = UserListRequest.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
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
     * @param mafId
     *            the user maf id
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get a user for a given mafId", notes = "Return a user for a given mafId in the system", response = UserListRequest.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
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
     * @param uid
     *            the user uid
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get a user for a given uid", notes = "Return a user for a given uid in the system", response = UserListRequest.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getUsersFromUid(@ApiParam(value = "User Id", required = true) @PathParam("uid") String uid) {
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

    /**
     * Get the user roles for a given uid.
     * 
     * @param uid
     *            the user uid
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Get user's roles for a given uid", notes = "Return a list of roles for a given uid in the system", response = SystemLevelRoleType.class, httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    public Result getUsersRoles(@ApiParam(value = "User Id", required = true) @PathParam("uid") String uid) {
        try {
            IUserAccount userAccount = accountManager.getUserAccountFromUid(uid);
            if (userAccount == null) {
                return getJsonErrorResponse(new ApiError(404, "There is no user with the given uid."));
            }
            List<String> rolesNames = userAccount.getSystemLevelRoleTypeNames();
            List<SystemLevelRoleType> roles = new ArrayList<SystemLevelRoleType>();
            for (String name : rolesNames) {
                roles.add(SystemLevelRoleType.getActiveRoleFromName(name));
            }
            return getJsonSuccessResponse(roles);
        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "INTERNAL SERVER ERROR", e));
        }
    }

    /**
     * Add a role to a given user
     * 
     * @param uid
     *            the user id
     * @param roleId
     *            The role id
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Add a role to a user", notes = "Add a role to a user", response = UserListRequest.class, httpMethod = "PUT")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result addRole(@ApiParam(value = "A user id", required = true) @PathParam("uid") String uid,
            @ApiParam(value = "A role id", required = true) @PathParam("role_id") Long roleId) {
        try {

            // check if purchase order exist
            IUserAccount userAccount = accountManager.getUserAccountFromUid(uid);
            if (userAccount == null) {
                return getJsonErrorResponse(new ApiError(404, "The user with the specified user id is not found"));
            }

            if (SystemLevelRoleType.getActiveRoleFromId(roleId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The role with the specified id is not found"));
            }

            accountManager.addSystemLevelRoleType(uid, SystemLevelRoleType.getActiveRoleFromId(roleId).name);

            UserListRequest userRequest = new UserListRequest(userAccount);

            // json success
            return getJsonSuccessResponse(userRequest);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Remove a role from a given user
     * 
     * @param uid
     *            the user id
     * @param roleId
     *            The role id
     * 
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Remove a role from a user", notes = "Remove a role from a user", response = UserListRequest.class, httpMethod = "PUT")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result removeRole(@ApiParam(value = "A user id", required = true) @PathParam("uid") String uid,
            @ApiParam(value = "A role id", required = true) @PathParam("role_id") Long roleId) {
        try {

            // check if purchase order exist
            IUserAccount userAccount = accountManager.getUserAccountFromUid(uid);
            if (userAccount == null) {
                return getJsonErrorResponse(new ApiError(404, "The user with the specified user id is not found"));
            }

            if (SystemLevelRoleType.getActiveRoleFromId(roleId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The role with the specified id is not found"));
            }

            accountManager.removeSystemLevelRoleType(uid, SystemLevelRoleType.getActiveRoleFromId(roleId).name);

            UserListRequest userRequest = new UserListRequest(accountManager.getUserAccountFromUid(uid));

            // json success
            return getJsonSuccessResponse(userRequest);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

    /**
     * Create a User.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Create a User", notes = "Create a User", response = UserRequestPost.class, httpMethod = "POST")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "A User", required = true, dataType = "UserRequestPost", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 404, message = "not found", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result createUser() {
        try {
            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<UserRequestPost> userRequestForm = userRequestPostFormTemplate.bind(json);

            // if errors
            if (userRequestForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = userRequestForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            UserRequestPost userRequestPost = userRequestForm.get();

            // get SystemLevelRoleTypesNames
            List<String> roleTypeNames = new ArrayList<String>();
            if (userRequestPost.systemLevelRoleTypesIds != null) {
                for (Long roleId : userRequestPost.systemLevelRoleTypesIds) {
                    SystemLevelRoleType roleType = SystemLevelRoleType.getActiveRoleFromId(roleId);
                    if (roleType == null) {
                        return getJsonErrorResponse(new ApiError(404, "One of the role type with the specified ids is not found"));
                    } else {
                        roleTypeNames.add(roleType.name);
                    }
                }
            } else {
                return getJsonErrorResponse(new ApiError(404, "You should have at least one role type"));
            }

            // fill to match with DB
            accountManager.createNewUserAccount(userRequestPost.uid, IUserAccount.AccountType.valueOf(userRequestPost.accountType), userRequestPost.firstName,
                    userRequestPost.lastName, userRequestPost.mail, roleTypeNames);

            if (userRequestPost.preferredLanguage != null && userRequestPost.preferredLanguage.length() > 0) {
                if (messages.getValidLanguageMap().get(userRequestPost.preferredLanguage) == null) {
                    return getJsonErrorResponse(new ApiError(500, "The only available language are : " + messages.getValidLanguageMap().keySet().toString()));
                }
                accountManager.updatePreferredLanguage(userRequestPost.uid, userRequestPost.preferredLanguage);
            }
            if (userRequestPost.password != null && userRequestPost.password.length() > 0) {
                accountManager.updatePassword(userRequestPost.uid, userRequestPost.password);
            }

            UserListRequest userListRequest = new UserListRequest(accountManager.getUserAccountFromUid(userRequestPost.uid));
            // json success
            return getJsonSuccessResponse(userListRequest);
        } catch (

        Exception e)

        {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }

    }

    /**
     * Update a User.
     * 
     * * WARNING: If an optional attribute is not given, then its current value
     * will be settled to null.
     * 
     * @param id
     *            the user id
     * 
     * @return the JSON object of the corresponding user.
     */
    @ApiAuthentication(additionalCheck = ApiAuthenticationBizdockCheck.class)
    @ApiOperation(value = "Update a User", notes = "Update a User", response = UserRequestPut.class, httpMethod = "PUT")
    @ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "A User", required = true, dataType = "UserRequestPut", paramType = "body") })
    @ApiResponses(value = { @ApiResponse(code = 201, message = "success"), @ApiResponse(code = 400, message = "bad request", response = ApiError.class),
            @ApiResponse(code = 500, message = "error", response = ApiError.class) })
    @BodyParser.Of(BodyParser.Raw.class)
    public Result updateUser(@ApiParam(value = "A user maf id", required = true) @PathParam("maf_id") Long mafId) {
        try {

            if (accountManager.getUserAccountFromMafUid(mafId) == null) {
                return getJsonErrorResponse(new ApiError(404, "The user with the specified maf id is not found"));
            }

            // Json to object
            JsonNode json = getRequestBodyAsJsonNode(request());

            // fill the play form
            Form<UserRequestPut> userRequestPutForm = userRequestPutFormTemplate.bind(json);

            // if errors
            if (userRequestPutForm.hasErrors()) {
                // get errors
                Map<String, List<ValidationError>> allErrors = userRequestPutForm.errors();
                // get errors to String Format
                String errorMsg = ApiError.getValidationErrorsMessage(getMessagesPlugin(), allErrors);
                return getJsonErrorResponse(new ApiError(400, errorMsg));
            }

            // Validation Form
            UserRequestPut userRequestPut = userRequestPutForm.get();

            // get SystemLevelRoleTypesNames
            List<String> roleTypeNames = new ArrayList<String>();
            if (userRequestPut.systemLevelRoleTypesIds != null) {
                for (Long roleIds : userRequestPut.systemLevelRoleTypesIds) {
                    SystemLevelRoleType roleType = SystemLevelRoleType.getActiveRoleFromId(roleIds);
                    if (roleType == null) {
                        return getJsonErrorResponse(new ApiError(404, "One of the role type with the specified ids is not found"));
                    } else {
                        roleTypeNames.add(roleType.name);
                    }
                }
            }

            UserListRequest userListRequest = new UserListRequest(accountManager.getUserAccountFromMafUid(mafId));

            if (userRequestPut.firstName.length() == 0) {
                userRequestPut.firstName = userListRequest.firstName;
            }
            if (userRequestPut.lastName.length() == 0) {
                userRequestPut.lastName = userListRequest.lastName;
            }
            if (userRequestPut.mail.length() == 0) {
                userRequestPut.mail = userListRequest.mail;
            }
            if (userRequestPut.accountType.length() == 0) {
                userRequestPut.accountType = IUserAccount.AccountType.valueOf(userRequestPut.accountType).name();
            }
            if (userRequestPut.preferredLanguage.length() == 0) {
                userRequestPut.preferredLanguage = userListRequest.preferredLanguage;
            } else {
                if (messages.getValidLanguageMap().get(userRequestPut.preferredLanguage) == null) {
                    return getJsonErrorResponse(new ApiError(500, "The only available language are : " + messages.getValidLanguageMap().keySet().toString()));
                }
            }

            // fill to match with DB
            accountManager.updateBasicUserData(userListRequest.uid, userRequestPut.firstName, userRequestPut.lastName);
            accountManager.updateMail(userListRequest.uid, userRequestPut.mail);
            accountManager.updateUserAccountType(userListRequest.uid, IUserAccount.AccountType.valueOf(userRequestPut.accountType));
            accountManager.overwriteSystemLevelRoleTypes(userListRequest.uid, roleTypeNames);
            accountManager.updateActivationStatus(userListRequest.uid, userRequestPut.isActive);
            accountManager.updatePreferredLanguage(userListRequest.uid, userRequestPut.preferredLanguage);
            if (userRequestPut.password.length() > 0) {
                accountManager.updatePassword(userListRequest.uid, userRequestPut.password);
            }

            UserListRequest userListRequestResult = new UserListRequest(accountManager.getUserAccountFromUid(userListRequest.uid));
            // json success
            return getJsonSuccessResponse(userListRequestResult);

        } catch (Exception e) {
            return getJsonErrorResponse(new ApiError(500, "Unexpected error", e));
        }
    }

}
