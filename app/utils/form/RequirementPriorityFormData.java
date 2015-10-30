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

import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.CustomConstraints.MultiLanguagesStringMaxLength;
import framework.utils.CustomConstraints.MultiLanguagesStringRequired;
import framework.utils.MultiLanguagesString;
import models.delivery.RequirementPriority;
import models.framework_models.parent.IModelConstants;

/**
 * A requirement priority form data is used to manage the fields when
 * adding/editing a requirement priority.
 * 
 * @author Johann Kohler
 */
public class RequirementPriorityFormData {

    public Long id;

    public boolean isMust;

    @MultiLanguagesStringRequired
    @MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString name;

    @MultiLanguagesStringMaxLength(value = IModelConstants.VLARGE_STRING)
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
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public RequirementPriorityFormData(RequirementPriority requirementPriority, II18nMessagesPlugin i18nMessagesPlugin) {
        this.id = requirementPriority.id;
        this.isMust = requirementPriority.isMust;
        this.name = MultiLanguagesString.getByKey(requirementPriority.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(requirementPriority.description, i18nMessagesPlugin);
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
