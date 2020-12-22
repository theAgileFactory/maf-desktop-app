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

import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import play.data.validation.ValidationError;

/**
 * The portfolio entry list request.
 * 
 * @author Oury Diallo
 */
public class PortfolioEntryListRequest {

    public Long managerId;
    public List<Long> sponsoringUnitId;
    public List<Long> deliveryUnitId;
    public List<Long> portfolioId;
    public Boolean archived;
    public List<Long> portfolioEntryTypeId;
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
     * @param archived
     *            is archived
     * @param portfolioEntryTypeId
     *            the portfolio entry id
     * @param isPublic
     *            is public
     */
    public PortfolioEntryListRequest(Long managerId, List<Long> sponsoringUnitId, List<Long> deliveryUnitId, List<Long> portfolioId, Boolean archived,
                                     List<Long> portfolioEntryTypeId, Boolean isPublic) {

        this.managerId = managerId;
        this.sponsoringUnitId = sponsoringUnitId;
        this.deliveryUnitId = deliveryUnitId;
        this.portfolioId = portfolioId;
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
        if (sponsoringUnitId != null) {
            for (Long id : sponsoringUnitId) {
                if (OrgUnitDao.getOrgUnitById(id) == null) {
                    errors.add(new ValidationError("sponsoringUnitId", "The sponsoringUnit with id "+id+" does not exist"));
                }
            }
        }
        if (deliveryUnitId != null) {
            for (Long id : deliveryUnitId) {
                if (OrgUnitDao.getOrgUnitById(id) == null) {
                    errors.add(new ValidationError("deliveryUnitId", "The deliveryUnitId with id "+id+" does not exist"));
                }
            }
        }
        if (portfolioId != null) {
            for (Long id : portfolioId) {
                if (PortfolioDao.getPortfolioById(id) == null) {
                    errors.add(new ValidationError("portfolioId", "The portfolio with id "+id+" does not exist"));
                }
            }
        }
        if (portfolioEntryTypeId != null) {
            for (Long id : portfolioEntryTypeId) {
                if (PortfolioEntryDao.getPETypeById(id) == null) {
                    errors.add(new ValidationError("portfolioEntryTypeId", "The portfolioEntryType with id "+id+" does not exist"));
                }
            }
        }

        return errors.isEmpty() ? null : errors;
    }

}
