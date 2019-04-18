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

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Finder;
import framework.services.account.IPreferenceManagerPlugin;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;
import models.governance.*;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;
import services.budgettracking.IBudgetTrackingService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DAO for the {@link LifeCycleMilestone} and {@link LifeCycleMilestoneInstance}
 * and {@link LifeCycleMilestoneInstanceApprover} and
 * {@link LifeCycleMilestoneInstanceStatusType} and {@link LifeCyclePhase}
 * objects.
 * 
 * @author Johann Kohler
 */
public abstract class LifeCycleMilestoneDao {

    public static final String DELETED = "deleted";
    public static final String LIFE_CYCLE_PROCESS_ID = "lifeCycleProcess.id";
    public static final String ORDER = "order";
    public static final String ORDER_DESC = "order DESC";
    public static final String LIFE_CYCLE_MILESTONE_ID = "lifeCycleMilestone.id";
    public static final String IS_PASSED = "isPassed";
    public static final String LIFE_CYCLE_INSTANCE_PORTFOLIO_ENTRY_ID = "lifeCycleInstance.portfolioEntry.id";
    public static final String LIFE_CYCLE_INSTANCE_IS_ACTIVE = "lifeCycleInstance.isActive";
    public static final String LIFE_CYCLE_MILESTONE_INSTANCE_STATUS_TYPE_IS_APPROVED = "lifeCycleMilestoneInstanceStatusType.isApproved";
    public static final String LIFE_CYCLE_MILESTONE_INSTANCE_ID = "lifeCycleMilestoneInstance.id";
    public static final Finder<Long, LifeCycleMilestone> findLifeCycleMilestone = new Finder<>(LifeCycleMilestone.class);

    public static final Finder<Long, LifeCycleMilestoneInstance> findLifeCycleMilestoneInstance = new Finder<>(LifeCycleMilestoneInstance.class);

    public static final Finder<Long, LifeCycleMilestoneInstanceApprover> findLifeCycleMilestoneInstanceApprover = new Finder<>(
            LifeCycleMilestoneInstanceApprover.class);

    public static final Finder<Long, LifeCycleMilestoneInstanceStatusType> findLifeCycleMilestoneInstanceStatusType = new Finder<>(
            LifeCycleMilestoneInstanceStatusType.class);

    public static final Finder<Long, LifeCyclePhase> findLifeCyclePhase = new Finder<>(LifeCyclePhase.class);
    
    /**
     * Default constructor.
     */
    public LifeCycleMilestoneDao() {
    }

    /**
     * Get a life cycle milestone by id.
     * 
     * @param id
     *            the life cycle milestone id
     */
    public static LifeCycleMilestone getLCMilestoneById(Long id) {
        return findLifeCycleMilestone.where().eq(DELETED, false).eq("id", id).findUnique();
    }

    /**
     * Get the milestone of a process for a given type.
     * 
     * @param processId
     *            the life cycle process id
     * @param type
     *            the type
     */
    public static LifeCycleMilestone getLCMilestoneByProcessAndType(Long processId, LifeCycleMilestone.Type type) {
        return findLifeCycleMilestone.where().eq(DELETED, false).eq(LIFE_CYCLE_PROCESS_ID, processId).eq("type", type).findUnique();
    }

    /**
     * Get a life cycle milestone by short name.
     * 
     * @param shortName
     *            the milestone short name
     */
    public static LifeCycleMilestone getLCMilestoneByShortName(String shortName) {
        return findLifeCycleMilestone.where().eq(DELETED, false).eq("shortName", shortName).findUnique();
    }

    /**
     * Get all milestone of a process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     */
    public static List<LifeCycleMilestone> getLCMilestoneAsListByLCProcess(Long lifeCycleProcessId) {
        return findLifeCycleMilestone.orderBy(ORDER).where().eq(DELETED, false).eq(LIFE_CYCLE_PROCESS_ID, lifeCycleProcessId).findList();
    }

