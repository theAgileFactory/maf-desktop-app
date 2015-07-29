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

import models.pmo.Stakeholder;
import models.pmo.StakeholderType;
import play.db.ebean.Model.Finder;

import com.avaje.ebean.ExpressionList;

import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;

/**
 * DAO for the {@link Stakeholder} and {@link StakeholderType} objects.
 * 
 * @author Johann Kohler
 */
public abstract class StakeholderDao {

    public static Finder<Long, Stakeholder> findStakeholder = new Finder<>(Long.class, Stakeholder.class);

    public static Finder<Long, StakeholderType> findStakeholderType = new Finder<>(Long.class, StakeholderType.class);

    /**
     * Default constructor.
     */
    public StakeholderDao() {
    }

    /**
     * Get a stakeholder by id.
     * 
     * @param id
     *            the stakeholer id
     */
    public static Stakeholder getStakeholderById(Long id) {
        return findStakeholder.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all direct stakeholders of a portfolio entry.
     * 
     * note: the stakeholder are ordered by "last name, first name"
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<Stakeholder> getStakeholderAsListByPE(Long portfolioEntryId) {
        return findStakeholder.orderBy("actor.lastName, actor.firstName").where().eq("deleted", false).eq("portfolio_entry_id", portfolioEntryId)
                .eq("actor.deleted", false).findList();
    }

    /**
     * Get all stakeholders of the portfolios of a portfolio entry.
     * 
     * note: the stakeholder are ordered by "last name, first name"
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<Stakeholder> getStakeholderAsListByPortfolioOfPE(Long portfolioEntryId) {
        return findStakeholder.orderBy("actor.lastName, actor.firstName").where().eq("deleted", false).eq("portfolio.portfolioEntries.id", portfolioEntryId)
                .eq("portfolio.portfolioEntries.deleted", false).eq("actor.deleted", false).findList();
    }

    /**
     * Get all stakeholders of a portfolio.
     * 
     * @param portfolioId
     *            the portfolio id
     */
    public static Pagination<Stakeholder> getStakeholderAsPaginationByPortfolio(Long portfolioId) {
        return new Pagination<>(findStakeholder.orderBy("actor.lastName, actor.firstName").where().eq("deleted", false).eq("portfolio_id", portfolioId)
                .eq("actor.deleted", false));
    }

    /**
     * Define if an actor is a direct stakeholder of a portfolio entry.
     * 
     * @param actorId
     *            the actor id
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static Boolean isStakeholderOfPE(Long actorId, Long portfolioEntryId) {
        return findStakeholder.where().eq("deleted", false).eq("actor_id", actorId).eq("portfolio_entry_id", portfolioEntryId).findRowCount() > 0;
    }

    /**
     * Get a stakeholder of a specific type for a portfolio entry and an actor.
     * 
     * note: this method is useful to avoid to duplicate a same actor with a
     * role (type) for a portfolio entry
     * 
     * @param actorId
     *            the actor id
     * @param stakeholderTypeId
     *            the stakeholder type id
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static Stakeholder getStakeholderByActorAndTypeAndPE(Long actorId, Long stakeholderTypeId, Long portfolioEntryId) {
        return findStakeholder.where().eq("deleted", false).eq("stakeholder_type_id", stakeholderTypeId).eq("actor_id", actorId)
                .eq("portfolio_entry_id", portfolioEntryId).findUnique();
    }

    /**
     * Get a stakeholder of a specific type for a portfolio and an actor.
     * 
     * note: this method is useful to avoid to duplicate a same actor with a
     * role (type) for a portfolio
     * 
     * @param actorId
     *            the actor id
     * @param stakeholderTypeId
     *            the stakeholder type id
     * @param portfolioId
     *            the portfolio id
     */
    public static Stakeholder getStakeholderByActorAndTypeAndPortfolio(Long actorId, Long stakeholderTypeId, Long portfolioId) {
        return findStakeholder.where().eq("deleted", false).eq("stakeholder_type_id", stakeholderTypeId).eq("actor_id", actorId)
                .eq("portfolio_id", portfolioId).findUnique();
    }

    /**
     * Get all stakeholders for portfolio entry and an actor.
     * 
     * @param actorId
     *            the actor id
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<Stakeholder> getStakeholderAsListByActorAndPE(Long actorId, Long portfolioEntryId) {
        return findStakeholder.where().eq("deleted", false).eq("actor_id", actorId).eq("portfolio_entry_id", portfolioEntryId).findList();
    }

    /**
     * Get all stakeholders for portfolio and an actor.
     * 
     * @param actorId
     *            the actor id
     * @param portfolioId
     *            the portfolio id
     */
    public static List<Stakeholder> getStakeholderAsListByActorAndPortfolio(Long actorId, Long portfolioId) {
        return findStakeholder.where().eq("deleted", false).eq("actor_id", actorId).eq("portfolio_id", portfolioId).findList();
    }

    /**
     * Get the stakeholders list with filters.
     * 
     * @param actorId
     *            if not null then return only stakeholders associated the given
     *            actor.
     * @param portfolioId
     *            if not null then return only stakeholders associated the given
     *            portfolio.
     * @param portfolioEntryId
     *            if not null then return only stakeholders associated the given
     *            portfolio entry.
     * @param stakeholderTypeId
     *            if not null then return only stakeholders with the given type.
     */
    public static List<Stakeholder> getStakeholderAsListByFilter(Long actorId, Long portfolioId, Long portfolioEntryId, Long stakeholderTypeId) {

        ExpressionList<Stakeholder> e = findStakeholder.where().eq("deleted", false);

        if (actorId != null) {
            e = e.eq("actor.id", actorId);
        }
        if (portfolioId != null) {
            e = e.eq("portfolio.id", portfolioId);
        }
        if (portfolioEntryId != null) {
            e = e.eq("portfolioEntry.id", portfolioEntryId);
        }
        if (stakeholderTypeId != null) {
            e = e.eq("stakeholderType.id", stakeholderTypeId);
        }

        return e.findList();
    }

    /**
     * Get a stakeholder type by id.
     * 
     * @param id
     *            the stakeholder type id
     */
    public static StakeholderType getStakeholderTypeById(Long id) {
        return findStakeholderType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all stakeholder types.
     */
    public static List<StakeholderType> getStakeholderTypeAsList() {
        return findStakeholderType.where().eq("deleted", false).findList();
    }

    /**
     * Get all selectable stakeholder types for a portfolio entry type.
     * 
     * @param portfolioEntryTypeId
     *            the portfolio entry type id
     */
    public static List<StakeholderType> getStakeholderTypeActiveAsListByPEType(Long portfolioEntryTypeId) {
        return findStakeholderType.where().eq("deleted", false).eq("selectable", true).eq("portfolioEntryTypes.id", portfolioEntryTypeId).findList();
    }

    /**
     * Get all selectable stakeholder types for a portfolio entry type as a
     * value holder collection.
     * 
     * @param portfolioEntryTypeId
     *            the portfolio entry type id
     */
    public static ISelectableValueHolderCollection<Long> getStakeholderTypeActiveAsVHByPEType(Long portfolioEntryTypeId) {
        return new DefaultSelectableValueHolderCollection<>(getStakeholderTypeActiveAsListByPEType(portfolioEntryTypeId));
    }

    /**
     * Get all selectable stakeholder types for portfolio type.
     * 
     * @param portfolioTypeId
     *            the portfolio type
     */
    public static List<StakeholderType> getStakeholderTypeActiveAsListByPortfolioType(Long portfolioTypeId) {
        return findStakeholderType.where().eq("deleted", false).eq("selectable", true).eq("portfolioTypes.id", portfolioTypeId).findList();
    }

    /**
     * Get all selectable stakeholder types for portfolio type as a value holder
     * collection.
     * 
     * @param portfolioTypeId
     *            the portfolio type
     */
    public static ISelectableValueHolderCollection<Long> getStakeholderTypeActiveAsVHByPortfolioType(Long portfolioTypeId) {
        return new DefaultSelectableValueHolderCollection<>(getStakeholderTypeActiveAsListByPortfolioType(portfolioTypeId));
    }

    /**
     * Get the stakeholder types list with filter.
     * 
     * @param isActive
     *            true to return only active actors, false only non-active, null
     *            all.
     */
    public static List<StakeholderType> getStakeholderTypeAsListByFilter(Boolean isActive) {
        if (isActive != null) {
            return findStakeholderType.where().eq("deleted", false).eq("selectable", isActive).findList();
        } else {
            return getStakeholderTypeAsList();
        }
    }

}
