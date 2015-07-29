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

import java.util.List;

import models.delivery.Iteration;
import play.db.ebean.Model.Finder;

import com.avaje.ebean.ExpressionList;

/**
 * DAO for the {@link Iteration} object.
 * 
 * @author Pierre-Yves Cloux
 */
public abstract class IterationDAO {

    public static Finder<Long, Iteration> find = new Finder<>(Long.class, Iteration.class);

    /**
     * Default constructor.
     */
    public IterationDAO() {
    }

    /**
     * Get an iteration by id.
     * 
     * @param id
     *            the iteration id
     */
    public static Iteration getIterationById(Long id) {
        return IterationDAO.find.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all iterations of a portfolio entry as an expression list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ExpressionList<Iteration> getIterationAllAsExprByPE(Long portfolioEntryId) {
        return IterationDAO.find.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId);
    }

    /**
     * Get all iterations of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<Iteration> getIterationAllAsListByPE(Long portfolioEntryId) {
        return getIterationAllAsExprByPE(portfolioEntryId).findList();
    }

    /**
     * Get all iterations of a portfolio entry for a release.
     * 
     * @param releaseId
     *            the release id
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<Iteration> getIterationAllAsListByPEAndRelease(Long releaseId, Long portfolioEntryId) {
        return IterationDAO.find.where().eq("deleted", false).eq("release.id", releaseId).eq("portfolioEntry.id", portfolioEntryId).findList();
    }

    /**
     * Get list of all iterations of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<Iteration> getIterationsListByPE(Long portfolioEntryId) {
        return IterationDAO.find.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId).findList();
    }

}
