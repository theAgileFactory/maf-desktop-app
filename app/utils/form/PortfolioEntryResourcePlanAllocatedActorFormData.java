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
import java.util.*;

import dao.finance.CurrencyDAO;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.utils.Utilities;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocatedActorDetail;
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

    @Required
    public Long actor;

    @Required
    public Long stakeholderType;

    public String startDate;

    public String endDate;

    public Long portfolioEntryPlanningPackage;

    public boolean isConfirmed;

    public boolean followPackageDates;

    @Required
    public String currencyCode;

    @Required
    public BigDecimal currencyRate;

    @Required
    public BigDecimal days;

    @Required
    public BigDecimal dailyRate;

    public BigDecimal forecastDays;

    public BigDecimal forecastDailyRate;

    public boolean monthlyAllocated;

    public List<MonthAllocation> monthAllocations;

    public class MonthAllocation {
        public Integer year;
        public Double januaryAllocationValue;
        public Double februaryAllocationValue;
        public Double marchAllocationValue;
        public Double aprilAllocationValue;
        public Double mayAllocationValue;
        public Double juneAllocationValue;
        public Double julyAllocationValue;
        public Double augustAllocationValue;
        public Double septemberAllocationValue;
        public Double octoberAllocationValue;
        public Double novemberAllocationValue;
        public Double decemberAllocationValue;

        public MonthAllocation() {
        }

        public MonthAllocation(Integer year) {
            this.year = year;
        }
    }

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
        this.monthAllocations = new ArrayList<>();
        this.monthAllocations.add(new MonthAllocation());
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
        this.startDate = allocatedActor.startDate != null ? Utilities.getDateFormat(null).format(allocatedActor.startDate) : null;
        this.endDate = allocatedActor.endDate != null ? Utilities.getDateFormat(null).format(allocatedActor.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedActor.portfolioEntryPlanningPackage != null ? allocatedActor.portfolioEntryPlanningPackage.id : null;
        this.isConfirmed = allocatedActor.isConfirmed;

        this.currencyCode = allocatedActor.currency != null ? allocatedActor.currency.code : null;
        this.currencyRate = allocatedActor.currencyRate;

        this.days = allocatedActor.days;
        this.dailyRate = allocatedActor.dailyRate;
        this.forecastDays = allocatedActor.forecastDays;
        this.forecastDailyRate = allocatedActor.forecastDailyRate;

        List<PortfolioEntryResourcePlanAllocatedActorDetail> details = allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails;
        this.monthlyAllocated = !details.isEmpty();
        this.monthAllocations = new ArrayList<>();
        for(PortfolioEntryResourcePlanAllocatedActorDetail detail : details) {
            Optional<MonthAllocation> optionalMonthlyAllocation = this.monthAllocations.stream().filter(allocation -> allocation.year.equals(detail.year)).findFirst();
            MonthAllocation monthAllocation = optionalMonthlyAllocation.isPresent() ? optionalMonthlyAllocation.get() : new MonthAllocation(detail.year);
            switch (detail.month) {
                case 1:
                    monthAllocation.januaryAllocationValue = detail.days;
                    break;
                case 2:
                    monthAllocation.februaryAllocationValue = detail.days;
                    break;
                case 3:
                    monthAllocation.marchAllocationValue = detail.days;
                    break;
                case 4:
                    monthAllocation.aprilAllocationValue = detail.days;
                    break;
                case 5:
                    monthAllocation.mayAllocationValue = detail.days;
                    break;
                case 6:
                    monthAllocation.juneAllocationValue = detail.days;
                    break;
                case 7:
                    monthAllocation.julyAllocationValue = detail.days;
                    break;
                case 8:
                    monthAllocation.augustAllocationValue = detail.days;
                    break;
                case 9:
                    monthAllocation.septemberAllocationValue = detail.days;
                    break;
                case 10:
                    monthAllocation.octoberAllocationValue = detail.days;
                    break;
                case 11:
                    monthAllocation.novemberAllocationValue = detail.days;
                    break;
                case 12:
                default:
                    monthAllocation.decemberAllocationValue = detail.days;
            }
            if (!optionalMonthlyAllocation.isPresent()) {
                monthAllocations.add(monthAllocation);
            }
        }

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

        this.startDate = allocatedOrgUnit.startDate != null ? Utilities.getDateFormat(null).format(allocatedOrgUnit.startDate) : null;
        this.endDate = allocatedOrgUnit.endDate != null ? Utilities.getDateFormat(null).format(allocatedOrgUnit.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedOrgUnit.portfolioEntryPlanningPackage != null ? allocatedOrgUnit.portfolioEntryPlanningPackage.id
                : null;
        this.isConfirmed = true;

        this.currencyCode = allocatedOrgUnit.currency != null ? allocatedOrgUnit.currency.code : null;
        this.currencyRate = allocatedOrgUnit.currencyRate;

        this.days = allocatedOrgUnit.days;
        this.dailyRate = allocatedOrgUnit.dailyRate;
        this.forecastDays = allocatedOrgUnit.forecastDays;
        this.forecastDailyRate = allocatedOrgUnit.forecastDailyRate;

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

        this.startDate = allocatedCompetency.startDate != null ? Utilities.getDateFormat(null).format(allocatedCompetency.startDate) : null;
        this.endDate = allocatedCompetency.endDate != null ? Utilities.getDateFormat(null).format(allocatedCompetency.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedCompetency.portfolioEntryPlanningPackage != null ? allocatedCompetency.portfolioEntryPlanningPackage.id
                : null;
        this.isConfirmed = true;

        this.currencyCode = allocatedCompetency.currency != null ? allocatedCompetency.currency.code : null;
        this.currencyRate = allocatedCompetency.currencyRate;

        this.days = allocatedCompetency.days;
        this.dailyRate = allocatedCompetency.dailyRate;
        this.forecastDays = null;
        this.forecastDailyRate = null;

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

        allocatedActor.portfolioEntryPlanningPackage = this.portfolioEntryPlanningPackage != null
                ? PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(this.portfolioEntryPlanningPackage) : null;

        allocatedActor.followPackageDates = allocatedActor.portfolioEntryPlanningPackage != null ? this.followPackageDates : null;

        if (allocatedActor.followPackageDates == null || !allocatedActor.followPackageDates) {
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

        allocatedActor.currency = CurrencyDAO.getCurrencyByCode(this.currencyCode);
        allocatedActor.currencyRate = this.currencyRate;

        allocatedActor.dailyRate = this.dailyRate;
        allocatedActor.forecastDays = this.forecastDays;
        allocatedActor.forecastDailyRate = this.forecastDailyRate;

        allocatedActor.days = BigDecimal.valueOf(0);

        if (this.monthlyAllocated) {
            for (MonthAllocation monthAllocation : monthAllocations) {
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 1, monthAllocation.januaryAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 2, monthAllocation.februaryAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 3, monthAllocation.marchAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 4, monthAllocation.aprilAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 5, monthAllocation.mayAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 6, monthAllocation.juneAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 7, monthAllocation.julyAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 8, monthAllocation.augustAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 9, monthAllocation.septemberAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 10, monthAllocation.octoberAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 11, monthAllocation.novemberAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 12, monthAllocation.decemberAllocationValue));
            }
        } else {
            allocatedActor.days = this.days;
            for (PortfolioEntryResourcePlanAllocatedActorDetail detail : allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails) {
                detail.doDelete();
                detail.save();
            }
            allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.clear();
        }
    }

    private PortfolioEntryResourcePlanAllocatedActorDetail createOrUpdateAllocationDetail(PortfolioEntryResourcePlanAllocatedActor allocatedActor, Integer year, Integer month, Double days) {
        Optional<PortfolioEntryResourcePlanAllocatedActorDetail> optionalDetail = allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.stream().filter(detail -> detail.year.equals(year) && detail.month.equals(month)).findFirst();
        PortfolioEntryResourcePlanAllocatedActorDetail detail;
        if (optionalDetail.isPresent()) {
            // Update
            detail = optionalDetail.get();
            detail.days = days;
            detail.update();
        } else {
            // Create
            detail = new PortfolioEntryResourcePlanAllocatedActorDetail(allocatedActor, year, month, days);
            detail.save();
        }
        allocatedActor.days = allocatedActor.days.add(BigDecimal.valueOf(days == null ? 0 : days));
        return detail;
    }
}
