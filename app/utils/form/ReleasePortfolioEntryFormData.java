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

import models.delivery.Release;
import models.delivery.ReleasePortfolioEntry;
import models.delivery.ReleasePortfolioEntry.Type;
import models.pmo.PortfolioEntry;
import play.data.validation.Constraints.Required;
import dao.delivery.ReleaseDAO;
import dao.pmo.PortfolioEntryDao;

/**
 * An KPI value definition form data is used to manage the fields when managing
 * a KPI value definition.
 * 
 * @author Johann Kohler
 */
public class ReleasePortfolioEntryFormData {

    // the portfolio entry id
    public Long id;

    @Required
    public Long release;

    @Required
    public String type;

    /**
     * Default constructor.
     */
    public ReleasePortfolioEntryFormData() {
        this.type = ReleasePortfolioEntry.Type.NONE.name();
    }

    /**
     * Construct with a portfolio entry id.
     * 
     * @param portfolioEntryId
     *            the portfolioEntryId
     */
    public ReleasePortfolioEntryFormData(Long portfolioEntryId) {
        super();
        this.id = portfolioEntryId;
    }

    /**
     * Get the association between the release and the portfolio entry.
     */
    public ReleasePortfolioEntry get() {

        Release release = ReleaseDAO.getReleaseById(this.release);
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(this.id);

        ReleasePortfolioEntry releasePortfolioEntry = new ReleasePortfolioEntry(release, portfolioEntry);

        releasePortfolioEntry.type = Type.valueOf(this.type);

        return releasePortfolioEntry;

    }
}
