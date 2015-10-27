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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.utils.FileField;
import framework.utils.FileFieldOptionalValidator;
import framework.utils.Msg;
import framework.utils.Utilities;
import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntryPlanningPackage;
import models.pmo.PortfolioEntryPlanningPackage.Status;
import play.Logger;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import play.data.validation.ValidationError;

/**
 * An portfolio entry planning package form data is used to manage the fields
 * when adding/editing a portfolio entry planning package.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPlanningPackageFormData {

    // the portfolioEntry id
    public Long id;

    public Long planningPackageId;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @MaxLength(value = IModelConstants.VLARGE_STRING)
    public String description;

    public String startDate;

    public String endDate;

    @Required
    public Long portfolioEntryPlanningPackageType;

    public boolean isImportant;

    public Long portfolioEntryPlanningPackageGroup;

    @Required
    public String status;

    @ValidateWith(value = FileFieldOptionalValidator.class, message = "form.input.file_field.error")
    public FileField document;

    /**
     * Form validation.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        // check the dates
        if (!this.startDate.equals("") && this.endDate.equals("")) {
            // the start date cannot be filled alone
            errors.add(new ValidationError("startDate", Msg.get("object.portfolio_entry_planning_package.start_date.invalid")));
        }
        if (!this.startDate.equals("") && !this.endDate.equals("")) {
            // the end date should be after the start date
            try {
                if (Utilities.getDateFormat(null).parse(this.startDate).after(Utilities.getDateFormat(null).parse(this.endDate))) {
                    errors.add(new ValidationError("endDate", Msg.get("object.portfolio_entry_planning_package.end_date.invalid")));
                }
            } catch (ParseException e) {
                Logger.error("Impossible to parse the planning package date when validate them", e);
            }
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Default constructor.
     */
    public PortfolioEntryPlanningPackageFormData() {
        status = Status.NOT_STARTED.name();
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryPlanningPackage
     *            the portfolio entry planning package in the DB
     */
    public PortfolioEntryPlanningPackageFormData(PortfolioEntryPlanningPackage portfolioEntryPlanningPackage) {

        this.id = portfolioEntryPlanningPackage.portfolioEntry.id;
        this.planningPackageId = portfolioEntryPlanningPackage.id;

        this.name = portfolioEntryPlanningPackage.name;
        this.description = portfolioEntryPlanningPackage.description;
        this.startDate = portfolioEntryPlanningPackage.startDate != null ? Utilities.getDateFormat(null).format(portfolioEntryPlanningPackage.startDate)
                : null;
        this.endDate = portfolioEntryPlanningPackage.endDate != null ? Utilities.getDateFormat(null).format(portfolioEntryPlanningPackage.endDate) : null;
        this.portfolioEntryPlanningPackageType = portfolioEntryPlanningPackage.portfolioEntryPlanningPackageType.id;
        this.isImportant = portfolioEntryPlanningPackage.isImportant;
        this.portfolioEntryPlanningPackageGroup = portfolioEntryPlanningPackage.portfolioEntryPlanningPackageGroup != null
                ? portfolioEntryPlanningPackage.portfolioEntryPlanningPackageGroup.id : null;
        this.status = portfolioEntryPlanningPackage.status.name();

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryPlanningPackage
     *            the portfolio entry planning package in the DB
     */
    public void fill(PortfolioEntryPlanningPackage portfolioEntryPlanningPackage) {

        portfolioEntryPlanningPackage.name = this.name;
        portfolioEntryPlanningPackage.description = this.description;

        try {
            portfolioEntryPlanningPackage.startDate = Utilities.getDateFormat(null).parse(this.startDate);
        } catch (ParseException e) {
            portfolioEntryPlanningPackage.startDate = null;
        }

        try {
            portfolioEntryPlanningPackage.endDate = Utilities.getDateFormat(null).parse(this.endDate);
        } catch (ParseException e) {
            portfolioEntryPlanningPackage.endDate = null;
        }

        portfolioEntryPlanningPackage.portfolioEntryPlanningPackageType = PortfolioEntryPlanningPackageDao
                .getPEPlanningPackageTypeById(this.portfolioEntryPlanningPackageType);

        portfolioEntryPlanningPackage.isImportant = this.isImportant;

        portfolioEntryPlanningPackage.portfolioEntryPlanningPackageGroup = this.portfolioEntryPlanningPackageGroup != null
                ? PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupById(this.portfolioEntryPlanningPackageGroup) : null;

        portfolioEntryPlanningPackage.status = Status.valueOf(this.status);

    }
}
