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

import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.ValidationError;

/**
 * The portfolio entry Planning Package type put/post request.
 * 
 * @author Marc Schaer
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfolioEntryPlanningPackageTypeRequest {

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    @JsonProperty
    @ApiModelProperty(required = true)
    public String name;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    @JsonProperty
    @ApiModelProperty(required = true)
    public String cssClass;

    @JsonProperty
    @ApiModelProperty(required = true)
    public Boolean isActive;

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        return errors.isEmpty() ? null : errors;

    }

}
