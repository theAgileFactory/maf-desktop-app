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

import javax.persistence.Column;

import dao.pmo.ActorDao;
import dao.pmo.PortfolioDao;
import models.framework_models.parent.IModelConstants;
import models.pmo.Portfolio;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

/**
 * A portfolio form data is used to manage the fields when editing a portfolio.
 * 
 * @author Johann Kohler
 */
public class PortfolioFormData {

    public Long id;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Column(length = IModelConstants.MEDIUM_STRING)
    public String refId;

    public boolean isActive;

    @Required
    public Long portfolioType;

    @Required
    public Long manager;

    /**
     * Default constructor.
     */
    public PortfolioFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolio
     *            the portfolio in the DB
     */
    public PortfolioFormData(Portfolio portfolio) {

        id = portfolio.id;
        name = portfolio.name;
        isActive = portfolio.isActive;
        portfolioType = portfolio.portfolioType != null ? portfolio.portfolioType.id : null;
        manager = portfolio.manager != null ? portfolio.manager.id : null;
        refId = portfolio.refId;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolio
     *            the portfolio in the DB
     */
    public void fill(Portfolio portfolio) {

        portfolio.name = name;
        portfolio.isActive = isActive;
        portfolio.portfolioType = portfolioType != null ? PortfolioDao.getPortfolioTypeById(portfolioType) : null;
        portfolio.manager = manager != null ? ActorDao.getActorById(manager) : null;
        portfolio.refId = refId;

    }

}
