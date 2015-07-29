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

import models.pmo.PortfolioEntryType;
import dao.pmo.PortfolioEntryDao;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.PickerHandler;
import framework.utils.PickerHandler.Parameters;

/**
 * Picker for {@link PortfolioEntryType} objects.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryTypePicker {

    public static PickerHandler<Long> pickerTemplate = new PickerHandler<Long>(Long.class, new PickerHandler.Handle<Long>() {

        @Override
        public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
            defaultParameters.put(Parameters.SEARCH_ENABLED, "false");
            return defaultParameters;
        }

        @Override
        public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
            return PortfolioEntryDao.getPETypeActiveAsVH();
        }

    });

}