    /**
     * Get all active life cycle milestones (of all active processes) as value
     * holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getLCMilestoneActiveAsVH() {

        DefaultSelectableValueHolderCollection<Long> valueHolderCollection = new DefaultSelectableValueHolderCollection<>();
        List<LifeCycleMilestone> list = findLifeCycleMilestone.where().eq(DELETED, false).eq("isActive", true).eq("lifeCycleProcess.deleted", false)
                .eq("lifeCycleProcess.isActive", true).findList();
        for (LifeCycleMilestone lifeCycleMilestone : list) {
            valueHolderCollection.add(new DefaultSelectableValueHolder<>(lifeCycleMilestone.id,
                    lifeCycleMilestone.lifeCycleProcess.getShortName() + " - " + lifeCycleMilestone.getName()));

        }
        return valueHolderCollection;

    }

    /**
     * Get all life cycle milestones of a process as value holder collection.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     */
    public static ISelectableValueHolderCollection<Long> getLCMilestoneAsVHByLCProcess(Long lifeCycleProcessId) {

        DefaultSelectableValueHolderCollection<Long> valueHolderCollection = new DefaultSelectableValueHolderCollection<>();
        List<LifeCycleMilestone> list = findLifeCycleMilestone.where().eq(DELETED, false).eq(LIFE_CYCLE_PROCESS_ID, lifeCycleProcessId).findList();
        for (LifeCycleMilestone lifeCycleMilestone : list) {
            String name = lifeCycleMilestone.getShortName();
            if (lifeCycleMilestone.getName() != null && !lifeCycleMilestone.getName().equals("")) {
                name += " (" + lifeCycleMilestone.getName() + ")";
            }
            valueHolderCollection.add(new DefaultSelectableValueHolder<>(lifeCycleMilestone.id, name));

        }
        return valueHolderCollection;

    }

    /**
     * Get the milestone of a process with the previous order.
     * 
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     * @param order
     *            the current order
     */
    public static LifeCycleMilestone getLCMilestoneAsPreviousByLCProcess(Long lifeCycleProcessId, int order) {
        return findLifeCycleMilestone.orderBy(ORDER_DESC).where().eq(DELETED, false).eq(LIFE_CYCLE_PROCESS_ID, lifeCycleProcessId).lt(ORDER, order)
                .setMaxRows(1).findUnique();
    }

    /**
     * Get the milestone of a process with the next order.
     * 
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     * @param order
     *            the current order
     */
    public static LifeCycleMilestone getLCMilestoneAsNextByLCProcess(Long lifeCycleProcessId, int order) {
        return findLifeCycleMilestone.orderBy("order ASC").where().eq(DELETED, false).eq(LIFE_CYCLE_PROCESS_ID, lifeCycleProcessId).gt(ORDER, order)
                .setMaxRows(1).findUnique();
    }

    /**
     * Get the last order for a process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process
     */
    public static Integer getLCMilestoneAsLastOrderByLCProcess(Long lifeCycleProcessId) {
        LifeCycleMilestone lastMilestone = findLifeCycleMilestone.orderBy(ORDER_DESC).where().eq(DELETED, false)
                .eq(LIFE_CYCLE_PROCESS_ID, lifeCycleProcessId).setMaxRows(1).findUnique();
        if (lastMilestone == null) {
            return -1;
        } else {
            return lastMilestone.order;
        }

    }

    /**
     * Get a milestone instance by id.
     * 
     * @param id
     *            the milestone instance id
     */
    public static LifeCycleMilestoneInstance getLCMilestoneInstanceById(Long id) {
        return findLifeCycleMilestoneInstance.where().eq(DELETED, false).eq("id", id).findUnique();
    }

    public static List<LifeCycleMilestoneInstance> getLCMilestoneInstanceByMilestoneAndLifeCycleInstance(Long milestoneId, Long lifeCycleInstanceId) {
        return findLifeCycleMilestoneInstance.where()
                .eq(DELETED, false)
                .eq("life_cycle_milestone_id", milestoneId)
                .eq("life_cycle_instance_id", lifeCycleInstanceId)
                .findList();
    }

