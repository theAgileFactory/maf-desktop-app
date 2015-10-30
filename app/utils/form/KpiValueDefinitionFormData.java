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
import models.framework_models.kpi.KpiValueDefinition;
import models.framework_models.kpi.KpiValueDefinition.RenderType;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

/**
 * An KPI value definition form data is used to manage the fields when managing
 * a KPI value definition.
 * 
 * @author Johann Kohler
 */
public class KpiValueDefinitionFormData {

    public Long id;
    public String valueType;

    @MultiLanguagesStringRequired(groups = { CustomGroup.class })
    @MultiLanguagesStringMaxLength(value = IModelConstants.VLARGE_STRING, groups = { CustomGroup.class })
    public MultiLanguagesString name;

    @Required(groups = { StandardGroup.class, CustomGroup.class })
    public boolean isTrendDisplayed;

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
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public KpiValueDefinitionFormData(KpiValueDefinition kpiValueDefinition, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = kpiValueDefinition.id;
        this.name = MultiLanguagesString.getByKey(kpiValueDefinition.name, i18nMessagesPlugin);
        this.renderType = kpiValueDefinition.renderType.name();
        this.renderPattern = kpiValueDefinition.renderPattern;
        this.isTrendDisplayed = kpiValueDefinition.isTrendDisplayed;

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

        kpiValueDefinition.isTrendDisplayed = this.isTrendDisplayed;

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
