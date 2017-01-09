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
package dao.governance;

import java.util.Date;
import java.util.List;

import models.governance.LifeCycleInstancePlanning;
import models.governance.LifeCycleMilestoneInstance;
import models.governance.PlannedLifeCycleMilestoneInstance;
import com.avaje.ebean.Model.Finder;

import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

/**
 * DAO for the {@link LifeCycleInstancePlanning} and
 * {@link PlannedLifeCycleMilestoneInstance} objects.
 * 
 * @author Johann Kohler
 */
public abstract class LifeCyclePlanningDao {

    public static Finder<Long, LifeCycleInstancePlanning> findLifeCycleInstancePlanning = new Finder<>(LifeCycleInstancePlanning.class);

    public static Finder<Long, PlannedLifeCycleMilestoneInstance> findPlannedLifeCycleMilestoneInstance = new Finder<>(
            PlannedLifeCycleMilestoneInstance.class);

    /**
     * Default constructor.
     */
    public LifeCyclePlanningDao() {
    }

    /**
     * Get the last (current) planning of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static LifeCycleInstancePlanning getLCInstancePlanningAsLastByPE(Long portfolioEntryId) {
        List<LifeCycleInstancePlanning> list =
                findLifeCycleInstancePlanning.orderBy("creationDate DESC").where().eq("deleted", false).eq("lifeCycleInstance.deleted", false)
                        .eq("lifeCycleInstance.portfolioEntry.id", portfolioEntryId).findList();
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Get a planned date by id.
     * 
     * @param id
     *            the planned date id
     */
    public static PlannedLifeCycleMilestoneInstance getPlannedLCMilestoneInstanceById(Long id) {
        return findPlannedLifeCycleMilestoneInstance.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get the passed date: if there is a passed and approved milestone instance
     * then return its passed date, else return the planned date.
     * 
     * @param plannedLifeCycleMilestoneInstanceId
     *            the planned date id
     */
    public static Date getPlannedLCMilestoneInstanceAsPassedDate(Long plannedLifeCycleMilestoneInstanceId) {

        PlannedLifeCycleMilestoneInstance plannedLifeCycleMilestoneInstance = getPlannedLCMilestoneInstanceById(plannedLifeCycleMilestoneInstanceId);

        // by default the date is the planned date
        Date date = plannedLifeCycleMilestoneInstance.plannedDate;

        // if there is a passed and approved milestone instance, then we take
        // the passed date
        for (LifeCycleMilestoneInstance milestoneInstance : LifeCycleMilestoneDao.getLCMilestoneInstanceAsListByPEAndLCMilestone(
                plannedLifeCycleMilestoneInstance.lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntry.id,
                plannedLifeCycleMilestoneInstance.lifeCycleMilestone.id)) {
            if (milestoneInstance.isPassed && milestoneInstance.lifeCycleMilestoneInstanceStatusType != null
                    && milestoneInstance.lifeCycleMilestoneInstanceStatusType.isApproved) {
                date = milestoneInstance.passedDate;
            }
        }

        return date;
    }

    /**
     * The planned date of a milestone for a planning.
     * 
     * @param planningId
     *            the planning id
     * @param milestoneId
     *            the milestone id
     */
    public static PlannedLifeCycleMilestoneInstance getPlannedLCMilestoneInstanceByLCInstancePlanningAndLCMilestone(Long planningId, Long milestoneId) {
        return findPlannedLifeCycleMilestoneInstance.where().eq("deleted", false).eq("lifeCycleInstancePlanning.id", planningId)
                .eq("lifeCycleMilestone.id", milestoneId).findUnique();

    }

    /**
     * Get the list of planned date of a portfolio entry for a specific
     * milestone.
     * 
     * @param lifeCycleMilestoneId
     *            the milestone id
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<PlannedLifeCycleMilestoneInstance> getPlannedLCMilestoneInstanceAsListByLCMilestoneAndPE(Long lifeCycleMilestoneId,
            Long portfolioEntryId) {
        return findPlannedLifeCycleMilestoneInstance.orderBy("lifeCycleInstancePlanning.creationDate").where().eq("deleted", false)
                .eq("lifeCycleMilestone.id", lifeCycleMilestoneId).eq("lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntry.id", portfolioEntryId)
                .findList();
    }

    /**
     * Get the list of planned date of a milestone.
     * 
     * @param lifeCycleMilestoneId
     *            the milestone id
     */
    public static List<PlannedLifeCycleMilestoneInstance> getPlannedLCMilestoneInstanceAsListByLCMilestone(Long lifeCycleMilestoneId) {
        return findPlannedLifeCycleMilestoneInstance.where().eq("deleted", false).eq("lifeCycleMilestone.id", lifeCycleMilestoneId).findList();
    }

    /**
     * Get not approved and late milestone of the entries of a portfolio.
     * 
     * @param portfolioId
     *            the portfolio id
     */
    public static List<PlannedLifeCycleMilestoneInstance> getPlannedLCMilestoneInstanceNotApprovedAsListOfPortfolio(Long portfolioId) {
        return findPlannedLifeCycleMilestoneInstance.where().eq("deleted", false).le("plannedDate", new Date())
                .eq("lifeCycleInstancePlanning.isFrozen", false)
                .eq("lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntryWithCurrentInstanceAsActive.deleted", false)
                .eq("lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntryWithCurrentInstanceAsActive.archived", false)
                .eq("lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntryWithCurrentInstanceAsActive.portfolios.id", portfolioId).findList();
    }
    
    /**
     * Get not approved and late milestone owned by a manager.
     * 
     * @param portfolioId
     *            the portfolio id
     */
    public static List<PlannedLifeCycleMilestoneInstance> getPlannedLCMilestoneInstanceNotApprovedAsListOfManager(Long managerId) {
        return findPlannedLifeCycleMilestoneInstance.where().eq("deleted", false).le("plannedDate", new Date())
                .eq("lifeCycleInstancePlanning.isFrozen", false)
                .eq("lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntryWithCurrentInstanceAsActive.deleted", false)
                .eq("lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntryWithCurrentInstanceAsActive.archived", false)
                .eq("lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntryWithCurrentInstanceAsActive.manager.id", managerId).findList();
    }

    /**
     * Get not approved milestone of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<PlannedLifeCycleMilestoneInstance> getPlannedLCMilestoneInstanceNotApprovedAsListOfPE(Long portfolioEntryId) {
        return findPlannedLifeCycleMilestoneInstance.orderBy("lifeCycleMilestone.order").where().eq("deleted", false).eq("lifeCycleMilestone.deleted", false)
                .eq("lifeCycleInstancePlanning.isFrozen", false)
                .eq("lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntryWithCurrentInstanceAsActive.id", portfolioEntryId).findList();
    }

    /**
     * get the list of last planned dates of a portfolio entry. We return one
     * date for each milestone of the life cycle process of the portfolio entry
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<PlannedLifeCycleMilestoneInstance> getPlannedLCMilestoneInstanceLastAsListByPE(Long portfolioEntryId) {

        /**
         * we get all planned_life_cycle_milestone_instance of the
         * portfolioEntry, if there is more than one with the the same
         * life_cycle_milestone_id then we get the last
         * 
         * EXAMPLE (for the portfolioEntry portfolioEntryId)
         * 
         * planned_life_cycle_milestone_instance
         * |----|---------------------------------|-------------------------|
         * |id..|life_cycle_instance_planning_id..|life_cycle_milestone_id..|
         * |----|---------------------------------|-------------------------|
         * |1...|1................................|4........................|
         * |2...|1................................|5........................|
         * |3...|1................................|6........................|
         * |5...|4................................|5........................|
         * |6...|4................................|6........................|
         * |----|---------------------------------|-------------------------|
         * 
         * life_cycle_instance_planning
         * |----|-------------------|---------------------------------------|
         * |id..|creation_date......|name...................................|
         * |----|----------------- -|---------------------------------------|
         * |1...|2014-04-15 15:00:00|initial planning for PROJ001...........|
         * |4...|2014-04-18 14:00:00|new planning for PROJ001...............|
         * |----|----------------- -|---------------------------------------|
         * 
         * so we get
         * |----|---------------------------------|-------------------------|
         * |id..|life_cycle_instance_planning_id..|life_cycle_milestone_id..|
         * |----|---------------------------------|-------------------------|
         * |1...|1................................|4........................|
         * |5...|4................................|5........................|
         * |6...|4................................|6........................|
         * |----|---------------------------------|-------------------------|
         */

        String sql =
                "SELECT plcmi.id " + getSelectSqlForGetLasts("", portfolioEntryId) + " AND lcip.creation_date=(SELECT MAX(ilcip.creation_date) "
                        + getSelectSqlForGetLasts("i", portfolioEntryId)
                        + " GROUP BY iplcmi.life_cycle_milestone_id HAVING iplcmi.life_cycle_milestone_id=plcmi.life_cycle_milestone_id) ORDER BY lcm.order";

        RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("plcmi.id", "id").create();

        return findPlannedLifeCycleMilestoneInstance.query().setRawSql(rawSql).findList();
    }

