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
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.utils.Utilities;
import models.finance.WorkOrder;
import models.framework_models.parent.IModelConstants;
import play.Logger;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.i18n.Messages;

/**
 * A work order form data is used to manage the fields when adding/editing a
 * work order for a portfolio entry.
 * 
 * @author Johann Kohler
 */
public class WorkOrderFormData {

    // the portfolioEntry id
    public Long id;

    public boolean fromResource;

    public Long workOrderId;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @MaxLength(value = IModelConstants.VLARGE_STRING)
    public String description;

    public Long portfolioEntryPlanningPackage;

    public String startDate;

    public String dueDate;

    @Required
    public String currencyCode;

    @Required
    public BigDecimal currencyRate;

    @Required
    public BigDecimal amount;

    public boolean isOpex;

    public boolean shared;

    public BigDecimal amountReceived;

    public boolean followPackageDates;

    /**
     * Default constructor.
     */
    public WorkOrderFormData() {
    }

    /**
     * Validate the dates.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        if (this.startDate != null && this.dueDate != null) {

            try {

                if (!this.startDate.equals("") && !this.dueDate.equals("")
                        && Utilities.getDateFormat(null).parse(this.startDate).after(Utilities.getDateFormat(null).parse(this.dueDate))) {
                    // the due date should be after the start date
                    errors.add(new ValidationError("dueDate", Messages.get("object.work_order.due_date.invalid")));
                }

            } catch (Exception e) {
                Logger.warn("impossible to parse the dates when testing the formats");
            }
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param workOrder
     *            the work order in the DB
     */
    public WorkOrderFormData(WorkOrder workOrder) {

        this.id = workOrder.portfolioEntry.id;
        this.workOrderId = workOrder.id;

        this.name = workOrder.name;
        this.description = workOrder.description;
        this.startDate = workOrder.startDate != null ? Utilities.getDateFormat(null).format(workOrder.startDate) : null;
        this.dueDate = workOrder.dueDate != null ? Utilities.getDateFormat(null).format(workOrder.dueDate) : null;
        this.currencyCode = workOrder.currency != null ? workOrder.currency.code : null;
        this.currencyRate = workOrder.currencyRate;
        this.amount = workOrder.amount;
        this.isOpex = workOrder.isOpex != null ? workOrder.isOpex : false;
        this.shared = workOrder.shared != null ? workOrder.shared : false;
        this.amountReceived = workOrder.amountReceived;
        this.portfolioEntryPlanningPackage = workOrder.portfolioEntryPlanningPackage != null ? workOrder.portfolioEntryPlanningPackage.id : null;
        this.followPackageDates = workOrder.followPackageDates != null ? workOrder.followPackageDates : false;
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param workOrder
     *            the work order in the DB
     */
    public void fill(WorkOrder workOrder) {

        workOrder.name = this.name;
        workOrder.description = this.description;

        workOrder.portfolioEntryPlanningPackage = this.portfolioEntryPlanningPackage != null
                ? PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(this.portfolioEntryPlanningPackage) : null;
        workOrder.followPackageDates = workOrder.portfolioEntryPlanningPackage != null ? this.followPackageDates : null;

        if (workOrder.followPackageDates == null || workOrder.followPackageDates == false) {
            try {
                workOrder.startDate = Utilities.getDateFormat(null).parse(this.startDate);
            } catch (ParseException e) {
                workOrder.startDate = null;
            }

            try {
                workOrder.dueDate = Utilities.getDateFormat(null).parse(this.dueDate);
            } catch (ParseException e) {
                workOrder.dueDate = null;
            }
        } else {
            workOrder.startDate = workOrder.portfolioEntryPlanningPackage.startDate;
            workOrder.dueDate = workOrder.portfolioEntryPlanningPackage.endDate;
        }

        workOrder.currency = CurrencyDAO.getCurrencyByCode(this.currencyCode);
        workOrder.currencyRate = this.currencyRate;
        workOrder.amount = this.amount;
        workOrder.isOpex = this.isOpex;
        workOrder.shared = this.shared;
        workOrder.amountReceived = this.amountReceived;

    }

}
