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
 * The org unit list request.
 * 
 * @author Oury Diallo
 */
public class OrgUnitListRequest {

    public Boolean isActive;
    public Long managerId;
    public Long parentId;
    public Long orgUnitTypeId;
    public Boolean canSponsor;
    public Boolean canDeliver;

    /**
     * Constructor with initial values.
     * 
     * @param isActive
     *            is active
     * @param managerId
     *            the manager id
     * @param parentId
     *            the parent id
     * @param orgUnitTypeId
     *            the org unit type id
     * @param canSponsor
     *            can sponsor
     * @param canDeliver
     *            can deliver
     */
    public OrgUnitListRequest(Boolean isActive, Long managerId, Long parentId, Long orgUnitTypeId, Boolean canSponsor, Boolean canDeliver) {

        this.isActive = isActive;
        this.managerId = managerId;
        this.parentId = parentId;
        this.orgUnitTypeId = orgUnitTypeId;
        this.canSponsor = canSponsor;
        this.canDeliver = canDeliver;
    }

    /**
     * Default constructor.
     */
    public OrgUnitListRequest() {
    }

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (managerId != null && ActorDao.getActorById(managerId) == null) {
            errors.add(new ValidationError("managerId", "The manager does not exist"));
        }
        if (parentId != null && OrgUnitDao.getOrgUnitById(parentId) == null) {
            errors.add(new ValidationError("parentId", "The parent does not exist"));
        }
        if (orgUnitTypeId != null && OrgUnitDao.getOrgUnitTypeById(orgUnitTypeId) == null) {
            errors.add(new ValidationError("orgUnitTypeId", "The orgUnitType does not exist"));
        }

        return errors.isEmpty() ? null : errors;
    }

}
