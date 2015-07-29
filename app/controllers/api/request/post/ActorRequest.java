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

import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModelProperty;

import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;

/**
 * The actor post/put request.
 * 
 * @author Johann Kohler
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActorRequest {

    @JsonProperty
    public String employeeId;

    @ApiModelProperty(required = true)
    @JsonProperty
    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String firstName;

    @ApiModelProperty(required = true)
    @JsonProperty
    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String lastName;

    @JsonProperty
    @MaxLength(value = IModelConstants.LARGE_STRING)
    public String uid;

    @JsonProperty
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String title;

    @JsonProperty
    public boolean isActive;

    @JsonProperty
    @Email
    @MaxLength(value = IModelConstants.LARGE_STRING)
    public String mail;

    @JsonProperty
    @MaxLength(value = IModelConstants.PHONE_NUMBER)
    public String mobilePhone;

    @JsonProperty
    @MaxLength(value = IModelConstants.PHONE_NUMBER)
    public String fixPhone;

    @JsonProperty
    public Long actorTypeId;

    @JsonProperty
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String erpRefId;

    @JsonProperty
    public Long orgUnitId;

    @JsonProperty
    public Long managerId;

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (actorTypeId != null && ActorDao.getActorTypeById(actorTypeId) == null) {
            errors.add(new ValidationError("actorTypeId", "The actorType does not exist"));
        }

        if (orgUnitId != null && OrgUnitDao.getOrgUnitById(orgUnitId) == null) {
            errors.add(new ValidationError("orgUnitById", "The orgUnitBy does not exist"));
        }

        if (managerId != null && ActorDao.getActorById(managerId) == null) {
            errors.add(new ValidationError("managerId", "The manager does not exist"));
        }

        return errors.isEmpty() ? null : errors;

    }

}
