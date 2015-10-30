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
import models.framework_models.common.CustomAttributeItemOption;
import models.framework_models.common.CustomAttributeMultiItemOption;
import models.framework_models.parent.IModelConstants;

/**
 * An custom attribute item form data is used to manage the fields when managing
 * an item of a "select" custom attribute.
 * 
 * @author Johann Kohler
 */
public class CustomAttributeItemFormData {

    public Long id;
    public Long customAttributeDefinitionId;

    @MultiLanguagesStringRequired
    @MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString name;

    @MultiLanguagesStringMaxLength(value = IModelConstants.VLARGE_STRING)
    public MultiLanguagesString description;

    /**
     * Default constructor.
     */
    public CustomAttributeItemFormData() {
    }

    /**
     * Construct the form data with a DB entry (single case).
     * 
     * @param item
     *            the custom attribute item in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public CustomAttributeItemFormData(CustomAttributeItemOption item, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = item.id;
        this.customAttributeDefinitionId = item.customAttributeDefinition.id;

        this.name = MultiLanguagesString.getByKey(item.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(item.description, i18nMessagesPlugin);

    }

    /**
     * Construct the form data with a DB entry (multi case).
     * 
     * @param item
     *            the custom attribute item in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public CustomAttributeItemFormData(CustomAttributeMultiItemOption item, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = item.id;
        this.customAttributeDefinitionId = item.customAttributeDefinition.id;

        this.name = MultiLanguagesString.getByKey(item.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(item.description, i18nMessagesPlugin);

    }

    /**
     * Fill the DB entry with the form values (single case).
     * 
     * @param item
     *            the custom attribute item in the DB
     */
    public void fill(CustomAttributeItemOption item) {
        item.name = this.name.getKeyIfValue();
        item.description = this.description.getKeyIfValue();
    }

    /**
     * Fill the DB entry with the form values (multi case).
     * 
     * @param item
     *            the custom attribute item in the DB
     */
    public void fill(CustomAttributeMultiItemOption item) {
        item.name = this.name.getKeyIfValue();
        item.description = this.description.getKeyIfValue();
    }

}
