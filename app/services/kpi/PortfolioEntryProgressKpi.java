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

import constants.IMafConstants;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.pmo.PortfolioEntryDao;
import dao.timesheet.TimesheetDao;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.kpi.IKpiRunner;
import framework.services.kpi.Kpi;
import framework.services.script.IScriptService;
import models.framework_models.kpi.KpiData;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryPlanningPackage;

/**
 * The "Portfolio entry progress" KPI computation class.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryProgressKpi implements IKpiRunner {

    @Override
    public BigDecimal computeMain(IPreferenceManagerPlugin preferenceManagerPlugin, IScriptService scriptService, Kpi kpi, Long objectId) {
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(objectId);

        BigDecimal numerator = BigDecimal.ZERO;
        BigDecimal denominator = BigDecimal.ZERO;
        BigDecimal value = new BigDecimal(100);

        for (PortfolioEntryPlanningPackage planningPackage : portfolioEntry.planningPackages) {

            BigDecimal days = PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsDaysByPlanningPackage(planningPackage)
                    .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitAsDaysByPlanningPackage(planningPackage))
                    .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedCompetencyAsDaysByPlanningPackage(planningPackage));

            denominator = denominator.add(days);

            switch (planningPackage.status) {
            case CLOSED:
                numerator = numerator.add(days);
                break;
            case ON_GOING:
                numerator = numerator.add(days.multiply(getOnGoingRate(preferenceManagerPlugin)));
                break;
            default:
                break;
            }

        }

        if (!denominator.equals(BigDecimal.ZERO)) {
            value = numerator.divide(denominator, RoundingMode.HALF_UP).multiply(value);
        }

        return value;
    }

    @Override
    public BigDecimal computeAdditional1(IPreferenceManagerPlugin preferenceManagerPlugin, IScriptService scriptService, Kpi kpi, Long objectId) {

        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(objectId);

        BigDecimal numerator = BigDecimal.ZERO;

        for (PortfolioEntryPlanningPackage planningPackage : portfolioEntry.planningPackages) {

            BigDecimal days = PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsDaysByPlanningPackage(planningPackage)
                    .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitAsDaysByPlanningPackage(planningPackage))
                    .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedCompetencyAsDaysByPlanningPackage(planningPackage));

            switch (planningPackage.status) {
            case CLOSED:
                numerator = numerator.add(days);
                break;
            case ON_GOING:
                numerator = numerator.add(days.multiply(getOnGoingRate(preferenceManagerPlugin)));
                break;
            default:
                break;
            }

        }

        return numerator;
    }

    @Override
    public BigDecimal computeAdditional2(IPreferenceManagerPlugin preferenceManagerPlugin, IScriptService scriptService, Kpi kpi, Long objectId) {
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(objectId);

        BigDecimal value = BigDecimal.ZERO;

        for (PortfolioEntryPlanningPackage planningPackage : portfolioEntry.planningPackages) {
            value = value.add(TimesheetDao.getTimesheetLogAsTotalHoursByPEPlanningPackage(planningPackage));
        }

        return value.divide(TimesheetDao.getTimesheetReportHoursPerDay(preferenceManagerPlugin), RoundingMode.HALF_UP);
    }

    @Override
    public String link(Long objectId) {
        return controllers.core.routes.PortfolioEntryPlanningController.packages(objectId).url();
    }

    /**
     * Get the on going fulfillment rate.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     */
    private BigDecimal getOnGoingRate(IPreferenceManagerPlugin preferenceManagerPlugin) {
        Integer percentage = preferenceManagerPlugin.getPreferenceValueAsInteger(IMafConstants.PACKAGE_STATUS_ON_GOING_FULFILLMENT_PERCENTAGE_PREFERENCE);
        return new BigDecimal(percentage / 100.0);
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
