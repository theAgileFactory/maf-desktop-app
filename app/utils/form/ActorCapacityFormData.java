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

import dao.pmo.ActorDao;
import models.pmo.Actor;
import models.pmo.ActorCapacity;
import play.data.validation.Constraints.Required;

import java.util.List;

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
    public Double januaryValue = 0.0;

    @Required
    public Double februaryValue = 0.0;

    @Required
    public Double marchValue = 0.0;

    @Required
    public Double aprilValue = 0.0;

    @Required
    public Double mayValue = 0.0;

    @Required
    public Double juneValue = 0.0;

    @Required
    public Double julyValue = 0.0;

    @Required
    public Double augustValue = 0.0;

    @Required
    public Double septemberValue = 0.0;

    @Required
    public Double octoberValue = 0.0;

    @Required
    public Double novemberValue = 0.0;

    @Required
    public Double decemberValue = 0.0;

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

        List<ActorCapacity> capacities = ActorDao.getActorCapacityAsArrayByActorAndYear(actor, year, 0);

        capacities.forEach(actorCapacity -> {
            switch (actorCapacity.month) {
                case 1:
                    this.januaryValue = actorCapacity.value;
                    break;
                case 2:
                    this.februaryValue = actorCapacity.value;
                    break;
                case 3:
                    this.marchValue = actorCapacity.value;
                    break;
                case 4:
                    this.aprilValue = actorCapacity.value;
                    break;
                case 5:
                    this.mayValue = actorCapacity.value;
                    break;
                case 6:
                    this.juneValue = actorCapacity.value;
                    break;
                case 7:
                    this.julyValue = actorCapacity.value;
                    break;
                case 8:
                    this.augustValue = actorCapacity.value;
                    break;
                case 9:
                    this.septemberValue = actorCapacity.value;
                    break;
                case 10:
                    this.octoberValue = actorCapacity.value;
                    break;
                case 11:
                    this.novemberValue = actorCapacity.value;
                    break;
                case 12:
                    this.decemberValue = actorCapacity.value;
            }
        });

    }

    /**
     * Fill and return the capacities for a year.
     */
    public List<ActorCapacity> getFilledCapacities() {
        Actor actor = ActorDao.getActorById(this.id);

        List<ActorCapacity> capacities;

        capacities = ActorDao.getActorCapacityAsArrayByActorAndYear(actor, year, 0);

        if (capacities.isEmpty()) {
            capacities.add(new ActorCapacity(actor, this.year, 1, this.januaryValue));
            capacities.add(new ActorCapacity(actor, this.year, 2, this.februaryValue));
            capacities.add(new ActorCapacity(actor, this.year, 3, this.marchValue));
            capacities.add(new ActorCapacity(actor, this.year, 4, this.aprilValue));
            capacities.add(new ActorCapacity(actor, this.year, 5, this.mayValue));
            capacities.add(new ActorCapacity(actor, this.year, 6, this.juneValue));
            capacities.add(new ActorCapacity(actor, this.year, 7, this.julyValue));
            capacities.add(new ActorCapacity(actor, this.year, 8, this.augustValue));
            capacities.add(new ActorCapacity(actor, this.year, 9, this.septemberValue));
            capacities.add(new ActorCapacity(actor, this.year, 10, this.octoberValue));
            capacities.add(new ActorCapacity(actor, this.year, 11, this.novemberValue));
            capacities.add(new ActorCapacity(actor, this.year, 12, this.decemberValue));
        } else {
            capacities.forEach(capacity -> {
                switch (capacity.month) {
                    case 1:
                        capacity.value = this.januaryValue;
                        break;
                    case 2:
                        capacity.value = this.februaryValue;
                        break;
                    case 3:
                        capacity.value = this.marchValue;
                        break;
                    case 4:
                        capacity.value = this.aprilValue;
                        break;
                    case 5:
                        capacity.value = this.mayValue;
                        break;
                    case 6:
                        capacity.value = this.juneValue;
                        break;
                    case 7:
                        capacity.value = this.julyValue;
                        break;
                    case 8:
                        capacity.value = this.augustValue;
                        break;
                    case 9:
                        capacity.value = this.septemberValue;
                        break;
                    case 10:
                        capacity.value = this.octoberValue;
                        break;
                    case 11:
                        capacity.value = this.novemberValue;
                        break;
                    case 12:
                        capacity.value = this.decemberValue;
                        break;
                }
            });
        }

        return capacities;
    }

}
