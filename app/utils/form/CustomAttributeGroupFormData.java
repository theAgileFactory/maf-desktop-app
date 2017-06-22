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
import framework.utils.CustomConstraints;
import framework.utils.MultiLanguagesString;
import models.framework_models.common.CustomAttributeGroup;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints;

/**
 * Form data to fill a {@link models.framework_models.common.CustomAttributeGroup}
 *
 * @author Guillaume Petit
 */
public class CustomAttributeGroupFormData {

    public Long id;

    public String objectType;

    @Constraints.Required
    public String name;

    @CustomConstraints.MultiLanguagesStringRequired
    @CustomConstraints.MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString label;

    public CustomAttributeGroupFormData() {
    }

    public CustomAttributeGroupFormData(CustomAttributeGroup group, II18nMessagesPlugin ii18nMessagesPlugin) {
        this.id = group.id;
        this.objectType = group.objectType;
        this.name = group.getName();
        this.label = MultiLanguagesString.getByKey(group.label, ii18nMessagesPlugin);
    }

    public void fill(CustomAttributeGroup group) {
        group.name = this.name;
        group.label = this.label.getKeyIfValue();
        group.objectType = this.objectType;
    }
}
