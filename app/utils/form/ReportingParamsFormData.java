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

import models.reporting.Reporting;
import models.reporting.Reporting.Format;
import play.data.validation.Constraints.Required;
import framework.utils.LanguageUtil;

/**
 * An reporting params form data is used to configure the parameters of a report
 * in order to generate it.
 * 
 * @author Johann Kohler
 */
public class ReportingParamsFormData {

    public Long id;

    @Required
    public String language;

    @Required
    public String format;

    /**
     * Default constructor.
     */
    public ReportingParamsFormData() {
    }

    /**
     * Construct a reporting params form data with a DB entry.
     * 
     * @param report
     *            a reporting
     */
    public ReportingParamsFormData(Reporting report) {

        this.id = report.id;
        this.language = LanguageUtil.getCurrent().getCode();
        this.format = Format.PDF.name();

    }

}
