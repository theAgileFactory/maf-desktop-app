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

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Finder;

import framework.utils.CssValueForValueHolder;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import models.pmo.PortfolioEntryPlanningPackage;
import models.pmo.PortfolioEntryPlanningPackageGroup;
import models.pmo.PortfolioEntryPlanningPackagePattern;
import models.pmo.PortfolioEntryPlanningPackageType;

/**
 * DAO for the {@link PortfolioEntryPlanningPackage} and
 * {@link PortfolioEntryPlanningPackageGroup} and
 * {@link PortfolioEntryPlanningPackagePattern} objects.
 * 
 * @author Johann Kohler
 */
public abstract class PortfolioEntryPlanningPackageDao {

    public static Finder<Long, PortfolioEntryPlanningPackage> findPortfolioEntryPlanningPackage = new Finder<>(PortfolioEntryPlanningPackage.class);

    public static Finder<Long, PortfolioEntryPlanningPackageGroup> findPortfolioEntryPlanningPackageGroup = new Finder<>(
            PortfolioEntryPlanningPackageGroup.class);

    public static Finder<Long, PortfolioEntryPlanningPackagePattern> findPortfolioEntryPlanningPackagePattern = new Finder<>(
            PortfolioEntryPlanningPackagePattern.class);

    public static Finder<Long, PortfolioEntryPlanningPackageType> findPortfolioEntryPlanningPackageType = new Finder<>(
            PortfolioEntryPlanningPackageType.class);

    /**
     * Default constructor.
     */
    public PortfolioEntryPlanningPackageDao() {
    }

    /**
     * Get a portfolio entry planning package by id.
     * 
     * @param id
     *            the planning package id
     */
    public static PortfolioEntryPlanningPackage getPEPlanningPackageById(Long id) {
        return findPortfolioEntryPlanningPackage.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all planning packages of a portfolio entry as an ordered (by end
     * date) list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<PortfolioEntryPlanningPackage> getPEPlanningPackageOrderedAsListByPE(Long portfolioEntryId) {
        return findPortfolioEntryPlanningPackage.orderBy("endDate").where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId).findList();
    }

