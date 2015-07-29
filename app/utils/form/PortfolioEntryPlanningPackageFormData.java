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

import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntryPlanningPackage;
import models.pmo.PortfolioEntryPlanningPackage.Status;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.utils.FileField;
import framework.utils.FileFieldOptionalValidator;
import framework.utils.Utilities;

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
    public String cssClass;

    public boolean isImportant;

    public Long portfolioEntryPlanningPackageGroup;

    @Required
    public String status;

    @ValidateWith(value = FileFieldOptionalValidator.class, message = "form.input.file_field.error")
    public FileField document;

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
        this.startDate =
                portfolioEntryPlanningPackage.startDate != null ? Utilities.getDateFormat(null).format(portfolioEntryPlanningPackage.startDate) : null;
        this.endDate = portfolioEntryPlanningPackage.endDate != null ? Utilities.getDateFormat(null).format(portfolioEntryPlanningPackage.endDate) : null;
        this.cssClass = portfolioEntryPlanningPackage.cssClass;
        this.isImportant = portfolioEntryPlanningPackage.isImportant;
        this.portfolioEntryPlanningPackageGroup =
                portfolioEntryPlanningPackage.portfolioEntryPlanningPackageGroup != null ? portfolioEntryPlanningPackage.portfolioEntryPlanningPackageGroup.id
                        : null;
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

        portfolioEntryPlanningPackage.cssClass = this.cssClass;

        portfolioEntryPlanningPackage.isImportant = this.isImportant;

        portfolioEntryPlanningPackage.portfolioEntryPlanningPackageGroup =
                this.portfolioEntryPlanningPackageGroup != null ? PortfolioEntryPlanningPackageDao
                        .getPEPlanningPackageGroupById(this.portfolioEntryPlanningPackageGroup) : null;

        portfolioEntryPlanningPackage.status = Status.valueOf(this.status);

    }
}
