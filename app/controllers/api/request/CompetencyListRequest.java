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
 * The competency list request.
 * 
 * @author Oury Diallo
 */
public class CompetencyListRequest {

    public Boolean isActive;
    public Long actorId;

    /**
     * Constructor with initial values.
     * 
     * @param isActive
     *            is active
     * @param actorId
     *            the actor id
     */
    public CompetencyListRequest(Boolean isActive, Long actorId) {

        this.isActive = isActive;
        this.actorId = actorId;
    }

    /**
     * Default constructor.
     */
    public CompetencyListRequest() {
    }

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (actorId != null && ActorDao.getActorById(actorId) == null) {
            errors.add(new ValidationError("actorId", "The actor does not exist"));
        }

        return errors.isEmpty() ? null : errors;
    }

}
