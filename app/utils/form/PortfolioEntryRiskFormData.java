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

import java.text.ParseException;

import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryRiskAndIssueDao;
import framework.utils.Utilities;
import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntryRisk;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

/**
 * An portfolioEntry risk form data is used to manage the fields when
 * adding/editing an portfolioEntry risk.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryRiskFormData {

    // the portfolioEntry id
    public Long id;

    public Long riskId;

    public Boolean isActive;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Required
    @MaxLength(value = IModelConstants.VLARGE_STRING)
    public String description;

    public String riskType;

    public String targetDate;

    public Boolean isMitigated;

    @MaxLength(value = IModelConstants.VLARGE_STRING)
    public String mitigationComment;

    public String owner;

    /**
     * Default constructor.
     */
    public PortfolioEntryRiskFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param portfolioEntryRisk
     *            the portfolio entry risk in the DB
     */
    public PortfolioEntryRiskFormData(PortfolioEntryRisk portfolioEntryRisk) {
        this.id = portfolioEntryRisk.portfolioEntry.id;
        this.riskId = portfolioEntryRisk.id;
        this.isActive = portfolioEntryRisk.isActive;
        this.name = portfolioEntryRisk.name;
        this.description = portfolioEntryRisk.description;
        this.riskType = portfolioEntryRisk.portfolioEntryRiskType != null ? String.valueOf(portfolioEntryRisk.portfolioEntryRiskType.id) : null;
        this.targetDate = portfolioEntryRisk.targetDate != null ? Utilities.getDateFormat(null).format(portfolioEntryRisk.targetDate) : null;
        this.isMitigated = portfolioEntryRisk.isMitigated;
        this.mitigationComment = portfolioEntryRisk.mitigationComment;
        this.owner = portfolioEntryRisk.owner != null ? String.valueOf(portfolioEntryRisk.owner.id) : null;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryRisk
     *            the portfolio entry risk in the DB
     */
    public void fill(PortfolioEntryRisk portfolioEntryRisk) {
        try {
            portfolioEntryRisk.targetDate = Utilities.getDateFormat(null).parse(targetDate);
        } catch (ParseException e) {
            portfolioEntryRisk.targetDate = null;
        }
        portfolioEntryRisk.name = name;
        portfolioEntryRisk.description = description;
        portfolioEntryRisk.isActive = isActive != null ? isActive : false;
        portfolioEntryRisk.portfolioEntryRiskType = !riskType.equals("") ? PortfolioEntryRiskAndIssueDao.getPERiskTypeById(Long.parseLong(riskType)) : null;
        portfolioEntryRisk.owner = !owner.equals("") ? ActorDao.getActorById(Long.parseLong(owner)) : null;
        portfolioEntryRisk.isMitigated = isMitigated != null ? isMitigated : false;
        portfolioEntryRisk.mitigationComment = mitigationComment;
    }

}
