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

import models.delivery.RequirementStatus;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A requirement status form data is used to manage the fields when
 * adding/editing a requirement status.
 * 
 * @author Johann Kohler
 */
public class RequirementStatusFormData {

    public Long id;

    @Required
    public String type;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    /**
     * Default constructor.
     */
    public RequirementStatusFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param requirementStatus
     *            the requirement status in the DB
     */
    public RequirementStatusFormData(RequirementStatus requirementStatus) {
        this.id = requirementStatus.id;
        this.type = requirementStatus.type.name();
        this.name = MultiLanguagesString.getByKey(requirementStatus.name);
        this.description = MultiLanguagesString.getByKey(requirementStatus.description);
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param requirementStatus
     *            the requirement status in the DB
     */
    public void fill(RequirementStatus requirementStatus) {
        requirementStatus.type = RequirementStatus.Type.valueOf(this.type);
        requirementStatus.name = this.name.getKeyIfValue();
        requirementStatus.description = this.description.getKeyIfValue();
    }
}
