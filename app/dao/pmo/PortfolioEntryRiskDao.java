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

import models.pmo.PortfolioEntryRisk;
import models.pmo.PortfolioEntryRiskType;
import play.Play;
import com.avaje.ebean.Model.Finder;

import com.avaje.ebean.ExpressionList;

import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;

/**
 * DAO for the {@link PortfolioEntryRisk} and {@link PortfolioEntryRiskType}
 * objects.
 * 
 * @author Johann Kohler
 */
public abstract class PortfolioEntryRiskDao {

    public static Finder<Long, PortfolioEntryRisk> findPortfolioEntryRisk = new Finder<>(Long.class, PortfolioEntryRisk.class);

    public static Finder<Long, PortfolioEntryRiskType> findPortfolioEntryRiskType = new Finder<>(Long.class, PortfolioEntryRiskType.class);

    /**
     * Default constructor.
     */
    public PortfolioEntryRiskDao() {
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
     * Get all not occurred risks of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param viewAll
     *            set to true if the inactive entries must be also returned
     */
    public static Pagination<PortfolioEntryRisk> getPERiskAsPaginationByPE(Long portfolioEntryId, Boolean viewAll) {
        ExpressionList<PortfolioEntryRisk> e =
                findPortfolioEntryRisk.where().eq("deleted", false).eq("portfolio_entry_id", portfolioEntryId).eq("has_occured", false);
        if (!viewAll) {
            e.eq("is_active", true);
        }
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
        return findPortfolioEntryRisk.where().eq("deleted", false).eq("portfolio_entry_id", portfolioEntryId).eq("has_occured", false).eq("is_active", true)
                .findRowCount();
    }

    /**
     * Get all issues of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param viewAll
     *            set to true if the inactive entries must be also returned
     */
    public static Pagination<PortfolioEntryRisk> getPEIssueAsPaginationByPE(Long portfolioEntryId, Boolean viewAll) {
        ExpressionList<PortfolioEntryRisk> e =
                findPortfolioEntryRisk.where().eq("deleted", false).eq("portfolio_entry_id", portfolioEntryId).eq("has_occured", true);
        if (!viewAll) {
            e.eq("is_active", true);
        }
        e.orderBy("creation_date DESC");
        return new Pagination<>(e, 5, Play.application().configuration().getInt("maf.number_page_links"));
    }

    /**
     * Get the number of active issues of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static int getPEIssueAsNbActiveByPE(Long portfolioEntryId) {
        return findPortfolioEntryRisk.where().eq("deleted", false).eq("portfolio_entry_id", portfolioEntryId).eq("has_occured", true).eq("is_active", true)
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
    public static List<PortfolioEntryRisk> getPERiskAsListByFilter(Boolean isActive, Long portfolioEntryId) {

        ExpressionList<PortfolioEntryRisk> e = findPortfolioEntryRisk.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId);
        if (isActive != null) {
            e = e.eq("isActive", isActive);
        }
        return e.findList();
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

}