    /**
     * Get all planning packages of a portfolio entry as expression list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ExpressionList<PortfolioEntryPlanningPackage> getPEPlanningPackageAsExprByPE(Long portfolioEntryId) {
        return findPortfolioEntryPlanningPackage.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId);
    }

    /**
     * Get all planning packages of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<PortfolioEntryPlanningPackage> getPEPlanningPackageAsListByPE(Long portfolioEntryId) {
        return getPEPlanningPackageAsExprByPE(portfolioEntryId).findList();
    }

    /**
     * Get all planning packages of a portfolio entry as a value holder
     * collection.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ISelectableValueHolderCollection<Long> getPEPlanningPackageAsVHByPE(Long portfolioEntryId) {
        return new DefaultSelectableValueHolderCollection<>(getPEPlanningPackageAsListByPE(portfolioEntryId));
    }

    /**
     * Search from all planning packages of a portfolio entry.
     * 
     * @param key
     *            the key word
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<PortfolioEntryPlanningPackage> getPEPlanningPackageAsListByKeywordsAndPE(String key, Long portfolioEntryId) {
        return getPEPlanningPackageAsExprByPE(portfolioEntryId).like("name", key + "%").findList();
    }

    /**
     * Search from all planning packages of a portfolio entry as a value holder
     * collection.
     * 
     * @param key
     *            the key word
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ISelectableValueHolderCollection<Long> getPEPlanningPackageAsVHByKeywordsAndPE(String key, Long portfolioEntryId) {
        return new DefaultSelectableValueHolderCollection<>(getPEPlanningPackageAsListByKeywordsAndPE(key, portfolioEntryId));
    }

    /**
     * Get a package group by id.
     * 
     * @param id
     *            the package group id
     */
    public static PortfolioEntryPlanningPackageGroup getPEPlanningPackageGroupById(Long id) {
        return findPortfolioEntryPlanningPackageGroup.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all groups.
     */
    public static List<PortfolioEntryPlanningPackageGroup> getPEPlanningPackageGroupAsList() {
        return findPortfolioEntryPlanningPackageGroup.where().eq("deleted", false).findList();
    }

    /**
     * Get all groups as a value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getPEPlanningPackageGroupAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getPEPlanningPackageGroupAsList());
    }

    /**
     * Get the active groups.
     */
    public static List<PortfolioEntryPlanningPackageGroup> getPEPlanningPackageGroupActiveAsList() {
        return findPortfolioEntryPlanningPackageGroup.where().eq("deleted", false).eq("isActive", true).findList();
    }

    /**
     * Get active groups as a value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getPEPlanningPackageGroupActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getPEPlanningPackageGroupActiveAsList());
    }

    /**
     * Get active and non empty (with at least one pattern) groups.
     */
    public static List<PortfolioEntryPlanningPackageGroup> getPEPlanningPackageGroupActiveNonEmptyAsList() {
        return findPortfolioEntryPlanningPackageGroup.fetch("portfolioEntryPlanningPackagePatterns").where().eq("deleted", false).eq("isActive", true)
                .findList();
    }

    /**
     * Get active non empty (with at least one pattern) groups. as a value
     * holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getPEPlanningPackageGroupActiveNonEmptyAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getPEPlanningPackageGroupActiveNonEmptyAsList());
    }

    /**
     * Get the portfolio entry planning package groups list with filter.
     * 
     * @param isActive
     *            true to return only active package group, false only
     *            non-active, null all.
     **/
    public static List<PortfolioEntryPlanningPackageGroup> getPEPlanningPackageGroupAsListByFilter(Boolean isActive) {
        if (isActive != null) {
            return findPortfolioEntryPlanningPackageGroup.where().eq("deleted", false).eq("isActive", isActive).findList();
        }
        return getPEPlanningPackageGroupAsList();
    }

    /**
     * Get a portfolio entry planning package pattern by id.
     * 
     * @param id
     *            the planning package pattern id
     */
    public static PortfolioEntryPlanningPackagePattern getPEPlanningPackagePatternById(Long id) {
        return findPortfolioEntryPlanningPackagePattern.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get portfolio entry planning package patterns list.
     * 
     * @param packageGroupId
     *            the planning package group id
     */
    public static List<PortfolioEntryPlanningPackagePattern> getPEPlanningPackagePatternAsListByGroup(Long packageGroupId) {
        return findPortfolioEntryPlanningPackagePattern.where().eq("deleted", false).eq("portfolioEntryPlanningPackageGroup.id", packageGroupId).findList();
    }

    /**
     * Get the pattern of a group with the previous order.
     * 
     * 
     * @param packageGroupId
     *            the package group id
     * @param order
     *            the current order
     */
    public static PortfolioEntryPlanningPackagePattern getPEPlanningPackagePatternPreviousByGroup(Long packageGroupId, int order) {
        return findPortfolioEntryPlanningPackagePattern.orderBy("order DESC").where().eq("deleted", false)
                .eq("portfolioEntryPlanningPackageGroup.id", packageGroupId).lt("order", order).setMaxRows(1).findUnique();
    }

    /**
     * Get the pattern of a group with the next order.
     * 
     * 
     * @param packageGroupId
     *            the package group id
     * @param order
     *            the current order
     */
    public static PortfolioEntryPlanningPackagePattern getPEPlanningPackagePatternNextByGroup(Long packageGroupId, int order) {
        return findPortfolioEntryPlanningPackagePattern.orderBy("order ASC").where().eq("deleted", false)
                .eq("portfolioEntryPlanningPackageGroup.id", packageGroupId).gt("order", order).setMaxRows(1).findUnique();
    }

    /**
     * Get the last order for a package group.
     * 
     * @param packageGroupId
     *            the package group id
     */
    public static Integer getPEPlanningPackagePatternAsLastOrderByGroup(Long packageGroupId) {
        PortfolioEntryPlanningPackagePattern lastPackagePattern = findPortfolioEntryPlanningPackagePattern.orderBy("order DESC").where().eq("deleted", false)
                .eq("portfolioEntryPlanningPackageGroup.id", packageGroupId).setMaxRows(1).findUnique();
        if (lastPackagePattern == null) {
            return -1;
        } else {
            return lastPackagePattern.order;
        }
    }

    /**
     * Get a portfolio entry planning package type by id.
     * 
     * @param id
     *            the planning package type id
     */
    public static PortfolioEntryPlanningPackageType getPEPlanningPackageTypeById(Long id) {
        return findPortfolioEntryPlanningPackageType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all types.
     */
    public static List<PortfolioEntryPlanningPackageType> getPEPlanningPackageTypeAsList() {
        return findPortfolioEntryPlanningPackageType.where().eq("deleted", false).findList();
    }

    /**
     * Get the active types.
     */
    public static List<PortfolioEntryPlanningPackageType> getPEPlanningPackageTypeActiveAsList() {
        return findPortfolioEntryPlanningPackageType.where().eq("deleted", false).eq("isActive", true).findList();
    }

    /**
     * Get active types as a value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getPEPlanningPackageTypeActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getPEPlanningPackageTypeActiveAsList());
    }

    /**
     * Get active types as a value holder collection with the CSS
     * implementation.
     */
    public static DefaultSelectableValueHolderCollection<CssValueForValueHolder> getPEPlanningPackageTypeActiveAsCssVH() {
        DefaultSelectableValueHolderCollection<CssValueForValueHolder> selectablePortfolioEntryReportStatusTypes;
        selectablePortfolioEntryReportStatusTypes = new DefaultSelectableValueHolderCollection<>();
        for (PortfolioEntryPlanningPackageType type : getPEPlanningPackageTypeActiveAsList()) {
            selectablePortfolioEntryReportStatusTypes.add(new DefaultSelectableValueHolder<>(
                    new CssValueForValueHolder(String.valueOf(type.id), type.getName(), type.cssClass), String.valueOf(type.id)));

        }
        return selectablePortfolioEntryReportStatusTypes;
    }

    /**
     * Get the portfolio entry planning package types list with filter.
     * 
     * @param isActive
     *            true to return only active package type, false only
     *            non-active, null all.
     **/
    public static List<PortfolioEntryPlanningPackageType> getPEPlanningPackageTypeAsListByFilter(Boolean isActive) {
        if (isActive != null) {
            return findPortfolioEntryPlanningPackageType.where().eq("deleted", false).eq("isActive", isActive).findList();
        }
        return getPEPlanningPackageTypeAsList();
    }

}
