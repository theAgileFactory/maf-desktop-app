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
package controllers.api.request;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.ValidationError;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;

/**
 * The portfolio list request.
 * 
 * @author Oury Diallo
 */
public class PortfolioListRequest {

    public Boolean isActive;
    public Long managerId;
    public Long portfolioEntryId;
    public Long portfolioTypeId;

    /**
     * Constructor with initial values.
     * 
     * @param isActive
     *            is active
     * @param managerId
     *            the manager id
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param portfolioTypeId
     *            the portfolio type id
     */
    public PortfolioListRequest(Boolean isActive, Long managerId, Long portfolioEntryId, Long portfolioTypeId) {

        this.isActive = isActive;
        this.managerId = managerId;
        this.portfolioEntryId = portfolioEntryId;
        this.portfolioTypeId = portfolioTypeId;
    }

    /**
     * Default constructor.
     */
    public PortfolioListRequest() {
    }

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (managerId != null && ActorDao.getActorById(managerId) == null) {
            errors.add(new ValidationError("managerId", "The manager does not exist"));
        }
        if (portfolioEntryId != null && PortfolioEntryDao.getPEById(portfolioEntryId) == null) {
            errors.add(new ValidationError("portfolioEntryId", "The portfolioEntry does not exist"));
        }
        if (portfolioTypeId != null && PortfolioDao.getPortfolioTypeById(portfolioTypeId) == null) {
            errors.add(new ValidationError("portfolioTypeId", "The portfolioType does not exist"));
        }

        return errors.isEmpty() ? null : errors;
    }

}
