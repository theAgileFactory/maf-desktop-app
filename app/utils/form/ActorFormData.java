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
import models.pmo.Actor;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;

/**
 * An actor form data is used to manage the fields when managing an actor.
 * 
 * @author Johann Kohler
 */
public class ActorFormData {

    public Long id;

    public String employeeId;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String firstName;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String lastName;

    @MaxLength(value = IModelConstants.LARGE_STRING)
    public String uid;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String title;

    public boolean isActive;

    @Email
    @MaxLength(value = IModelConstants.LARGE_STRING)
    public String mail;

    @MaxLength(value = IModelConstants.PHONE_NUMBER)
    public String mobilePhone;

    @MaxLength(value = IModelConstants.PHONE_NUMBER)
    public String fixPhone;

    public Long actorType;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String erpRefId;

    public Long orgUnit;

    public Long manager;

    /**
     * Default constructor.
     */
    public ActorFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param actor
     *            the actor in the DB
     */
    public ActorFormData(Actor actor) {

        this.id = actor.id;
        this.employeeId = actor.employeeId;
        this.firstName = actor.firstName;
        this.lastName = actor.lastName;
        this.uid = actor.uid;
        this.title = actor.title;
        this.isActive = actor.isActive;
        this.mail = actor.mail;
        this.mobilePhone = actor.mobilePhone;
        this.fixPhone = actor.fixPhone;
        this.actorType = actor.actorType != null ? actor.actorType.id : null;
        this.erpRefId = actor.erpRefId;
        this.orgUnit = actor.orgUnit != null ? actor.orgUnit.id : null;
        this.manager = actor.manager != null ? actor.manager.id : null;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param actor
     *            the actor in the DB
     */
    public void fill(Actor actor) {

        actor.isActive = this.isActive;
        actor.employeeId = this.employeeId;
        actor.firstName = this.firstName;
        actor.lastName = this.lastName;
        actor.uid = this.uid != null && !this.uid.equals("") ? this.uid : null;
        actor.title = this.title;
        actor.mail = this.mail;
        actor.mobilePhone = this.mobilePhone;
        actor.fixPhone = this.fixPhone;
        actor.actorType = this.actorType != null ? ActorDao.getActorTypeById(this.actorType) : null;
        actor.erpRefId = this.erpRefId;
        actor.orgUnit = this.orgUnit != null ? OrgUnitDao.getOrgUnitById(this.orgUnit) : null;
        actor.manager = this.manager != null ? ActorDao.getActorById(this.manager) : null;

    }

}
