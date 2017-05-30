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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Finder;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import framework.utils.Pagination;
import models.finance.PortfolioEntryResourcePlan;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.governance.LifeCycleInstancePlanning;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryPlanningPackage;
import models.sql.TotalDays;
import play.Play;

/**
 * DAO for the {@link PortfolioEntryResourcePlan},
 * {@link PortfolioEntryResourcePlanAllocatedActor},
 * {@link PortfolioEntryResourcePlanAllocatedCompetency},
 * {@link PortfolioEntryResourcePlanAllocatedOrgUnit} objects.
 *
 * @author Pierre-Yves Cloux
 */
public abstract class PortfolioEntryResourcePlanDAO {

    public static Finder<Long, PortfolioEntryResourcePlanAllocatedActor> findPEResourcePlanAllocatedActor = new Finder<>(
            PortfolioEntryResourcePlanAllocatedActor.class);

    public static Finder<Long, PortfolioEntryResourcePlanAllocatedCompetency> findPEResourcePlanAllocatedCompetency = new Finder<>(
            PortfolioEntryResourcePlanAllocatedCompetency.class);

    public static Finder<Long, PortfolioEntryResourcePlanAllocatedOrgUnit> findPEResourcePlanAllocatedOrgUnit = new Finder<>(
            PortfolioEntryResourcePlanAllocatedOrgUnit.class);

    /**
     * Default constructor.
     */
    public PortfolioEntryResourcePlanDAO() {
    }

    /**
     * Get the total allocated actors days of a resource plan.
     *
     * @param resourcePlanId
     *            the resource plan id
     */
    public static BigDecimal getPEResourcePlanAsDaysById(Long resourcePlanId) {

        BigDecimal totalDays = BigDecimal.ZERO;

        String sql1 = "SELECT SUM(perpaa.days) as totalDays FROM portfolio_entry_resource_plan_allocated_actor perpaa WHERE perpaa.deleted = 0 "
                + "AND perpaa.portfolio_entry_resource_plan_id = '" + resourcePlanId + "'";
        BigDecimal totalDaysActor = Ebean.find(TotalDays.class).setRawSql(RawSqlBuilder.parse(sql1).create()).findUnique().totalDays;
        if (totalDaysActor != null) {
            totalDays = totalDays.add(totalDaysActor);
        }

        String sql2 = "SELECT SUM(perpaou.days) as totalDays FROM portfolio_entry_resource_plan_allocated_org_unit perpaou WHERE perpaou.deleted = 0 "
                + "AND perpaou.portfolio_entry_resource_plan_id = '" + resourcePlanId + "'";
        BigDecimal totalDaysOrgUnit = Ebean.find(TotalDays.class).setRawSql(RawSqlBuilder.parse(sql2).create()).findUnique().totalDays;
        if (totalDaysOrgUnit != null) {
            totalDays = totalDays.add(totalDaysOrgUnit);
        }

        String sql3 = "SELECT SUM(perpac.days) as totalDays FROM portfolio_entry_resource_plan_allocated_competency perpac WHERE perpac.deleted = 0 "
                + "AND perpac.portfolio_entry_resource_plan_id = '" + resourcePlanId + "'";
        BigDecimal totalDaysCompetency = Ebean.find(TotalDays.class).setRawSql(RawSqlBuilder.parse(sql3).create()).findUnique().totalDays;
        if (totalDaysCompetency != null) {
            totalDays = totalDays.add(totalDaysCompetency);
        }

        return totalDays;
    }

