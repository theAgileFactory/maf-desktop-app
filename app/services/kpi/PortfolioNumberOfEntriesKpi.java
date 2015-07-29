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

import dao.pmo.PortfolioEntryDao;
import framework.services.kpi.IKpiRunner;
import framework.services.kpi.Kpi;

/**
 * The "Number of entries for a Portfolio" KPI computation class.
 * 
 * @author Johann Kohler
 */
public class PortfolioNumberOfEntriesKpi implements IKpiRunner {

    @Override
    public BigDecimal computeMain(Kpi kpi, Long objectId) {
        return new BigDecimal(PortfolioEntryDao.getPEAsNbActiveByPortfolio(objectId));
    }

    @Override
    public BigDecimal computeAdditional1(Kpi kpi, Long objectId) {
        return new BigDecimal(PortfolioEntryDao.getPEAsNbActiveConceptualByPortfolio(objectId));
    }

    @Override
    public BigDecimal computeAdditional2(Kpi kpi, Long objectId) {
        return new BigDecimal(PortfolioEntryDao.getPEAsNbActiveNonConceptualByPortfolio(objectId));
    }

    @Override
    public String link(Long objectId) {
        return null;
    }

}
