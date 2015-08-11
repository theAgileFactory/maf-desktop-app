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

import models.framework_models.kpi.KpiColorRule;
import play.data.validation.Constraints.Required;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.MultiLanguagesString;

/**
 * An KPI color rule form data is used to manage the fields when managing a KPI
 * color rule.
 * 
 * @author Johann Kohler
 */
public class KpiColorRuleFormData {

    public Long kpiDefinitionId;

    public Long kpiColorRuleId;

    @Required
    public String rule;

    @Required
    public String cssColor;

    public MultiLanguagesString renderLabel;

    /**
     * Default constructor.
     */
    public KpiColorRuleFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param kpiColorRule
     *            the KPI color rule in the DB
     * @param i18nMessagesPlugin 
     *            the i18n manager
     */
    public KpiColorRuleFormData(KpiColorRule kpiColorRule,II18nMessagesPlugin i18nMessagesPlugin) {

        this.kpiDefinitionId = kpiColorRule.kpiDefinition.id;
        this.kpiColorRuleId = kpiColorRule.id;
        this.rule = kpiColorRule.rule;
        this.cssColor = kpiColorRule.cssColor;
        this.renderLabel = MultiLanguagesString.getByKey(kpiColorRule.renderLabel,i18nMessagesPlugin);

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param kpiColorRule
     *            the KPI color rule in the DB
     */
    public void fill(KpiColorRule kpiColorRule) {

        kpiColorRule.rule = this.rule;
        kpiColorRule.cssColor = this.cssColor;
        kpiColorRule.renderLabel = this.renderLabel.getKeyIfValue();

    }

}
