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
package utils.form;

import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.utils.FileField;
import framework.utils.FileFieldOptionalValidator;

/**
 * A portfolio entry create form data is used to manage the fields when
 * creating an portfolio entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryCreateFormData {

    /**
     * The fields of the step 1.
     */
    public interface Step1Group {
    }

    /**
     * The fields for the step 2.
     */
    public interface Step2Group {
    }

    /**
     * Default constructor.
     */
    public PortfolioEntryCreateFormData() {
    }

    /**
     * Construct with a default manager.
     * 
     * @param managerId
     *            the manager id
     */
    public PortfolioEntryCreateFormData(Long managerId) {
        this.manager = managerId;
    }

    public boolean isConfidential;

    @Required(groups = { Step1Group.class })
    @MinLength(value = 3, groups = { Step1Group.class })
    @MaxLength(value = IModelConstants.MEDIUM_STRING, groups = { Step1Group.class })
    public String name;

    @Required(groups = { Step1Group.class })
    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String description;

    @Required(groups = { Step1Group.class })
    public Long manager;

    public Long portfolio;

    @Required(groups = { Step1Group.class })
    public Long portfolioEntryType;

    @Required(groups = { Step1Group.class })
    public Long requestedLifeCycleProcess;

    @ValidateWith(value = FileFieldOptionalValidator.class, message = "form.input.file_field.error", groups = { Step1Group.class })
    public FileField scopeDescription;

    @Required(groups = { Step2Group.class })
    public Long id;

    @Override
    public String toString() {
        return "PortfolioEntryCreateFormData [isConfidential=" + isConfidential + ", name=" + name + ", description=" + description + ", portfolio="
                + portfolio + ", portfolioEntryType=" + portfolioEntryType + ", requestedLifeCycleProcess=" + requestedLifeCycleProcess
                + ", scopeDescription=" + scopeDescription + "]";
    }
}