    /**
     * get an allocated actor by id.
     *
     * @param id
     *            the allocated actor id
     */
    public static PortfolioEntryResourcePlanAllocatedActor getPEPlanAllocatedActorById(Long id) {
        return PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedActor.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all allocation of an actor for not archived portfolio entries as a
     * pagination object.
     *
     * @param actorId
     *            the actor id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static Pagination<PortfolioEntryResourcePlanAllocatedActor> getPEPlanAllocatedActorAsPaginationByActorAndActive(Long actorId, boolean activeOnly) {
        return new Pagination<>(getPEPlanAllocatedActorAsExprByActorAndActive(actorId, activeOnly, false), 5,
                Play.application().configuration().getInt("maf.number_page_links"));
    }

    /**
     * Get all allocation of an actor for not archived portfolio entries.
     *
     * @param actorId
     *            the actor id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static List<PortfolioEntryResourcePlanAllocatedActor> getPEPlanAllocatedActorAsListByActorAndActiveAndArchived(Long actorId, boolean activeOnly, boolean withArchived) {
        return getPEPlanAllocatedActorAsExprByActorAndActive(actorId, activeOnly, withArchived).findList();
    }

    /**
     * Get all allocation of an actor for not archived portfolio entries as an
     * expression list.
     *
     * @param actorId
     *            the actor id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static ExpressionList<PortfolioEntryResourcePlanAllocatedActor> getPEPlanAllocatedActorAsExprByActorAndActive(Long actorId, boolean activeOnly, boolean withArchived) {

        ExpressionList<PortfolioEntryResourcePlanAllocatedActor> expr = PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedActor.orderBy("endDate")
                .where().eq("deleted", false).eq("actor.id", actorId).eq("portfolioEntryResourcePlan.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.isFrozen", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.isActive", true)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.deleted", false);

        if (!withArchived) {
            expr = expr.add(Expr.eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.archived", false));
        }

        if (activeOnly) {
            expr = expr.add(Expr.or(Expr.isNull("endDate"), Expr.gt("endDate", new Date())));
        }

        return expr;
    }

    /**
     * Get all allocation of the actors of an org unit for not archived
     * portfolio entries.
     *
     * @param orgUnitId
     *            the org unit id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static List<PortfolioEntryResourcePlanAllocatedActor> getPEPlanAllocatedActorAsListByOrgUnitAndActive(Long orgUnitId, boolean activeOnly) {
        return getPEPlanAllocatedActorAsExprByOrgUnitAndActive(orgUnitId, activeOnly).findList();
    }

    /**
     * Get all allocation of the actors of an org unit for not archived
     * portfolio entries as an expression list.
     *
     * @param orgUnitId
     *            the org unit id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static ExpressionList<PortfolioEntryResourcePlanAllocatedActor> getPEPlanAllocatedActorAsExprByOrgUnitAndActive(Long orgUnitId,
            boolean activeOnly) {

        ExpressionList<PortfolioEntryResourcePlanAllocatedActor> expr = PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedActor.where()
                .eq("deleted", false).eq("actor.isActive", true).eq("actor.deleted", false).eq("actor.orgUnit.id", orgUnitId)
                .eq("portfolioEntryResourcePlan.deleted", false).eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.isFrozen", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.isActive", true)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.archived", false);

        if (activeOnly) {
            expr = expr.add(Expr.or(Expr.isNull("endDate"), Expr.gt("endDate", new Date())));
        }

        return expr;
    }

    /**
     * Get all allocation of the actors from the given manager for not archived
     * portfolio entries.
     *
     * @param actorId
     *            the manager id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static List<PortfolioEntryResourcePlanAllocatedActor> getPEPlanAllocatedActorAsListByManager(Long actorId, boolean activeOnly) {
        return getPEPlanAllocatedActorAsExprByManager(actorId, activeOnly).findList();
    }

    /**
     * Get all allocation of the actors from the given manager for not archived
     * portfolio entries, as an expression list.
     *
     * @param actorId
     *            the manager id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static ExpressionList<PortfolioEntryResourcePlanAllocatedActor> getPEPlanAllocatedActorAsExprByManager(Long actorId, boolean activeOnly) {

        ExpressionList<PortfolioEntryResourcePlanAllocatedActor> expr = PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedActor.where()
                .eq("deleted", false).eq("actor.isActive", true).eq("actor.deleted", false).eq("actor.manager.id", actorId)
                .eq("portfolioEntryResourcePlan.deleted", false).eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.isFrozen", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.isActive", true)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.archived", false);

        if (activeOnly) {
            expr = expr.add(Expr.or(Expr.isNull("endDate"), Expr.gt("endDate", new Date())));
        }

        return expr;
    }

    /**
     * Get the actor allocations for a portfolio entry according to some
     * filters.
     *
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param start
     *            the startDate or the endDate should be after this date
     * @param end
     *            the startDate or the endDate should be before this date
     * @param onlyConfirmed
     *            if true, then return only the confirmed allocations
     * @param orgUnitId
     *            the org unit id
     * @param competencyId
     *            the competency id (for the default)
     */
    public static List<PortfolioEntryResourcePlanAllocatedActor> getPEPlanAllocatedActorAsListByPE(Long portfolioEntryId, Date start, Date end,
            boolean onlyConfirmed, Long orgUnitId, Long competencyId) {
        ExpressionList<PortfolioEntryResourcePlanAllocatedActor> exprAllocatedActors = PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedActor.where()
                .eq("deleted", false).isNotNull("startDate").isNotNull("endDate").le("startDate", end).ge("endDate", start)
                .eq("portfolioEntryResourcePlan.deleted", false).eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.isFrozen", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.isActive", true)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.id", portfolioEntryId);
        if (onlyConfirmed) {
            exprAllocatedActors = exprAllocatedActors.eq("isConfirmed", true);
        }
        if (orgUnitId != null) {
            exprAllocatedActors = exprAllocatedActors.eq("actor.orgUnit.id", orgUnitId);
        }
        if (competencyId != null) {
            exprAllocatedActors = exprAllocatedActors.eq("actor.defaultCompetency.id", competencyId);
        }
        return exprAllocatedActors.findList();
    }