    /**
     * Process the life cycle milestone instance to pass it. The used status
     * type is the default one of the corresponding milestone.
     * 
     * @param lifeCycleMilestoneInstanceId
     *            the milestone instance to pass
     * @param budgetTrackingService
     *            the budget tracking service
     */
    public static LifeCycleMilestoneInstance doPassed(Long lifeCycleMilestoneInstanceId, IBudgetTrackingService budgetTrackingService) {
        LifeCycleMilestoneInstance lifeCycleMilestoneInstance = getLCMilestoneInstanceById(lifeCycleMilestoneInstanceId);
        return doPassed(lifeCycleMilestoneInstanceId, lifeCycleMilestoneInstance.lifeCycleMilestone.defaultLifeCycleMilestoneInstanceStatusType,
                lifeCycleMilestoneInstance.gateComments, budgetTrackingService);
    }

    /**
     * delete a life cycle milestone instance and rollback the planning
     *
     * @param lifecycleMilestoneInstanceId the life cycle milestone instance to be deleted
     * @param budgetTrackingService the budget tracking service
     */
    public static void doDelete(Long lifecycleMilestoneInstanceId, IBudgetTrackingService budgetTrackingService) {

        LifeCycleMilestoneInstance lifeCycleMilestoneInstance = getLCMilestoneInstanceById(lifecycleMilestoneInstanceId);

        PortfolioEntry portfolioEntry = lifeCycleMilestoneInstance.lifeCycleInstance.portfolioEntry;

        /*
         * update the plannings
         */

        LifeCycleInstancePlanning currentPlanning = lifeCycleMilestoneInstance.lifeCycleInstance.portfolioEntry.activeLifeCycleInstance
                .getCurrentLifeCycleInstancePlanning();

        // recompute the budget tracking for resources
        if (budgetTrackingService.isActive()) {
            budgetTrackingService.recomputeAllBugdetAndForecastFromResource(currentPlanning);
        }

        List<LifeCycleMilestoneInstance> approvedLifecycleMilestoneInstances = lifeCycleMilestoneInstance.lifeCycleInstance.getApprovedLifecycleMilestoneInstances();

        // update the lifecycle instance
        if (!lifeCycleMilestoneInstance.lifeCycleInstance.isConcept && approvedLifecycleMilestoneInstances.isEmpty()) {
            lifeCycleMilestoneInstance.lifeCycleInstance.isConcept = true;
        }

        approvedLifecycleMilestoneInstances.remove(lifeCycleMilestoneInstance);

        // Update the last approved lifecycle milestone instance
        if (portfolioEntry.lastApprovedLifeCycleMilestoneInstance != null && lifeCycleMilestoneInstance.id.equals(portfolioEntry.lastApprovedLifeCycleMilestoneInstance.id)) {
            if (approvedLifecycleMilestoneInstances.isEmpty()) {
                portfolioEntry.lastApprovedLifeCycleMilestoneInstance = null;
                lifeCycleMilestoneInstance.lifeCycleInstance.isConcept = true;
            } else {
                portfolioEntry.lastApprovedLifeCycleMilestoneInstance = approvedLifecycleMilestoneInstances
                        .stream()
                        .sorted((m1, m2) -> m2.passedDate.compareTo(m1.passedDate))
                        .findFirst()
                        .orElse(null);
            }
        }

        lifeCycleMilestoneInstance.lifeCycleInstance.save();

        // Delete the lifecycle milestone instance
        lifeCycleMilestoneInstance.doDelete();
        createNextPlanningFromPreviousOne(lifeCycleMilestoneInstance, currentPlanning);

        portfolioEntry.save();
    }

