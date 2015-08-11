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

import models.delivery.RequirementSeverity;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A requirement severity form data is used to manage the fields when
 * adding/editing a requirement severity.
 * 
 * @author Johann Kohler
 */
public class RequirementSeverityFormData {

    public Long id;

    public boolean isBlocker;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    /**
     * Default constructor.
     */
    public RequirementSeverityFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param requirementSeverity
     *            the requirement severity in the DB
     * @param i18nMessagesPlugin 
     *            the i18n manager
     */
    public RequirementSeverityFormData(RequirementSeverity requirementSeverity, II18nMessagesPlugin i18nMessagesPlugin) {
        this.id = requirementSeverity.id;
        this.isBlocker = requirementSeverity.isBlocker;
        this.name = MultiLanguagesString.getByKey(requirementSeverity.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(requirementSeverity.description, i18nMessagesPlugin);
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param requirementSeverity
     *            the requirement severity in the DB
     */
    public void fill(RequirementSeverity requirementSeverity) {
        requirementSeverity.isBlocker = this.isBlocker;
        requirementSeverity.name = this.name.getKeyIfValue();
        requirementSeverity.description = this.description.getKeyIfValue();
    }
}
