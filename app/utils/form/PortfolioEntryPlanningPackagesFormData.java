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

import javax.validation.Valid;

import framework.utils.Msg;
import framework.utils.Utilities;
import models.pmo.PortfolioEntryPlanningPackage;
import play.Logger;
import play.data.validation.ValidationError;

/**
 * Form to manage all planning packages.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPlanningPackagesFormData {

    // the portfolio entry id
    public Long id;

    @Valid
    public List<PortfolioEntryPlanningPackageFormData> planningPackagesFormData = new ArrayList<PortfolioEntryPlanningPackageFormData>();

    /**
     * Default constructor.
     */
    public PortfolioEntryPlanningPackagesFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryPlanningPackages
     *            the existing planning packages
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public PortfolioEntryPlanningPackagesFormData(List<PortfolioEntryPlanningPackage> portfolioEntryPlanningPackages, Long portfolioEntryId) {

        this.id = portfolioEntryId;

        for (PortfolioEntryPlanningPackage portfolioEntryPlanningPackage : portfolioEntryPlanningPackages) {
            planningPackagesFormData.add(new PortfolioEntryPlanningPackageFormData(portfolioEntryPlanningPackage));
        }

    }

    /**
     * Form validation.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        int i = 0;
        for (PortfolioEntryPlanningPackageFormData planningPackageFormData : planningPackagesFormData) {

            // check the dates
            if (!planningPackageFormData.startDate.equals("") && planningPackageFormData.endDate.equals("")) {
                // the start date cannot be filled alone
                errors.add(new ValidationError("planningPackagesFormData[" + i + "].startDate",
                        Msg.get("object.portfolio_entry_planning_package.start_date.invalid")));
            }
            if (!planningPackageFormData.startDate.equals("") && !planningPackageFormData.endDate.equals("")) {
                // the end date should be after the start date
                try {
                    if (Utilities.getDateFormat(null).parse(planningPackageFormData.startDate)
                            .after(Utilities.getDateFormat(null).parse(planningPackageFormData.endDate))) {
                        errors.add(new ValidationError("planningPackagesFormData[" + i + "].endDate",
                                Msg.get("object.portfolio_entry_planning_package.end_date.invalid")));
                    }
                } catch (ParseException e) {
                    Logger.error("Impossible to parse the planning package date when validate them", e);
                }
            }

            i++;

        }

        return errors.isEmpty() ? null : errors;

    }

}
