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
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.services.account.IPreferenceManagerPlugin;
import framework.utils.Utilities;
import models.finance.*;
import play.Play;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
import services.budgettracking.IBudgetTrackingService;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A portfolio entry resource plan allocated org unit form data is used to
 * manage the fields when adding/editing an allocated org unit for a resource
 * plan.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedOrgUnitFormData extends ResourceAllocationFormData {

    IBudgetTrackingService budgetTrackingService;

    IPreferenceManagerPlugin preferenceManager;

    @Required
    public Long orgUnit;

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
    public PortfolioEntryResourcePlanAllocatedOrgUnitFormData() {
        super();
        this.budgetTrackingService = Play.application().injector().instanceOf(IBudgetTrackingService.class);
        this.preferenceManager = Play.application().injector().instanceOf(IPreferenceManagerPlugin.class);
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param allocatedOrgUnit
     *            the allocated org unit in the DB
     */
    public PortfolioEntryResourcePlanAllocatedOrgUnitFormData(PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit) {

        this.id = allocatedOrgUnit.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.allocationId = allocatedOrgUnit.id;

        this.orgUnit = allocatedOrgUnit.orgUnit.id;
        this.startDate = allocatedOrgUnit.startDate != null ? Utilities.getDateFormat(null).format(allocatedOrgUnit.startDate) : null;
        this.endDate = allocatedOrgUnit.endDate != null ? Utilities.getDateFormat(null).format(allocatedOrgUnit.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedOrgUnit.portfolioEntryPlanningPackage != null ? allocatedOrgUnit.portfolioEntryPlanningPackage.id
                : null;
        this.allocationStatus = allocatedOrgUnit.portfolioEntryResourcePlanAllocationStatusType.status.name();
        this.lastStatusTypeUpdateActor = allocatedOrgUnit.lastStatusTypeUpdateActor != null ? allocatedOrgUnit.lastStatusTypeUpdateActor.id : 0L;
        this.lastStatusTypeUpdateTime = allocatedOrgUnit.lastStatusTypeUpdateTime != null ? Utilities.getDateFormat(null).format(allocatedOrgUnit.lastStatusTypeUpdateTime) : null;

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
     * Fill the DB entry with the form values.
     * 
     * @param allocatedOrgUnit
     *            the allocated org unit in the DB
     */
    public void fill(PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit) {

        allocatedOrgUnit.orgUnit = OrgUnitDao.getOrgUnitById(this.orgUnit);

        allocatedOrgUnit.portfolioEntryPlanningPackage = this.portfolioEntryPlanningPackage != null
                ? PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(this.portfolioEntryPlanningPackage) : null;

        allocatedOrgUnit.followPackageDates = allocatedOrgUnit.portfolioEntryPlanningPackage != null ? this.followPackageDates : null;

        if (allocatedOrgUnit.followPackageDates == null || !allocatedOrgUnit.followPackageDates) {
            try {
                allocatedOrgUnit.startDate = Utilities.getDateFormat(null).parse(this.startDate);
            } catch (ParseException e) {
                allocatedOrgUnit.startDate = null;
            }

            try {
                allocatedOrgUnit.endDate = Utilities.getDateFormat(null).parse(this.endDate);
            } catch (ParseException e) {
                allocatedOrgUnit.endDate = null;
            }
        } else {
            allocatedOrgUnit.startDate = allocatedOrgUnit.portfolioEntryPlanningPackage.startDate;
            allocatedOrgUnit.endDate = allocatedOrgUnit.portfolioEntryPlanningPackage.endDate;
        }

        allocatedOrgUnit.portfolioEntryResourcePlanAllocationStatusType = PortfolioEntryResourcePlanDAO.getAllocationStatusByType(PortfolioEntryResourcePlanAllocationStatusType.AllocationStatus.DRAFT);

        allocatedOrgUnit.currency = CurrencyDAO.getCurrencyByCode(this.currencyCode);
        allocatedOrgUnit.currencyRate = this.currencyRate;

        allocatedOrgUnit.days = this.days;
        allocatedOrgUnit.dailyRate = this.dailyRate;
        allocatedOrgUnit.forecastDays = this.forecastDays;
        allocatedOrgUnit.forecastDailyRate = this.forecastDailyRate;

        allocatedOrgUnit.monthlyAllocated = this.monthlyAllocated;

        if (this.monthlyAllocated) {

            // Set allocations by month
            List<PortfolioEntryResourcePlanAllocatedOrgUnitDetail> details = new ArrayList<>();
            for (MonthAllocation monthAllocation : monthAllocations) {
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 0, monthAllocation.januaryAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 1, monthAllocation.februaryAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 2, monthAllocation.marchAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 3, monthAllocation.aprilAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 4, monthAllocation.mayAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 5, monthAllocation.juneAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 6, monthAllocation.julyAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 7, monthAllocation.augustAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 8, monthAllocation.septemberAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 9, monthAllocation.octoberAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 10, monthAllocation.novemberAllocationValue));
                details.add((PortfolioEntryResourcePlanAllocatedOrgUnitDetail) processMonthAllocation(allocatedOrgUnit, monthAllocation.year, 11, monthAllocation.decemberAllocationValue));
            }

            allocatedOrgUnit.portfolioEntryResourcePlanAllocatedOrgUnitDetails.clear();
            allocatedOrgUnit.portfolioEntryResourcePlanAllocatedOrgUnitDetails.addAll(details.stream().filter(detail -> detail != null).collect(Collectors.toList()));

            // Set start date and end date
            Comparator<? super PortfolioEntryResourcePlanAllocatedOrgUnitDetail> comp = (d1, d2) -> {
                int c = Integer.compare(d1.year, d2.year);
                return c == 0 ? Integer.compare(d1.month, d2.month) : c;
            };

            PortfolioEntryResourcePlanAllocatedOrgUnitDetail startMonth = allocatedOrgUnit.portfolioEntryResourcePlanAllocatedOrgUnitDetails.stream().min(comp).get();
            PortfolioEntryResourcePlanAllocatedOrgUnitDetail endMonth = allocatedOrgUnit.portfolioEntryResourcePlanAllocatedOrgUnitDetails.stream().max(comp).get();

            Calendar c = GregorianCalendar.getInstance();
            c.set(startMonth.year, startMonth.month, 1);
            allocatedOrgUnit.startDate = c.getTime();

            c.set(endMonth.year, endMonth.month, 1);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            allocatedOrgUnit.endDate = c.getTime();

        } else if (allocatedOrgUnit.startDate != null && allocatedOrgUnit.endDate != null) { // If start and end dates are provided, distribute evenly the days across the months
            allocatedOrgUnit.computeAllocationDetails(budgetTrackingService.isActive(), preferenceManager.getPreferenceValueAsBoolean(IMafConstants.RESOURCES_WEEK_DAYS_ALLOCATION_PREFERENCE));
        } else { // If no manual allocation and no start date and end date are provided, just remove the monthly distribution
            allocatedOrgUnit.clearAllocations();
        }
    }

}
