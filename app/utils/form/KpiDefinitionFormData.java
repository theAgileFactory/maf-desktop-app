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

import models.framework_models.kpi.KpiDefinition;
import play.data.validation.Constraints.Required;

/**
 * An KPI definition form data is used to manage the fields when managing a KPI
 * definition.
 * 
 * @author Johann Kohler
 */
public class KpiDefinitionFormData {

    public Long id;

    @Required
    public boolean isActive;

    public String parameters;

    /**
     * Default constructor.
     */
    public KpiDefinitionFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param kpiDefinition
     *            the KPI definition in the DB
     */
    public KpiDefinitionFormData(KpiDefinition kpiDefinition) {

        this.id = kpiDefinition.id;
        this.isActive = kpiDefinition.isActive;
        this.parameters = kpiDefinition.parameters;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param kpiDefinition
     *            the KPI definition in the DB
     */
    public void fill(KpiDefinition kpiDefinition) {

        kpiDefinition.isActive = this.isActive;
        if (kpiDefinition.parameters != null) {
            kpiDefinition.parameters = this.parameters;
        }

    }

}
