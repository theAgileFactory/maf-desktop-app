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
package controllers.api.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import framework.services.account.AccountManagementException;
import framework.services.account.IUserAccount;
import framework.services.account.IUserAccount.AccountType;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.ValidationError;

/**
 * The user list request.
 * 
 * @author Marc Schaer
 */
public class UserListRequest {

    @JsonProperty
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public AccountType accountType;

    @JsonProperty
    public Boolean isActive;

    @JsonProperty
    public Boolean isDisplayed;

    @JsonProperty
    public String firstName;

    @JsonProperty
    public String lastName;

    @JsonProperty
    public Long mafUid;

    @JsonProperty
    @MaxLength(value = IModelConstants.LARGE_STRING)
    public String uid;

    @JsonProperty
    public String mail;

    @JsonProperty
    public String preferredLanguage;

    @JsonProperty
    public Boolean markedForDeletion;

    @JsonProperty
    public List<? extends Role> roles = Collections.synchronizedList(new ArrayList<Role>());

    @JsonProperty
    public List<? extends Role> selectableRoles = Collections.synchronizedList(new ArrayList<Role>());

    @JsonProperty
    public List<String> systemLevelRoleTypes = Collections.synchronizedList(new ArrayList<String>());

    @JsonProperty
    public List<? extends Permission> permissions = Collections.synchronizedList(new ArrayList<Permission>());

    /**
     * Constructor with arguments.
     * 
     * @param accountType
     * @param isActive
     * @param isDisplayed
     * @param firstName
     * @param lastName
     * @param mafUid
     * @param uid
     * @param mail
     * @param preferredLanguage
     * @param markedForDeletion
     * @param roles
     * @param selectableRoles
     * @param systemLevelRoleTypes
     * @param permissions
     */
    public UserListRequest(AccountType accountType, Boolean isActive, Boolean isDisplayed, String firstName, String lastName, Long mafUid, String uid,
            String mail, String preferredLanguage, Boolean markedForDeletion, List<Role> roles, List<Role> selectableRoles, List<String> systemLevelRoleTypes,
            List<Permission> permissions) {
        this.accountType = accountType;
        this.isActive = isActive;
        this.isDisplayed = isDisplayed;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mafUid = mafUid;
        this.uid = uid;
        this.mail = mail;
        this.preferredLanguage = preferredLanguage;
        this.markedForDeletion = markedForDeletion;
        this.roles = roles;
        this.selectableRoles = selectableRoles;
        this.systemLevelRoleTypes = systemLevelRoleTypes;
        this.permissions = permissions;
    }

    public UserListRequest(IUserAccount account) throws AccountManagementException {
        this.accountType = account.getAccountType();
        this.isActive = account.isActive();
        this.isDisplayed = account.isDisplayed();
        this.firstName = account.getFirstName();
        this.lastName = account.getLastName();
        this.mafUid = account.getMafUid();
        this.uid = account.getIdentifier();
        this.mail = account.getMail();
        this.preferredLanguage = account.getPreferredLanguage();
        this.markedForDeletion = account.isMarkedForDeletion();
        this.roles = account.getRoles();
        this.selectableRoles = account.getSelectableRoles();
        this.systemLevelRoleTypes = account.getSystemLevelRoleTypeNames();
        this.permissions = account.getPermissions();
    }

    /**
     * Default constructor.
     */
    public UserListRequest() {
    }

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        // TODO
        /*
         * if (managerId != null && ActorDao.getActorById(managerId) == null) {
         * errors.add(new ValidationError("managerId",
         * "The manager does not exist")); }
         */

        return errors.isEmpty() ? null : errors;

    }
}