    /**
     * Get the total allocated actors days (for the current resource plan) of a
     * planning package.
     *
     * @param planningPackage
     *            the planning package
     */
    public static BigDecimal getPEPlanAllocatedActorAsDaysByPlanningPackage(PortfolioEntryPlanningPackage planningPackage) {

        // get the current life cycle planning
        LifeCycleInstancePlanning planning = planningPackage.portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        // get the resource plan
        PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

        String sql = "SELECT SUM(perpaa.days) as totalDays FROM portfolio_entry_resource_plan_allocated_actor perpaa WHERE perpaa.deleted = 0 "
                + "AND perpaa.portfolio_entry_resource_plan_id = '" + resourcePlan.id + "' AND perpaa.portfolio_entry_planning_package_id = '"
                + planningPackage.id + "'";

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalDays> query = Ebean.find(TotalDays.class);

        BigDecimal totalDays = query.setRawSql(rawSql).findUnique().totalDays;

        if (totalDays == null) {
            return BigDecimal.ZERO;
        }

        return totalDays;
    }

    /**
     * Get the total allocated actors days (for the current resource plan) of a
     * portfolio entry.
     *
     * @param portfolioEntry
     *            the portfolio entry
     * @param isConfirmed
     *            set to true for confirmed resource; to false for non-confirmed
     *            resource; to null for all
     */
    public static BigDecimal getPEPlanAllocatedActorAsDaysByPEAndConfirmed(PortfolioEntry portfolioEntry, Boolean isConfirmed) {

        // get the current life cycle planning
        LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        // get the resource plan
        PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

        String sql = "SELECT SUM(perpaa.days) as totalDays FROM portfolio_entry_resource_plan_allocated_actor perpaa WHERE perpaa.deleted = 0 "
                + "AND perpaa.portfolio_entry_resource_plan_id = '" + resourcePlan.id + "'";

        if (isConfirmed != null) {
            if (isConfirmed) {
                sql += " AND perpaa.is_confirmed = '1'";
            } else {
                sql += " AND perpaa.is_confirmed = '0'";
            }
        }

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalDays> query = Ebean.find(TotalDays.class);

        BigDecimal totalDays = query.setRawSql(rawSql).findUnique().totalDays;

        if (totalDays == null) {
            return BigDecimal.ZERO;
        }

        return totalDays;
    }

    /**
     * Get the allocated actors of a portfolio entry.
     *
     * @param portfolioEntryId
     *            the portfolio entry id
     **/
    public static List<PortfolioEntryResourcePlanAllocatedActor> getPEPlanAllocatedActorAsListByPE(Long portfolioEntryId) {

        ExpressionList<PortfolioEntryResourcePlanAllocatedActor> exprAllocatedActors = PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedActor.where()
                .eq("deleted", false).eq("portfolioEntryResourcePlan.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.isFrozen", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.isActive", true)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.id", portfolioEntryId);
        return exprAllocatedActors.findList();
    }