    /**
     * Process the life cycle milestone instance to pass it.<br/>
     * -set the isPassed attribute to true and assign the status type<br/>
     * -assign the current portfolio entry budget and create a new one<br/>
     * -freeze all plannings and create a new one<br/>
     * -if the milestone is approved, set the is concept flag to false<br/>
     *
     * @param lifeCycleMilestoneInstanceId
     *            the milestone instance to pass
     * @param lifeCycleMilestoneInstanceStatusType
     *            the status type of the passed milestone
     * @param gateComments
     *            the gate comments
     * @param budgetTrackingService
     *            the budget tracking service
     */
    public static LifeCycleMilestoneInstance doPassed(Long lifeCycleMilestoneInstanceId,
            LifeCycleMilestoneInstanceStatusType lifeCycleMilestoneInstanceStatusType, String gateComments, IBudgetTrackingService budgetTrackingService) {

        LifeCycleMilestoneInstance lifeCycleMilestoneInstance = getLCMilestoneInstanceById(lifeCycleMilestoneInstanceId);

        /*
         * update the milestone instance
         */
        lifeCycleMilestoneInstance.isPassed = true;
        lifeCycleMilestoneInstance.lifeCycleMilestoneInstanceStatusType = lifeCycleMilestoneInstanceStatusType;
        lifeCycleMilestoneInstance.gateComments = gateComments;
        lifeCycleMilestoneInstance.save();

        /*
         * assign the budget of the current planning to the milestone instance
         * and create a new budget (that is a copy of the current) for the new
         * planning
         */
        LifeCycleInstancePlanning currentPlanning = lifeCycleMilestoneInstance.lifeCycleInstance.portfolioEntry.activeLifeCycleInstance
                .getCurrentLifeCycleInstancePlanning();
        lifeCycleMilestoneInstance.portfolioEntryBudget = currentPlanning.portfolioEntryBudget;
        lifeCycleMilestoneInstance.portfolioEntryResourcePlan = currentPlanning.portfolioEntryResourcePlan;
        lifeCycleMilestoneInstance.save();

        // recompute the budget tracking for resources
        if (budgetTrackingService.isActive()) {
            budgetTrackingService.recomputeAllBugdetAndForecastFromResource(currentPlanning);
        }

        createNextPlanningFromPreviousOne(lifeCycleMilestoneInstance, currentPlanning);

        /*
         * update the life cycle instance
         */
        if (lifeCycleMilestoneInstanceStatusType.isApproved) {
            lifeCycleMilestoneInstance.lifeCycleInstance.isConcept = false;

            /*
             * set the last approved milestone instance of the portfolio entry
             * if there is no last approved milestone instance OR the existing
             * last approved milestone instance has been passed before the
             * current one
             */
            PortfolioEntry portfolioEntry = lifeCycleMilestoneInstance.lifeCycleInstance.portfolioEntry;
            if (
                portfolioEntry.lastApprovedLifeCycleMilestoneInstance == null
                || portfolioEntry.lastApprovedLifeCycleMilestoneInstance.deleted
                || !portfolioEntry.lastApprovedLifeCycleMilestoneInstance.passedDate.after(lifeCycleMilestoneInstance.passedDate)
            ) {
                portfolioEntry.lastApprovedLifeCycleMilestoneInstance = lifeCycleMilestoneInstance;
            }

            portfolioEntry.save();

            lifeCycleMilestoneInstance.lifeCycleInstance.save();

        }

        return lifeCycleMilestoneInstance;

    }

