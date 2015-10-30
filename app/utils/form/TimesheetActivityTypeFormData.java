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
import models.framework_models.parent.IModelConstants;
import models.timesheet.TimesheetActivityType;

/**
 * A timesheet activity type form data is used to manage the fields when
 * adding/editing a timesheet activity type.
 * 
 * @author Johann Kohler
 */
public class TimesheetActivityTypeFormData {

    public Long id;

    @MultiLanguagesStringRequired
    @MultiLanguagesStringMaxLength(value = IModelConstants.MEDIUM_STRING)
    public MultiLanguagesString name;

    @MultiLanguagesStringMaxLength(value = IModelConstants.VLARGE_STRING)
    public MultiLanguagesString description;

    /**
     * Default constructor.
     */
    public TimesheetActivityTypeFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param timesheetActivityType
     *            the timesheet activity type in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public TimesheetActivityTypeFormData(TimesheetActivityType timesheetActivityType, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = timesheetActivityType.id;
        this.name = MultiLanguagesString.getByKey(timesheetActivityType.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(timesheetActivityType.description, i18nMessagesPlugin);

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param timesheetActivityType
     *            the timesheet activity type in the DB
     */
    public void fill(TimesheetActivityType timesheetActivityType) {

        timesheetActivityType.name = this.name.getKeyIfValue();
        timesheetActivityType.description = this.description.getKeyIfValue();

    }
}
