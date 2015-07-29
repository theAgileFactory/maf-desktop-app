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

import models.framework_models.parent.IModelConstants;
import models.pmo.ActorType;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * An actor type form data is used to manage the fields when adding/editing an
 * actor type.
 * 
 * @author Johann Kohler
 */
public class ActorTypeFormData {

    public Long id;

    public boolean selectable;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String refId;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    /**
     * Default constructor.
     */
    public ActorTypeFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param actorType
     *            the actor type in the DB
     */
    public ActorTypeFormData(ActorType actorType) {

        this.id = actorType.id;
        this.selectable = actorType.selectable;
        this.refId = actorType.refId;
        this.name = MultiLanguagesString.getByKey(actorType.name);
        this.description = MultiLanguagesString.getByKey(actorType.description);

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param actorType
     *            the actor type in the DB
     */
    public void fill(ActorType actorType) {

        actorType.selectable = this.selectable;
        actorType.refId = this.refId;
        actorType.name = this.name.getKeyIfValue();
        actorType.description = this.description.getKeyIfValue();

    }
}
