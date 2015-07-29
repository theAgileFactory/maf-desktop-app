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
import models.pmo.OrgUnit;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;

/**
 * An org unit form data is used to manage the fields when managing an org unit.
 * 
 * @author Johann Kohler
 */
public class OrgUnitFormData {

    public Long id;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String refId;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    public boolean isActive;

    public boolean canSponsor;

    public boolean canDeliver;

    public Long orgUnitType;

    public Long manager;

    public Long parent;

    /**
     * Default constructor.
     */
    public OrgUnitFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param orgUnit
     *            the org unit in the DB
     */
    public OrgUnitFormData(OrgUnit orgUnit) {

        this.id = orgUnit.id;
        this.refId = orgUnit.refId;
        this.name = orgUnit.name;
        this.isActive = orgUnit.isActive;
        this.canSponsor = orgUnit.canSponsor;
        this.canDeliver = orgUnit.canDeliver;
        this.orgUnitType = orgUnit.orgUnitType != null ? orgUnit.orgUnitType.id : null;
        this.manager = orgUnit.manager != null ? orgUnit.manager.id : null;
        this.parent = orgUnit.parent != null ? orgUnit.parent.id : null;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param orgUnit
     *            the org unit in the DB
     */
    public void fill(OrgUnit orgUnit) {

        orgUnit.refId = this.refId;
        orgUnit.name = this.name;
        orgUnit.isActive = this.isActive;
        orgUnit.canSponsor = this.canSponsor;
        orgUnit.canDeliver = this.canDeliver;
        orgUnit.orgUnitType = this.orgUnitType != null ? OrgUnitDao.getOrgUnitTypeById(this.orgUnitType) : null;
        orgUnit.manager = this.manager != null ? ActorDao.getActorById(this.manager) : null;
        orgUnit.parent = this.parent != null ? OrgUnitDao.getOrgUnitById(this.parent) : null;

    }

}
