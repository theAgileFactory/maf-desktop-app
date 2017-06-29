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

import dao.finance.CurrencyDAO;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.utils.Utilities;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocatedActorDetail;
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import org.apache.commons.lang3.tuple.Pair;
import play.Logger;
import play.Play;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import services.budgettracking.IBudgetTrackingService;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A portfolio entry resource plan allocated actor form data is used to manage
 * the fields when adding/editing an allocated actor for a resource plan.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedActorFormData {

    IBudgetTrackingService budgetTrackingService;

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

    public static class MonthAllocation {
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

        public void addValue(Integer month, Double days) {
            switch (month) {
                case 0:
                    this.januaryAllocationValue = days;
                    break;
                case 1:
                    this.februaryAllocationValue = days;
                    break;
                case 2:
                    this.marchAllocationValue = days;
                    break;
                case 3:
                    this.aprilAllocationValue = days;
                    break;
                case 4:
                    this.mayAllocationValue = days;
                    break;
                case 5:
                    this.juneAllocationValue = days;
                    break;
                case 6:
                    this.julyAllocationValue = days;
                    break;
                case 7:
                    this.augustAllocationValue = days;
                    break;
                case 8:
                    this.septemberAllocationValue = days;
                    break;
                case 9:
                    this.octoberAllocationValue = days;
                    break;
                case 10:
                    this.novemberAllocationValue = days;
                    break;
                case 11:
                default:
                    this.decemberAllocationValue = days;
            }
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
        this.budgetTrackingService = Play.application().injector().instanceOf(IBudgetTrackingService.class);
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param allocatedActor
     *            the allocated actor in the DB
     */
    public PortfolioEntryResourcePlanAllocatedActorFormData(PortfolioEntryResourcePlanAllocatedActor allocatedActor) {

        this.budgetTrackingService = Play.application().injector().instanceOf(IBudgetTrackingService.class);

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

        this.monthlyAllocated = allocatedActor.monthlyAllocated;
        this.monthAllocations = new ArrayList<>();
        List<PortfolioEntryResourcePlanAllocatedActorDetail> details = allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails;
        for(PortfolioEntryResourcePlanAllocatedActorDetail detail : details) {
            Optional<MonthAllocation> optionalMonthlyAllocation = this.monthAllocations.stream().filter(allocation -> allocation.year.equals(detail.year)).findFirst();
            MonthAllocation monthAllocation = optionalMonthlyAllocation.isPresent() ? optionalMonthlyAllocation.get() : new MonthAllocation(detail.year);
            monthAllocation.addValue(detail.month, detail.days);
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

        this.budgetTrackingService = Play.application().injector().instanceOf(IBudgetTrackingService.class);

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

        this.monthAllocations = new ArrayList<>();

        Map<Pair<Integer, Integer>, Double> allocationDistribution = PortfolioEntryResourcePlanAllocatedActor.getAllocationDistribution(allocatedOrgUnit.startDate, allocatedOrgUnit.endDate, budgetTrackingService.isActive() ? this.forecastDays : this.days);
        allocationDistribution.forEach((key, days) -> {
            Integer year = key.getLeft();
            Integer month = key.getRight();
            Optional<MonthAllocation> optionalMonthlyAllocation = this.monthAllocations.stream().filter(allocation -> allocation.year.equals(year)).findFirst();
            MonthAllocation monthAllocation = optionalMonthlyAllocation.isPresent() ? optionalMonthlyAllocation.get() : new MonthAllocation(year);
            monthAllocation.addValue(month, days);
            if (!optionalMonthlyAllocation.isPresent()) {
                monthAllocations.add(monthAllocation);
            }
        });

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

        this.budgetTrackingService = Play.application().injector().instanceOf(IBudgetTrackingService.class);

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

        this.monthAllocations = new ArrayList<>();

        Map<Pair<Integer, Integer>, Double> allocationDistribution = PortfolioEntryResourcePlanAllocatedActor.getAllocationDistribution(allocatedCompetency.startDate, allocatedCompetency.endDate, budgetTrackingService.isActive() ? this.forecastDays : this.days);
        allocationDistribution.forEach((key, days) -> {
            Integer year = key.getLeft();
            Integer month = key.getRight();
            Optional<MonthAllocation> optionalMonthlyAllocation = this.monthAllocations.stream().filter(allocation -> allocation.year.equals(year)).findFirst();
            MonthAllocation monthAllocation = optionalMonthlyAllocation.isPresent() ? optionalMonthlyAllocation.get() : new MonthAllocation(year);
            monthAllocation.addValue(month, days);
            if (!optionalMonthlyAllocation.isPresent()) {
                monthAllocations.add(monthAllocation);
            }
        });

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

        allocatedActor.monthlyAllocated = this.monthlyAllocated;
        allocatedActor.days = this.days;

        if (this.monthlyAllocated) {

            // Set allocations by month

            List<PortfolioEntryResourcePlanAllocatedActorDetail> details = new ArrayList<>();
            for (MonthAllocation monthAllocation : monthAllocations) {
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 0, monthAllocation.januaryAllocationValue));
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 1, monthAllocation.februaryAllocationValue));
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 2, monthAllocation.marchAllocationValue));
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 3, monthAllocation.aprilAllocationValue));
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 4, monthAllocation.mayAllocationValue));
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 5, monthAllocation.juneAllocationValue));
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 6, monthAllocation.julyAllocationValue));
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 7, monthAllocation.augustAllocationValue));
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 8, monthAllocation.septemberAllocationValue));
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 9, monthAllocation.octoberAllocationValue));
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 10, monthAllocation.novemberAllocationValue));
                details.add(processMonthAllocation(allocatedActor, monthAllocation.year, 11, monthAllocation.decemberAllocationValue));
            }
            allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.clear();
            allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.addAll(details.stream().filter(detail -> detail != null).collect(Collectors.toList()));

            // Set start date and end date
            Comparator<? super PortfolioEntryResourcePlanAllocatedActorDetail> comp = (d1, d2) -> {
                int c = Integer.compare(d1.year, d2.year);
                return c == 0 ? Integer.compare(d1.month, d2.month) : c;
            };

            PortfolioEntryResourcePlanAllocatedActorDetail startMonth = allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.stream().min(comp).get();
            PortfolioEntryResourcePlanAllocatedActorDetail endMonth = allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.stream().max(comp).get();

            Calendar c = GregorianCalendar.getInstance();
            c.set(startMonth.year, startMonth.month, 1);
            allocatedActor.startDate = c.getTime();

            c.set(endMonth.year, endMonth.month, 1);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            allocatedActor.endDate = c.getTime();

        } else if (allocatedActor.startDate != null && allocatedActor.endDate != null) { // If start and end dates are provided, distribute evenly the days across the months
            allocatedActor.computeAllocationDetails(budgetTrackingService.isActive());
        } else { // If no manual allocation and no start date and end date are provided, just remove the monthly distribution
            allocatedActor.clearAllocations();
        }
    }

    private PortfolioEntryResourcePlanAllocatedActorDetail processMonthAllocation(PortfolioEntryResourcePlanAllocatedActor allocatedActor, Integer year, Integer month, Double days) {
        if (days != null && days != 0) {
            return allocatedActor.createOrUpdateAllocationDetail(year, month, days);
        } else {
            if (allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.isEmpty()) {
                allocatedActor.computeAllocationDetails(budgetTrackingService.isActive());
            }
            PortfolioEntryResourcePlanAllocatedActorDetail detail = allocatedActor.getDetail(year, month);
            if (detail != null) {
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.remove(detail);
                detail.doDelete();
            }
            return null;
        }
    }
}
