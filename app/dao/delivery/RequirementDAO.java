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

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Finder;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import models.delivery.Requirement;
import models.delivery.RequirementPriority;
import models.delivery.RequirementSeverity;
import models.delivery.RequirementStatus;
import models.delivery.RequirementStatus.Type;
import models.delivery.TotalRequirement;
import models.delivery.TotalStoryPoints;

/**
 * DAO for the {@link Requirement}, {@link RequirementPriority},
 * {@link RequirementSeverity}, {@link RequirementStatus}
 * {@link TotalRequirement}, {@link TotalStoryPoints} objects.
 * 
 * @author Pierre-Yves Cloux
 */
public abstract class RequirementDAO {

    public static Finder<Long, Requirement> findRequirement = new Finder<>(Requirement.class);
    public static Finder<Long, RequirementPriority> findRequirementPriority = new Finder<>(RequirementPriority.class);
    public static Finder<Long, RequirementSeverity> findRequirementSeverity = new Finder<>(RequirementSeverity.class);
    public static Finder<Long, RequirementStatus> findRequirementStatus = new Finder<>(RequirementStatus.class);

    /**
     * Default constructor.
     */
    public RequirementDAO() {
    }

    /**
     * Get a requirement by id.
     * 
     * @param id
     *            the requirement id
     */
    public static Requirement getRequirementById(Long id) {
        return RequirementDAO.findRequirement.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all requirements of a portfolio entry as an expression list.
     * 
     * They could be direct or across a deliverable.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ExpressionList<Requirement> getRequirementAllAsExprByPE(Long portfolioEntryId) {
        return findRequirement.where().eq("deleted", false).disjunction().eq("portfolioEntry.id", portfolioEntryId)
                .eq("delivrables.portfolioEntryDeliverables.portfolioEntry.id", portfolioEntryId);
    }

    /**
     * Get direct requirements of a portfolio entry as an expression list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ExpressionList<Requirement> getRequirementAsExprByPE(Long portfolioEntryId) {
        return RequirementDAO.findRequirement.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId);
    }

    /**
     * Get the direct needs of a portfolio entry for a given status type, and
     * return an expression list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param statusType
     *            the status type
     * @param isMust
     *            set to true to get only mandatory needs, to false to get only
     *            optional needs, to null to get all
     */
    public static ExpressionList<Requirement> getRequirementNeedsAsExprByPEAndStatusType(Long portfolioEntryId, Type statusType, Boolean isMust) {
        ExpressionList<Requirement> expr = RequirementDAO.findRequirement.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId)
                .eq("isDefect", false).eq("requirementStatus.type", statusType);
        if (isMust != null) {
            expr = expr.eq("requirementPriority.isMust", isMust);
        }
        return expr;
    }

