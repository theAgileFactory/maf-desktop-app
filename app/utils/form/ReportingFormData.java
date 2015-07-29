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

import java.util.ArrayList;
import java.util.List;

import models.framework_models.account.Principal;
import models.reporting.Reporting;
import dao.reporting.ReportingDao;

/**
 * An reporting form data is used to manage the fields when editing a report.
 * 
 * @author Johann Kohler
 */
public class ReportingFormData {

    public Long id;

    public boolean isActive;

    public boolean isPublic;

    public Long category;

    public List<String> principals = new ArrayList<String>();

    /**
     * Default constructor.
     */
    public ReportingFormData() {
    }

    /**
     * Construct a reporting form data with a DB entry.
     * 
     * @param report
     *            a reporting
     */
    public ReportingFormData(Reporting report) {

        this.id = report.id;
        this.isActive = report.isActive;
        this.isPublic = report.isPublic;
        this.category = report.reportingCategory.id;
        if (report.reportingAuthorization != null && report.reportingAuthorization.principals != null) {
            for (Principal principal : report.reportingAuthorization.principals) {
                this.principals.add(principal.uid);
            }
        }

    }

    /**
     * Fill a reporting with the form values.
     * 
     * @param report
     *            the reporting to fill
     */
    public void fill(Reporting report) {

        report.isActive = this.isActive;
        report.isPublic = this.isPublic;
        report.reportingCategory = ReportingDao.getReportingCategoryById(this.category);
        report.reportingAuthorization.principals = new ArrayList<Principal>();
        for (String principal : this.principals) {
            report.reportingAuthorization.principals.add(Principal.getPrincipalFromUid(principal));
        }

    }

}
