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

import java.util.ArrayList;
import java.util.List;

import models.pmo.Actor;
import models.pmo.Competency;
import dao.pmo.ActorDao;

/**
 * An actor competencies form data is used to manage the competencies of an
 * actor.
 * 
 * @author Johann Kohler
 */
public class ActorCompetenciesFormData {

    public Long id;

    public List<Long> competencies = new ArrayList<Long>();

    /**
     * Default constructor.
     */
    public ActorCompetenciesFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param actor
     *            the actor in the DB
     */
    public ActorCompetenciesFormData(Actor actor) {

        this.id = actor.id;

        if (actor.competencies != null) {
            for (Competency competency : actor.competencies) {
                this.competencies.add(competency.id);
            }
        }

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param actor
     *            the portfolio entry in the DB
     */
    public void fill(Actor actor) {

        actor.competencies = new ArrayList<Competency>();
        for (Long competency : this.competencies) {
            if (competency != null) {
                actor.competencies.add(ActorDao.getCompetencyById(competency));
            }
        }

    }

}