    /**
     * Return true if there is at least one allocated actor for the currency.
     *
     * @param currency
     *            the currency code
     */
    public static boolean hasAllocatedActorByCurrency(String currency) {
        return findPEResourcePlanAllocatedActor.where().eq("deleted", false).eq("currency.code", currency).findRowCount() > 0;
    }

    /**
     * get an allocated competency by id.
     *
     * @param id
     *            the allocated competency id
     */
    public static PortfolioEntryResourcePlanAllocatedCompetency getPEResourcePlanAllocatedCompetencyById(Long id) {
        return PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedCompetency.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * get a deleted allocated competency by id.
     *
     * @param id
     *            the deleted allocated competency id
     */
    public static PortfolioEntryResourcePlanAllocatedCompetency getPEResourcePlanAllocatedCompetencyDeletedById(Long id) {
        return PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedCompetency.where().eq("deleted", true).eq("id", id).findUnique();
    }

    /**
     * Get the competencies allocations for a portfolio entry according to some
     * filters.
     *
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param start
     *            the startDate or the endDate should be after this date
     * @param end
     *            the startDate or the endDate should be before this date
     * @param onlyConfirmed
     *            if true, then return only the confirmed allocations
     * @param competencyId
     *            the competency id
     */
    public static List<PortfolioEntryResourcePlanAllocatedCompetency> getPEPlanAllocatedCompetencyAsListByPE(Long portfolioEntryId, Date start, Date end,
            boolean onlyConfirmed, Long competencyId) {
        ExpressionList<PortfolioEntryResourcePlanAllocatedCompetency> exprAllocatedCompetencies = PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedCompetency
                .where().eq("deleted", false).isNotNull("startDate").isNotNull("endDate").le("startDate", end).ge("endDate", start)
                .eq("portfolioEntryResourcePlan.deleted", false).eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.isFrozen", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.isActive", true)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.id", portfolioEntryId);
        if (onlyConfirmed) {
            exprAllocatedCompetencies = exprAllocatedCompetencies.eq("isConfirmed", true);
        }
        if (competencyId != null) {
            exprAllocatedCompetencies = exprAllocatedCompetencies.eq("competency.id", competencyId);
        }
        return exprAllocatedCompetencies.findList();
    }

    /**
     * Get the total allocated competency days (for the current resource plan)
     * of a planning package.
     *
     * @param planningPackage
     *            the planning package
     */
    public static BigDecimal getPEResourcePlanAllocatedCompetencyAsDaysByPlanningPackage(PortfolioEntryPlanningPackage planningPackage) {

        // get the current life cycle planning
        LifeCycleInstancePlanning planning = planningPackage.portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        // get the resource plan
        PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

        String sql = "SELECT SUM(perpac.days) as totalDays FROM portfolio_entry_resource_plan_allocated_competency perpac WHERE perpac.deleted = 0 "
                + "AND perpac.portfolio_entry_resource_plan_id = '" + resourcePlan.id + "' AND perpac.portfolio_entry_planning_package_id = '"
                + planningPackage.id + "'";

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalDays> query = Ebean.find(TotalDays.class);

        BigDecimal totalDays = query.setRawSql(rawSql).findUnique().totalDays;

        if (totalDays == null) {
            return BigDecimal.ZERO;
        }

        return totalDays;
    }

    /**
     * Get the total allocated competency days (for the current resource plan)
     * of a portfolio entry.
     *
     * @param portfolioEntry
     *            the portfolio entry
     * @param isConfirmed
     *            set to true for confirmed resource; to false for non-confirmed
     *            resource; to null for all
     */
    public static BigDecimal getPEResourcePlanAllocatedCompetencyAsDaysByPortfolioEntry(PortfolioEntry portfolioEntry, Boolean isConfirmed) {

        // get the current life cycle planning
        LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        // get the resource plan
        PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

        String sql = "SELECT SUM(perpac.days) as totalDays FROM portfolio_entry_resource_plan_allocated_competency perpac WHERE perpac.deleted = 0 "
                + "AND perpac.portfolio_entry_resource_plan_id = '" + resourcePlan.id + "'";

        if (isConfirmed != null) {
            if (isConfirmed) {
                sql += " AND perpac.is_confirmed = '1'";
            } else {
                sql += " AND perpac.is_confirmed = '0'";
            }
        }

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalDays> query = Ebean.find(TotalDays.class);

        BigDecimal totalDays = query.setRawSql(rawSql).findUnique().totalDays;

        if (totalDays == null) {
            return BigDecimal.ZERO;
        }

        return totalDays;
    }

