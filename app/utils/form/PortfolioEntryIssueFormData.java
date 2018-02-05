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

import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryRiskAndIssueDao;
import framework.utils.Utilities;
import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntryIssue;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

import java.text.ParseException;

/**
 * An portfolioEntry issue form data is used to manage the fields when
 * adding/editing an portfolioEntry issue.
 * 
 * @author Guillaume Petit
 */
public class PortfolioEntryIssueFormData {

    // the portfolioEntry id
    public Long id;

    public Long issueId;

    public Boolean isActive;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Required
    @MaxLength(value = IModelConstants.VLARGE_STRING)
    public String description;

    public String issueType;

    public String dueDate;

    public String owner;

    /**
     * Default constructor.
     */
    public PortfolioEntryIssueFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     *
     * @param portfolioEntryIssue
     *            the portfolio entry issue in the DB
     */
    public PortfolioEntryIssueFormData(PortfolioEntryIssue portfolioEntryIssue) {
        this.id = portfolioEntryIssue.portfolioEntry.id;
        this.issueId = portfolioEntryIssue.id;
        this.isActive = portfolioEntryIssue.isActive;
        this.name = portfolioEntryIssue.name;
        this.description = portfolioEntryIssue.description;
        this.issueType = portfolioEntryIssue.portfolioEntryIssueType != null ? String.valueOf(portfolioEntryIssue.portfolioEntryIssueType.id) : null;
        this.dueDate = portfolioEntryIssue.dueDate != null ? Utilities.getDateFormat(null).format(portfolioEntryIssue.dueDate) : null;
        this.owner = portfolioEntryIssue.owner != null ? String.valueOf(portfolioEntryIssue.owner.id) : null;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param portfolioEntryIssue
     *            the portfolio entry issue in the DB
     */
    public void fill(PortfolioEntryIssue portfolioEntryIssue) {
        try {
            portfolioEntryIssue.dueDate = Utilities.getDateFormat(null).parse(dueDate);
        } catch (ParseException e) {
            portfolioEntryIssue.dueDate = null;
        }
        portfolioEntryIssue.name = name;
        portfolioEntryIssue.description = description;
        portfolioEntryIssue.isActive = isActive != null ? isActive : false;
        portfolioEntryIssue.portfolioEntryIssueType = !issueType.equals("") ? PortfolioEntryRiskAndIssueDao.getPEIssueTypeById(Long.parseLong(issueType)) : null;
        portfolioEntryIssue.owner = !owner.equals("") ? ActorDao.getActorById(Long.parseLong(owner)) : null;
    }

}