    private static void createNextPlanningFromPreviousOne(LifeCycleMilestoneInstance lifeCycleMilestoneInstance, LifeCycleInstancePlanning oldPlanning) {
        // set all plannings to frozen
        lifeCycleMilestoneInstance.lifeCycleInstance.lifeCycleInstancePlannings.forEach(LifeCycleInstancePlanning::doFrozen);

        // create the new planning
        LifeCycleInstancePlanning planning = new LifeCycleInstancePlanning(lifeCycleMilestoneInstance.lifeCycleInstance);
        if (oldPlanning.portfolioEntryBudget != null && oldPlanning.portfolioEntryResourcePlan != null) {
            Map<String, Map<Long, Long>> allocatedResourcesMapOldToNew = new HashMap<>();
            planning.portfolioEntryResourcePlan = oldPlanning.portfolioEntryResourcePlan.cloneInDB(allocatedResourcesMapOldToNew);
            planning.portfolioEntryBudget = oldPlanning.portfolioEntryBudget.cloneInDB(allocatedResourcesMapOldToNew);

            // reassign the new allocated resources to existing work order
            lifeCycleMilestoneInstance.lifeCycleInstance.portfolioEntry.workOrders
                    .stream()
                    .filter(workOrder -> workOrder.resourceObjectType != null)
                    .forEach(workOrder -> {
                        workOrder.resourceObjectId = allocatedResourcesMapOldToNew.get(workOrder.resourceObjectType).get(workOrder.resourceObjectId);
                        workOrder.save();
            });

        }
        planning.save();

        // get the last planned dates
        Map<Long, Date> lastDates = new HashMap<>();
        List<PlannedLifeCycleMilestoneInstance> plannedMilestones = LifeCyclePlanningDao
                .getPlannedLCMilestoneInstanceLastAsListByPE(lifeCycleMilestoneInstance.lifeCycleInstance.portfolioEntry.id);

        for (PlannedLifeCycleMilestoneInstance lastDate : plannedMilestones) {
            lastDates.put(lastDate.lifeCycleMilestone.id, lastDate.plannedDate);
        }

        // add the new plannings
        List<LifeCycleMilestone> milestones = lifeCycleMilestoneInstance.lifeCycleInstance.lifeCycleProcess.lifeCycleMilestones;
        milestones.addAll(
                plannedMilestones.stream()
                        .map(plannedMilestone -> plannedMilestone.lifeCycleMilestone)
                        .filter(milestone -> milestone.isAdditional)
                        .collect(Collectors.toList())
        );

        for (LifeCycleMilestone milestone : milestones) {

            /**
             * Check if the milestone for the portfolio entry has an approved
             * milestone instance, meaning the milestone is passed and approved.
             */
            boolean hasApprovedInstancesForMilestoneOfPortfolioEntry = Ebean.find(LifeCycleMilestoneInstance.class).where()
                    .eq(DELETED, false)
                    .eq(LIFE_CYCLE_MILESTONE_ID, milestone.id)
                    .eq(IS_PASSED, true)
                    .eq(LIFE_CYCLE_INSTANCE_PORTFOLIO_ENTRY_ID, lifeCycleMilestoneInstance.lifeCycleInstance.portfolioEntry.id)
                    .eq(LIFE_CYCLE_INSTANCE_IS_ACTIVE, true)
                    .eq(LIFE_CYCLE_MILESTONE_INSTANCE_STATUS_TYPE_IS_APPROVED, true).findRowCount() > 0;

            if (!hasApprovedInstancesForMilestoneOfPortfolioEntry) {
                PlannedLifeCycleMilestoneInstance plannedInstance = new PlannedLifeCycleMilestoneInstance(planning, milestone);
                if (lastDates.containsKey(milestone.id)) {
                    plannedInstance.plannedDate = lastDates.get(milestone.id);
                }
                plannedInstance.save();
            }
        }
    }

    /**
     * Get all active milestone instances of portfolio entry for a specific
     * milestone.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param lifeCycleMilestoneId
     *            the milestone id
     * @param order
     *            the order (ASC or DESC)
     */
    public static List<LifeCycleMilestoneInstance> getLCMilestoneInstanceAsListByPEAndLCMilestone(Long portfolioEntryId, Long lifeCycleMilestoneId,
            String order) {
        return findLifeCycleMilestoneInstance.where().eq(DELETED, false).eq(LIFE_CYCLE_MILESTONE_ID, lifeCycleMilestoneId)
                .eq(LIFE_CYCLE_INSTANCE_PORTFOLIO_ENTRY_ID, portfolioEntryId).eq(LIFE_CYCLE_INSTANCE_IS_ACTIVE, true).orderBy("passedDate " + order).findList();
    }

    /**
     * Get the approved instances of a process instance.
     * 
     * @param lifeCycleInstanceId
     *            the process instance id
     */
    public static List<LifeCycleMilestoneInstance> getLCMilestoneInstanceAsListByLCInstance(Long lifeCycleInstanceId) {
        return findLifeCycleMilestoneInstance.orderBy("passedDate").where().eq(DELETED, false).eq("lifeCycleInstance.id", lifeCycleInstanceId)
                .eq(IS_PASSED, true).eq(LIFE_CYCLE_MILESTONE_INSTANCE_STATUS_TYPE_IS_APPROVED, true).findList();
    }

