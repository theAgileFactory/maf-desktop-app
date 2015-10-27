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

import models.pmo.PortfolioEntryPlanningPackage;

/**
 * Form to manage all planning packages.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPlanningPackagesFormData {

    // the portfolio entry id
    public Long id;

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

}
