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

import models.timesheet.TimesheetActivity;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import dao.timesheet.TimesheetDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A timesheet activity form data is used to manage the fields when
 * adding/editing a timesheet activity.
 * 
 * @author Johann Kohler
 */
public class TimesheetActivityFormData {

    public Long id;

    @Required
    public Long type;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    /**
     * Default constructor.
     */
    public TimesheetActivityFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param timesheetActivity
     *            the timesheet activity in the DB
     * @param i18nMessagesPlugin 
     *            the i18n manager
     */
    public TimesheetActivityFormData(TimesheetActivity timesheetActivity, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = timesheetActivity.id;
        this.type = timesheetActivity.timesheetActivityType.id;
        this.name = MultiLanguagesString.getByKey(timesheetActivity.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(timesheetActivity.description, i18nMessagesPlugin);

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param timesheetActivity
     *            the timesheet activity in the DB
     */
    public void fill(TimesheetActivity timesheetActivity) {

        timesheetActivity.timesheetActivityType = TimesheetDao.getTimesheetActivityTypeById(this.type);
        timesheetActivity.name = this.name.getKeyIfValue();
        timesheetActivity.description = this.description.getKeyIfValue();

    }
}
