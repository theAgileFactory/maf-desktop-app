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
import dao.pmo.StakeholderDao;

/**
 * The stakeholder list request.
 * 
 * @author Oury Diallo
 */
public class StakeholderListRequest {

    public Long actorId;
    public Long portfolioId;
    public Long portfolioEntryId;
    public Long stakeholderTypeId;

    /**
     * Constructor with initial values.
     * 
     * @param actorId
     *            the actor id
     * @param portfolioId
     *            the portfolio id
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param stakeholderTypeId
     *            the stakeholder type id
     */
    public StakeholderListRequest(Long actorId, Long portfolioId, Long portfolioEntryId, Long stakeholderTypeId) {

        this.actorId = actorId;
        this.portfolioId = portfolioId;
        this.portfolioEntryId = portfolioEntryId;
        this.stakeholderTypeId = stakeholderTypeId;
    }

    /**
     * Default constructor.
     */
    public StakeholderListRequest() {
    }

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (actorId != null && ActorDao.getActorById(actorId) == null) {
            errors.add(new ValidationError("actorId", "The actor does not exist"));
        }
        if (portfolioId != null && PortfolioDao.getPortfolioById(portfolioId) == null) {
            errors.add(new ValidationError("portfolioId", "The portfolio does not exist"));
        }
        if (portfolioEntryId != null && PortfolioEntryDao.getPEById(portfolioEntryId) == null) {
            errors.add(new ValidationError("portfolioEntryId", "The portfolioEntry does not exist"));
        }
        if (stakeholderTypeId != null && StakeholderDao.getStakeholderTypeById(stakeholderTypeId) == null) {
            errors.add(new ValidationError("stakeholderTypeId", "The stakeholderType does not exist"));
        }

        return errors.isEmpty() ? null : errors;
    }

}
