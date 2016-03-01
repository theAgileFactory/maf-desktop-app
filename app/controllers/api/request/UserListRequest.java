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

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;

import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
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

    @Inject
    IAccountManagerPlugin accountManager;

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
    public List<String> systemLevelRoleTypes = Collections.synchronizedList(new ArrayList<String>());

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
     * @param systemLevelRoleTypes
     */
    public UserListRequest(AccountType accountType, Boolean isActive, Boolean isDisplayed, String firstName, String lastName, Long mafUid, String uid,
            String mail, String preferredLanguage, Boolean markedForDeletion, List<String> systemLevelRoleTypes) {
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
        this.systemLevelRoleTypes = systemLevelRoleTypes;
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
        this.systemLevelRoleTypes = account.getSystemLevelRoleTypeNames();
    }

    /**
     * Default constructor.
     */
    public UserListRequest() {
    }

    /**
     * Form validator.
     * 
     * @throws AccountManagementException
     */
    public List<ValidationError> validate() throws AccountManagementException {
        List<ValidationError> errors = new ArrayList<>();

        if (mafUid != null && accountManager.getUserAccountFromMafUid(mafUid) == null) {
            errors.add(new ValidationError("mafid", "The mafUid does not exist"));
        }
        if (uid != null && accountManager.getUserAccountFromUid(uid) == null) {
            errors.add(new ValidationError("uid", "The uid does not exist"));
        }
        if (mail != null && accountManager.getUserAccountFromEmail(mail) == null) {
            errors.add(new ValidationError("mail", "The mail does not exist"));
        }

        return errors.isEmpty() ? null : errors;

    }
}
