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

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.utils.Utilities;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.i18n.Messages;

/**
 * A portfolio entry resource plan allocated actor form data is used to manage
 * the fields when adding/editing an allocated actor for a resource plan.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedActorFormData {

    // the portfolioEntry id
    public Long id;

    public Long allocatedActorId;

    // used only when reallocate an allocated org unit to an actor
    public Long allocatedOrgUnitId;

    // used only when reallocate an allocated competency to an actor
    public Long allocatedCompetencyId;

    @Required(groups = { ReallocateGroup.class, DefaultGroup.class })
    public Long actor;

    // used only when reallocate an allocated resource to an actor
    @Required(groups = { ReallocateGroup.class })
    public Long stakeholderType;

    @Required(groups = { ReallocateGroup.class, DefaultGroup.class })
    public BigDecimal days;

    public String startDate;

    public String endDate;

    public Long portfolioEntryPlanningPackage;

    public boolean isConfirmed;

    public boolean followPackageDates;

    /**
     * Validate the dates.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        if (this.startDate != null && this.endDate != null) {

            try {

                if (!this.startDate.equals("") && this.endDate.equals("")) {
                    // the start date cannot be filled alone
                    errors.add(new ValidationError("startDate", Messages.get("object.allocated_resource.start_date.invalid")));
                }

                if (!this.startDate.equals("") && !this.endDate.equals("")
                        && Utilities.getDateFormat(null).parse(this.startDate).after(Utilities.getDateFormat(null).parse(this.endDate))) {
                    // the end date should be after the start date
                    errors.add(new ValidationError("endDate", Messages.get("object.allocated_resource.end_date.invalid")));
                }

            } catch (Exception e) {
                Logger.warn("impossible to parse the allocation dates when testing the formats");
            }
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Default constructor.
     */
    public PortfolioEntryResourcePlanAllocatedActorFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param allocatedActor
     *            the allocated actor in the DB
     */
    public PortfolioEntryResourcePlanAllocatedActorFormData(PortfolioEntryResourcePlanAllocatedActor allocatedActor) {

        this.id = allocatedActor.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.allocatedActorId = allocatedActor.id;

        this.actor = allocatedActor.actor.id;
        this.days = allocatedActor.days;
        this.startDate = allocatedActor.startDate != null ? Utilities.getDateFormat(null).format(allocatedActor.startDate) : null;
        this.endDate = allocatedActor.endDate != null ? Utilities.getDateFormat(null).format(allocatedActor.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedActor.portfolioEntryPlanningPackage != null ? allocatedActor.portfolioEntryPlanningPackage.id : null;
        this.isConfirmed = allocatedActor.isConfirmed;

        this.followPackageDates = allocatedActor.followPackageDates != null ? allocatedActor.followPackageDates : false;

    }

    /**
     * Construct the form data with an allocated org unit.
     * 
     * Used when an allocated org unit should be reallocated to an actor.
     * 
     * @param allocatedOrgUnit
     *            the allocated org unit
     */
    public PortfolioEntryResourcePlanAllocatedActorFormData(PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit) {

        this.id = allocatedOrgUnit.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.allocatedOrgUnitId = allocatedOrgUnit.id;

        this.days = allocatedOrgUnit.days;
        this.startDate = allocatedOrgUnit.startDate != null ? Utilities.getDateFormat(null).format(allocatedOrgUnit.startDate) : null;
        this.endDate = allocatedOrgUnit.endDate != null ? Utilities.getDateFormat(null).format(allocatedOrgUnit.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedOrgUnit.portfolioEntryPlanningPackage != null ? allocatedOrgUnit.portfolioEntryPlanningPackage.id
                : null;
        this.isConfirmed = true;

        this.followPackageDates = allocatedOrgUnit.followPackageDates != null ? allocatedOrgUnit.followPackageDates : false;

    }

    /**
     * Construct the form data with an allocated competency.
     * 
     * Used when an allocated competency should be reallocated to an actor.
     * 
     * @param allocatedCompetency
     *            the allocated competency
     */
    public PortfolioEntryResourcePlanAllocatedActorFormData(PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency) {

        this.id = allocatedCompetency.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.allocatedCompetencyId = allocatedCompetency.id;

        this.days = allocatedCompetency.days;
        this.startDate = allocatedCompetency.startDate != null ? Utilities.getDateFormat(null).format(allocatedCompetency.startDate) : null;
        this.endDate = allocatedCompetency.endDate != null ? Utilities.getDateFormat(null).format(allocatedCompetency.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedCompetency.portfolioEntryPlanningPackage != null ? allocatedCompetency.portfolioEntryPlanningPackage.id
                : null;
        this.isConfirmed = true;

        this.followPackageDates = allocatedCompetency.followPackageDates != null ? allocatedCompetency.followPackageDates : false;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param allocatedActor
     *            the allocated actor in the DB
     */
    public void fill(PortfolioEntryResourcePlanAllocatedActor allocatedActor) {

        allocatedActor.actor = ActorDao.getActorById(this.actor);
        allocatedActor.days = this.days;

        allocatedActor.portfolioEntryPlanningPackage = this.portfolioEntryPlanningPackage != null
                ? PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(this.portfolioEntryPlanningPackage) : null;

        allocatedActor.followPackageDates = allocatedActor.portfolioEntryPlanningPackage != null ? this.followPackageDates : null;

        if (allocatedActor.followPackageDates == null || allocatedActor.followPackageDates == false) {
            try {
                allocatedActor.startDate = Utilities.getDateFormat(null).parse(this.startDate);
            } catch (ParseException e) {
                allocatedActor.startDate = null;
            }

            try {
                allocatedActor.endDate = Utilities.getDateFormat(null).parse(this.endDate);
            } catch (ParseException e) {
                allocatedActor.endDate = null;
            }
        } else {
            allocatedActor.startDate = allocatedActor.portfolioEntryPlanningPackage.startDate;
            allocatedActor.endDate = allocatedActor.portfolioEntryPlanningPackage.endDate;
        }

        allocatedActor.isConfirmed = this.isConfirmed;

    }

    /**
     * The group for the default case.
     * 
     * @author Johann Kohler
     */
    public interface DefaultGroup {
    }

    /**
     * The group for the reallocate form.
     * 
     * @author Johann Kohler
     */
    public interface ReallocateGroup {
    }
}
