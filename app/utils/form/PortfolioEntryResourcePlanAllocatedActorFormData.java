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
import org.apache.commons.lang3.tuple.Pair;
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

        this.monthlyAllocated = allocatedActor.monthlyAllocated;
        this.monthAllocations = new ArrayList<>();
        List<PortfolioEntryResourcePlanAllocatedActorDetail> details = allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails;
        for(PortfolioEntryResourcePlanAllocatedActorDetail detail : details) {
            Optional<MonthAllocation> optionalMonthlyAllocation = this.monthAllocations.stream().filter(allocation -> allocation.year.equals(detail.year)).findFirst();
            MonthAllocation monthAllocation = optionalMonthlyAllocation.isPresent() ? optionalMonthlyAllocation.get() : new MonthAllocation(detail.year);
            switch (detail.month) {
                case 0:
                    monthAllocation.januaryAllocationValue = detail.days;
                    break;
                case 1:
                    monthAllocation.februaryAllocationValue = detail.days;
                    break;
                case 2:
                    monthAllocation.marchAllocationValue = detail.days;
                    break;
                case 3:
                    monthAllocation.aprilAllocationValue = detail.days;
                    break;
                case 4:
                    monthAllocation.mayAllocationValue = detail.days;
                    break;
                case 5:
                    monthAllocation.juneAllocationValue = detail.days;
                    break;
                case 6:
                    monthAllocation.julyAllocationValue = detail.days;
                    break;
                case 7:
                    monthAllocation.augustAllocationValue = detail.days;
                    break;
                case 8:
                    monthAllocation.septemberAllocationValue = detail.days;
                    break;
                case 9:
                    monthAllocation.octoberAllocationValue = detail.days;
                    break;
                case 10:
                    monthAllocation.novemberAllocationValue = detail.days;
                    break;
                case 11:
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

        allocatedActor.monthlyAllocated = this.monthlyAllocated;
        allocatedActor.days = this.days;

        if (this.monthlyAllocated) {

            // Set allocations by month
            for (MonthAllocation monthAllocation : monthAllocations) {
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 0, monthAllocation.januaryAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 1, monthAllocation.februaryAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 2, monthAllocation.marchAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 3, monthAllocation.aprilAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 4, monthAllocation.mayAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 5, monthAllocation.juneAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 6, monthAllocation.julyAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 7, monthAllocation.augustAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 8, monthAllocation.septemberAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 9, monthAllocation.octoberAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 10, monthAllocation.novemberAllocationValue));
                allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.add(createOrUpdateAllocationDetail(allocatedActor, monthAllocation.year, 11, monthAllocation.decemberAllocationValue));
            }

            // Set start date and end date
            Comparator<? super PortfolioEntryResourcePlanAllocatedActorDetail> comp = (d1, d2) -> {
                int c = Integer.compare(d1.year, d2.year);
                return c == 0 ? Integer.compare(d1.month, d2.month) : c;
            };

            PortfolioEntryResourcePlanAllocatedActorDetail startMonth = allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.stream().filter(d -> d.days != null).min(comp).get();
            PortfolioEntryResourcePlanAllocatedActorDetail endMonth = allocatedActor.portfolioEntryResourcePlanAllocatedActorDetails.stream().filter(d -> d.days != null).max(comp).get();

            Calendar c = GregorianCalendar.getInstance();
            c.set(startMonth.year, startMonth.month, 1);
            allocatedActor.startDate = c.getTime();

            c.set(endMonth.year, endMonth.month, 0);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            allocatedActor.endDate = c.getTime();

        } else if (allocatedActor.startDate != null && allocatedActor.endDate != null) { // If start and end dates are provided, distribute evenly the days across the months
            // Clear current allocation details
            allocatedActor.clearAllocations();

            // Distribute allocations monthly from start date to end date
            long endMillis = removeTime(allocatedActor.endDate).getTimeInMillis();
            long startMillis = removeTime(allocatedActor.startDate).getTimeInMillis();
            int days = 1 + (int) ((endMillis - startMillis) / (1000 * 60 * 60 * 24));
            Double dayRate = allocatedActor.days.doubleValue() / days;
            Calendar start = removeTime(allocatedActor.startDate);
            Map<Pair<Integer, Integer>, Double> daysMap = new HashMap<>();
            for (int i = 0; i < days; i++) {
                Pair<Integer, Integer> month = Pair.of(start.get(Calendar.YEAR), start.get(Calendar.MONTH));
                Double d = daysMap.get(month) == null ? 0.0 : daysMap.get(month);
                daysMap.put(month, d + dayRate);
                start.add(Calendar.DAY_OF_MONTH, 1);
            }
            for (Pair<Integer, Integer> month : daysMap.keySet()) {
                createOrUpdateAllocationDetail(allocatedActor, month.getLeft(), month.getRight(), daysMap.get(month));
            }
        } else { // If no manual allocation and no start date and end date are provided, just remove the monthly distribution
            allocatedActor.clearAllocations();
        }
    }

    /**
     * Remove the time part of a date and return a calendar.
     *
     * @param date
     *            the date
     */
    private static Calendar removeTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private PortfolioEntryResourcePlanAllocatedActorDetail createOrUpdateAllocationDetail(PortfolioEntryResourcePlanAllocatedActor allocatedActor, Integer year, Integer month, Double days) {
        if (month == null) {
            return null;
        }
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
        return detail;
    }
}