    /**
     * Get the SQL fragment that selects the tables with join (FROM) and basic
     * filters (WHERE).
     * 
     * @param prefix
     *            the base prefix for the tables
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    private static String getSelectSqlForGetLasts(String prefix, Long portfolioEntryId) {

        String s =
                "FROM planned_life_cycle_milestone_instance {p}plcmi"
                        + " JOIN life_cycle_instance_planning {p}lcip ON {p}plcmi.life_cycle_instance_planning_id = {p}lcip.id"
                        + " JOIN life_cycle_instance {p}lci ON {p}lcip.life_cycle_instance_id = {p}lci.id"
                        + " JOIN life_cycle_milestone {p}lcm ON {p}plcmi.life_cycle_milestone_id = {p}lcm.id" + " WHERE {p}lci.portfolio_entry_id="
                        + portfolioEntryId
                        + " AND {p}plcmi.deleted=false AND {p}lcip.deleted=false AND {p}lci.deleted=false AND {p}lci.is_active=true AND {p}lcm.deleted=false"
                        + " AND ({p}lcm.is_active=true OR (SELECT COUNT(*) FROM life_cycle_milestone_instance {p}lcmi"
                        + " WHERE {p}lcmi.deleted=false AND {p}lcmi.life_cycle_instance_id = {p}lci.id AND {p}lcmi.life_cycle_milestone_id = {p}lcm.id) > 0)";
        return s.replaceAll("\\{p\\}", prefix);
    }

}