    /**
     * Get the direct needs of a portfolio entry for a given status type and
     * priority, and return an expression list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param type
     *            the status type
     * @param requirementPriorityId
     *            the requirement priority id
     */
    public static ExpressionList<Requirement> getRequirementNeedsAsExprByPEAndStatusTypeAndPriority(Long portfolioEntryId, Type type,
            Long requirementPriorityId) {
        return RequirementDAO.findRequirement.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId).eq("isDefect", false)
                .eq("requirementStatus.type", type).eq("requirementPriority.id", requirementPriorityId);
    }

    /**
     * Get the direct defects of a portfolio entry for a given status type, and
     * return an expression list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param statusType
     *            the status type
     * @param isBlocker
     *            set to true to get only blocker defects, to false to get only
     *            non blocker defects, to null to get all
     */
    public static ExpressionList<Requirement> getRequirementDefectsAsExprByPEAndStatusType(Long portfolioEntryId, Type statusType, Boolean isBlocker) {
        ExpressionList<Requirement> expr = RequirementDAO.findRequirement.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId)
                .eq("isDefect", true).eq("requirementStatus.type", statusType);
        if (isBlocker != null) {
            expr = expr.eq("requirementSeverity.isBlocker", isBlocker);
        }
        return expr;
    }

    /**
     * Get the direct defects of a portfolio entry for a given status type and a
     * severity, and return an expression list.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param statusType
     *            the status type
     * @param requirementSeverityId
     *            the requirement severity id
     */
    public static ExpressionList<Requirement> getRequirementDefectsAsExprByPEAndStatusTypeAndSeverity(Long portfolioEntryId, Type statusType,
            Long requirementSeverityId) {
        return RequirementDAO.findRequirement.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId).eq("isDefect", true)
                .eq("requirementStatus.type", statusType).eq("requirementSeverity.id", requirementSeverityId);
    }

    /**
     * Get the total story points of a portfolio entry for direct requirements.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param isClosed
     *            if true: get only closed requirements, if false: get only
     *            non-closed requirements, if null: get all requirements
     */
    public static Integer getStoryPointsByPE(Long portfolioEntryId, Boolean isClosed) {

        String sql = "SELECT SUM(r.story_points) AS totalStoryPoints " + "FROM requirement r "
                + "JOIN requirement_status rs ON r.requirement_status_id = rs.id AND rs.deleted = 0 " + "WHERE r.deleted = 0 AND r.portfolio_entry_id = "
                + portfolioEntryId;

        if (isClosed != null) {
            if (isClosed) {
                sql += " AND (rs.type = '" + RequirementStatus.Type.CLOSED.name() + "' OR rs.type = '" + RequirementStatus.Type.DEPLOYED.name() + "')";
            } else {
                sql += " AND rs.type <> '" + RequirementStatus.Type.CLOSED.name() + "' AND rs.type <> '" + RequirementStatus.Type.DEPLOYED.name() + "'";
            }
        }

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalStoryPoints> query = Ebean.find(TotalStoryPoints.class);

        Integer totalStoryPoints = query.setRawSql(rawSql).findUnique().totalStoryPoints;

        if (totalStoryPoints == null) {
            return 0;
        }

        return totalStoryPoints;
    }

    /**
     * Get the total number of direct requirement of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param isClosed
     *            if true: get only closed requirements, if false: get only
     *            non-closed requirements, if null: get all requirements
     */
    public static Integer getRequirementAsCountByPE(Long portfolioEntryId, Boolean isClosed) {

        String sql = "SELECT COUNT(r.id) AS totalRequirement " + "FROM requirement r "
                + "JOIN requirement_status rs ON r.requirement_status_id = rs.id AND rs.deleted = 0 " + "WHERE r.deleted = 0 AND r.portfolio_entry_id = "
                + portfolioEntryId;

        if (isClosed != null) {
            if (isClosed) {
                sql += " AND (rs.type = '" + RequirementStatus.Type.CLOSED.name() + "' OR rs.type = '" + RequirementStatus.Type.DEPLOYED.name() + "')";
            } else {
                sql += " AND rs.type <> '" + RequirementStatus.Type.CLOSED.name() + "' AND rs.type <> '" + RequirementStatus.Type.DEPLOYED.name() + "'";
            }
        }

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalRequirement> query = Ebean.find(TotalRequirement.class);

        Integer totalRequirement = query.setRawSql(rawSql).findUnique().totalRequirement;

        if (totalRequirement == null) {
            return 0;
        }

        return totalRequirement;
    }

    /**
     * Get the total number of open and direct defects of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param isBlocker
     *            if true: get only blocker defects, if false: get only
     *            non-blocker defects, if null: get all defects
     */
    public static Integer getRequirementAsOpenDefectCountByPE(Long portfolioEntryId, Boolean isBlocker) {

        ExpressionList<Requirement> expr = RequirementDAO.findRequirement.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId)
                .eq("isDefect", true).ne("requirementStatus.type", RequirementStatus.Type.CLOSED)
                .ne("requirementStatus.type", RequirementStatus.Type.DEPLOYED);

        if (isBlocker != null) {
            expr = expr.eq("requirementSeverity.isBlocker", isBlocker);
        }
        return expr.findRowCount();

    }

    /**
     * Get the total number of direct requirements of a portfolio entry
     * according to the isScoped value.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param isScoped
     *            if true: get only the scoped requirements, if false: get only
     *            the non-scoped requirements, if null: get all requirements
     */
    public static Integer getRequirementAsCountByPEAndIsScoped(Long portfolioEntryId, Boolean isScoped) {

        ExpressionList<Requirement> expr = RequirementDAO.findRequirement.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId);

        if (isScoped != null) {
            expr = expr.eq("isScoped", isScoped);
        }

        return expr.findRowCount();
    }

    /**
     * Get the closed story points of direct requirements of an iteration.
     * 
     * @param iterationId
     *            the iteration id
     */
    public static Integer getStoryPointClosedAsCountByIteration(Long iterationId) {

        String sql = "SELECT SUM(r.story_points) AS totalStoryPoints " + "FROM requirement r "
                + "JOIN requirement_status rs ON r.requirement_status_id = rs.id AND rs.deleted = 0 " + "WHERE r.deleted = 0 AND r.iteration_id = "
                + iterationId + " AND (rs.type = '" + RequirementStatus.Type.CLOSED.name() + "' OR rs.type = '" + RequirementStatus.Type.DEPLOYED.name()
                + "')";

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<TotalStoryPoints> query = Ebean.find(TotalStoryPoints.class);

        Integer totalStoryPoints = query.setRawSql(rawSql).findUnique().totalStoryPoints;

        if (totalStoryPoints == null) {
            return 0;
        }

        return totalStoryPoints;

    }

    /**
     * Get list of direct requirements of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id.
     */
    public static List<Requirement> getRequirementAsListByPE(Long portfolioEntryId) {
        return RequirementDAO.findRequirement.where().eq("deleted", false).eq("portfolioEntry.id", portfolioEntryId).findList();
    }

    /**
     * Get all requirement priorities.
     */
    public static List<RequirementPriority> getRequirementPriorityAsList() {
        return RequirementDAO.findRequirementPriority.where().eq("deleted", false).findList();
    }

    /**
     * Get all requirement priorities as a value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getRequirementPriorityAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getRequirementPriorityAsList());
    }

    /**
     * Get an Requirement priority by id.
     * 
     * @param id
     *            the Requirement priority id
     */
    public static RequirementPriority getRequirementPriorityById(Long id) {
        return RequirementDAO.findRequirementPriority.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all requirement severities.
     */
    public static List<RequirementSeverity> getRequirementSeverityAsList() {
        return RequirementDAO.findRequirementSeverity.where().eq("deleted", false).findList();
    }

    /**
     * Get all requirement severities as a value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getRequirementSeverityAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getRequirementSeverityAsList());
    }

    /**
     * Get an Requirement severity by id.
     * 
     * @param id
     *            the Requirement severity id
     */
    public static RequirementSeverity getRequirementSeverityById(Long id) {
        return RequirementDAO.findRequirementSeverity.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all requirement status.
     */
    public static List<RequirementStatus> getRequirementStatusAsList() {
        return RequirementDAO.findRequirementStatus.orderBy("FIELD(type, 'NEW', 'OPEN', 'CLOSED', 'DEPLOYED')").where().eq("deleted", false).findList();
    }

    /**
     * Get all requirement status as a value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getRequirementStatusAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getRequirementStatusAsList());
    }

    /**
     * Get all open requirement status.
     */
    public static List<RequirementStatus> getRequirementStatusOpenAsList() {
        return RequirementDAO.findRequirementStatus.orderBy("FIELD(type, 'NEW', 'OPEN')").where().eq("deleted", false)
                .ne("type", RequirementStatus.Type.CLOSED).ne("type", RequirementStatus.Type.DEPLOYED).findList();
    }

    /**
     * Get an Requirement status by id.
     * 
     * @param id
     *            the Requirement status id
     */
    public static RequirementStatus getRequirementStatusById(Long id) {
        return RequirementDAO.findRequirementStatus.where().eq("deleted", false).eq("id", id).findUnique();
    }

}
