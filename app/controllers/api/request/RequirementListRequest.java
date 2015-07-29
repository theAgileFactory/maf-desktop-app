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
import dao.delivery.IterationDAO;
import dao.delivery.ReleaseDAO;
import dao.delivery.RequirementDAO;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryDao;

/**
 * The requirement list request.
 * 
 * @author Oury Diallo
 */
public class RequirementListRequest {

    public Long authorId;
    public Long portfolioEntryId;
    public Long iterationId;
    public Long releaseId;
    public Long requirementPriorityId;
    public Long requirementSeverityId;
    public Long requirementStatusId;

    /**
     * Constructor with initial values.
     * 
     * @param authorId
     *            the author id
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param iterationId
     *            the iteration id
     * @param releaseId
     *            the release id
     * @param requirementPriorityId
     *            the requirement priority id
     * @param requirementSeverityId
     *            the requirement severity id
     * @param requirementStatusId
     *            the requirement status id
     */
    public RequirementListRequest(Long authorId, Long portfolioEntryId, Long iterationId, Long releaseId, Long requirementPriorityId,
            Long requirementSeverityId, Long requirementStatusId) {

        this.authorId = authorId;
        this.portfolioEntryId = portfolioEntryId;
        this.iterationId = iterationId;
        this.releaseId = releaseId;
        this.requirementPriorityId = requirementPriorityId;
        this.requirementSeverityId = requirementSeverityId;
        this.requirementStatusId = requirementStatusId;
    }

    /**
     * Default constructor.
     */
    public RequirementListRequest() {
    }

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (authorId != null && ActorDao.getActorById(authorId) == null) {
            errors.add(new ValidationError("authorId", "The author does not exist"));
        }
        if (portfolioEntryId != null && PortfolioEntryDao.getPEById(portfolioEntryId) == null) {
            errors.add(new ValidationError("portfolioEntryId", "The portfolioEntry does not exist"));
        }
        if (iterationId != null && IterationDAO.getIterationById(iterationId) == null) {
            errors.add(new ValidationError("iterationId", "The iteration does not exist"));
        }
        if (releaseId != null && ReleaseDAO.getReleaseById(releaseId) == null) {
            errors.add(new ValidationError("releaseId", "The release does not exist"));
        }
        if (requirementPriorityId != null && RequirementDAO.getRequirementById(requirementPriorityId) == null) {
            errors.add(new ValidationError("requirementPriorityId", "The requirementPriority does not exist"));
        }
        if (requirementSeverityId != null && RequirementDAO.getRequirementSeverityById(requirementSeverityId) == null) {
            errors.add(new ValidationError("requirementSeverityId", "The requirementSeverity does not exist"));
        }
        if (requirementStatusId != null && RequirementDAO.getRequirementStatusById(requirementStatusId) == null) {
            errors.add(new ValidationError("requirementStatusId", "The requirementStatus does not exist"));
        }

        return errors.isEmpty() ? null : errors;
    }

}
