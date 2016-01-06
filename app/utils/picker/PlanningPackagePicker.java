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
package utils.picker;

import java.util.List;
import java.util.Map;

import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.PickerHandler;
import framework.utils.PickerHandler.Parameters;

/**
 * The pickers for the planning packages.
 * 
 * @author Johann Kohler
 */
public class PlanningPackagePicker {

    public static PickerHandler<Long> pickerTemplate = new PickerHandler<Long>(Long.class, new PickerHandler.Handle<Long>() {

        @Override
        public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
            defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
            return defaultParameters;
        }

        @Override
        public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
            Long portfolioEntryId = Long.valueOf(context.get("portfolioEntryId"));
            return PortfolioEntryPlanningPackageDao.getPEPlanningPackageAsVHByPE(portfolioEntryId, getDisplayExpenditureType(context));
        }

        @Override
        public ISelectableValueHolderCollection<Long> getFoundValueHolders(String searchString, Map<String, String> context) {
            Long portfolioEntryId = Long.valueOf(context.get("portfolioEntryId"));
            searchString = searchString.replaceAll("\\*", "%");
            return PortfolioEntryPlanningPackageDao.getPEPlanningPackageAsVHByKeywordsAndPE(searchString, portfolioEntryId,
                    getDisplayExpenditureType(context));
        }

        /**
         * Return true if the expenditure type should be displayed.
         * 
         * @param context
         *            the context
         */
        private boolean getDisplayExpenditureType(Map<String, String> context) {
            String displayExpenditureTypeAsString = context.get("displayExpenditureType");
            if (displayExpenditureTypeAsString == null || displayExpenditureTypeAsString.equals("")) {
                return false;
            } else {
                return Boolean.valueOf(displayExpenditureTypeAsString);
            }
        }

    });

}
