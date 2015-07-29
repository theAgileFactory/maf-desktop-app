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

import models.pmo.PortfolioEntryPlanningPackageGroup;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A portfolio entry planning package group form data is used to manage the
 * fields when managing a package group.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPlanningPackageGroupFormData {

    public Long id;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    public boolean isActive;

    /**
     * Default constructor.
     */
    public PortfolioEntryPlanningPackageGroupFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param packageGroup
     *            the package group in the DB
     */
    public PortfolioEntryPlanningPackageGroupFormData(PortfolioEntryPlanningPackageGroup packageGroup) {

        this.id = packageGroup.id;
        this.name = MultiLanguagesString.getByKey(packageGroup.name);
        this.description = MultiLanguagesString.getByKey(packageGroup.description);
        this.isActive = packageGroup.isActive;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param packageGroup
     *            the package group in the DB
     */
    public void fill(PortfolioEntryPlanningPackageGroup packageGroup) {

        packageGroup.isActive = this.isActive;
        packageGroup.name = this.name.getKeyIfValue();
        packageGroup.description = this.description.getKeyIfValue();

    }

}
