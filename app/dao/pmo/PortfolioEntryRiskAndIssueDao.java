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
package dao.pmo;

import java.util.List;

import models.pmo.PortfolioEntryIssue;
import models.pmo.PortfolioEntryIssueType;
import models.pmo.PortfolioEntryRisk;
import models.pmo.PortfolioEntryRiskType;
import play.Play;
import com.avaje.ebean.Model.Finder;

import com.avaje.ebean.ExpressionList;

import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;

/**
 * DAO for the {@link PortfolioEntryRisk} and {@link PortfolioEntryRiskType}, {@link PortfolioEntryIssue} and {@link PortfolioEntryIssueType}
 * objects.
 * 
 * @author Johann Kohler
 */
public abstract class PortfolioEntryRiskAndIssueDao {

    public static Finder<Long, PortfolioEntryRisk> findPortfolioEntryRisk = new Finder<>(PortfolioEntryRisk.class);

    public static Finder<Long, PortfolioEntryRiskType> findPortfolioEntryRiskType = new Finder<>(PortfolioEntryRiskType.class);

    public static Finder<Long, PortfolioEntryIssue> findPortfolioEntryIssue = new Finder<>(PortfolioEntryIssue.class);

    public static Finder<Long, PortfolioEntryIssueType> findPortfolioEntryIssueType = new Finder<>(PortfolioEntryIssueType.class);

    /**
     * Default constructor.
     */
    public PortfolioEntryRiskAndIssueDao() {
    }

    /**
     * Get a portfolio entry risk by id.
     *
     * @param riskId
     *            the risk id
     */
    public static PortfolioEntryRisk getPERiskById(Long riskId) {
        return findPortfolioEntryRisk.where().eq("deleted", false).eq("id", riskId).findUnique();
    }

    /**
     * Get a portfolio entry risk by id.
     *
     * @param issueId
     *            the issue id
     */
    public static PortfolioEntryIssue getPEIssueById(Long issueId) {
        return findPortfolioEntryIssue.where().eq("deleted", false).eq("id", issueId).findUnique();
    }

    /**
     * Get all risks of a portfolio entry.
     *
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param viewAll
     *            set to true if the inactive entries must be also returned
     */
    public static Pagination<PortfolioEntryRisk> getPERiskAsPaginationByPE(Long portfolioEntryId, Boolean viewAll) {
        ExpressionList<PortfolioEntryRisk> e = getPERiskAsExpressionByPE(portfolioEntryId, viewAll);
        e.orderBy("creation_date DESC");
        return new Pagination<>(e, 5, Play.application().configuration().getInt("maf.number_page_links"));
    }

    /**
     * Get all issues of a portfolio entry.
     *
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param viewAll
     *            set to true if the inactive entries must be also returned
     */
    public static Pagination<PortfolioEntryIssue> getPEIssueAsPaginationByPE(Long portfolioEntryId, Boolean viewAll) {
        ExpressionList<PortfolioEntryIssue> e = getPEIssueAsExpressionByPE(portfolioEntryId, viewAll);
        e.orderBy("creation_date DESC");
        return new Pagination<>(e, 5, Play.application().configuration().getInt("maf.number_page_links"));
    }

    /**
     * Get the number of active not occurred risks of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static int getPERiskAsNbActiveByPE(Long portfolioEntryId) {
        return findPortfolioEntryRisk.where().eq("deleted", false).eq("portfolio_entry_id", portfolioEntryId).eq("is_active", true)
                .findRowCount();
    }

    /**
     * Get the number of active issues of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static int getPEIssueAsNbActiveByPE(Long portfolioEntryId) {
        return findPortfolioEntryIssue.where().eq("deleted", false).eq("portfolio_entry_id", portfolioEntryId).eq("is_active", true)
                .findRowCount();
    }

    /**
     * Get the portfolio entry risks of a portfolio entry with filters.
     *
     * @param isActive
     *            true to return only active risks, false only non-active, null
     *            all.
     * @param portfolioEntryId
     *            the portfolio entry
     */
    public static List<PortfolioEntryRisk> getPERiskAsListByPE(Boolean isActive, Long portfolioEntryId) {
        return getPERiskAsExpressionByPE(portfolioEntryId, isActive).findList();
    }

