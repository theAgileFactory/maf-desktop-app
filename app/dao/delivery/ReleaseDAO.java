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
package dao.delivery;

import java.util.ArrayList;
import java.util.List;

import models.delivery.Release;
import models.delivery.ReleasePortfolioEntry;
import models.delivery.ReleasePortfolioEntry.Type;
import models.delivery.ReleasePortfolioEntryId;
import models.delivery.Requirement;
import com.avaje.ebean.Model.Finder;

import com.avaje.ebean.ExpressionList;

import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;

/**
 * DAO for the {@link Release}, {@link ReleasePortfolioEntry},
 * {@link ReleasePortfolioEntryId} objects.
 * 
 * @author Pierre-Yves Cloux
 */
public abstract class ReleaseDAO {

    public static Finder<Long, Release> findRelease = new Finder<>(Long.class, Release.class);
    public static Finder<Long, ReleasePortfolioEntry> findReleasePortfolioEntry = new Finder<>(Long.class, ReleasePortfolioEntry.class);

    /**
     * Default constructor.
     */
    public ReleaseDAO() {
    }

    /**
     * Get the releases for which the given user is the manager and return an
     * expression list.
     * 
     * @param managerId
     *            the actor id
     * @param viewAll
     *            set to true to display all releases inluding the non-active
     *            ones
     */
    public static ExpressionList<Release> getReleaseAsExprByManager(Long managerId, boolean viewAll) {
        ExpressionList<Release> expr = ReleaseDAO.findRelease.where().eq("deleted", false).eq("manager.id", managerId);
        if (!viewAll) {
            expr = expr.eq("isActive", true);
        }
        return expr;
    }

    /**
     * Get the releases for which the given user is the manager and return a
     * pagination object.
     * 
     * @param managerId
     *            the actor id
     * @param viewAll
     *            set to true to display all releases inluding the non-active
     *            ones
     */
    public static Pagination<Release> getReleaseAsPaginationByManager(Long managerId, boolean viewAll) {
        return new Pagination<>(getReleaseAsExprByManager(managerId, viewAll));
    }

    /**
     * Get the releases for which the given user is the manager and return a
     * pagination object.
     * 
     * @param managerId
     *            the actor id
     * @param viewAll
     *            set to true to display all releases inluding the non-active
     *            ones
     */
    public static List<Release> getReleaseAsListByManager(Long managerId, boolean viewAll) {
        return getReleaseAsExprByManager(managerId, viewAll).findList();
    }

    /**
     * Get all active releases.
     */
    public static List<Release> getReleaseActiveAsList() {
        return ReleaseDAO.findRelease.where().eq("deleted", false).eq("isActive", true).findList();
    }

    /**
     * Search from all active releases for which the criteria matches with the
     * name.
     * 
     * @param key
     *            the search criteria (wild cards should be %)
     */
    public static List<Release> getReleaseActiveAsListByName(String key) {
        return ReleaseDAO.findRelease.where().eq("deleted", false).eq("isActive", true).ilike("name", key + "%").findList();
    }

    /**
     * Get the releases of a portfolio entry for a requirements' relation type.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param type
     *            the requirements' relation type
     */
    public static List<Release> getReleaseAsListByPEForType(Long portfolioEntryId, Type type) {
        return ReleaseDAO.findRelease.where().eq("deleted", false).eq("releasesPortfolioEntries.portfolioEntry.id", portfolioEntryId)
                .eq("releasesPortfolioEntries.type", type).findList();
    }

    /**
     * Get all active releases as a value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getReleaseActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getReleaseActiveAsList());
    }

    /**
     * Search from all active releases and return a value holder collection.
     * 
     * @param key
     *            the search criteria (wild cards should be %)
     */
    public static ISelectableValueHolderCollection<Long> getReleaseActiveAsVHByName(String key) {
        return new DefaultSelectableValueHolderCollection<>(getReleaseActiveAsListByName(key));
    }

    /**
     * Get the releases of a portfolio entry for a requirements' relation type
     * and return a value holder collection.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param type
     *            the requirements' relation type
     */
    public static ISelectableValueHolderCollection<Long> getReleaseAsVHByPEAndType(Long portfolioEntryId, Type type) {
        return new DefaultSelectableValueHolderCollection<>(getReleaseAsListByPEForType(portfolioEntryId, type));
    }

    /**
     * Get the releases list with filters.
     * 
     * @param isActive
     *            true to return only active releases, false only non-active,
     *            null all.
     * @param managerId
     *            if not null then return only releases with the given manager.
     * @param portfolioEntryId
     *            if not null then return only releases for the given portfolio
     *            entry.
     */
    public static List<Release> getReleaseAsListByActiveAndManagerAndPE(Boolean isActive, Long managerId, Long portfolioEntryId) {

        ExpressionList<Release> e = ReleaseDAO.findRelease.where().eq("deleted", false);

        if (isActive != null) {
            e = e.eq("isActive", isActive);
        }
        if (managerId != null) {
            e = e.eq("manager.id", managerId);
        }
        if (portfolioEntryId != null) {
            e = e.eq("releasesPortfolioEntries.portfolioEntry.id", portfolioEntryId);
        }
        return e.findList();
    }

    /**
     * Get an Release by id.
     * 
     * @param id
     *            the Release id the R the Release idelease id
     */
    public static Release getReleaseById(Long id) {
        return ReleaseDAO.findRelease.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get a portfolio entry association of a release.
     * 
     * @param releaseId
     *            the release id
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ReleasePortfolioEntry getReleaseByIdAndPE(Long releaseId, Long portfolioEntryId) {
        return ReleaseDAO.findReleasePortfolioEntry.where().eq("id.releaseId", releaseId).eq("id.portfolioEntryId", portfolioEntryId).findUnique();
    }

    /**
     * Return the requirements of a release for a specific portfolio entry.
     * 
     * @param releaseId
     *            the release id
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<Requirement> getRequirementAsListByIdAndPE(Long releaseId, Long portfolioEntryId) {

        ReleasePortfolioEntry releasePortfolioEntry = getReleaseByIdAndPE(releaseId, portfolioEntryId);

        switch (releasePortfolioEntry.type) {
        case ALL:
            return releasePortfolioEntry.getPortfolioEntry().requirements;
        case BY_ITERATION:
            return RequirementDAO.getRequirementAsListByPEAndReleaseOfIteration(releaseId, portfolioEntryId);
        case BY_REQUIREMENT:
            return RequirementDAO.getRequirementAsListByPEAndRelease(releaseId, portfolioEntryId);
        case NONE:
            return new ArrayList<>();
        default:
            return null;
        }

    }

}
