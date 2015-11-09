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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import dao.pmo.PortfolioEntryDao;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.kpi.IKpiRunner;
import framework.services.kpi.Kpi;
import models.framework_models.kpi.KpiData;
import utils.finance.Totals;

/**
 * The "Deviation CAPEX" KPI computation class.
 * 
 * @author Johann Kohler
 */
public class DeviationCapexKpi implements IKpiRunner {

    @Override
    public BigDecimal computeMain(IPreferenceManagerPlugin preferenceManagerPlugin, Kpi kpi, Long objectId) {
        Totals totals = new Totals(0.0, PortfolioEntryDao.getPEAsBudgetAmountByOpex(objectId, false), 0.0,
                PortfolioEntryDao.getPEAsCostToCompleteAmountByOpex(preferenceManagerPlugin, objectId, false), 0.0,
                PortfolioEntryDao.getPEAsEngagedAmountByOpex(preferenceManagerPlugin, objectId, false));
        Double deviation = totals.getDeviationRate(false);
        if (deviation != null) {
            return new BigDecimal(totals.getDeviationRate(false));
        } else {
            return null;
        }
    }

    @Override
    public BigDecimal computeAdditional1(IPreferenceManagerPlugin preferenceManagerPlugin, Kpi kpi, Long objectId) {
        return new BigDecimal(PortfolioEntryDao.getPEAsBudgetAmountByOpex(objectId, false));
    }

    @Override
    public BigDecimal computeAdditional2(IPreferenceManagerPlugin preferenceManagerPlugin, Kpi kpi, Long objectId) {
        return new BigDecimal(PortfolioEntryDao.getPEAsCostToCompleteAmountByOpex(preferenceManagerPlugin, objectId, false)
                + PortfolioEntryDao.getPEAsEngagedAmountByOpex(preferenceManagerPlugin, objectId, false));
    }

    @Override
    public String link(Long objectId) {
        return controllers.core.routes.PortfolioEntryFinancialController.status(objectId).url();
    }

    @Override
    public Pair<Date, Date> getTrendPeriod(IPreferenceManagerPlugin preferenceManagerPlugin, Kpi kpi, Long objectId) {
        return null;
    }

    @Override
    public Pair<String, List<KpiData>> getStaticTrendLine(IPreferenceManagerPlugin preferenceManagerPlugin, Kpi kpi, Long objectId) {
        return null;
    }

}
