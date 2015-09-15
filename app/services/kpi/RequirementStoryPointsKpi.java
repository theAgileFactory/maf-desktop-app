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

import dao.delivery.RequirementDAO;
import framework.services.kpi.IKpiRunner;
import framework.services.kpi.Kpi;
import models.framework_models.kpi.KpiData;

/**
 * The "Requirement story points" KPI computation class.
 * 
 * @author Johann Kohler
 */
public class RequirementStoryPointsKpi implements IKpiRunner {

    @Override
    public BigDecimal computeMain(Kpi kpi, Long objectId) {
        return new BigDecimal(RequirementDAO.getStoryPointsByPE(objectId, false));
    }

    @Override
    public BigDecimal computeAdditional1(Kpi kpi, Long objectId) {
        return new BigDecimal(RequirementDAO.getStoryPointsByPE(objectId, null));
    }

    @Override
    public BigDecimal computeAdditional2(Kpi kpi, Long objectId) {
        return new BigDecimal(RequirementDAO.getStoryPointsByPE(objectId, true));
    }

    @Override
    public String link(Long objectId) {
        return controllers.core.routes.PortfolioEntryDeliveryController.requirements(objectId, false).url();
    }

    @Override
    public Pair<Date, Date> getTrendPeriod(Kpi kpi, Long objectId) {
        return null;
    }

    @Override
    public Pair<String, List<KpiData>> getStaticTrendLine(Kpi kpi, Long objectId) {
        return null;
    }

}
