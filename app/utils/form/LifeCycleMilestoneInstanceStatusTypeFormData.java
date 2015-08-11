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

import models.governance.LifeCycleMilestoneInstanceStatusType;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A life cycle milestone instance status type form data is used to manage the
 * fields when managing a status type.
 * 
 * @author Johann Kohler
 */
public class LifeCycleMilestoneInstanceStatusTypeFormData {

    public Long id;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    public boolean selectable;

    public boolean isApproved;

    /**
     * Default constructor.
     */
    public LifeCycleMilestoneInstanceStatusTypeFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param statusType
     *            the life cycle milestone instance status type in the DB
     * @param i18nMessagesPlugin 
     *            the i18n manager
     */
    public LifeCycleMilestoneInstanceStatusTypeFormData(LifeCycleMilestoneInstanceStatusType statusType, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = statusType.id;
        this.name = MultiLanguagesString.getByKey(statusType.name, i18nMessagesPlugin);
        this.description = MultiLanguagesString.getByKey(statusType.description, i18nMessagesPlugin);
        this.selectable = statusType.selectable;
        this.isApproved = statusType.isApproved;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param statusType
     *            the life cycle milestone instance status type in the DB
     */
    public void fill(LifeCycleMilestoneInstanceStatusType statusType) {

        statusType.selectable = this.selectable;
        statusType.isApproved = this.isApproved;
        statusType.name = this.name.getKeyIfValue();
        statusType.description = this.description.getKeyIfValue();

    }

}
