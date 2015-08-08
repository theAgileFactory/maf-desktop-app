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

import java.util.ArrayList;
import java.util.List;

import models.pmo.Portfolio;
import models.pmo.PortfolioType;
import models.sql.TotalAmount;
import com.avaje.ebean.Model.Finder;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import dao.finance.CurrencyDAO;
import dao.finance.PurchaseOrderDAO;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;

/**
 * DAO for the {@link Portfolio} and {@link PortfolioType} objects.
 * 
 * @author Johann Kohler
 */
public abstract class PortfolioDao {

    public static Finder<Long, Portfolio> findPortfolio = new Finder<>(Portfolio.class);

    public static Finder<Long, PortfolioType> findPortfolioType = new Finder<>(PortfolioType.class);

    /**
     * Default constructor.
     */
    public PortfolioDao() {
    }

    /**
     * Get a portfolio by id.
     * 
     * @param id
     *            the portfolio id
     */
    public static Portfolio getPortfolioById(Long id) {
        return findPortfolio.where().eq("id", id).eq("deleted", false).findUnique();
    }

    /**
     * Get all portfolios.
     */
    public static List<Portfolio> getPortfolioAsList() {
        return findPortfolio.where().eq("deleted", false).findList();
    }

    /**
     * Get all active portfolios.
     */
    public static List<Portfolio> getPortfolioActiveAsList() {
        return findPortfolio.where().eq("deleted", false).eq("isActive", true).findList();
    }

