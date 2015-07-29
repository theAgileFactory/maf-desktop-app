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
package utils.form;

import java.util.ArrayList;
import java.util.List;

import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import dao.pmo.PortfolioDao;

/**
 * An portfolioEntry portfolios form is used to manage the portfolios of a
 * portfolioEntry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPortfoliosFormData {

    public Long id;

    public List<Long> portfolios = new ArrayList<Long>();

    /**
     * Default constructor.
     */
    public PortfolioEntryPortfoliosFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntry
     *            the portfolio entry in the DB
     */
    public PortfolioEntryPortfoliosFormData(PortfolioEntry portfolioEntry) {

        this.id = portfolioEntry.id;

        if (portfolioEntry.portfolios != null) {
            for (Portfolio portfolio : portfolioEntry.portfolios) {
                this.portfolios.add(portfolio.id);
            }
        }

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntry
     *            the portfolio entry in the DB
     */
    public void fill(PortfolioEntry portfolioEntry) {

        portfolioEntry.portfolios = new ArrayList<Portfolio>();
        for (Long portfolio : this.portfolios) {
            if (portfolio != null) {
                portfolioEntry.portfolios.add(PortfolioDao.getPortfolioById(portfolio));
            }
        }

    }

}