    /**
     * Get the portfolio entry risks of a portfolio entry with filters.
     *
     * @param isActive
     *            true to return only active risks, false only non-active, null
     *            all.
     * @param portfolioEntryId
     *            the portfolio entry
     */
    public static List<PortfolioEntryIssue> getPEIssueAsListByPE(Boolean isActive, Long portfolioEntryId) {
        return getPEIssueAsExpressionByPE(portfolioEntryId, isActive).findList();
    }

    private static ExpressionList<PortfolioEntryRisk> getPERiskAsExpressionByPE(Long portfolioEntryId, Boolean viewAll) {
        ExpressionList<PortfolioEntryRisk> e = findPortfolioEntryRisk.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId);
        if (!viewAll) {
            e = e.eq("isActive", true);
        }
        return e;
    }

    private static ExpressionList<PortfolioEntryIssue> getPEIssueAsExpressionByPE(Long portfolioEntryId, Boolean viewAll) {
        ExpressionList<PortfolioEntryIssue> e = findPortfolioEntryIssue.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId);
        if (!viewAll) {
            e = e.eq("isActive", true);
        }
        return e;
    }

    /**
     * Get a portfolio entry risk type by id.
     * 
     * @param id
     *            the portfolio entry risk type id
     */
    public static PortfolioEntryRiskType getPERiskTypeById(Long id) {
        return findPortfolioEntryRiskType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all portfolio entry risk types.
     */
    public static List<PortfolioEntryRiskType> getPERiskTypeAsList() {
        return findPortfolioEntryRiskType.where().eq("deleted", false).findList();
    }

    /**
     * Get all selectable portfolio entry risk types.
     */
    public static List<PortfolioEntryRiskType> getPERiskTypeActiveAsList() {
        return findPortfolioEntryRiskType.where().eq("deleted", false).eq("selectable", true).findList();
    }

    /**
     * Get all selectable portfolio entry risk types as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getPERiskTypeActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getPERiskTypeActiveAsList());
    }

    /**
     * Get the portfolio entry risk types list with filters.
     * 
     * @param isActive
     *            true to return only active risks, false only non-active, null
     *            all.
     */
    public static List<PortfolioEntryRiskType> getPERiskTypeAsListByFilter(Boolean isActive) {
        if (isActive != null) {
            return findPortfolioEntryRiskType.where().eq("deleted", false).eq("selectable", isActive).findList();
        } else {
            return getPERiskTypeAsList();
        }
    }


    /**
     * Get a portfolio entry issue type by id.
     *
     * @param id
     *            the portfolio entry issue type id
     */
    public static PortfolioEntryIssueType getPEIssueTypeById(Long id) {
        return findPortfolioEntryIssueType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all portfolio entry issue types.
     */
    public static List<PortfolioEntryIssueType> getPEIssueTypeAsList() {
        return findPortfolioEntryIssueType.where().eq("deleted", false).findList();
    }

    /**
     * Get all selectable portfolio entry issue types.
     */
    public static List<PortfolioEntryIssueType> getPEIssueTypeActiveAsList() {
        return findPortfolioEntryIssueType.where().eq("deleted", false).eq("selectable", true).findList();
    }

    /**
     * Get all selectable portfolio entry issue types as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getPEIssueTypeActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getPEIssueTypeActiveAsList());
    }

    /**
     * Get the portfolio entry issue types list with filters.
     *
     * @param isActive
     *            true to return only active issues, false only non-active, null
     *            all.
     */
    public static List<PortfolioEntryIssueType> getPEIssueTypeAsListByFilter(Boolean isActive) {
        if (isActive != null) {
            return findPortfolioEntryIssueType.where().eq("deleted", false).eq("selectable", isActive).findList();
        } else {
            return getPEIssueTypeAsList();
        }
    }
}
