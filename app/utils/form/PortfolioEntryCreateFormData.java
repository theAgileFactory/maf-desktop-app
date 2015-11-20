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

import framework.utils.FileField;
import framework.utils.FileFieldOptionalValidator;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;

/**
 * A portfolio entry create form data is used to manage the fields when creating
 * an portfolio entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryCreateFormData {

    /**
     * Default constructor.
     */
    public PortfolioEntryCreateFormData() {
    }

    /**
     * Construct with a default manager.
     * 
     * @param isRelease
     *            true for a release, false for an initiative
     * @param managerId
     *            the manager id
     */
    public PortfolioEntryCreateFormData(boolean isRelease, Long managerId) {
        this.isRelease = isRelease;
        this.manager = managerId;
    }

    public boolean isRelease;

    public boolean isConfidential;

    @Required
    @MinLength(value = 3)
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Required
    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String description;

    @Required
    public Long manager;

    public Long portfolio;

    @Required
    public Long portfolioEntryType;

    @Required
    public Long requestedLifeCycleProcess;

    @ValidateWith(value = FileFieldOptionalValidator.class, message = "form.input.file_field.error")
    public FileField scopeDescription;

    @Override
    public String toString() {
        return "PortfolioEntryCreateFormData [isConfidential=" + isConfidential + ", name=" + name + ", description=" + description + ", portfolio="
                + portfolio + ", portfolioEntryType=" + portfolioEntryType + ", requestedLifeCycleProcess=" + requestedLifeCycleProcess
                + ", scopeDescription=" + scopeDescription + "]";
    }
}
