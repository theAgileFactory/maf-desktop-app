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

import models.delivery.RequirementPriority;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A requirement priority form data is used to manage the fields when
 * adding/editing a requirement priority.
 * 
 * @author Johann Kohler
 */
public class RequirementPriorityFormData {

    public Long id;

    public boolean isMust;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    /**
     * Default constructor.
     */
    public RequirementPriorityFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param requirementPriority
     *            the requirement priority in the DB
     */
    public RequirementPriorityFormData(RequirementPriority requirementPriority) {
        this.id = requirementPriority.id;
        this.isMust = requirementPriority.isMust;
        this.name = MultiLanguagesString.getByKey(requirementPriority.name);
        this.description = MultiLanguagesString.getByKey(requirementPriority.description);
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param requirementPriority
     *            the requirement priority in the DB
     */
    public void fill(RequirementPriority requirementPriority) {
        requirementPriority.isMust = this.isMust;
        requirementPriority.name = this.name.getKeyIfValue();
        requirementPriority.description = this.description.getKeyIfValue();
    }
}
