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
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import models.finance.PortfolioEntryResourcePlanAllocationStatusType;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;

import java.math.BigDecimal;
import java.text.ParseException;

/**
 * A portfolio entry resource plan allocated competency form data is used to
 * manage the fields when adding/editing an allocated competency for a resource
 * plan.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedCompetencyFormData extends ResourceAllocationFormData {

    @Required
    public Long competency;

    @Constraints.Required
    public String currencyCode;

    @Constraints.Required
    public BigDecimal currencyRate;

    @Constraints.Required
    public BigDecimal dailyRate;

    /**
     * Default constructor.
     */
    public PortfolioEntryResourcePlanAllocatedCompetencyFormData() {
        super();
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param allocatedCompetency
     *            the allocated competency in the DB
     */
    public PortfolioEntryResourcePlanAllocatedCompetencyFormData(PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency) {

        this.id = allocatedCompetency.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.allocationId = allocatedCompetency.id;

        this.competency = allocatedCompetency.competency.id;
        this.startDate = allocatedCompetency.startDate != null ? Utilities.getDateFormat(null).format(allocatedCompetency.startDate) : null;
        this.endDate = allocatedCompetency.endDate != null ? Utilities.getDateFormat(null).format(allocatedCompetency.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedCompetency.portfolioEntryPlanningPackage != null ? allocatedCompetency.portfolioEntryPlanningPackage.id
                : null;
        this.allocationStatus = allocatedCompetency.portfolioEntryResourcePlanAllocationStatusType.status.name();
        this.lastStatusTypeUpdateActor = allocatedCompetency.lastStatusTypeUpdateActor != null ? allocatedCompetency.lastStatusTypeUpdateActor.id : 0L;
        this.lastStatusTypeUpdateTime = allocatedCompetency.lastStatusTypeUpdateTime != null ? Utilities.getDateFormat(null).format(allocatedCompetency.lastStatusTypeUpdateTime) : null;

        this.currencyCode = allocatedCompetency.currency != null ? allocatedCompetency.currency.code : null;
        this.currencyRate = allocatedCompetency.currencyRate;

        this.days = allocatedCompetency.days;
        this.dailyRate = allocatedCompetency.dailyRate;

        this.followPackageDates = allocatedCompetency.followPackageDates != null ? allocatedCompetency.followPackageDates : false;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param allocatedCompetency
     *            the allocated competency in the DB
     */
    public void fill(PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency) {

        allocatedCompetency.competency = ActorDao.getCompetencyById(this.competency);

        allocatedCompetency.portfolioEntryPlanningPackage = this.portfolioEntryPlanningPackage != null
                ? PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(this.portfolioEntryPlanningPackage) : null;

        allocatedCompetency.followPackageDates = allocatedCompetency.portfolioEntryPlanningPackage != null ? this.followPackageDates : null;

        if (allocatedCompetency.followPackageDates == null || !allocatedCompetency.followPackageDates) {
            try {
                allocatedCompetency.startDate = Utilities.getDateFormat(null).parse(this.startDate);
            } catch (ParseException e) {
                allocatedCompetency.startDate = null;
            }

            try {
                allocatedCompetency.endDate = Utilities.getDateFormat(null).parse(this.endDate);
            } catch (ParseException e) {
                allocatedCompetency.endDate = null;
            }
        } else {
            allocatedCompetency.startDate = allocatedCompetency.portfolioEntryPlanningPackage.startDate;
            allocatedCompetency.endDate = allocatedCompetency.portfolioEntryPlanningPackage.endDate;
        }

        allocatedCompetency.portfolioEntryResourcePlanAllocationStatusType = PortfolioEntryResourcePlanDAO.getAllocationStatusByType(PortfolioEntryResourcePlanAllocationStatusType.AllocationStatus.DRAFT);

        allocatedCompetency.currency = CurrencyDAO.getCurrencyByCode(this.currencyCode);
        allocatedCompetency.currencyRate = this.currencyRate;

        allocatedCompetency.days = this.days;
        allocatedCompetency.dailyRate = this.dailyRate;

    }
}
