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
import models.pmo.ActorCapacity;
import play.data.validation.Constraints.Required;
import dao.pmo.ActorDao;

/**
 * An actor capacity form data is used to manage the capacities of an actor for
 * a year.
 * 
 * @author Johann Kohler
 */
public class ActorCapacityFormData {

    // the actor id
    public Long id;

    public Integer year;

    @Required
    public Double januaryValue;

    @Required
    public Double februaryValue;

    @Required
    public Double marchValue;

    @Required
    public Double aprilValue;

    @Required
    public Double mayValue;

    @Required
    public Double juneValue;

    @Required
    public Double julyValue;

    @Required
    public Double augustValue;

    @Required
    public Double septemberValue;

    @Required
    public Double octoberValue;

    @Required
    public Double novemberValue;

    @Required
    public Double decemberValue;

    /**
     * Default constructor.
     */
    public ActorCapacityFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param actor
     *            the actor in the DB
     * @param year
     *            the year
     */
    public ActorCapacityFormData(Actor actor, Integer year) {

        this.id = actor.id;
        this.year = year;

        ActorCapacity[] capacities = ActorDao.getActorCapacityAsArrayByActorAndYear(actor, year, 0).toArray(new ActorCapacity[0]);

        this.januaryValue = capacities[0].value;
        this.februaryValue = capacities[1].value;
        this.marchValue = capacities[2].value;
        this.aprilValue = capacities[3].value;
        this.mayValue = capacities[4].value;
        this.juneValue = capacities[5].value;
        this.julyValue = capacities[6].value;
        this.augustValue = capacities[7].value;
        this.septemberValue = capacities[8].value;
        this.octoberValue = capacities[9].value;
        this.novemberValue = capacities[10].value;
        this.decemberValue = capacities[11].value;

    }

    /**
     * Fill and return the capacities for a year.
     */
    public ActorCapacity[] getFilledCapacities() {
        Actor actor = ActorDao.getActorById(this.id);
        ActorCapacity[] capacities = ActorDao.getActorCapacityAsArrayByActorAndYear(actor, this.year, 0).toArray(new ActorCapacity[0]);
        capacities[0].value = this.januaryValue;
        capacities[1].value = this.februaryValue;
        capacities[2].value = this.marchValue;
        capacities[3].value = this.aprilValue;
        capacities[4].value = this.mayValue;
        capacities[5].value = this.juneValue;
        capacities[6].value = this.julyValue;
        capacities[7].value = this.augustValue;
        capacities[8].value = this.septemberValue;
        capacities[9].value = this.octoberValue;
        capacities[10].value = this.novemberValue;
        capacities[11].value = this.decemberValue;
        return capacities;
    }

}