    /**
     * Get all portfolios as value holder collection.
     */
    public static DefaultSelectableValueHolderCollection<Long> getPortfolioAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getPortfolioAsList());
    }

    /**
     * Get all active portfolios as value holder collection.
     */
    public static DefaultSelectableValueHolderCollection<Long> getPortfolioActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getPortfolioActiveAsList());
    }

    /**
     * Get the total portfolio entry budget in the default currency of a
     * portfolio.
     * 
     * @param id
     *            the portfolio id
     * @param isOpex
     *            set to true for OPEX budget, else CAPEX
     */
    public static Double getPortfolioAsBudgetAmountByOpex(Long id, boolean isOpex) {

        String sql =
                "SELECT SUM(pebl.amount) as totalAmount FROM portfolio_entry_budget_line pebl "
                        + "JOIN portfolio_entry_budget peb ON pebl.portfolio_entry_budget_id=peb.id "
                        + "JOIN life_cycle_instance_planning lcip ON peb.id = lcip.portfolio_entry_budget_id "
                        + "JOIN portfolio_entry pe ON lcip.life_cycle_instance_id = pe.active_life_cycle_instance_id "
                        + "JOIN portfolio_has_portfolio_entry phpe ON pe.id = phpe.portfolio_entry_id " + "WHERE pebl.deleted=0 AND pebl.is_opex=" + isOpex
                        + " AND pebl.currency_code='" + CurrencyDAO.getCurrencyDefault().code + "' "
                        + "AND peb.deleted=0 AND lcip.deleted=0 AND lcip.is_frozen=0 " + "AND pe.deleted=0 AND pe.archived=0 AND phpe.portfolio_id=" + id;

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalAmount> query = Ebean.find(TotalAmount.class);

        Double totalAmount = query.setRawSql(rawSql).findUnique().totalAmount;

        if (totalAmount == null) {
            return 0.0;
        }

        return totalAmount;
    }

    /**
     * Get the total portfolio entries "cost to complete" (not engaged work
     * orders) in the default currency of a portfolio.
     * 
     * @param id
     *            the portfolio id
     * @param isOpex
     *            set to true for OPEX budget, else CAPEX
     */
    public static Double getPortfolioAsCostToCompleteAmountByOpex(Long id, boolean isOpex) {

        String baseSqlSelect =
                "SELECT SUM(wo.amount) AS totalAmount FROM work_order wo " + "JOIN portfolio_entry pe ON wo.portfolio_entry_id = pe.id "
                        + "JOIN portfolio_has_portfolio_entry phpe ON wo.portfolio_entry_id = phpe.portfolio_entry_id";

        String baseSqlCond =
                " AND wo.deleted=0 AND wo.is_opex=" + isOpex + " AND wo.currency_code='" + CurrencyDAO.getCurrencyDefault().code
                        + "' AND pe.deleted=0 AND pe.archived=0 AND phpe.portfolio_id=" + id;

        List<String> sqls = new ArrayList<>();

        if (PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder()) { // if
                                                                     // purchase
                                                                     // orders
                                                                     // are
                                                                     // enable

            // either the work orders without purchase order line
            sqls.add(baseSqlSelect + " WHERE wo.purchase_order_line_item_id IS NULL" + baseSqlCond);

            // or with one but cancelled
            sqls.add(baseSqlSelect
                    + " JOIN purchase_order_line_item poli ON wo.purchase_order_line_item_id = poli.id WHERE (poli.deleted=1 OR poli.is_cancelled=1) "
                    + baseSqlCond);

        } else { // if purchase orders are not enable
            // the is_engaged flag is settled to false
            sqls.add(baseSqlSelect + " WHERE wo.is_engaged=0" + baseSqlCond);
        }

        Double totalAmount = 0.0;
        for (String sql : sqls) {
            RawSql rawSql = RawSqlBuilder.parse(sql).create();
            Query<TotalAmount> query = Ebean.find(TotalAmount.class);
            Double totalAmountLoop = query.setRawSql(rawSql).findUnique().totalAmount;
            if (totalAmountLoop != null) {
                totalAmount += totalAmountLoop;
            }
        }

        return totalAmount;
    }

    /**
     * Get the total portfolio entry engaged amount (=engaged work orders +
     * "free" purchase order line items assigned to the PE) in the default
     * currency of a portfolio.
     * 
     * @param id
     *            the portfolio id
     * @param isOpex
     *            set to true for OPEX budget, else CAPEX
     */
    public static Double getPortfolioAsEngagedAmountByOpex(Long id, boolean isOpex) {

        String baseWOSqlSelect =
                "SELECT SUM(wo.amount) AS totalAmount FROM work_order wo " + "JOIN portfolio_entry pe ON wo.portfolio_entry_id = pe.id "
                        + "JOIN portfolio_has_portfolio_entry phpe ON wo.portfolio_entry_id = phpe.portfolio_entry_id";

        String baseWOSqlCond =
                " AND wo.deleted=0 AND wo.is_opex=" + isOpex + " AND wo.currency_code='" + CurrencyDAO.getCurrencyDefault().code
                        + "' AND pe.deleted=0 AND pe.archived=0 AND phpe.portfolio_id=" + id;

        List<String> sqls = new ArrayList<>();

        if (PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder()) { // if
                                                                     // purchase
                                                                     // orders
                                                                     // are
                                                                     // enable

            // either the work orders with an active purchase order line
            sqls.add(baseWOSqlSelect
                    + " JOIN purchase_order_line_item poli ON wo.purchase_order_line_item_id = poli.id WHERE poli.deleted=0 AND poli.is_cancelled=0"
                    + baseWOSqlCond);

            // or the purchase order lines assigned to an entry of the portfolio
            // but never engaged by a work order
            sqls.add("SELECT SUM(poli.amount) AS totalAmount FROM purchase_order_line_item poli " + "JOIN purchase_order po ON poli.purchase_order_id=po.id "
                    + "JOIN portfolio_entry pe ON po.portfolio_entry_id = pe.id "
                    + "JOIN portfolio_has_portfolio_entry phpe ON pe.id = phpe.portfolio_entry_id "
                    + "LEFT OUTER JOIN work_order wo ON poli.id=wo.purchase_order_line_item_id "
                    + "WHERE poli.deleted=0 AND poli.is_cancelled=0 AND poli.currency_code='" + CurrencyDAO.getCurrencyDefault().code + "' AND poli.is_opex="
                    + isOpex + " AND po.deleted=0 AND po.is_cancelled=0 AND pe.deleted=0 AND pe.archived=0 AND phpe.portfolio_id=" + id
                    + " AND wo.purchase_order_line_item_id IS NULL");

        } else { // if purchase orders are not enable
            // the is_engaged flag is settled to true
            sqls.add(baseWOSqlSelect + " WHERE wo.is_engaged=1" + baseWOSqlCond);
        }

        Double totalAmount = 0.0;
        for (String sql : sqls) {
            RawSql rawSql = RawSqlBuilder.parse(sql).create();
            Query<TotalAmount> query = Ebean.find(TotalAmount.class);
            Double totalAmountLoop = query.setRawSql(rawSql).findUnique().totalAmount;
            if (totalAmountLoop != null) {
                totalAmount += totalAmountLoop;
            }
        }

        return totalAmount;
    }

    /**
     * Get all active portfolios as pagination object for which an actor is a
     * stakeholder.
     * 
     * @param actorId
     *            the actor id
     */
    public static Pagination<Portfolio> getPortfolioActiveAsPaginationByStakeholder(Long actorId) {
        return new Pagination<>(findPortfolio.where().eq("isActive", true).eq("stakeholders.actor.id", actorId).eq("deleted", false)
                .eq("stakeholders.deleted", false));
    }

    /**
     * Get all active portfolios for which an actor is the manager.
     * 
     * @param actorId
     *            the actor id
     * @param viewAll
     *            set to true if the inactive portfolios must be also returned
     */
    public static Pagination<Portfolio> getPortfolioAsPaginationByManager(Long actorId, Boolean viewAll) {
        ExpressionList<Portfolio> e = findPortfolio.where().eq("deleted", false).eq("manager.id", actorId);
        if (!viewAll) {
            e = e.eq("isActive", true);
        }
        return new Pagination<>(e);
    }

    /**
     * Search from all portfolios for which the criteria matches the name or the
     * ref id.
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     */
    public static List<Portfolio> getPortfolioAsListByKeywords(String key) {
        return findPortfolio.where().eq("deleted", false).or(Expr.ilike("name", key + "%"), Expr.ilike("refId", key + "%")).findList();
    }

    /**
     * Search from all portfolios with the search process defined by the method
     * "getPortfolioAsListByKeywords" and return a value holder collection.
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     */
    public static DefaultSelectableValueHolderCollection<Long> getPortfolioAsVHByKeywords(String key) {
        return new DefaultSelectableValueHolderCollection<>(getPortfolioAsListByKeywords(key));
    }

    /**
     * Get all active portfolios as pagination object for which an actor is
     * manager or stakeholder.
     * 
     * @param actorId
     *            the actor id
     */
    public static Pagination<Portfolio> getPortfolioActiveAsPaginationByStakeholderOrManager(Long actorId) {
        String raw = "deleted=false AND is_active=true AND (";
        raw += "manager.id=" + actorId + " OR ";
        raw += "(stakeholders.deleted=false AND stakeholders.actor.id=" + actorId + ")";
        raw += ")";
        ExpressionList<Portfolio> expression = findPortfolio.where().raw(raw);
        return new Pagination<>(expression.findList().size(), expression);
    }

    /**
     * Get the portfolios list with filter.
     * 
     * @param isActive
     *            true to return only active portfolios, false only non-active,
     *            null all.
     * @param managerId
     *            if not null then return only portfolios with the given
     *            manager.
     * @param portfolioEntryId
     *            if not null then return only portfolios containing the given
     *            portfolio entry.
     * @param portfolioTypeId
     *            if not null then return only portfolios with the given type.
     */
    public static List<Portfolio> getPortfolioAsListByFilter(Boolean isActive, Long managerId, Long portfolioEntryId, Long portfolioTypeId) {

        ExpressionList<Portfolio> e = findPortfolio.where().eq("deleted", false);
        if (isActive != null) {
            e = e.eq("isActive", isActive);
        }
        if (managerId != null) {
            e = e.eq("manager.id", managerId);
        }
        if (portfolioEntryId != null) {
            e = e.eq("portfolioEntries.id", portfolioEntryId);
        }
        if (portfolioTypeId != null) {
            e = e.eq("portfolioType.id", portfolioTypeId);
        }

        return e.findList();

    }

    /**
     * Get a portfolio type by id.
     * 
     * @param id
     *            the portfolio type id
     */
    public static PortfolioType getPortfolioTypeById(Long id) {
        return findPortfolioType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all portfolio types.
     */
    public static List<PortfolioType> getPortfolioTypeAsList() {
        return findPortfolioType.where().eq("deleted", false).findList();
    }

    /**
     * Get all selectable portfolio types.
     */
    public static List<PortfolioType> getPortfolioTypeActiveAsList() {
        return findPortfolioType.where().eq("deleted", false).eq("selectable", true).findList();
    }

    /**
     * Get all selectable portfolio types as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getPortfolioTypeActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getPortfolioTypeActiveAsList());
    }

    /**
     * Get the portfolio types list with filters.
     * 
     * @param isActive
     *            true to return only active portfolio types, false only
     *            non-active, null all.
     */
    public static List<PortfolioType> getPortfolioTypeAsListByFilter(Boolean isActive) {
        if (isActive != null) {
            return findPortfolioType.where().eq("deleted", false).eq("selectable", isActive).findList();
        } else {
            return getPortfolioTypeAsList();
        }
    }

}
