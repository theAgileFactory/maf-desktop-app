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
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;

/**
 * The portfolio entry list request.
 * 
 * @author Oury Diallo
 */
public class PortfolioEntryListRequest {

    public Long managerId;
    public Long sponsoringUnitId;
    public Long deliveryUnitId;
    public Long portfolioId;
    public Long releaseId;
    public Boolean archived;
    public Long portfolioEntryTypeId;
    public Boolean isPublic;

    /**
     * Constructor with initial values.
     * 
     * @param managerId
     *            the manger id
     * @param sponsoringUnitId
     *            the sponsoring unit id
     * @param deliveryUnitId
     *            the delivery unit id
     * @param portfolioId
     *            the portfolio id
     * @param releaseId
     *            the release id
     * @param archived
     *            is archived
     * @param portfolioEntryTypeId
     *            the portfolio entry id
     * @param isPublic
     *            is public
     */
    public PortfolioEntryListRequest(Long managerId, Long sponsoringUnitId, Long deliveryUnitId, Long portfolioId, Long releaseId, Boolean archived,
            Long portfolioEntryTypeId, Boolean isPublic) {

        this.managerId = managerId;
        this.sponsoringUnitId = sponsoringUnitId;
        this.deliveryUnitId = deliveryUnitId;
        this.portfolioId = portfolioId;
        this.releaseId = releaseId;
        this.archived = archived;
        this.portfolioEntryTypeId = portfolioEntryTypeId;
        this.isPublic = isPublic;
    }

    /**
     * Default constructor.
     */
    public PortfolioEntryListRequest() {
    }

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (managerId != null && ActorDao.getActorById(managerId) == null) {
            errors.add(new ValidationError("managerId", "The manager does not exist"));
        }
        if (sponsoringUnitId != null && OrgUnitDao.getOrgUnitById(sponsoringUnitId) == null) {
            errors.add(new ValidationError("sponsoringUnitId", "The sponsoringUnit does not exist"));
        }
        if (deliveryUnitId != null && OrgUnitDao.getOrgUnitById(deliveryUnitId) == null) {
            errors.add(new ValidationError("deliveryUnitId", "The deliveryUnit does not exist"));
        }
        if (portfolioId != null && PortfolioDao.getPortfolioById(portfolioId) == null) {
            errors.add(new ValidationError("portfolioId", "The portfolio does not exist"));
        }
        if (releaseId != null && ReleaseDAO.getReleaseById(releaseId) == null) {
            errors.add(new ValidationError("releaseId", "The release does not exist"));
        }
        if (portfolioEntryTypeId != null && PortfolioEntryDao.getPETypeById(portfolioEntryTypeId) == null) {
            errors.add(new ValidationError("portfolioEntryTypeId", "The portfolioEntryType does not exist"));
        }

        return errors.isEmpty() ? null : errors;
    }

}
