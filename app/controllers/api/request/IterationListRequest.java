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
import dao.delivery.ReleaseDAO;
import dao.pmo.PortfolioEntryDao;

/**
 * The iteration list request.
 * 
 * @author Oury Diallo
 */
public class IterationListRequest {

    public Long portfolioEntryId;
    public Long releaseId;

    /**
     * Constructor with initial values.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param releaseId
     *            the release id
     */
    public IterationListRequest(Long portfolioEntryId, Long releaseId) {

        this.portfolioEntryId = portfolioEntryId;
        this.releaseId = releaseId;
    }

    /**
     * Default constructor.
     */
    public IterationListRequest() {
    }

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (portfolioEntryId != null && PortfolioEntryDao.getPEById(portfolioEntryId) == null) {
            errors.add(new ValidationError("portfolioEntryId", "The portfolioEntry does not exist"));
        }
        if (releaseId != null && ReleaseDAO.getReleaseById(releaseId) == null) {
            errors.add(new ValidationError("releaseId", "The release does not exist"));
        }

        return errors.isEmpty() ? null : errors;
    }

}
