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

import models.pmo.PortfolioEntryReportStatusType;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.utils.MultiLanguagesString;
import framework.utils.MultiLanguagesStringValidator;

/**
 * A portfolio entry report status type form data is used to manage the fields
 * when adding/editing a portfolio entry report status type.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryReportStatusTypeFormData {

    public Long id;

    public boolean selectable;

    @Required
    @ValidateWith(value = MultiLanguagesStringValidator.class, message = "form.input.multi_languages_string.required.error")
    public MultiLanguagesString name;

    public MultiLanguagesString description;

    @Required
    public String cssClass;

    /**
     * Default constructor.
     */
    public PortfolioEntryReportStatusTypeFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryReportStatusType
     *            the portfolio entry report status type in the DB
     */
    public PortfolioEntryReportStatusTypeFormData(PortfolioEntryReportStatusType portfolioEntryReportStatusType) {

        this.id = portfolioEntryReportStatusType.id;
        this.selectable = portfolioEntryReportStatusType.selectable;
        this.name = MultiLanguagesString.getByKey(portfolioEntryReportStatusType.name);
        this.description = MultiLanguagesString.getByKey(portfolioEntryReportStatusType.description);
        this.cssClass = portfolioEntryReportStatusType.cssClass;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryReportStatusType
     *            the portfolio entry report status type in the DB
     */
    public void fill(PortfolioEntryReportStatusType portfolioEntryReportStatusType) {

        portfolioEntryReportStatusType.selectable = this.selectable;
        portfolioEntryReportStatusType.name = this.name.getKeyIfValue();
        portfolioEntryReportStatusType.description = this.description.getKeyIfValue();
        portfolioEntryReportStatusType.cssClass = this.cssClass;

    }
}