    /**
     * Get all active milestone instances of portfolio entry for a specific
     * milestone with the ascendant order.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param lifeCycleMilestoneId
     *            the milestone id
     */
    public static List<LifeCycleMilestoneInstance> getLCMilestoneInstanceAsListByPEAndLCMilestone(Long portfolioEntryId, Long lifeCycleMilestoneId) {
        return getLCMilestoneInstanceAsListByPEAndLCMilestone(portfolioEntryId, lifeCycleMilestoneId, "DESC");
    }

    /**
     * Define if all approvers of a milestone instance have voted.
     * 
     * @param lifeCycleMilestonInstanceId
     *            the milestone instance id
     */
    public static boolean hasLCMilestoneInstanceAllApproversVoted(Long lifeCycleMilestonInstanceId) {
        Integer nOfApprovers = findLifeCycleMilestoneInstanceApprover.where().eq(DELETED, false)
                .eq(LIFE_CYCLE_MILESTONE_INSTANCE_ID, lifeCycleMilestonInstanceId).findRowCount();
        Integer nOfVotingApprovers = findLifeCycleMilestoneInstanceApprover.where().eq(DELETED, false)
                .eq(LIFE_CYCLE_MILESTONE_INSTANCE_ID, lifeCycleMilestonInstanceId).isNotNull("approvalDate").findRowCount();
        return nOfApprovers.equals(nOfVotingApprovers);
    }

    /**
     * Get all active and non-passed milestone instance as expression, meaning
     * all milestone instances for which a vote/decision is required.
     */
    public static ExpressionList<LifeCycleMilestoneInstance> getLCMilestoneInstanceAsExpr() {
        return findLifeCycleMilestoneInstance.where().eq(DELETED, false).eq(IS_PASSED, false)
                .eq(LIFE_CYCLE_INSTANCE_IS_ACTIVE, true).eq("lifeCycleInstance.portfolioEntry.deleted", false);
    }

    /**
     * Get all milestone instances as pagination object for which a user (here
     * called an approver) should vote.
     * @param approverId
     */
    public static ExpressionList<LifeCycleMilestoneInstance> getLCMilestoneInstanceAsExprByApprover(Actor approver) {
        if (approver.orgUnit != null) {
            return getLCMilestoneInstanceAsExpr()
                    .or(Expr.eq("lifeCycleMilestoneInstanceApprovers.actor.id", approver.id), Expr.eq("lifeCycleMilestoneInstanceApprovers.orgUnit.id", approver.orgUnit.id))
                    .isNull("lifeCycleMilestoneInstanceApprovers.hasApproved");
        }

        return getLCMilestoneInstanceAsExpr()
                .eq("lifeCycleMilestoneInstanceApprovers.actor.id", approver.id)
                .isNull("lifeCycleMilestoneInstanceApprovers.hasApproved");
    }

    /**
     * Get all milestone instances as pagination object for which a
     * vote/decision is required.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     */
    public static Pagination<LifeCycleMilestoneInstance> getLCMilestoneInstanceAsPagination(IPreferenceManagerPlugin preferenceManagerPlugin) {
        return new Pagination<>(preferenceManagerPlugin, getLCMilestoneInstanceAsExpr());
    }

    /**
     * Get all milestone instances for public portfolio entries.
     */
    public static List<LifeCycleMilestoneInstance> getLCMilestoneInstancePublicPEAsList() {

        return findLifeCycleMilestoneInstance.where().eq(DELETED, false).eq("lifeCycleInstance.deleted", false).eq(LIFE_CYCLE_INSTANCE_IS_ACTIVE, true)
                .eq("lifeCycleInstance.portfolioEntry.deleted", false).eq("lifeCycleInstance.portfolioEntry.isPublic", true).findList();

    }

    /**
     * Define if a milestone of a portfolio entry has an approved milestone
     * instance (meaning the milestone is passed and approved), and this last
     * has occurred before a date.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param lifeCycleMilestoneId
     *            the milestone id
     * @param limitDate
     *            the limit date that the milestone instance has occurred
     */
    public static boolean hasLCMilestoneInstanceApprovedByPEAndLCMilestone(Long portfolioEntryId, Long lifeCycleMilestoneId, Date limitDate) {
        return findLifeCycleMilestoneInstance.where().eq(DELETED, false).eq(LIFE_CYCLE_MILESTONE_ID, lifeCycleMilestoneId).eq(IS_PASSED, true)
                .le("passedDate", limitDate).eq(LIFE_CYCLE_INSTANCE_PORTFOLIO_ENTRY_ID, portfolioEntryId).eq(LIFE_CYCLE_INSTANCE_IS_ACTIVE, true)
                .eq(LIFE_CYCLE_MILESTONE_INSTANCE_STATUS_TYPE_IS_APPROVED, true).findRowCount() > 0;
    }

