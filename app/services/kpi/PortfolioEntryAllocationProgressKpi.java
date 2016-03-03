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
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.pmo.PortfolioEntryDao;
import dao.timesheet.TimesheetDao;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.kpi.IKpiRunner;
import framework.services.kpi.Kpi;
import framework.services.script.IScriptService;
import models.framework_models.kpi.KpiData;
import models.pmo.PortfolioEntry;

/**
 * The "Portfolio entry allocation progress" KPI computation class.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryAllocationProgressKpi implements IKpiRunner {

    @Override
    public BigDecimal computeMain(IPreferenceManagerPlugin preferenceManagerPlugin, IScriptService scriptService, Kpi kpi, Long objectId) {

        BigDecimal timesheetedDays = computeAdditional1(preferenceManagerPlugin, scriptService, kpi, objectId).setScale(2, RoundingMode.HALF_UP);

        BigDecimal forecastDays = computeAdditional2(preferenceManagerPlugin, scriptService, kpi, objectId);

        if (forecastDays.compareTo(BigDecimal.ZERO) > 0) {
            return timesheetedDays.divide(forecastDays, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        }

        return null;

    }

    @Override
    public BigDecimal computeAdditional1(IPreferenceManagerPlugin preferenceManagerPlugin, IScriptService scriptService, Kpi kpi, Long objectId) {
        return convertInDays(TimesheetDao.getTimesheetLogAsTotalHoursByPE(objectId), preferenceManagerPlugin);
    }

    @Override
    public BigDecimal computeAdditional2(IPreferenceManagerPlugin preferenceManagerPlugin, IScriptService scriptService, Kpi kpi, Long objectId) {
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(objectId);
        return PortfolioEntryResourcePlanDAO.getPEResourcePlanAsForecastDaysByPE(portfolioEntry);
    }

    @Override
    public String link(Long objectId) {
        return controllers.core.routes.PortfolioEntryStatusReportingController.timesheets(objectId).url();
    }

    /**
     * Convert a number of hours to a number of days.
     * 
     * @param hours
     *            the number of hours
     * @param preferenceManagerPlugin
     *            the preference manager service
     */
    private BigDecimal convertInDays(BigDecimal hours, IPreferenceManagerPlugin preferenceManagerPlugin) {
        return hours.setScale(2, BigDecimal.ROUND_HALF_UP).divide(TimesheetDao.getTimesheetReportHoursPerDay(preferenceManagerPlugin),
                BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public Pair<Date, Date> getTrendPeriod(IPreferenceManagerPlugin preferenceManagerPlugin, IScriptService scriptService, Kpi kpi, Long objectId) {
        return null;
    }

    @Override
    public Pair<String, List<KpiData>> getStaticTrendLine(IPreferenceManagerPlugin preferenceManagerPlugin, IScriptService scriptService, Kpi kpi,
            Long objectId) {
        return null;
    }

}
