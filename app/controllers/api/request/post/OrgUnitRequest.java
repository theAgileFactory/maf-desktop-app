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
 * The org unit post/put request.
 * 
 * @author Johann Kohler
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgUnitRequest {

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String refId;

    @JsonProperty
    @ApiModelProperty(required = true)
    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @JsonProperty
    public boolean isActive;

    @JsonProperty
    public boolean canSponsor;

    @JsonProperty
    public boolean canDeliver;

    @JsonProperty
    public Long orgUnitTypeId;

    @JsonProperty
    public Long managerId;

    @JsonProperty
    public Long parentId;

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (orgUnitTypeId != null && OrgUnitDao.getOrgUnitTypeById(orgUnitTypeId) == null) {
            errors.add(new ValidationError("orgUnitTypeId", "The orgUnitType does not exist"));
        }

        if (managerId != null && ActorDao.getActorById(managerId) == null) {
            errors.add(new ValidationError("managerId", "The manager does not exist"));
        }

        if (parentId != null && OrgUnitDao.getOrgUnitById(parentId) == null) {
            errors.add(new ValidationError("parentId", "The parent does not exist"));
        }

        return errors.isEmpty() ? null : errors;

    }

}