    /**
     * Get the list of current milestones status of a portfolio entry.
     * 
     * a status is a label that defines the state of a milestone (not planned,
     * pending or passed: approved/rejected)
     * 
     * it returns the list of status (one for each milestone of the process of
     * the portfolio entry), with a larger display for the one given as a
     * parameter
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param lifeCycleMilestoneId
     *            the milestone id for which the status must have a larger
     *            display
     */
    public static List<String> getLCMilestoneAsStatusByPEAndLCMilestone(Long portfolioEntryId, Long lifeCycleMilestoneId) {

        List<String> status = new ArrayList<>();
        for (LifeCycleMilestone milestone : LifeCycleMilestoneDao.getLCMilestoneAsListByPe(portfolioEntryId)) {

            if (milestone.isActive) {

                String cssClass = null;

                List<LifeCycleMilestoneInstance> milestoneInstances = LifeCycleMilestoneDao.getLCMilestoneInstanceAsListByPEAndLCMilestone(portfolioEntryId,
                        milestone.id, "DESC");
                if (!milestoneInstances.isEmpty()) {
                    LifeCycleMilestoneInstance lastMilestoneInstance = milestoneInstances.get(0);

                    if (lastMilestoneInstance.isPassed) {
                        if (lastMilestoneInstance.lifeCycleMilestoneInstanceStatusType != null) {
                            if (lastMilestoneInstance.lifeCycleMilestoneInstanceStatusType.isApproved) {
                                cssClass = "label-success";
                            } else {
                                cssClass = "label-danger";
                            }
                        }
                    } else {
                        cssClass = "label-warning";
                    }
                } else {
                    cssClass = "label-default";
                }

                if (cssClass != null) {
                    String s = "<span class='label " + cssClass + "'>" + milestone.getShortName() + "</span>";
                    if (milestone.id.equals(lifeCycleMilestoneId)) {
                        s = "<span style='fontSize-size: 1.3em;'>" + s + "</span>";
                    }
                    status.add(s);
                }

            }
        }
        return status;
    }

    /**
     * Get a life cycle milestone instance approver by id.
     * 
     * @param id
     *            the life cycle milestone instance approver id
     */
    public static LifeCycleMilestoneInstanceApprover getLCMilestoneInstanceApproverById(Long id) {
        return findLifeCycleMilestoneInstanceApprover.where().eq(DELETED, false).eq("id", id).findUnique();
    }

    /**
     * Get the life cycle milestone instance approver for an actor and a
     * milestone instance.
     * 
     * note: this is used to define if an actor is an approver of a milestone
     * instance
     * 
     * @param actorId
     *            the actor id
     * @param milestoneInstanceId
     *            the milestone instance id
     */
    public static LifeCycleMilestoneInstanceApprover getLCMilestoneInstanceApproverByActorAndLCMilestoneInstance(Long actorId, Long actorOrgUnitId, Long milestoneInstanceId) {
        if (actorOrgUnitId == null) {
            return findLifeCycleMilestoneInstanceApprover.where().eq(DELETED, false)
                .eq("actor.id", actorId)
                .eq(LIFE_CYCLE_MILESTONE_INSTANCE_ID, milestoneInstanceId).findUnique();

        }
        return findLifeCycleMilestoneInstanceApprover.where().eq(DELETED, false)
                .or(Expr.eq("actor.id", actorId), Expr.eq("orgUnit.id", actorOrgUnitId))
                .eq(LIFE_CYCLE_MILESTONE_INSTANCE_ID, milestoneInstanceId).findUnique();
    }

