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

import models.pmo.Competency;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A competency form data is used to manage the fields when adding/editing a
 * competency.
 * 
 * @author Johann Kohler
 */
public class CompetencyFormData {

    public Long id;

    public boolean isActive;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    /**
     * Default constructor.
     */
    public CompetencyFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param competency
     *            the competency in the DB
     */
    public CompetencyFormData(Competency competency) {

        this.id = competency.id;
        this.isActive = competency.isActive;
        this.name = MultiLanguagesString.getByKey(competency.name);
        this.description = MultiLanguagesString.getByKey(competency.description);

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param competency
     *            the competency in the DB
     */
    public void fill(Competency competency) {

        competency.isActive = this.isActive;
        competency.name = this.name.getKeyIfValue();
        competency.description = this.description.getKeyIfValue();

    }
}
