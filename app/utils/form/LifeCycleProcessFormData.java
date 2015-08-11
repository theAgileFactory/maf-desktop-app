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

import models.governance.LifeCycleProcess;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A life cycle process form data is used to manage the fields when managing a
 * life cycle process.
 * 
 * @author Johann Kohler
 */
public class LifeCycleProcessFormData {

    public Long id;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString shortName;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    public boolean isActive;

    /**
     * Default constructor.
     */
    public LifeCycleProcessFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param lifeCycleProcess
     *            the life cycle process in the DB
     * @param i18nMessagesPlugin 
     *            the i18n manager
     */
    public LifeCycleProcessFormData(LifeCycleProcess lifeCycleProcess, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = lifeCycleProcess.id;
        this.shortName = MultiLanguagesString.getByKey(lifeCycleProcess.shortName,i18nMessagesPlugin);
        this.name = MultiLanguagesString.getByKey(lifeCycleProcess.name,i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(lifeCycleProcess.description,i18nMessagesPlugin);
        this.isActive = lifeCycleProcess.isActive;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param lifeCycleProcess
     *            the life cycle process in the DB
     */
    public void fill(LifeCycleProcess lifeCycleProcess) {

        lifeCycleProcess.isActive = this.isActive;
        lifeCycleProcess.shortName = this.shortName.getKeyIfValue();
        lifeCycleProcess.name = this.name.getKeyIfValue();
        lifeCycleProcess.description = this.description.getKeyIfValue();

    }

}