    /**
     * Get the allocated competencies of a portfolio entry.
     *
     * @param portfolioEntryId
     *            the portfolio entry id
     **/
    public static List<PortfolioEntryResourcePlanAllocatedCompetency> getPEResourcePlanAllocatedCompetencyAsListByPE(Long portfolioEntryId) {
        ExpressionList<PortfolioEntryResourcePlanAllocatedCompetency> exprAllocatedCompetencies = PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedCompetency
                .where().eq("deleted", false).eq("portfolioEntryResourcePlan.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.isFrozen", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.isActive", true)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.id", portfolioEntryId);

        return exprAllocatedCompetencies.findList();
    }

    /**
     * Return true if there is at least one allocated competency for the
     * currency.
     *
     * @param currency
     *            the currency code
     */
    public static boolean hasAllocatedCompetencyByCurrency(String currency) {
        return findPEResourcePlanAllocatedCompetency.where().eq("deleted", false).eq("currency.code", currency).findRowCount() > 0;
    }

    /**
     * get an allocated org unit by id.
     *
     * @param id
     *            the allocated org unit id
     */
    public static PortfolioEntryResourcePlanAllocatedOrgUnit getPEResourcePlanAllocatedOrgUnitById(Long id) {
        return PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedOrgUnit.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * get a deleted allocated org unit by id.
     *
     * @param id
     *            the deleted allocated org unit id
     */
    public static PortfolioEntryResourcePlanAllocatedOrgUnit getPEResourcePlanAllocatedOrgUnitDeletedById(Long id) {
        return PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedOrgUnit.where().eq("deleted", true).eq("id", id).findUnique();
    }

    /**
     * Get all allocation of an org unit for not archived portfolio entries as a
     * pagination object.
     *
     * @param orgUnitId
     *            the org unit id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static Pagination<PortfolioEntryResourcePlanAllocatedOrgUnit> getPEResourcePlanAllocatedOrgUnitAsPaginationByOrgUnit(Long orgUnitId,
            boolean activeOnly) 
    {
    	ExpressionList<PortfolioEntryResourcePlanAllocatedOrgUnit> expressionList = getPEResourcePlanAllocatedOrgUnitAsExprByOrgUnit(orgUnitId, activeOnly);
    	String str = expressionList.query().getGeneratedSql();
        return new Pagination<>(expressionList, 5,
                Play.application().configuration().getInt("maf.number_page_links"));
    }

    /**
     * Get all allocation of an org unit for not archived portfolio entries.
     *
     * @param orgUnitId
     *            the org unit id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static List<PortfolioEntryResourcePlanAllocatedOrgUnit> getPEResourcePlanAllocatedOrgUnitAsListByOrgUnit(Long orgUnitId, boolean activeOnly) {
        return getPEResourcePlanAllocatedOrgUnitAsExprByOrgUnit(orgUnitId, activeOnly).findList();
    }

    /**
     * Get all allocation of an org unit for not archived portfolio entries as
     * an expression list.
     *
     * @param orgUnitId
     *            the org unit id
     * @param activeOnly
     *            if true, it returns only the allocation for which the end date
     *            is in the future
     */
    public static ExpressionList<PortfolioEntryResourcePlanAllocatedOrgUnit> getPEResourcePlanAllocatedOrgUnitAsExprByOrgUnit(Long orgUnitId,
            boolean activeOnly) {

        ExpressionList<PortfolioEntryResourcePlanAllocatedOrgUnit> expr = PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedOrgUnit.orderBy("endDate")
                .where().eq("deleted", false).eq("orgUnit.id", orgUnitId).eq("portfolioEntryResourcePlan.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.isFrozen", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.isActive", true)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.archived", false);

        if (activeOnly) {
            expr = expr.add(Expr.or(Expr.isNull("endDate"), Expr.gt("endDate", new Date())));
        }

        return expr;
    }

    /**
     * Get the org unit allocations for a portfolio entry according to some
     * filters.
     *
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param start
     *            the startDate or the endDate should be after this date
     * @param end
     *            the startDate or the endDate should be before this date
     * @param onlyConfirmed
     *            if true, then return only the confirmed allocations
     * @param orgUnitId
     *            the org unit id
     */
    public static List<PortfolioEntryResourcePlanAllocatedOrgUnit> getPEResourcePlanAllocatedOrgUnitAsListByPE(Long portfolioEntryId, Date start, Date end,
            boolean onlyConfirmed, Long orgUnitId) {
        ExpressionList<PortfolioEntryResourcePlanAllocatedOrgUnit> exprAllocatedOrgUnits = PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedOrgUnit
                .where().eq("deleted", false).isNotNull("startDate").isNotNull("endDate").le("startDate", end).ge("endDate", start)
                .eq("portfolioEntryResourcePlan.deleted", false).eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.isFrozen", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.isActive", true)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.id", portfolioEntryId);
        if (onlyConfirmed) {
            exprAllocatedOrgUnits = exprAllocatedOrgUnits.eq("isConfirmed", true);
        }
        if (orgUnitId != null) {
            exprAllocatedOrgUnits = exprAllocatedOrgUnits.eq("orgUnit.id", orgUnitId);
        }
        return exprAllocatedOrgUnits.findList();
    }

