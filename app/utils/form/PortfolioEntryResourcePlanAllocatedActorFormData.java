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

import constants.IMafConstants;
import dao.finance.CurrencyDAO;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import dao.pmo.StakeholderDao;
import framework.services.account.IPreferenceManagerPlugin;
import framework.utils.Utilities;
import models.finance.*;
import models.pmo.Stakeholder;
import org.apache.commons.lang3.tuple.Pair;
import play.Play;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
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
public class PortfolioEntryResourcePlanAllocatedActorFormData extends ResourceAllocationFormData {

    IBudgetTrackingService budgetTrackingService;

    IPreferenceManagerPlugin preferenceManager;

    // used only when reallocate an allocated org unit to an actor
    public Long allocatedOrgUnitId;

    // used only when reallocate an allocated competency to an actor
    public Long allocatedCompetencyId;

    @Required
    public Long actor;

    @Required
    public Long stakeholderType;

    public BigDecimal forecastDays;

    public BigDecimal forecastDailyRate;

    @Constraints.Required
    public String currencyCode;

    @Constraints.Required
    public BigDecimal currencyRate;

    @Constraints.Required
    public BigDecimal dailyRate;

    /**
     * Default constructor.
     */
    public PortfolioEntryResourcePlanAllocatedActorFormData() {
        super();

        this.budgetTrackingService = Play.application().injector().instanceOf(IBudgetTrackingService.class);
        this.preferenceManager = Play.application().injector().instanceOf(IPreferenceManagerPlugin.class);
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param allocatedActor
     *            the allocated actor in the DB
     */
    public PortfolioEntryResourcePlanAllocatedActorFormData(PortfolioEntryResourcePlanAllocatedActor allocatedActor) {

        this.budgetTrackingService = Play.application().injector().instanceOf(IBudgetTrackingService.class);
        this.preferenceManager = Play.application().injector().instanceOf(IPreferenceManagerPlugin.class);

        this.id = allocatedActor.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.allocationId = allocatedActor.id;

        this.actor = allocatedActor.actor.id;
        List<Stakeholder> stakeholderTypes = StakeholderDao.getStakeholderAsListByActorAndPE(this.actor, allocatedActor.getAssociatedPortfolioEntry().id);
        this.stakeholderType = stakeholderTypes.isEmpty() ? null : stakeholderTypes.get(0).stakeholderType.id;
        this.startDate = allocatedActor.startDate != null ? Utilities.getDateFormat(null).format(allocatedActor.startDate) : null;
        this.endDate = allocatedActor.endDate != null ? Utilities.getDateFormat(null).format(allocatedActor.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedActor.portfolioEntryPlanningPackage != null ? allocatedActor.portfolioEntryPlanningPackage.id : null;
        this.allocationStatus = allocatedActor.portfolioEntryResourcePlanAllocationStatusType.status.name();
        this.lastStatusTypeUpdateActor = allocatedActor.lastStatusTypeUpdateActor != null ? allocatedActor.lastStatusTypeUpdateActor.id : 0L;
        this.lastStatusTypeUpdateTime = allocatedActor.lastStatusTypeUpdateTime != null ? Utilities.getDateFormat(null).format(allocatedActor.lastStatusTypeUpdateTime) : null;

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
        this.preferenceManager = Play.application().injector().instanceOf(IPreferenceManagerPlugin.class);

        this.id = allocatedOrgUnit.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.allocatedOrgUnitId = allocatedOrgUnit.id;

        this.startDate = allocatedOrgUnit.startDate != null ? Utilities.getDateFormat(null).format(allocatedOrgUnit.startDate) : null;
        this.endDate = allocatedOrgUnit.endDate != null ? Utilities.getDateFormat(null).format(allocatedOrgUnit.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedOrgUnit.portfolioEntryPlanningPackage != null ? allocatedOrgUnit.portfolioEntryPlanningPackage.id
                : null;

        this.allocationStatus = PortfolioEntryResourcePlanAllocationStatusType.AllocationStatus.DRAFT.name();

        this.currencyCode = allocatedOrgUnit.currency != null ? allocatedOrgUnit.currency.code : null;
        this.currencyRate = allocatedOrgUnit.currencyRate;

        this.days = allocatedOrgUnit.days;
        this.dailyRate = allocatedOrgUnit.dailyRate;
        this.forecastDays = allocatedOrgUnit.forecastDays;
        this.forecastDailyRate = allocatedOrgUnit.forecastDailyRate;

        this.monthlyAllocated = allocatedOrgUnit.monthlyAllocated;

        this.monthAllocations = new ArrayList<>();
        List<PortfolioEntryResourcePlanAllocatedOrgUnitDetail> details = allocatedOrgUnit.portfolioEntryResourcePlanAllocatedOrgUnitDetails;
        for(PortfolioEntryResourcePlanAllocatedOrgUnitDetail detail : details) {
            Optional<MonthAllocation> optionalMonthlyAllocation = this.monthAllocations.stream().filter(allocation -> allocation.year.equals(detail.year)).findFirst();
            MonthAllocation monthAllocation = optionalMonthlyAllocation.isPresent() ? optionalMonthlyAllocation.get() : new MonthAllocation(detail.year);
            monthAllocation.addValue(detail.month, detail.days);
            if (!optionalMonthlyAllocation.isPresent()) {
                monthAllocations.add(monthAllocation);
            }
        }

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
        this.preferenceManager = Play.application().injector().instanceOf(IPreferenceManagerPlugin.class);

        this.id = allocatedCompetency.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.allocatedCompetencyId = allocatedCompetency.id;

        this.startDate = allocatedCompetency.startDate != null ? Utilities.getDateFormat(null).format(allocatedCompetency.startDate) : null;
        this.endDate = allocatedCompetency.endDate != null ? Utilities.getDateFormat(null).format(allocatedCompetency.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedCompetency.portfolioEntryPlanningPackage != null ? allocatedCompetency.portfolioEntryPlanningPackage.id
                : null;

        this.allocationStatus = PortfolioEntryResourcePlanAllocationStatusType.AllocationStatus.DRAFT.name();

        this.currencyCode = allocatedCompetency.currency != null ? allocatedCompetency.currency.code : null;
        this.currencyRate = allocatedCompetency.currencyRate;

        this.days = allocatedCompetency.days;
        this.dailyRate = allocatedCompetency.dailyRate;
        this.forecastDays = null;
        this.forecastDailyRate = null;

        this.monthAllocations = new ArrayList<>();

        Boolean workingDaysOnly = preferenceManager.getPreferenceValueAsBoolean(IMafConstants.RESOURCES_WEEK_DAYS_ALLOCATION_PREFERENCE);
        Map<Pair<Integer, Integer>, Double> allocationDistribution = PortfolioEntryResourcePlanAllocatedActor.getAllocationDistribution(allocatedCompetency.startDate, allocatedCompetency.endDate, budgetTrackingService.isActive() ? this.forecastDays : this.days, workingDaysOnly);
        if (allocationDistribution != null) {
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
        }

        this.followPackageDates = allocatedCompetency.followPackageDates != null ? allocatedCompetency.followPackageDates : false;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param allocatedActor
     *            the allocated actor in the DB
     */
    public void fill(PortfolioEntryResourcePlanAllocatedActor allocatedActor) {

        if (!this.days.equals(allocatedActor.days) || !this.actor.equals(allocatedActor.actor.id)) {
            allocatedActor.portfolioEntryResourcePlanAllocationStatusType = PortfolioEntryResourcePlanDAO.getAllocationStatusByType(PortfolioEntryResourcePlanAllocationStatusType.AllocationStatus.DRAFT);
        }

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
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 0, monthAllocation.januaryAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 1, monthAllocation.februaryAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 2, monthAllocation.marchAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 3, monthAllocation.aprilAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 4, monthAllocation.mayAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 5, monthAllocation.juneAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 6, monthAllocation.julyAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 7, monthAllocation.augustAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 8, monthAllocation.septemberAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 9, monthAllocation.octoberAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 10, monthAllocation.novemberAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedActorDetail) processMonthAllocation(allocatedActor, monthAllocation.year, 11, monthAllocation.decemberAllocationValue));
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
            allocatedActor.computeAllocationDetails(budgetTrackingService.isActive(), preferenceManager.getPreferenceValueAsBoolean(IMafConstants.RESOURCES_WEEK_DAYS_ALLOCATION_PREFERENCE));
        } else { // If no manual allocation and no start date and end date are provided, just remove the monthly distribution
            allocatedActor.clearAllocations();
        }
    }

}
