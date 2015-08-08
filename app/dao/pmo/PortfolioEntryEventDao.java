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

import models.pmo.PortfolioEntryEvent;
import models.pmo.PortfolioEntryEventType;
import com.avaje.ebean.Model.Finder;

import com.avaje.ebean.ExpressionList;

import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;

/**
 * DAO for the {@link PortfolioEntryEvent} and {@link PortfolioEntryEventType}
 * objects.
 * 
 * @author Johann Kohler
 */
public abstract class PortfolioEntryEventDao {

    public static Finder<Long, PortfolioEntryEvent> findPortfolioEntryEvent = new Finder<>(PortfolioEntryEvent.class);

    public static Finder<Long, PortfolioEntryEventType> findPortfolioEntryEventType = new Finder<>(PortfolioEntryEventType.class);

    /**
     * Default constructor.
     */
    public PortfolioEntryEventDao() {
    }

    /**
     * Get a portfolio entry event by id.
     * 
     * @param id
     *            the event id
     */
    public static PortfolioEntryEvent getPEEventById(Long id) {
        return findPortfolioEntryEvent.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all events of a portfolio entry as an expression list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ExpressionList<PortfolioEntryEvent> getPEEventAsExprByPE(Long portfolioEntryId) {
        return findPortfolioEntryEvent.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId);
    }

    /**
     * Get all events of a portfolio entry as pagination.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static Pagination<PortfolioEntryEvent> getPEEventAsPaginationByPE(Long portfolioEntryId) {
        return new Pagination<>(getPEEventAsExprByPE(portfolioEntryId));
    }

    /**
     * Get a portfolio entry event type by id.
     * 
     * @param id
     *            the event id
     */
    public static PortfolioEntryEventType getPEEventTypeById(Long id) {
        return findPortfolioEntryEventType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all portfolio entry event types.
     */
    public static List<PortfolioEntryEventType> getPEEventTypeAsList() {
        return findPortfolioEntryEventType.where().eq("deleted", false).findList();
    }

    /**
     * Get all active portfolio entry event types.
     */
    public static List<PortfolioEntryEventType> getPEEventTypeActiveAsList() {
        return findPortfolioEntryEventType.where().eq("deleted", false).eq("selectable", true).findList();
    }

    /**
     * Get all selectable portfolio entry event types as a value holder
     * collection.
     */
    public static ISelectableValueHolderCollection<Long> getPEEventTypeActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getPEEventTypeActiveAsList());
    }

    /**
     * Get the portfolio entry event types list with filter.
     * 
     * @param isActive
     *            true to return only active event types, false only non-active,
     *            null all.
     */
    public static List<PortfolioEntryEventType> getPEEventTypeAsListByFilter(Boolean isActive) {
        if (isActive != null) {
            return findPortfolioEntryEventType.where().eq("deleted", false).eq("selectable", isActive).findList();
        } else {
            return getPEEventTypeAsList();
        }
    }

}
