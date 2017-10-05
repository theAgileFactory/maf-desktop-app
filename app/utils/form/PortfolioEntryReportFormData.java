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

import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntryReport;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import dao.pmo.PortfolioEntryReportDao;
import framework.utils.FileField;
import framework.utils.FileFieldOptionalValidator;

/**
 * A portfolio entry report form data is used to manage the fields when
 * adding/editing a portfolio entry report.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryReportFormData {

    // the portfolioEntry id
    public Long id;

    public Long reportId;

    @Required
    public Long status;

    @Required
    @MaxLength(value = IModelConstants.XXLARGE_STRING)
    public String comments;

    @ValidateWith(value = FileFieldOptionalValidator.class, message = "form.input.file_field.error")
    public FileField document;

    /**
     * Default constructor.
     */
    public PortfolioEntryReportFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryReport
     *            the portfolio entry report in the DB
     */
    public PortfolioEntryReportFormData(PortfolioEntryReport portfolioEntryReport) {

        this.id = portfolioEntryReport.portfolioEntry.id;
        this.reportId = portfolioEntryReport.id;
        this.status = portfolioEntryReport.portfolioEntryReportStatusType.id;
        this.comments = portfolioEntryReport.comments;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryReport
     *            the portfolio entry report in the DB
     */
    public void fill(PortfolioEntryReport portfolioEntryReport) {
        portfolioEntryReport.portfolioEntryReportStatusType = PortfolioEntryReportDao.getPEReportStatusTypeById(status);
        portfolioEntryReport.comments = this.comments;
    }

}
