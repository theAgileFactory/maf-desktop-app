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

import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryPlanningPackage;
import constants.IMafConstants;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.pmo.PortfolioEntryDao;
import dao.timesheet.TimesheetDao;
import framework.services.ServiceStaticAccessor;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.kpi.IKpiRunner;
import framework.services.kpi.Kpi;

/**
 * The "Portfolio entry progress" KPI computation class.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryProgressKpi implements IKpiRunner {

    @Override
    public BigDecimal computeMain(Kpi kpi, Long objectId) {
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(objectId);

        BigDecimal numerator = BigDecimal.ZERO;
        BigDecimal denominator = BigDecimal.ZERO;
        BigDecimal value = new BigDecimal(100);

        for (PortfolioEntryPlanningPackage planningPackage : portfolioEntry.planningPackages) {

            BigDecimal days =
                    PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsDaysByPlanningPackage(planningPackage)
                            .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitAsDaysByPlanningPackage(planningPackage))
                            .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedCompetencyAsDaysByPlanningPackage(planningPackage));

            denominator = denominator.add(days);

            switch (planningPackage.status) {
            case CLOSED:
                numerator = numerator.add(days);
                break;
            case ON_GOING:
                numerator = numerator.add(days.multiply(getOnGoingRate()));
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
    public BigDecimal computeAdditional1(Kpi kpi, Long objectId) {

        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(objectId);

        BigDecimal numerator = BigDecimal.ZERO;

        for (PortfolioEntryPlanningPackage planningPackage : portfolioEntry.planningPackages) {

            BigDecimal days =
                    PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsDaysByPlanningPackage(planningPackage)
                            .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitAsDaysByPlanningPackage(planningPackage))
                            .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedCompetencyAsDaysByPlanningPackage(planningPackage));

            switch (planningPackage.status) {
            case CLOSED:
                numerator = numerator.add(days);
                break;
            case ON_GOING:
                numerator = numerator.add(days.multiply(getOnGoingRate()));
                break;
            default:
                break;
            }

        }

        return numerator;
    }

    @Override
    public BigDecimal computeAdditional2(Kpi kpi, Long objectId) {
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(objectId);

        BigDecimal value = BigDecimal.ZERO;

        for (PortfolioEntryPlanningPackage planningPackage : portfolioEntry.planningPackages) {
            value = value.add(TimesheetDao.getTimesheetLogAsTotalHoursByPEPlanningPackage(planningPackage));
        }

        return value.divide(TimesheetDao.getTimesheetReportHoursPerDay(), RoundingMode.HALF_UP);
    }

    @Override
    public String link(Long objectId) {
        return controllers.core.routes.PortfolioEntryPlanningController.packages(objectId, false).url();
    }

    /**
     * Get the on going fulfillment rate.
     */
    private BigDecimal getOnGoingRate() {
        Integer percentage =
                ServiceStaticAccessor.getPreferenceManagerPlugin().getPreferenceValueAsInteger(
                        IMafConstants.PACKAGE_STATUS_ON_GOING_FULFILLMENT_PERCENTAGE_PREFERENCE);
        return new BigDecimal(percentage / 100.0);
    }

}
