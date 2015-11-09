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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import dao.delivery.ReleaseDAO;
import dao.pmo.PortfolioEntryDao;
import dao.timesheet.TimesheetDao;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.kpi.IKpiRunner;
import framework.services.kpi.Kpi;
import models.delivery.Release;
import models.delivery.Requirement;
import models.framework_models.kpi.KpiData;
import models.pmo.PortfolioEntry;

/**
 * The "Release burndown" KPI computation class.
 * 
 * @author Johann Kohler
 */
public class ReleaseBurndownKpi implements IKpiRunner {

    @Override
    public BigDecimal computeMain(IPreferenceManagerPlugin preferenceManagerPlugin, Kpi kpi, Long objectId) {

        List<Requirement> requirements = getRequirements(objectId);

        BigDecimal total = BigDecimal.ZERO;
        for (Requirement requirement : requirements) {
            if (requirement.remainingEffort != null) {
                total = total.add(new BigDecimal(requirement.remainingEffort));
            }
        }

        return convertInDays(total);

    }

    @Override
    public BigDecimal computeAdditional1(IPreferenceManagerPlugin preferenceManagerPlugin, Kpi kpi, Long objectId) {

        List<Requirement> requirements = getRequirements(objectId);

        BigDecimal total = BigDecimal.ZERO;
        for (Requirement requirement : requirements) {
            if (requirement.effort != null) {
                total = total.add(new BigDecimal(requirement.effort));
            }
        }

        return convertInDays(total);

    }

    @Override
    public BigDecimal computeAdditional2(IPreferenceManagerPlugin preferenceManagerPlugin, Kpi kpi, Long objectId) {

        List<Requirement> requirements = getRequirements(objectId);

        BigDecimal total = BigDecimal.ZERO;
        for (Requirement requirement : requirements) {
            if (requirement.initialEstimation != null) {
                total = total.add(new BigDecimal(requirement.initialEstimation));
            }
        }

        return convertInDays(total);

    }

    /**
     * Get the requirements of a release.
     * 
     * @param releadId
     *            the release id
     */
    private List<Requirement> getRequirements(Long releadId) {

        List<PortfolioEntry> portfolioEntries = PortfolioEntryDao.getPEAsListByRelease(releadId);

        List<Requirement> requirements = new ArrayList<>();

        for (PortfolioEntry portfolioEntry : portfolioEntries) {
            requirements.addAll(ReleaseDAO.getRequirementAsListByIdAndPE(releadId, portfolioEntry.id));
        }

        return requirements;
    }

    /**
     * Convert a number of hours to a number of days.
     * 
     * @param hours
     *            the number of hours
     */
    private BigDecimal convertInDays(BigDecimal hours) {
        return hours.setScale(2, BigDecimal.ROUND_HALF_UP).divide(TimesheetDao.getTimesheetReportHoursPerDay(), BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public String link(Long objectId) {
        return null;
    }

    @Override
    public Pair<Date, Date> getTrendPeriod(IPreferenceManagerPlugin preferenceManagerPlugin, Kpi kpi, Long objectId) {

        Release release = ReleaseDAO.getReleaseById(objectId);

        if (release.cutOffDate != null && release.endTestsDate != null) {

            Calendar startDateCal = Calendar.getInstance();
            startDateCal.setTime(release.cutOffDate);
            startDateCal.set(Calendar.HOUR_OF_DAY, 0);
            startDateCal.set(Calendar.MINUTE, 0);
            startDateCal.set(Calendar.SECOND, 0);
            startDateCal.set(Calendar.MILLISECOND, 0);

            Calendar endDateCal = Calendar.getInstance();
            endDateCal.setTime(release.endTestsDate);
            endDateCal.set(Calendar.HOUR_OF_DAY, 23);
            endDateCal.set(Calendar.MINUTE, 59);
            endDateCal.set(Calendar.SECOND, 59);
            endDateCal.set(Calendar.MILLISECOND, 999);

            return Pair.of(startDateCal.getTime(), endDateCal.getTime());

        }

        return null;
    }

    @Override
    public Pair<String, List<KpiData>> getStaticTrendLine(IPreferenceManagerPlugin preferenceManagerPlugin, Kpi kpi, Long objectId) {

        Release release = ReleaseDAO.getReleaseById(objectId);

        if (release.cutOffDate != null && release.endTestsDate != null && release.capacity != null) {

            List<KpiData> datas = new ArrayList<>();

            KpiData d1 = new KpiData();
            d1.timestamp = release.cutOffDate;
            d1.value = new BigDecimal(release.capacity);
            datas.add(d1);

            KpiData d2 = new KpiData();
            d2.timestamp = release.endTestsDate;
            d2.value = BigDecimal.ZERO;
            datas.add(d2);

            return Pair.of("kpi.release_burndown.static_trend_line.name", datas);

        }

        return null;

    }

}
