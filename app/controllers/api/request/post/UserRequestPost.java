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
package controllers.api.request.post;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModelProperty;

import framework.services.account.IUserAccount;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;

/**
 * The user post request.
 * 
 * @author Marc Schaer
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequestPost {

    @JsonProperty
    @ApiModelProperty(value = "account type", allowableValues = "STANDARD,VIEWER")
    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String accountType;

    @JsonProperty
    public Boolean isActive;

    @JsonProperty
    @MaxLength(value = 2)
    @ApiModelProperty(value = "2 letters code for language")
    public String preferredLanguage;

    @JsonProperty
    @ApiModelProperty(required = true)
    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String firstName;

    @ApiModelProperty(required = true)
    @JsonProperty
    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String lastName;

    @ApiModelProperty(required = true)
    @JsonProperty
    @Required
    @MaxLength(value = IModelConstants.LARGE_STRING)
    public String uid;

    @ApiModelProperty(required = true)
    @JsonProperty
    @Required
    @MaxLength(value = IModelConstants.LARGE_STRING)
    public String mail;

    @JsonProperty
    public String password;

    @JsonProperty
    public List<Long> systemLevelRoleTypesIds;

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        try {
            IUserAccount.AccountType.valueOf(accountType);
        } catch (Exception e) {
            errors.add(new ValidationError("accountType", "The account type is incorrect"));
        }

        return errors.isEmpty() ? null : errors;

    }

}
