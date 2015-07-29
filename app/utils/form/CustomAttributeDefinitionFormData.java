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

import models.framework_models.common.CustomAttributeDefinition;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * An custom attribute definition form data is used to manage the fields when
 * managing a custom attribute.
 * 
 * @author Johann Kohler
 */
public class CustomAttributeDefinitionFormData {

    public Long id;
    public String objectType;
    public String uuid;

    public String configuration;

    @Required
    public String attributeType;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    public boolean isDisplayed;

    /**
     * Default constructor.
     */
    public CustomAttributeDefinitionFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param customAttribute
     *            the custom attribute definition in the DB
     */
    public CustomAttributeDefinitionFormData(CustomAttributeDefinition customAttribute) {

        this.id = customAttribute.id;
        this.objectType = customAttribute.objectType;
        this.uuid = customAttribute.uuid;

        this.configuration = customAttribute.configuration != null ? new String(customAttribute.configuration) : null;
        this.attributeType = customAttribute.attributeType;
        this.name = MultiLanguagesString.getByKey(customAttribute.name);
        this.description = MultiLanguagesString.getByKey(customAttribute.description);
        this.isDisplayed = customAttribute.isDisplayed;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param customAttribute
     *            the custom attribute definition in the DB
     */
    public void fill(CustomAttributeDefinition customAttribute) {

        customAttribute.configuration = this.configuration.getBytes();
        customAttribute.attributeType = this.attributeType;
        customAttribute.name = this.name.getKeyIfValue();
        customAttribute.description = this.description.getKeyIfValue();
        customAttribute.isDisplayed = this.isDisplayed;

    }

}
