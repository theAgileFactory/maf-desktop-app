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

import javax.persistence.PersistenceException;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Finder;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import dao.finance.PurchaseOrderDAO;
import framework.services.account.IPreferenceManagerPlugin;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Pagination;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryDependency;
import models.pmo.PortfolioEntryDependencyType;
import models.pmo.PortfolioEntryType;
import models.sql.TotalAmount;

/**
 * DAO for the {@link PortfolioEntry} and {@link PortfolioEntryType} objects.
 * 
 * @author Johann Kohler
 */
public abstract class PortfolioEntryDao {

    public static Finder<Long, PortfolioEntry> findPortfolioEntry = new Finder<>(PortfolioEntry.class);

    public static Finder<Long, PortfolioEntryType> findPortfolioEntryType = new Finder<>(PortfolioEntryType.class);

    public static Finder<Long, PortfolioEntryDependencyType> findPortfolioEntryDependencyType = new Finder<>(PortfolioEntryDependencyType.class);

    public static Finder<Long, PortfolioEntryDependency> findPortfolioEntryDependency = new Finder<>(PortfolioEntryDependency.class);

    /**
     * Default constructor.
     */
    public PortfolioEntryDao() {
    }

    /**
     * Get a portfolio entry by id.
     * 
     * @param id
     *            the portfolio entry id
     */
    public static PortfolioEntry getPEById(Long id) {
        return findPortfolioEntry.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get a deleted portfolio entry by id.
     * 
     * @param id
     *            the portfolio entry id
     */
    public static PortfolioEntry getPEDeletedById(Long id) {
        return findPortfolioEntry.where().eq("deleted", true).eq("id", id).findUnique();
    }

    /**
     * Get a portfolio entry by erp ref id.
     * 
     * @param erpRefId
     *            the erp ref id
     */
    public static PortfolioEntry getPEByErpRefId(String erpRefId) {
        try {
            return findPortfolioEntry.where().eq("deleted", false).eq("erpRefId", erpRefId).findUnique();
        } catch (PersistenceException e) {
            return findPortfolioEntry.where().eq("deleted", false).eq("erpRefId", erpRefId).findList().get(0);
        }
    }
    
    /**
     * Get a portfolio entry by governanceId
     * 
     * @param governanceId
     *            the governanceId
     */
    public static PortfolioEntry getPEByGovernanceId(String governanceId) {
        try {
            return findPortfolioEntry.where().eq("deleted", false).eq("governanceId", governanceId).findUnique();
        } catch (PersistenceException e) {
            return findPortfolioEntry.where().eq("deleted", false).eq("governanceId", governanceId).findList().get(0);
        }
    }

    /**
     * Get the budget of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     * @param isOpex
     *            set to true for OPEX value, else CAPEX
     */
    public static Double getPEAsBudgetAmountByOpex(Long id, boolean isOpex) {
        return getPEAsBudgetAmountByOpex(id, isOpex, null);
    }

    /**
     * Get the budget of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     * @param isOpex
     *            set to true for OPEX value, else CAPEX
     * @param onlyEffort
     *            true for only effort (from allocation), false for only cost
     *            (direct), null for all
     */
    public static Double getPEAsBudgetAmountByOpex(Long id, boolean isOpex, Boolean onlyEffort) {

        String sql = "SELECT SUM(pebl.amount * pebl.currency_rate) as totalAmount FROM portfolio_entry_budget_line pebl "
                + "JOIN portfolio_entry_budget peb ON pebl.portfolio_entry_budget_id=peb.id "
                + "JOIN life_cycle_instance_planning lcip ON peb.id = lcip.portfolio_entry_budget_id "
                + "JOIN portfolio_entry pe ON lcip.life_cycle_instance_id = pe.active_life_cycle_instance_id " + "WHERE pebl.deleted=0 AND pebl.is_opex="
                + isOpex + " AND peb.deleted=0 AND lcip.deleted=0 AND lcip.is_frozen=0 " + "AND pe.id=" + id;

        if (onlyEffort != null) {
            if (onlyEffort) {
                sql += " AND pebl.resource_object_type IS NOT NULL";
            } else {
                sql += " AND pebl.resource_object_type IS NULL";
            }
        }

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalAmount> query = Ebean.find(TotalAmount.class);

        Double totalAmount = query.setRawSql(rawSql).findUnique().totalAmount;

        if (totalAmount == null) {
            return 0.0;
        }

        return totalAmount;
    }

    /**
     * Get the "cost to complete" of a portfolio entry.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param id
     *            the portfolio entry id
     * @param isOpex
     *            set to true for OPEX value, else CAPEX
     */
    public static Double getPEAsCostToCompleteAmountByOpex(IPreferenceManagerPlugin preferenceManagerPlugin, Long id, boolean isOpex) {
        return getPEAsCostToCompleteAmountByOpex(preferenceManagerPlugin, id, isOpex, null);
    }

    /**
     * Get the "cost to complete" of a portfolio entry.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param id
     *            the portfolio entry id
     * @param isOpex
     *            set to true for OPEX value, else CAPEX
     * @param onlyEffort
     *            true for only effort (from allocation), false for only cost
     *            (direct), null for all
     */
    public static Double getPEAsCostToCompleteAmountByOpex(IPreferenceManagerPlugin preferenceManagerPlugin, Long id, boolean isOpex, Boolean onlyEffort) {

        String baseSqlSelect = "SELECT SUM(wo.amount * wo.currency_rate) AS totalAmount FROM work_order wo "
                + "JOIN portfolio_entry pe ON wo.portfolio_entry_id = pe.id ";

        String baseSqlCond = " AND wo.deleted=0 AND wo.is_opex=" + isOpex + " AND pe.id=" + id;

        if (onlyEffort != null) {
            if (onlyEffort) {
                baseSqlCond += " AND wo.resource_object_type IS NOT NULL";
            } else {
                baseSqlCond += " AND wo.resource_object_type IS NULL";
            }
        }

        List<String> sqls = new ArrayList<>();

        if (PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(preferenceManagerPlugin)) {
            // if purchase orders are enable

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
     * Get the engaged amount of a portfolio entry.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param id
     *            the portfolio entry id
     * @param isOpex
     *            set to true for OPEX value, else CAPEX
     */
    public static Double getPEAsEngagedAmountByOpex(IPreferenceManagerPlugin preferenceManagerPlugin, Long id, boolean isOpex) {
        return getPEAsEngagedAmountByOpex(preferenceManagerPlugin, id, isOpex, null);
    }

    /**
     * Get the engaged amount of a portfolio entry.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param id
     *            the portfolio entry id
     * @param isOpex
     *            set to true for OPEX value, else CAPEX
     * @param onlyEffort
     *            true for only effort (from allocation), false for only cost
     *            (direct), null for all
     */
    public static Double getPEAsEngagedAmountByOpex(IPreferenceManagerPlugin preferenceManagerPlugin, Long id, boolean isOpex, Boolean onlyEffort) {

        String baseWOSqlSelect = "SELECT SUM(wo.amount * wo.currency_rate) AS totalAmount FROM work_order wo "
                + "JOIN portfolio_entry pe ON wo.portfolio_entry_id = pe.id ";

        String baseWOSqlCond = " AND wo.deleted=0 AND wo.is_opex=" + isOpex + " AND pe.id=" + id;

        if (onlyEffort != null) {
            if (onlyEffort) {
                baseWOSqlCond += " AND wo.resource_object_type IS NOT NULL";
            } else {
                baseWOSqlCond += " AND wo.resource_object_type IS NULL";
            }
        }

        List<String> sqls = new ArrayList<>();

        if (PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(preferenceManagerPlugin)) {
            // if purchase orders are enable

            // either the work orders with an active purchase order line
            sqls.add(baseWOSqlSelect
                    + " JOIN purchase_order_line_item poli ON wo.purchase_order_line_item_id = poli.id WHERE poli.deleted=0 AND poli.is_cancelled=0"
                    + baseWOSqlCond);

            // or the purchase order lines assigned to an entry of the portfolio
            // but never engaged by a work order
            if (onlyEffort == null || !onlyEffort) {
                sqls.add("SELECT SUM(poli.amount * poli.currency_rate) AS totalAmount FROM purchase_order_line_item poli "
                        + "JOIN purchase_order po ON poli.purchase_order_id=po.id " + "JOIN portfolio_entry pe ON po.portfolio_entry_id = pe.id "
                        + "LEFT OUTER JOIN work_order wo ON poli.id=wo.purchase_order_line_item_id "
                        + "WHERE poli.deleted=0 AND poli.is_cancelled=0 AND poli.is_opex=" + isOpex + " AND po.deleted=0 AND po.is_cancelled=0 AND pe.id="
                        + id + " AND wo.purchase_order_line_item_id IS NULL");
            }

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
     * Get all portfolio entries as expression.
     * 
     * @param viewArchived
     *            set to true if the archived entries must be also returned
     */
    public static ExpressionList<PortfolioEntry> getPEAsExpr(boolean viewArchived) {
        ExpressionList<PortfolioEntry> e = findPortfolioEntry.where().eq("deleted", false);
        if (!viewArchived) {
            e = e.eq("archived", false);
        }
        return e;
    }

    /**
     * Get the number of consumed licenses of PE.
     */
    public static int getPEAsNumberConsumedLicenses() {
        return findPortfolioEntry.where().eq("deleted", false).eq("archived", false).eq("isSyndicated", false).findRowCount();
    }

    /**
     * Define if an actor is the manager of at least one portfolio of a
     * portfolio entry.
     * 
     * @param actorId
     *            the actor id
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static Boolean isPortfolioManagerOfPE(Long actorId, Long portfolioEntryId) {
        return findPortfolioEntry.where().eq("deleted", false).eq("id", portfolioEntryId).eq("portfolios.deleted", false).eq("portfolios.manager.id", actorId)
                .eq("portfolios.manager.deleted", false).findRowCount() > 0;
    }

    /**
     * Define if an actor is at least one portfolio stakeholder of a portfolio
     * entry.
     * 
     * Note: if the actor is only a direct stakeholder of the portfolio entry,
     * this method return false
     * 
     * @param actorId
     *            the actor id
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static Boolean isPortfolioStakeholderOfPE(Long actorId, Long portfolioEntryId) {
        return findPortfolioEntry.where().eq("deleted", false).eq("id", portfolioEntryId).eq("portfolios.deleted", false)
                .eq("portfolios.stakeholders.deleted", false).eq("portfolios.stakeholders.actor.id", actorId).findRowCount() > 0;
    }

    /**
     * Return true if the given org unit is a delivery unit of the portfolio
     * entry.
     * 
     * @param orgUnitId
     *            the org unit id
     * @param portfolioEntryId
     *            the delivery id
     */
    public static boolean isDeliveryUnitOfPE(Long orgUnitId, Long portfolioEntryId) {
        return findPortfolioEntry.where().eq("deleted", false).eq("id", portfolioEntryId).eq("deliveryUnits.deleted", false).eq("deliveryUnits.id", orgUnitId)
                .findRowCount() > 0;
    }

    /**
     * Get all portfolio entries as pagination object for which an actor is the
     * manager.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param actorId
     *            the actor id
     * @param viewArchived
     *            set to true if the archived entries must be also returned
     */
    public static Pagination<PortfolioEntry> getPEAsPaginationByManager(IPreferenceManagerPlugin preferenceManagerPlugin, Long actorId,
            Boolean viewArchived) {
        return new Pagination<>(preferenceManagerPlugin, getPEAsExpr(viewArchived).eq("manager.id", actorId));
    }

    /**
     * Get the portfolio entries of a portfolio as pagination object.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param portfolioId
     *            the portfolio id
     * @param viewArchived
     *            set to true if the archived entries must be also returned
     */
    public static Pagination<PortfolioEntry> getPEAsPaginationByPortfolio(IPreferenceManagerPlugin preferenceManagerPlugin, Long portfolioId,
            Boolean viewArchived) {
        return new Pagination<>(preferenceManagerPlugin, getPEAsExpr(viewArchived).eq("portfolios.id", portfolioId));
    }

    /**
     * Get all active portfolio entries as pagination object for which an actor
     * is a direct stakeholder.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param actorId
     *            the actor id
     */
    public static Pagination<PortfolioEntry> getPEActiveAsPaginationByDirectStakeholder(IPreferenceManagerPlugin preferenceManagerPlugin, Long actorId) {
        return new Pagination<>(preferenceManagerPlugin,
                findPortfolioEntry.where().eq("deleted", false).eq("archived", false).eq("stakeholders.actor.id", actorId).eq("stakeholders.deleted", false));
    }

    /**
     * Get all portfolio entries for which an actor is part (manager, direct
     * stakeholder, portfolio stakeholder, delivery unit manager, delivery unit
     * member).
     * 
     * @param actorId
     *            the actor id
     */
    public static List<PortfolioEntry> getPEAsListByMember(Long actorId) {
        String raw = "deleted=false AND (";
        raw += "manager.id=" + actorId + " OR ";
        raw += "(stakeholders.deleted=false AND stakeholders.actor.id=" + actorId + ") OR ";
        raw += "(portfolios.deleted=false AND portfolios.manager.id=" + actorId + ") OR ";
        raw += "(portfolios.deleted=false AND portfolios.stakeholders.deleted=false AND portfolios.stakeholders.actor.id=" + actorId + ") OR ";
        raw += "(deliveryUnits.deleted=false AND deliveryUnits.manager.id=" + actorId + ") OR ";
        raw += "(deliveryUnits.deleted=false AND deliveryUnits.actors.id=" + actorId + ")";
        raw += ")";
        return findPortfolioEntry.where().raw(raw).findList();
    }

    /**
     * Get all active portfolio entries as pagination object for which an actor
     * is manager or direct stakeholder.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param actorId
     *            the actor id
     */
    public static Pagination<PortfolioEntry> getPEActiveAsPaginationByManagerOrDirectStakeholder(IPreferenceManagerPlugin preferenceManagerPlugin,
            Long actorId) {
        String raw = "deleted=false AND archived=false AND (";
        raw += "manager.id=" + actorId + " OR ";
        raw += "(stakeholders.deleted=false AND stakeholders.actor.id=" + actorId + ")";
        raw += ")";
        ExpressionList<PortfolioEntry> expression = findPortfolioEntry.where().raw(raw);
        return new Pagination<>(preferenceManagerPlugin, expression.findList().size(), expression);
    }

    /**
     * Get the active portfolio entries of an org unit (sponsoring or delivery).
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param orgUnitId
     *            the org unit id
     */
    public static Pagination<PortfolioEntry> getPEActiveAsPaginationByOrgUnit(IPreferenceManagerPlugin preferenceManagerPlugin, Long orgUnitId) {
        String raw = "deleted=false AND archived=false AND (";
        raw += "sponsoringUnit.id=" + orgUnitId + " OR ";
        raw += "(deliveryUnits.deleted=false AND deliveryUnits.id=" + orgUnitId + ")";
        raw += ")";
        ExpressionList<PortfolioEntry> expression = findPortfolioEntry.where().raw(raw);
        return new Pagination<>(preferenceManagerPlugin, expression.findList().size(), expression);
    }

    /**
     * Get the portfolio entries of a portfolio with a red last report.<br/>
     * 
     * @param portfolioId
     *            the portfolio id
     */
    public static List<PortfolioEntry> getPERedAsListByPortfolio(Long portfolioId) {
        return findPortfolioEntry.where().eq("deleted", false).eq("lastPortfolioEntryReport.portfolioEntryReportStatusType.cssClass", "danger")
                .eq("portfolios.id", portfolioId).findList();
    }

    /**
     * Get the last governance id as an integer.
     * 
     * If there is no portfolio entry, then return null.
     * 
     * If the last governance id is not an int, then return the id.
     */
    public static Integer getPEAsLastGovernanceId() {
        PortfolioEntry lastPortfolioEntry = findPortfolioEntry.orderBy("id DESC").where().setMaxRows(1).findUnique();
        if (lastPortfolioEntry == null) {
            return null;
        } else {
            int lastGovernanceId;
            try {
                lastGovernanceId = Integer.parseInt(lastPortfolioEntry.governanceId);
            } catch (NumberFormatException e) {
                lastGovernanceId = lastPortfolioEntry.id.intValue();
            }
            return lastGovernanceId;
        }

    }

    /**
     * Get the number of active entries of a portfolio.
     * 
     * @param portfolioId
     *            the portfolio id
     */
    public static int getPEAsNbActiveByPortfolio(Long portfolioId) {
        return findPortfolioEntry.where().eq("deleted", false).eq("archived", false).eq("portfolios.id", portfolioId).findRowCount();
    }

    /**
     * Get the number of active and non-conceptual entries of a portfolio.
     * 
     * @param portfolioId
     *            the portfolio id
     */
    public static int getPEAsNbActiveNonConceptualByPortfolio(Long portfolioId) {
        return findPortfolioEntry.where().eq("deleted", false).eq("archived", false).eq("portfolios.id", portfolioId)
                .eq("activeLifeCycleInstance.isConcept", false).findRowCount();
    }

    /**
     * Get the number of active and conceptual entries of a portfolio.
     * 
     * @param portfolioId
     *            the portfolio id
     */
    public static int getPEAsNbActiveConceptualByPortfolio(Long portfolioId) {
        return findPortfolioEntry.where().eq("deleted", false).eq("archived", false).eq("portfolios.id", portfolioId)
                .eq("activeLifeCycleInstance.isConcept", true).findRowCount();
    }

    /**
     * Get the portfolio entries list with filter.
     * 
     * @param managerId
     *            if not null then return only portfolio entries for the given
     *            manager.
     * @param sponsoringUnitId
     *            if not null then return only portfolio entries with the given
     *            sponsoring unit.
     * @param deliveryUnitId
     *            if not null then return only portfolio entries with the given
     *            delivery unit.
     * @param portfolioId
     *            if not null then return only portfolio entries belonging to
     *            the given portfolio.
     * @param archived
     *            true to return only archived portfolio entries, false only
     *            active, null all.
     * @param portfolioEntryTypeId
     *            if not null then return only portfolio entries with the given
     *            type.
     * @param isPublic
     *            true to return only public portfolio entries, false only
     *            confidential, null all.
     */
    public static List<PortfolioEntry> getPEAsListByFilter(Long managerId, Long sponsoringUnitId, Long deliveryUnitId, Long portfolioId, Boolean archived,
            Long portfolioEntryTypeId, Boolean isPublic) {

        ExpressionList<PortfolioEntry> e = findPortfolioEntry.where().eq("deleted", false);
        if (managerId != null) {
            e = e.eq("manager.id", managerId);
        }
        if (sponsoringUnitId != null) {
            e = e.eq("sponsoringUnit.id", sponsoringUnitId);
        }
        if (deliveryUnitId != null) {
            e = e.eq("deliveryUnits.id", deliveryUnitId);
        }
        if (portfolioId != null) {
            e = e.eq("portfolios.id", portfolioId);
        }
        if (archived != null) {
            e = e.eq("archived", archived);
        }
        if (portfolioEntryTypeId != null) {
            e = e.eq("portfolioEntryType.id", portfolioEntryTypeId);
        }
        if (isPublic != null) {
            e = e.eq("isPublic", isPublic);
        }

        return e.findList();
    }

    /**
     * Get a portfolio entry type by id.
     * 
     * @param id
     *            the portfolio entry type id
     */
    public static PortfolioEntryType getPETypeById(Long id) {
        return findPortfolioEntryType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all initiative types.
     */
    public static List<PortfolioEntryType> getPETypeInitiativeAsList() {
        return findPortfolioEntryType.where().eq("deleted", false).eq("isRelease", false).findList();
    }

    /**
     * Get all release types.
     */
    public static List<PortfolioEntryType> getPETypeReleaseAsList() {
        return findPortfolioEntryType.where().eq("deleted", false).eq("isRelease", true).findList();
    }

    /**
     * Get all selectable portfolio entry types as a list.
     */
    public static List<PortfolioEntryType> getPETypeActiveAsList() {
        return findPortfolioEntryType.where().eq("deleted", false).eq("selectable", true).findList();
    }

    /**
     * Get all selectable portfolio entry types as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getPETypeActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getPETypeActiveAsList());
    }

    /**
     * Get active portfolio entry types as value holder collection.
     * 
     * @param isRelease
     *            true for release, false for initiative
     */
    public static ISelectableValueHolderCollection<Long> getPETypeActiveAsVH(boolean isRelease) {
        return new DefaultSelectableValueHolderCollection<>(
                findPortfolioEntryType.where().eq("deleted", false).eq("selectable", true).eq("isRelease", isRelease).findList());
    }

    /**
     * Get the portfolio entry types list with filters.
     * 
     * @param isActive
     *            true to return only active portfolio entry type, false only
     *            non-active, null all.
     * @param isRelease
     *            true to return only release portfolio entry type, false only
     *            initiative, null all.
     */
    public static List<PortfolioEntryType> getPETypeAsListByFilter(Boolean isActive, Boolean isRelease) {
        ExpressionList<PortfolioEntryType> e = findPortfolioEntryType.where().eq("deleted", false);
        if (isActive != null) {
            e = e.eq("isActive", isActive);
        }
        if (isRelease != null) {
            e = e.eq("isRelease", isRelease);
        }
        return e.findList();
    }

    /**
     * Get a portfolio entry dependency type by id.
     * 
     * @param id
     *            the portfolio entry dependency type id
     */
    public static PortfolioEntryDependencyType getPEDependencyTypeById(Long id) {
        return findPortfolioEntryDependencyType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all portfolio entry dependency types.
     */
    public static List<PortfolioEntryDependencyType> getPEDependencyTypeAsList() {
        return findPortfolioEntryDependencyType.where().eq("deleted", false).findList();
    }

    /**
     * Get all active portfolio entry dependency types as a list.
     */
    public static List<PortfolioEntryDependencyType> getPEDependencyTypeActiveAsList() {
        return findPortfolioEntryDependencyType.where().eq("deleted", false).eq("isActive", true).findList();
    }

    /**
     * Get all active portfolio entry dependency types, including the contrary,
     * as value holder collection.
     */
    public static ISelectableValueHolderCollection<String> getPEDependencyTypeActiveWithContraryAsVH() {
        ISelectableValueHolderCollection<String> types = new DefaultSelectableValueHolderCollection<String>();
        int i = 0;
        for (PortfolioEntryDependencyType type : getPEDependencyTypeActiveAsList()) {

            DefaultSelectableValueHolder<String> vh1 = new DefaultSelectableValueHolder<String>(type.id + "#false", Msg.get(type.getNameKey()));
            vh1.setOrder(i);
            i++;
            types.add(vh1);

            DefaultSelectableValueHolder<String> vh2 = new DefaultSelectableValueHolder<String>(type.id + "#true", Msg.get(type.contrary));
            vh2.setOrder(i);
            i++;
            types.add(vh2);

        }
        return types;
    }

    /**
     * Get a portfolio entry dependency by id.
     * 
     * @param peDepSourceId
     *            the portfolio entry dependency id (source part)
     * @param peDepDestinationId
     *            the portfolio entry dependency id (destination part)
     * @param peDepTypeId
     *            the portfolio entry dependency id (type part)
     */
    public static PortfolioEntryDependency getPEDependencyById(Long peDepSourceId, Long peDepDestinationId, Long peDepTypeId) {
        return findPortfolioEntryDependency.where().eq("sourcePortfolioEntry.id", peDepSourceId).eq("destinationPortfolioEntry.id", peDepDestinationId)
                .eq("portfolioEntryDependencyType.id", peDepTypeId).findUnique();
    }

    /**
     * Get all dependencies (as source or destination) of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<PortfolioEntryDependency> getPEDependencyAsList(Long portfolioEntryId) {
        return findPortfolioEntryDependency.where().eq("sourcePortfolioEntry.deleted", false).eq("destinationPortfolioEntry.deleted", false)
                .eq("portfolioEntryDependencyType.deleted", false).disjunction().eq("sourcePortfolioEntry.id", portfolioEntryId)
                .eq("destinationPortfolioEntry.id", portfolioEntryId).findList();
    }

}