    /**
     * Get the total allocated org units days (for the current resource plan) of
     * a planning package.
     *
     * @param planningPackage
     *            the planning package
     */
    public static BigDecimal getPEResourcePlanAllocatedOrgUnitAsDaysByPlanningPackage(PortfolioEntryPlanningPackage planningPackage) {

        // get the current life cycle planning
        LifeCycleInstancePlanning planning = planningPackage.portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        // get the resource plan
        PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

        String sql = "SELECT SUM(perpaoo.days) as totalDays FROM portfolio_entry_resource_plan_allocated_org_unit perpaoo WHERE perpaoo.deleted = 0 "
                + "AND perpaoo.portfolio_entry_resource_plan_id = '" + resourcePlan.id + "' AND perpaoo.portfolio_entry_planning_package_id = '"
                + planningPackage.id + "'";

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalDays> query = Ebean.find(TotalDays.class);

        BigDecimal totalDays = query.setRawSql(rawSql).findUnique().totalDays;

        if (totalDays == null) {
            return BigDecimal.ZERO;
        }

        return totalDays;
    }

    /**
     * Get the total allocated org unit days (for the current resource plan) of
     * a portfolio entry.
     *
     * @param portfolioEntry
     *            the portfolio entry
     * @param isConfirmed
     *            set to true for confirmed resource; to false for non-confirmed
     *            resource; to null for all
     */
    public static BigDecimal getPEResourcePlanAllocatedOrgUnitAsDaysByPE(PortfolioEntry portfolioEntry, Boolean isConfirmed) {

        // get the current life cycle planning
        LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        // get the resource plan
        PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

        String sql = "SELECT SUM(perpaou.days) as totalDays FROM portfolio_entry_resource_plan_allocated_org_unit perpaou WHERE perpaou.deleted = 0 "
                + "AND perpaou.portfolio_entry_resource_plan_id = '" + resourcePlan.id + "'";

        if (isConfirmed != null) {
            if (isConfirmed) {
                sql += " AND perpaou.is_confirmed = '1'";
            } else {
                sql += " AND perpaou.is_confirmed = '0'";
            }
        }

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalDays> query = Ebean.find(TotalDays.class);

        BigDecimal totalDays = query.setRawSql(rawSql).findUnique().totalDays;

        if (totalDays == null) {
            return BigDecimal.ZERO;
        }

        return totalDays;
    }

