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
package controllers.api.request;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.ValidationError;
import dao.pmo.ActorDao;

/**
 * The actor capacity list request.
 * 
 * @author Johann Kohler
 */
public class ActorCapacityListRequest {

    public Long actorId;
    public Integer year;

    /**
     * Constructor with initial value.
     * 
     * @param actorId
     *            the actor id
     * @param year
     *            the year
     */
    public ActorCapacityListRequest(Long actorId, Integer year) {
        this.actorId = actorId;
        this.year = year;

    }

    /**
     * Default constructor.
     */
    public ActorCapacityListRequest() {
    }

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (actorId != null && ActorDao.getActorById(actorId) == null) {
            errors.add(new ValidationError("actorId", "The actor does not exist"));
        }

        if (year != null && (year < 0 || year > 9999)) {
            errors.add(new ValidationError("year", "Incorrect year format : please use 4 digits."));
        }
        return errors.isEmpty() ? null : errors;

    }

}