    /**
     * Get a life cycle milestone instance status type by id.
     * 
     * @param id
     *            the status type id
     */
    public static LifeCycleMilestoneInstanceStatusType getLCMilestoneInstanceStatusTypeById(Long id) {
        return findLifeCycleMilestoneInstanceStatusType.where().eq(DELETED, false).eq("id", id).findUnique();
    }

    /**
     * Get all status types.
     */
    public static List<LifeCycleMilestoneInstanceStatusType> getLCMilestoneInstanceStatusTypeAsList() {
        return findLifeCycleMilestoneInstanceStatusType.where().eq(DELETED, false).findList();
    }

    /**
     * Get all selectable status types as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getLCMilestoneInstanceStatusTypeActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(
                findLifeCycleMilestoneInstanceStatusType.where().eq(DELETED, false).eq("selectable", true).findList());
    }

    /**
     * Get a status type by name.
     * 
     * @param name
     *            the status type name
     */
    public static LifeCycleMilestoneInstanceStatusType getLCMilestoneInstanceStatusTypeByName(String name) {
        return findLifeCycleMilestoneInstanceStatusType.where().eq(DELETED, false).eq("name", name).findUnique();
    }

    /**
     * Get a life cycle phase by id.
     * 
     * @param id
     *            the life cycle phase id
     */
    public static LifeCyclePhase getLCPhaseById(Long id) {
        return findLifeCyclePhase.where().eq(DELETED, false).eq("id", id).findUnique();
    }

    /**
     * Get all phases of a process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     */
    public static List<LifeCyclePhase> getLCPhaseAsListByLCProcess(Long lifeCycleProcessId) {
        return findLifeCyclePhase.orderBy(ORDER).where().eq(DELETED, false).eq(LIFE_CYCLE_PROCESS_ID, lifeCycleProcessId).findList();
    }

    /**
     * Get the roadmap phases of a process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     */

    public static List<LifeCyclePhase> getLCPhaseRoadmapAsListByLCProcess(Long lifeCycleProcessId) {
        return findLifeCyclePhase.orderBy(ORDER).where().eq(DELETED, false).eq("isRoadmapPhase", true).eq(LIFE_CYCLE_PROCESS_ID, lifeCycleProcessId)
                .findList();
    }

    /**
     * Get the phase of a process with the previous order.
     * 
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     * @param order
     *            the current order
     */
    public static LifeCyclePhase getLCPhaseAsPreviousByLCProcess(Long lifeCycleProcessId, int order) {
        return findLifeCyclePhase.orderBy(ORDER_DESC).where().eq(DELETED, false).eq(LIFE_CYCLE_PROCESS_ID, lifeCycleProcessId).lt(ORDER, order)
                .setMaxRows(1).findUnique();
    }

    /**
     * Get the phase of a process with the next order.
     * 
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     * @param order
     *            the current order
     */
    public static LifeCyclePhase getLCPhaseAsNextByLCProcess(Long lifeCycleProcessId, int order) {
        return findLifeCyclePhase.orderBy("order ASC").where().eq(DELETED, false).eq(LIFE_CYCLE_PROCESS_ID, lifeCycleProcessId).gt(ORDER, order)
                .setMaxRows(1).findUnique();
    }

    /**
     * Get the last order for a process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process
     */
    public static Integer getLCPhaseAsLastOrderByLCProcess(Long lifeCycleProcessId) {
        LifeCyclePhase lastPhase = findLifeCyclePhase.orderBy(ORDER_DESC).where().eq(DELETED, false).eq(LIFE_CYCLE_PROCESS_ID, lifeCycleProcessId)
                .setMaxRows(1).findUnique();
        if (lastPhase == null) {
            return -1;
        } else {
            return lastPhase.order;
        }

    }

    /**
     * Get available milestones for a portfolio entry (including additional milestones defined in the planning) as list
     *
     * @param portfolioEntryId the portfolio entry id
     */
    public static List<LifeCycleMilestone> getLCMilestoneAsListByPe(Long portfolioEntryId) {

        return LifeCyclePlanningDao.getPlannedLCMilestoneInstanceLastAsListByPE(portfolioEntryId)
                .stream()
                .map(plannedMilestone -> plannedMilestone.lifeCycleMilestone)
                .collect(Collectors.toList());
    }
}
