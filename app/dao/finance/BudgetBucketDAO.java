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
package dao.finance;

import java.util.List;

import models.finance.BudgetBucket;
import models.finance.BudgetBucketLine;
import models.sql.TotalAmount;
import play.db.ebean.Model.Finder;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;

/**
 * DAO for the {@link BudgetBucket} and {@link BudgetBucketLine} object.
 * 
 * @author Pierre-Yves Cloux
 */
public abstract class BudgetBucketDAO {

    public static Finder<Long, BudgetBucket> findBudgetBucket = new Finder<>(Long.class, BudgetBucket.class);
    /**
     * Default finder for the entity class.
     */
    public static Finder<Long, BudgetBucketLine> findBudgetBucketLine = new Finder<>(Long.class, BudgetBucketLine.class);

    /**
     * Default constructor.
     */
    public BudgetBucketDAO() {
    }

    /**
     * Get all budget buckets.
     */
    public static List<BudgetBucket> getBudgetBucketAsList() {
        return BudgetBucketDAO.findBudgetBucket.where().eq("deleted", false).findList();
    }

    /**
     * Search from all budget buckets for which the criteria matches with the
     * name of the ref ID.
     * 
     * @param key
     *            the search criteria (wild cards should be %)
     */
    public static List<BudgetBucket> getBudgetBucketAsListByName(String key) {
        return BudgetBucketDAO.findBudgetBucket.where().eq("deleted", false).or(Expr.ilike("name", key + "%"), Expr.ilike("refId", key + "%")).findList();
    }

    /**
     * Get all budget buckets as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getBudgetBucketSelectableAsList() {
        return new DefaultSelectableValueHolderCollection<>(getBudgetBucketAsList());
    }

    /**
     * Search from all budget buckets with the search process defined by the
     * method "searchFromAllBudgetBuckets" and return a value holder collection.
     * 
     * @param key
     *            the search criteria (wild cards should be %)
     */
    public static ISelectableValueHolderCollection<Long> getBudgetBucketSelectableAsListByName(String key) {
        return new DefaultSelectableValueHolderCollection<>(getBudgetBucketAsListByName(key));
    }

    /**
     * Get an budget bucket by id.
     * 
     * @param id
     *            the budget bucket id
     */
    public static BudgetBucket getBudgetBucketById(Long id) {
        return BudgetBucketDAO.findBudgetBucket.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get the budget buckets list with filter.
     * 
     * @param isActive
     *            filter on BudgetBucketDAO active flag
     * @param isApproved
     *            filter on BudgetBucketDAO approved flag
     **/
    public static List<BudgetBucket> getBudgetBucketAsListByActiveAndApproved(Boolean isActive, Boolean isApproved) {

        ExpressionList<BudgetBucket> e = BudgetBucketDAO.findBudgetBucket.where().eq("deleted", false);

        if (isActive != null) {
            e = e.eq("isActive", isActive);
        }
        if (isApproved != null) {
            e = e.eq("isApproved", isApproved);
        }

        return e.findList();
    }

    /**
     * Get the total budget in the default currency of a budget bucket.
     * 
     * @param budgetBucketId
     *            the budget bucket id
     * @param isOpex
     *            set to true for OPEX budget, else CAPEX
     */
    public static Double getBudgetAsAmountByBucketAndOpex(Long budgetBucketId, boolean isOpex) {

        String sql =
                "SELECT SUM(bbl.amount) as totalAmount FROM budget_bucket_line bbl WHERE bbl.deleted = 0 AND bbl.currency_code = '"
                        + CurrencyDAO.getCurrencyDefault().code + "' AND bbl.is_opex = " + isOpex + " AND bbl.budget_bucket_id = " + budgetBucketId;

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalAmount> query = Ebean.find(TotalAmount.class);

        Double totalAmount = query.setRawSql(rawSql).findUnique().totalAmount;

        if (totalAmount == null) {
            return 0.0;
        }

        return totalAmount;
    }

    /**
     * get the lines of a budget bucket as expression list.
     * 
     * @param budgetBucketId
     *            the budget bucket id
     */
    public static ExpressionList<BudgetBucketLine> getBudgetBucketLineAsExprByBucket(Long budgetBucketId) {
        return BudgetBucketDAO.findBudgetBucketLine.where().eq("deleted", false).eq("budgetBucket.id", budgetBucketId);
    }

    /**
     * get the lines of a budget bucket as pagination.
     * 
     * @param budgetBucketId
     *            the budget bucket id
     */
    public static Pagination<BudgetBucketLine> getBudgetBucketLineAsPaginationByBucket(Long budgetBucketId) {
        return new Pagination<>(getBudgetBucketLineAsExprByBucket(budgetBucketId));
    }

    /**
     * Get an budget bucket line by id.
     * 
     * @param id
     *            the budget bucket line id
     */
    public static BudgetBucketLine getBudgetBucketLineById(Long id) {
        return BudgetBucketDAO.findBudgetBucketLine.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * @param budgetBucketId
     *            the budget bucket line id
     * 
     * @return budget bucket lines list
     **/
    public static List<BudgetBucketLine> getBudgetBucketLineAsListByBucket(Long budgetBucketId) {
        return BudgetBucketDAO.findBudgetBucketLine.where().eq("deleted", false).eq("budgetBucket.id", budgetBucketId).findList();
    }

}
