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
import play.data.validation.Constraints.Min;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;

/**
 * An KPI scheduler form data is used to manage the fields when managing a KPI
 * scheduler.
 * 
 * @author Johann Kohler
 */
public class KpiSchedulerFormData {

    public Long id;

    @Required
    @Pattern(value = "^([01]?[0-9]|2[0-3])h[0-5][0-9]$", message = "object.kpi_definition.scheduler_start_time.invalid")
    public String startTime;

    @Required
    @Min(value = 120)
    public Integer frequency;

    @Required
    public boolean realTime;

    /**
     * Default constructor.
     */
    public KpiSchedulerFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param kpiDefinition
     *            the KPI definition in the DB
     */
    public KpiSchedulerFormData(KpiDefinition kpiDefinition) {

        this.id = kpiDefinition.id;
        this.startTime = kpiDefinition.schedulerStartTime;
        this.frequency = kpiDefinition.schedulerFrequency;
        this.realTime = kpiDefinition.schedulerRealTime != null ? kpiDefinition.schedulerRealTime : false;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param kpiDefinition
     *            the KPI definition in the DB
     */
    public void fill(KpiDefinition kpiDefinition) {

        kpiDefinition.schedulerStartTime = this.startTime;
        kpiDefinition.schedulerFrequency = this.frequency;
        kpiDefinition.schedulerRealTime = this.realTime;

    }

}
