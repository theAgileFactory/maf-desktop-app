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
import dao.pmo.OrgUnitDao;

/**
 * The actor list request.
 * 
 * @author Oury Diallo
 */
public class ActorListRequest {

    public Boolean isActive;
    public Long managerId;
    public Long actorTypeId;
    public Long competencyId;
    public Long orgUnitId;

    /**
     * Constructor with initial values.
     * 
     * @param isActive
     *            is active
     * @param managerId
     *            the manager id
     * @param actorTypeId
     *            the actor type id
     * @param competencyId
     *            the competency id
     * @param orgUnitId
     *            the org unit id
     */
    public ActorListRequest(Boolean isActive, Long managerId, Long actorTypeId, Long competencyId, Long orgUnitId) {

        this.isActive = isActive;
        this.managerId = managerId;
        this.actorTypeId = actorTypeId;
        this.competencyId = competencyId;
        this.orgUnitId = orgUnitId;
    }

    /**
     * Default constructor.
     */
    public ActorListRequest() {
    }

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (managerId != null && ActorDao.getActorById(managerId) == null) {
            errors.add(new ValidationError("managerId", "The manager does not exist"));
        }
        if (actorTypeId != null && ActorDao.getActorTypeById(actorTypeId) == null) {
            errors.add(new ValidationError("actorTypeId", "The actorType does not exist"));
        }
        if (competencyId != null && ActorDao.getCompetencyById(competencyId) == null) {
            errors.add(new ValidationError("competencyId", "The competency does not exist"));
        }
        if (orgUnitId != null && OrgUnitDao.getOrgUnitById(orgUnitId) == null) {
            errors.add(new ValidationError("orgUnitId", "The orgUnit does not exist"));
        }

        return errors.isEmpty() ? null : errors;

    }

}
