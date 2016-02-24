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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModelProperty;

import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioEntryDao;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;

/**
 * The portfolio entry put request.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryRequestPut {

    @JsonProperty
    @ApiModelProperty(required = true)
    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @JsonProperty
    @ApiModelProperty(required = true)
    @Required
    public Long portfolioEntryTypeId;

    @JsonProperty
    @MaxLength(value = IModelConstants.SMALL_STRING)
    public String governanceId;

    @JsonProperty
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String erpRefId;

    @JsonProperty
    @Required
    @ApiModelProperty(required = true)
    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String description;

    @JsonProperty
    @Required
    @ApiModelProperty(required = true)
    public Long managerId;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    @JsonProperty
    public String refId;

    @JsonProperty
    public boolean isPublic;

    @JsonProperty
    public boolean archived;

    @JsonProperty
    public Long sponsoringUnitId;

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (portfolioEntryTypeId != null && PortfolioEntryDao.getPETypeById(portfolioEntryTypeId) == null) {
            errors.add(new ValidationError("portfolioEntryTypeId", "The portfolioEntryType does not exist"));
        }

        if (managerId != null && ActorDao.getActorById(managerId) == null) {
            errors.add(new ValidationError("managerId", "The manager does not exist"));
        }

        if (sponsoringUnitId != null && OrgUnitDao.getOrgUnitById(sponsoringUnitId) == null) {
            errors.add(new ValidationError("sponsoringUnitId", "The sponsoringUnit does not exist"));
        }

        return errors.isEmpty() ? null : errors;
    }

}
