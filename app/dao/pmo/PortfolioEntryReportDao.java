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

import models.pmo.PortfolioEntryReport;
import models.pmo.PortfolioEntryReportStatusType;
import play.Play;
import com.avaje.ebean.Model.Finder;

import com.avaje.ebean.ExpressionList;

import framework.utils.CssValueForValueHolder;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;

/**
 * DAO for the {@link PortfolioEntryReport} and
 * {@link PortfolioEntryReportStatusType} objects.
 * 
 * @author Johann Kohler
 */
public abstract class PortfolioEntryReportDao {

    public static Finder<Long, PortfolioEntryReport> findPortfolioEntryReport = new Finder<>(PortfolioEntryReport.class);

    public static Finder<Long, PortfolioEntryReportStatusType> findPortfolioEntryReportStatusType = new Finder<>(
            PortfolioEntryReportStatusType.class);

    /**
     * Default constructor.
     */
    public PortfolioEntryReportDao() {
    }

    /**
     * Get a portfolio entry report by id.
     * 
     * @param id
     *            the report id
     */
    public static PortfolioEntryReport getPEReportById(Long id) {
        return findPortfolioEntryReport.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all reports of a portfolio entry as pagination.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param viewAll
     *            set to true if the not published entries must be also returned
     */
    public static Pagination<PortfolioEntryReport> getPEReportAsPaginationByPE(Long portfolioEntryId, Boolean viewAll) {
        ExpressionList<PortfolioEntryReport> e = findPortfolioEntryReport.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId);
        if (!viewAll) {
            e.eq("isPublished", true);
        }
        e.orderBy("creationDate DESC");

        return new Pagination<>(e, 5, Play.application().configuration().getInt("maf.number_page_links"));

    }

    /**
     * Get the last report of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static PortfolioEntryReport getPEReportAsLastByPE(Long portfolioEntryId) {
        return findPortfolioEntryReport.orderBy("creationDate DESC").where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId).setMaxRows(1)
                .findUnique();
    }

    /**
     * Get portfolio entry reports of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<PortfolioEntryReport> getPEReportAsListByPE(Long portfolioEntryId) {
        return findPortfolioEntryReport.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId).findList();
    }

    /**
     * Get a status type by id.
     * 
     * @param id
     *            the status type id
     */
    public static PortfolioEntryReportStatusType getPEReportStatusTypeById(Long id) {
        return findPortfolioEntryReportStatusType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all portfolio entry report status types.
     */
    public static List<PortfolioEntryReportStatusType> getPEReportStatusTypeAsList() {
        return findPortfolioEntryReportStatusType.where().eq("deleted", false).findList();
    }

    /**
     * Get all selectable status types as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getPEReportStatusTypeActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(findPortfolioEntryReportStatusType.where().eq("deleted", false).eq("selectable", true).findList());
    }

    /**
     * Get all selectable status types as a value holder collection with the CSS
     * implementation.
     */
    public static DefaultSelectableValueHolderCollection<CssValueForValueHolder> getPEReportStatusTypeActiveAsCssVH() {
        DefaultSelectableValueHolderCollection<CssValueForValueHolder> selectablePortfolioEntryReportStatusTypes;
        selectablePortfolioEntryReportStatusTypes = new DefaultSelectableValueHolderCollection<>();
        List<PortfolioEntryReportStatusType> list =
                findPortfolioEntryReportStatusType.orderBy("id").where().eq("deleted", false).eq("selectable", true).findList();
        for (PortfolioEntryReportStatusType portfolioEntryReportStatusType : list) {
            selectablePortfolioEntryReportStatusTypes.add(new DefaultSelectableValueHolder<>(new CssValueForValueHolder(String
                    .valueOf(portfolioEntryReportStatusType.id), portfolioEntryReportStatusType.getName(), portfolioEntryReportStatusType.cssClass), String
                    .valueOf(portfolioEntryReportStatusType.id)));

        }
        return selectablePortfolioEntryReportStatusTypes;
    }

    /**
     * Get the portfolio entry report status types list with filters.
     * 
     * @param isActive
     *            true to return only active report status types, false only
     *            non-active, null all.
     */
    public static List<PortfolioEntryReportStatusType> getPEReportStatusTypeByFilter(Boolean isActive) {
        if (isActive != null) {
            return findPortfolioEntryReportStatusType.where().eq("deleted", false).eq("selectable", isActive).findList();
        } else {
            return getPEReportStatusTypeAsList();
        }
    }

}
