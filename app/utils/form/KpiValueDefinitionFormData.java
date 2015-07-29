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

import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;
import models.framework_models.kpi.KpiValueDefinition;
import models.framework_models.kpi.KpiValueDefinition.RenderType;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;

/**
 * An KPI value definition form data is used to manage the fields when managing
 * a KPI value definition.
 * 
 * @author Johann Kohler
 */
public class KpiValueDefinitionFormData {

    public Long id;
    public String valueType;

    @Required(groups = { CustomGroup.class })
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    @Required(groups = { StandardGroup.class, CustomGroup.class })
    public String renderType;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String renderPattern;

    /**
     * Default constructor.
     */
    public KpiValueDefinitionFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param kpiValueDefinition
     *            the KPI value definition in the DB
     */
    public KpiValueDefinitionFormData(KpiValueDefinition kpiValueDefinition) {

        this.id = kpiValueDefinition.id;
        this.name = MultiLanguagesString.getByKey(kpiValueDefinition.name);
        this.renderType = kpiValueDefinition.renderType.name();
        this.renderPattern = kpiValueDefinition.renderPattern;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param kpiValueDefinition
     *            the KPI value definition in the DB
     * @param updateName
     *            true if the name should be also updated
     */
    public void fill(KpiValueDefinition kpiValueDefinition, boolean updateName) {

        RenderType renderType = RenderType.valueOf(this.renderType);

        kpiValueDefinition.renderType = renderType;
        if (renderType.equals(RenderType.PATTERN)) {
            kpiValueDefinition.renderPattern = this.renderPattern;
        } else {
            kpiValueDefinition.renderPattern = null;
        }

        if (updateName) {
            kpiValueDefinition.name = this.name.getKeyIfValue();
        }

    }

    /**
     * The group for standard KPI.
     * 
     * @author Johann Kohler
     */
    public interface StandardGroup {
    }

    /**
     * The group for custom KPI.
     * 
     * @author Johann Kohler
     */
    public interface CustomGroup {
    }

}
