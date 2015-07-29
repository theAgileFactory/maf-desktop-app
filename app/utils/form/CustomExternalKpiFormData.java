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
import models.framework_models.kpi.KpiDefinition;
import models.framework_models.kpi.KpiValueDefinition;
import models.framework_models.kpi.KpiValueDefinition.RenderType;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;

/**
 * Form to create a custom and external KPI.
 * 
 * @author Johann Kohler
 */
public class CustomExternalKpiFormData {

    public String objectType;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString mainName;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString additional1Name;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString additional2Name;

    @Required
    public String cssGlyphicon;

    /**
     * Default constructor.
     */
    public CustomExternalKpiFormData() {
    }

    /**
     * Construct and return the corresponding KPI definition with value.
     */
    public KpiDefinition constructKpiDefinition() {

        // Compute the order
        Integer order = KpiDefinition.getLastOrder(this.objectType) + 10000;

        KpiValueDefinition mainValueDefinition = new KpiValueDefinition();
        mainValueDefinition.computationJsCode = null;
        mainValueDefinition.name = this.mainName.getKeyIfValue();
        mainValueDefinition.renderPattern = null;
        mainValueDefinition.renderType = RenderType.VALUE;

        KpiValueDefinition additional1ValueDefinition = new KpiValueDefinition();
        additional1ValueDefinition.computationJsCode = null;
        additional1ValueDefinition.name = this.additional1Name.getKeyIfValue();
        additional1ValueDefinition.renderPattern = null;
        additional1ValueDefinition.renderType = RenderType.VALUE;

        KpiValueDefinition additional2ValueDefinition = new KpiValueDefinition();
        additional2ValueDefinition.computationJsCode = null;
        additional2ValueDefinition.name = this.additional2Name.getKeyIfValue();
        additional2ValueDefinition.renderPattern = null;
        additional2ValueDefinition.renderType = RenderType.VALUE;

        KpiDefinition kpiDefinition = new KpiDefinition();
        kpiDefinition.uid = "KPI_CUSTOM_" + this.objectType.replaceAll("[^a-zA-Z0-9]", "") + "_" + order;
        kpiDefinition.cssGlyphicon = this.cssGlyphicon;
        kpiDefinition.isActive = true;
        kpiDefinition.isDisplayed = true;
        kpiDefinition.order = order;
        kpiDefinition.objectType = this.objectType;
        kpiDefinition.isExternal = true;
        kpiDefinition.isStandard = false;
        kpiDefinition.clazz = null;
        kpiDefinition.schedulerFrequency = null;
        kpiDefinition.schedulerRealTime = null;
        kpiDefinition.schedulerStartTime = null;
        kpiDefinition.parameters = null;
        kpiDefinition.mainKpiValueDefinition = mainValueDefinition;
        kpiDefinition.additional1KpiValueDefinition = additional1ValueDefinition;
        kpiDefinition.additional2KpiValueDefinition = additional2ValueDefinition;

        return kpiDefinition;

    }
}
