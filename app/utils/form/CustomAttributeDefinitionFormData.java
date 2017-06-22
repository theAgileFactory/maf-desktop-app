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
import models.framework_models.common.CustomAttributeDefinition;
import models.framework_models.common.CustomAttributeGroup;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * An custom attribute definition form data is used to manage the fields when
 * managing a custom attribute.
 * 
 * @author Johann Kohler
 */
public class CustomAttributeDefinitionFormData {

    public Long id;
    public String objectType;

    @Required
    public Long customAttributeGroup;

    public String uuid;
    public boolean canAddConditionalRule;

    public String configuration;

    @Required
    public String attributeType;

    @MultiLanguagesStringRequired
    @MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString name;

    @MultiLanguagesStringMaxLength(value = IModelConstants.VLARGE_STRING)
    public MultiLanguagesString description;

    public boolean isDisplayed;

    public String conditionalRuleFieldId;

    public String conditionalRuleFieldValue;

    /**
     * Default constructor.
     */
    public CustomAttributeDefinitionFormData() {
    }

    public CustomAttributeDefinitionFormData(String objectType) {
        this.objectType = objectType;
        this.customAttributeGroup = CustomAttributeGroup.getOrderedCustomAttributeGroupsByObjectType(objectType).get(0).id;
    }

    /**
     * Validate the dates.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        if ((conditionalRuleFieldId.equals("") && !conditionalRuleFieldValue.equals(""))
                || (!conditionalRuleFieldId.equals("") && conditionalRuleFieldValue.equals(""))) {
            errors.add(new ValidationError("conditionalRuleFieldId", Messages.get("object.custom_attribute_definition.conditional_rule.invalid")));
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param customAttribute
     *            the custom attribute definition in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public CustomAttributeDefinitionFormData(CustomAttributeDefinition customAttribute, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = customAttribute.id;
        this.objectType = customAttribute.objectType;
        if (customAttribute.customAttributeGroup != null) {
            this.customAttributeGroup = customAttribute.customAttributeGroup.id;
        }
        this.uuid = customAttribute.uuid;

        this.configuration = customAttribute.configuration != null ? new String(customAttribute.configuration) : null;
        this.attributeType = customAttribute.attributeType;
        this.name = MultiLanguagesString.getByKey(customAttribute.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(customAttribute.description, i18nMessagesPlugin);
        this.isDisplayed = customAttribute.isDisplayed;

        if (customAttribute.hasValidConditionalRule()) {
            this.conditionalRuleFieldId = customAttribute.getConditionalRuleFieldId();
            this.conditionalRuleFieldValue = customAttribute.getConditionalRuleValue();
        }

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param customAttribute
     *            the custom attribute definition in the DB
     */
    public void fill(CustomAttributeDefinition customAttribute) {

        customAttribute.customAttributeGroup = CustomAttributeGroup.getById(this.customAttributeGroup);
        customAttribute.configuration = this.configuration.getBytes();
        customAttribute.attributeType = this.attributeType;
        customAttribute.name = this.name.getKeyIfValue();
        customAttribute.description = this.description.getKeyIfValue();
        customAttribute.isDisplayed = this.isDisplayed;

        if (customAttribute.isAuthorizedAttributeTypeForConditionalRule() && !conditionalRuleFieldId.equals("") && !conditionalRuleFieldValue.equals("")) {
            customAttribute.conditionalRule = conditionalRuleFieldId + "=" + conditionalRuleFieldValue;
        } else {
            customAttribute.conditionalRule = null;
        }

    }

}
