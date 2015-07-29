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

import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import dao.pmo.PortfolioDao;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.PickerHandler;

/**
 * Picker for {@link Portfolio} and {@link PortfolioEntry} objects.
 * 
 * @author Pierre-Yves Cloux
 */
public class PortfolioPicker {

    /**
     * Picker handler for a single value picker of portfolios.
     */
    public static PickerHandler<Long> portfolioPickerHandler = new PickerHandler<Long>(Long.class, new PickerHandler.Handle<Long>() {
        @Override
        public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
            return PortfolioDao.getPortfolioActiveAsVH();
        }
    });
}
