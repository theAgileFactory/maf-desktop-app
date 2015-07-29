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
package services.kpi;

import java.math.BigDecimal;

import utils.finance.Totals;
import dao.pmo.PortfolioDao;
import framework.services.kpi.IKpiRunner;
import framework.services.kpi.Kpi;

/**
 * The "Deviation OPEX for a Portfolio" KPI computation class.
 * 
 * @author Johann Kohler
 */
public class PortfolioDeviationOpexKpi implements IKpiRunner {

    @Override
    public BigDecimal computeMain(Kpi kpi, Long objectId) {
        Totals totals =
                new Totals(PortfolioDao.getPortfolioAsBudgetAmountByOpex(objectId, true), 0.0, PortfolioDao.getPortfolioAsCostToCompleteAmountByOpex(
                        objectId, true), 0.0, PortfolioDao.getPortfolioAsEngagedAmountByOpex(objectId, true), 0.0);
        Double deviation = totals.getDeviationRate(true);
        if (deviation != null) {
            return new BigDecimal(totals.getDeviationRate(true));
        } else {
            return null;
        }
    }

    @Override
    public BigDecimal computeAdditional1(Kpi kpi, Long objectId) {
        return new BigDecimal(PortfolioDao.getPortfolioAsBudgetAmountByOpex(objectId, true));
    }

    @Override
    public BigDecimal computeAdditional2(Kpi kpi, Long objectId) {
        return new BigDecimal(PortfolioDao.getPortfolioAsCostToCompleteAmountByOpex(objectId, true)
                + PortfolioDao.getPortfolioAsEngagedAmountByOpex(objectId, true));
    }

    @Override
    public String link(Long objectId) {
        return null;
    }

}
