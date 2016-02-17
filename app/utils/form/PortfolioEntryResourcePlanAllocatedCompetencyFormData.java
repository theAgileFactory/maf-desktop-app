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

import dao.finance.CurrencyDAO;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.utils.Utilities;
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.i18n.Messages;

/**
 * A portfolio entry resource plan allocated competency form data is used to
 * manage the fields when adding/editing an allocated competency for a resource
 * plan.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedCompetencyFormData {

    // the portfolioEntry id
    public Long id;

    public Long allocatedCompetencyId;

    @Required
    public Long competency;

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
    public PortfolioEntryResourcePlanAllocatedCompetencyFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param allocatedCompetency
     *            the allocated competency in the DB
     */
    public PortfolioEntryResourcePlanAllocatedCompetencyFormData(PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency) {

        this.id = allocatedCompetency.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.allocatedCompetencyId = allocatedCompetency.id;

        this.competency = allocatedCompetency.competency.id;
        this.startDate = allocatedCompetency.startDate != null ? Utilities.getDateFormat(null).format(allocatedCompetency.startDate) : null;
        this.endDate = allocatedCompetency.endDate != null ? Utilities.getDateFormat(null).format(allocatedCompetency.endDate) : null;
        this.portfolioEntryPlanningPackage = allocatedCompetency.portfolioEntryPlanningPackage != null ? allocatedCompetency.portfolioEntryPlanningPackage.id
                : null;

        this.isConfirmed = allocatedCompetency.isConfirmed;

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

        if (allocatedCompetency.followPackageDates == null || allocatedCompetency.followPackageDates == false) {
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

        allocatedCompetency.isConfirmed = this.isConfirmed;

        allocatedCompetency.currency = CurrencyDAO.getCurrencyByCode(this.currencyCode);
        allocatedCompetency.currencyRate = this.currencyRate;

        allocatedCompetency.days = this.days;
        allocatedCompetency.dailyRate = this.dailyRate;

    }
}