    /**
     * Get the total allocated resources forecast days (for the current resource
     * plan) of a portfolio entry.
     *
     * Note: forecast days concern only actor and org unit.
     *
     * @param portfolioEntry
     *            the portfolio entry
     */
    public static BigDecimal getPEResourcePlanAsForecastDaysByPE(PortfolioEntry portfolioEntry) {

        // get the current life cycle planning
        LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        // get the resource plan
        PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

        BigDecimal totalDays = BigDecimal.ZERO;

        String sql1 = "SELECT SUM(perpaa.days) as totalDays FROM portfolio_entry_resource_plan_allocated_actor perpaa WHERE perpaa.deleted = 0 "
                + "AND perpaa.portfolio_entry_resource_plan_id = '" + resourcePlan.id + "' AND perpaa.forecast_days IS NULL";
        BigDecimal totalDaysActor = Ebean.find(TotalDays.class).setRawSql(RawSqlBuilder.parse(sql1).create()).findUnique().totalDays;
        if (totalDaysActor != null) {
            totalDays = totalDays.add(totalDaysActor);
        }

        String sql2 = "SELECT SUM(perpaa.forecast_days) as totalDays FROM portfolio_entry_resource_plan_allocated_actor perpaa WHERE perpaa.deleted = 0 "
                + "AND perpaa.portfolio_entry_resource_plan_id = '" + resourcePlan.id + "' AND perpaa.forecast_days IS NOT NULL";
        BigDecimal totalForecastDaysActor = Ebean.find(TotalDays.class).setRawSql(RawSqlBuilder.parse(sql2).create()).findUnique().totalDays;
        if (totalForecastDaysActor != null) {
            totalDays = totalDays.add(totalForecastDaysActor);
        }

        String sql3 = "SELECT SUM(perpaou.days) as totalDays FROM portfolio_entry_resource_plan_allocated_org_unit perpaou WHERE perpaou.deleted = 0 "
                + "AND perpaou.portfolio_entry_resource_plan_id = '" + resourcePlan.id + "' AND perpaou.forecast_days IS NULL";
        BigDecimal totalDaysOrgUnit = Ebean.find(TotalDays.class).setRawSql(RawSqlBuilder.parse(sql3).create()).findUnique().totalDays;
        if (totalDaysOrgUnit != null) {
            totalDays = totalDays.add(totalDaysOrgUnit);
        }

        String sql4 = "SELECT SUM(perpaou.forecast_days) as totalDays FROM portfolio_entry_resource_plan_allocated_org_unit perpaou WHERE perpaou.deleted = 0 "
                + "AND perpaou.portfolio_entry_resource_plan_id = '" + resourcePlan.id + "' AND perpaou.forecast_days IS NOT NULL";
        BigDecimal totalForecastDaysOrgUnit = Ebean.find(TotalDays.class).setRawSql(RawSqlBuilder.parse(sql4).create()).findUnique().totalDays;
        if (totalForecastDaysOrgUnit != null) {
            totalDays = totalDays.add(totalForecastDaysOrgUnit);
        }

        return totalDays;
    }

    /**
     * Get the allocated org units of a portfolio entry.
     *
     * @param portfolioEntryId
     *            the portfolio entry id
     **/
    public static List<PortfolioEntryResourcePlanAllocatedOrgUnit> getPEResourcePlanAllocatedOrgUnitAsListByPE(Long portfolioEntryId) {

        ExpressionList<PortfolioEntryResourcePlanAllocatedOrgUnit> exprAllocatedOrgUnits = PortfolioEntryResourcePlanDAO.findPEResourcePlanAllocatedOrgUnit
                .where().eq("deleted", false).eq("portfolioEntryResourcePlan.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.isFrozen", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.deleted", false)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.isActive", true)
                .eq("portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.id", portfolioEntryId);

        return exprAllocatedOrgUnits.findList();
    }

    /**
     * Return true if there is at least one allocated org unit for the currency.
     *
     * @param currency
     *            the currency code
     */
    public static boolean hasAllocatedOrgUnitByCurrency(String currency) {
        return findPEResourcePlanAllocatedOrgUnit.where().eq("deleted", false).eq("currency.code", currency).findRowCount() > 0;
    }

}
