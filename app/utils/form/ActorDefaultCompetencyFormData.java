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

import models.pmo.Actor;
import play.data.validation.Constraints.Required;
import dao.pmo.ActorDao;

/**
 * The form to select the default competency of an actor.
 * 
 * @author Johann Kohler
 */
public class ActorDefaultCompetencyFormData {

    public Long id;

    @Required
    public Long defaultCompetency;

    /**
     * Default constructor.
     */
    public ActorDefaultCompetencyFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param actor
     *            the actor in the DB
     */
    public ActorDefaultCompetencyFormData(Actor actor) {

        this.id = actor.id;
        this.defaultCompetency = actor.defaultCompetency != null ? actor.defaultCompetency.id : null;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param actor
     *            the actor in the DB
     */
    public void fill(Actor actor) {
        actor.defaultCompetency = ActorDao.getCompetencyById(this.defaultCompetency);
    }
}
