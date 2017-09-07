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
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.utils.Utilities;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.finance.PortfolioEntryResourcePlanAllocationStatusType;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.i18n.Messages;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * A portfolio entry resource plan allocated org unit form data is used to
 * manage the fields when adding/editing an allocated org unit for a resource
 * plan.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedOrgUnitFormData {

    // the portfolioEntry id
    public Long id;

    public Long allocatedOrgUnitId;

    @Required
    public Long orgUnit;

    public String startDate;

    public String endDate;

    public Long portfolioEntryPlanningPackage;

    public String allocationStatus;

    public Long lastStatusTypeUpdateActor = 0L;

    public String lastStatusTypeUpdateTime;

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
    public PortfolioEntryResourcePlanAllocatedOrgUnitFormData() {
        this.allocationStatus = PortfolioEntryResourcePlanAllocationStatusType.AllocationStatus.DRAFT.name();
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param allocatedOrgUnit
     *            the allocated org unit in the DB
     */
    public PortfolioEntryResourcePlanAllocatedOrgUnitFormData(PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit) {

        this.id = allocatedOrgUnit.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.allocatedOrgUnitId = allocatedOrgUnit.id;

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

    }
}
