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
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Finder;
import dao.pmo.PortfolioEntryDao;
import framework.services.account.IPreferenceManagerPlugin;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;
import models.governance.*;
import models.pmo.PortfolioEntry;
import services.budgettracking.IBudgetTrackingService;

import java.util.*;

/**
 * DAO for the {@link LifeCycleMilestone} and {@link LifeCycleMilestoneInstance}
 * and {@link LifeCycleMilestoneInstanceApprover} and
 * {@link LifeCycleMilestoneInstanceStatusType} and {@link LifeCyclePhase}
 * objects.
 * 
 * @author Johann Kohler
 */
public abstract class LifeCycleMilestoneDao {

    public static Finder<Long, LifeCycleMilestone> findLifeCycleMilestone = new Finder<>(LifeCycleMilestone.class);

    public static Finder<Long, LifeCycleMilestoneInstance> findLifeCycleMilestoneInstance = new Finder<>(LifeCycleMilestoneInstance.class);

    public static Finder<Long, LifeCycleMilestoneInstanceApprover> findLifeCycleMilestoneInstanceApprover = new Finder<>(
            LifeCycleMilestoneInstanceApprover.class);

    public static Finder<Long, LifeCycleMilestoneInstanceStatusType> findLifeCycleMilestoneInstanceStatusType = new Finder<>(
            LifeCycleMilestoneInstanceStatusType.class);

    public static Finder<Long, LifeCyclePhase> findLifeCyclePhase = new Finder<>(LifeCyclePhase.class);
    
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
        return findLifeCycleMilestone.where().eq("deleted", false).eq("id", id).findUnique();
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
        return findLifeCycleMilestone.where().eq("deleted", false).eq("lifeCycleProcess.id", processId).eq("type", type).findUnique();
    }

    /**
     * Get a life cycle milestone by short name.
     * 
     * @param shortName
     *            the milestone short name
     */
    public static LifeCycleMilestone getLCMilestoneByShortName(String shortName) {
        return findLifeCycleMilestone.where().eq("deleted", false).eq("shortName", shortName).findUnique();
    }

    /**
     * Get all milestone of a process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     */
    public static List<LifeCycleMilestone> getLCMilestoneAsListByLCProcess(Long lifeCycleProcessId) {
        return findLifeCycleMilestone.orderBy("order").where().eq("deleted", false).eq("lifeCycleProcess.id", lifeCycleProcessId).findList();
    }

    /**
     * Get all active life cycle milestones (of all active processes) as value
     * holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getLCMilestoneActiveAsVH() {

        DefaultSelectableValueHolderCollection<Long> valueHolderCollection = new DefaultSelectableValueHolderCollection<>();
        List<LifeCycleMilestone> list = findLifeCycleMilestone.where().eq("deleted", false).eq("isActive", true).eq("lifeCycleProcess.deleted", false)
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
        List<LifeCycleMilestone> list = findLifeCycleMilestone.where().eq("deleted", false).eq("lifeCycleProcess.id", lifeCycleProcessId).findList();
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
        return findLifeCycleMilestone.orderBy("order DESC").where().eq("deleted", false).eq("lifeCycleProcess.id", lifeCycleProcessId).lt("order", order)
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
        return findLifeCycleMilestone.orderBy("order ASC").where().eq("deleted", false).eq("lifeCycleProcess.id", lifeCycleProcessId).gt("order", order)
                .setMaxRows(1).findUnique();
    }

    /**
     * Get the last order for a process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process
     */
    public static Integer getLCMilestoneAsLastOrderByLCProcess(Long lifeCycleProcessId) {
        LifeCycleMilestone lastMilestone = findLifeCycleMilestone.orderBy("order DESC").where().eq("deleted", false)
                .eq("lifeCycleProcess.id", lifeCycleProcessId).setMaxRows(1).findUnique();
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
        return findLifeCycleMilestoneInstance.where().eq("deleted", false).eq("id", id).findUnique();
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

        // Update the last approved lifecycle milestone instance
        if (portfolioEntry.lastApprovedLifeCycleMilestoneInstance != null && lifeCycleMilestoneInstance.id.equals(portfolioEntry.lastApprovedLifeCycleMilestoneInstance.id)) {
            if (approvedLifecycleMilestoneInstances.isEmpty()) {
                portfolioEntry.lastApprovedLifeCycleMilestoneInstance = null;
            } else {
                portfolioEntry.lastApprovedLifeCycleMilestoneInstance = approvedLifecycleMilestoneInstances
                        .stream()
                        .sorted((m1, m2) -> m2.passedDate.compareTo(m1.passedDate))
                        .findFirst()
                        .get();
            }
        }

        updatePortfolioEntryWithNextMilestone(portfolioEntry, approvedLifecycleMilestoneInstances);

        portfolioEntry.save();

        lifeCycleMilestoneInstance.lifeCycleInstance.save();

        // Delete the lifecycle milestone instance
        lifeCycleMilestoneInstance.doDelete();
        createNextPlanningFromPreviousOne(lifeCycleMilestoneInstance, currentPlanning);

    }

    private static void updatePortfolioEntryWithNextMilestone(PortfolioEntry portfolioEntry, List<LifeCycleMilestoneInstance> approvedLifecycleMilestoneInstances) {
        // Update the next lifecycle milestone instance
        // Get the list of planned lifecycle milestone instances
        List<PlannedLifeCycleMilestoneInstance> plannedLifeCycleMilestoneInstances = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceLastAsListByPE(portfolioEntry.id);

        Optional<PlannedLifeCycleMilestoneInstance> nextMilestone = plannedLifeCycleMilestoneInstances
                .stream()
                // Filter the milestones already approved
                .filter(plannedMilestone -> approvedLifecycleMilestoneInstances
                        .stream()
                        .map(instance -> instance.lifeCycleMilestone.id)
                        .noneMatch(id -> id.equals(plannedMilestone.lifeCycleMilestone.id))
                )
                // Get the first not approved one
                .findFirst();

        portfolioEntry.nextPlannedLifeCycleMilestoneInstance = nextMilestone.isPresent() ? nextMilestone.get() : null;
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
            if (portfolioEntry.lastApprovedLifeCycleMilestoneInstance == null
                    || !portfolioEntry.lastApprovedLifeCycleMilestoneInstance.passedDate.after(lifeCycleMilestoneInstance.passedDate)) {
                portfolioEntry.lastApprovedLifeCycleMilestoneInstance = lifeCycleMilestoneInstance;
            }

            updatePortfolioEntryWithNextMilestone(portfolioEntry, lifeCycleMilestoneInstance.lifeCycleInstance.getApprovedLifecycleMilestoneInstances());
            portfolioEntry.save();

            lifeCycleMilestoneInstance.lifeCycleInstance.save();

        }

        createNextPlanningFromPreviousOne(lifeCycleMilestoneInstance, currentPlanning);

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
        for (PlannedLifeCycleMilestoneInstance lastDate : LifeCyclePlanningDao
                .getPlannedLCMilestoneInstanceLastAsListByPE(lifeCycleMilestoneInstance.lifeCycleInstance.portfolioEntry.id)) {
            lastDates.put(lastDate.lifeCycleMilestone.id, lastDate.plannedDate);
        }

        // add the new plannings
        for (LifeCycleMilestone milestone : lifeCycleMilestoneInstance.lifeCycleInstance.lifeCycleProcess.lifeCycleMilestones) {

            /**
             * Check if the milestone for the portfolio entry has an approved
             * milestone instance, meaning the milestone is passed and approved.
             */
            boolean hasApprovedInstancesForMilestoneOfPortfolioEntry = Ebean.find(LifeCycleMilestoneInstance.class).where()
                    .eq("deleted", false)
                    .eq("lifeCycleMilestone.id", milestone.id)
                    .eq("isPassed", true)
                    .eq("lifeCycleInstance.portfolioEntry.id", lifeCycleMilestoneInstance.lifeCycleInstance.portfolioEntry.id)
                    .eq("lifeCycleInstance.isActive", true).eq("lifeCycleMilestoneInstanceStatusType.isApproved", true).findRowCount() > 0;

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
        return findLifeCycleMilestoneInstance.where().eq("deleted", false).eq("lifeCycleMilestone.id", lifeCycleMilestoneId)
                .eq("lifeCycleInstance.portfolioEntry.id", portfolioEntryId).eq("lifeCycleInstance.isActive", true).orderBy("passedDate " + order).findList();
    }

    /**
     * Get the approved instances of a process instance.
     * 
     * @param lifeCycleInstanceId
     *            the process instance id
     */
    public static List<LifeCycleMilestoneInstance> getLCMilestoneInstanceAsListByLCInstance(Long lifeCycleInstanceId) {
        return findLifeCycleMilestoneInstance.orderBy("passedDate").where().eq("deleted", false).eq("lifeCycleInstance.id", lifeCycleInstanceId)
                .eq("isPassed", true).eq("lifeCycleMilestoneInstanceStatusType.isApproved", true).findList();
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
        Integer nOfApprovers = findLifeCycleMilestoneInstanceApprover.where().eq("deleted", false)
                .eq("lifeCycleMilestoneInstance.id", lifeCycleMilestonInstanceId).findRowCount();
        Integer nOfVotingApprovers = findLifeCycleMilestoneInstanceApprover.where().eq("deleted", false)
                .eq("lifeCycleMilestoneInstance.id", lifeCycleMilestonInstanceId).isNotNull("approvalDate").findRowCount();
        return nOfApprovers.equals(nOfVotingApprovers);
    }

    /**
     * Get all active and non-passed milestone instance as expression, meaning
     * all milestone instances for which a vote/decision is required.
     */
    public static ExpressionList<LifeCycleMilestoneInstance> getLCMilestoneInstanceAsExpr() {
        return findLifeCycleMilestoneInstance.where().eq("deleted", false).eq("isPassed", false)
                .eq("lifeCycleInstance.isActive", true).eq("lifeCycleInstance.portfolioEntry.deleted", false);
    }

    /**
     * Get all milestone instances as pagination object for which a user (here
     * called an approver) should vote.
     * @param approverId
     */
    public static ExpressionList<LifeCycleMilestoneInstance> getLCMilestoneInstanceAsExprByApprover(Long approverId) {
        return getLCMilestoneInstanceAsExpr()
                .eq("lifeCycleMilestoneInstanceApprovers.actor.id", approverId)
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

        return findLifeCycleMilestoneInstance.where().eq("deleted", false).eq("lifeCycleInstance.deleted", false).eq("lifeCycleInstance.isActive", true)
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
        return findLifeCycleMilestoneInstance.where().eq("deleted", false).eq("lifeCycleMilestone.id", lifeCycleMilestoneId).eq("isPassed", true)
                .le("passedDate", limitDate).eq("lifeCycleInstance.portfolioEntry.id", portfolioEntryId).eq("lifeCycleInstance.isActive", true)
                .eq("lifeCycleMilestoneInstanceStatusType.isApproved", true).findRowCount() > 0;
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

        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(portfolioEntryId);

        List<String> status = new ArrayList<>();
        for (LifeCycleMilestone milestone : portfolioEntry.activeLifeCycleInstance.lifeCycleProcess.lifeCycleMilestones) {

            if (milestone.isActive) {

                String cssClass = null;

                List<LifeCycleMilestoneInstance> milestoneInstances = LifeCycleMilestoneDao.getLCMilestoneInstanceAsListByPEAndLCMilestone(portfolioEntry.id,
                        milestone.id, "DESC");
                if (milestoneInstances.size() > 0) {
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
        return findLifeCycleMilestoneInstanceApprover.where().eq("deleted", false).eq("id", id).findUnique();
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
    public static LifeCycleMilestoneInstanceApprover getLCMilestoneInstanceApproverByActorAndLCMilestoneInstance(Long actorId, Long milestoneInstanceId) {
        return findLifeCycleMilestoneInstanceApprover.where().eq("deleted", false).eq("actor.id", actorId)
                .eq("lifeCycleMilestoneInstance.id", milestoneInstanceId).findUnique();
    }

    /**
     * Get a life cycle milestone instance status type by id.
     * 
     * @param id
     *            the status type id
     */
    public static LifeCycleMilestoneInstanceStatusType getLCMilestoneInstanceStatusTypeById(Long id) {
        return findLifeCycleMilestoneInstanceStatusType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all status types.
     */
    public static List<LifeCycleMilestoneInstanceStatusType> getLCMilestoneInstanceStatusTypeAsList() {
        return findLifeCycleMilestoneInstanceStatusType.where().eq("deleted", false).findList();
    }

    /**
     * Get all selectable status types as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getLCMilestoneInstanceStatusTypeActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(
                findLifeCycleMilestoneInstanceStatusType.where().eq("deleted", false).eq("selectable", true).findList());
    }

    /**
     * Get a status type by name.
     * 
     * @param name
     *            the status type name
     */
    public static LifeCycleMilestoneInstanceStatusType getLCMilestoneInstanceStatusTypeByName(String name) {
        return findLifeCycleMilestoneInstanceStatusType.where().eq("deleted", false).eq("name", name).findUnique();
    }

    /**
     * Get a life cycle phase by id.
     * 
     * @param id
     *            the life cycle phase id
     */
    public static LifeCyclePhase getLCPhaseById(Long id) {
        return findLifeCyclePhase.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all phases of a process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     */
    public static List<LifeCyclePhase> getLCPhaseAsListByLCProcess(Long lifeCycleProcessId) {
        return findLifeCyclePhase.orderBy("order").where().eq("deleted", false).eq("lifeCycleProcess.id", lifeCycleProcessId).findList();
    }

    /**
     * Get the roadmap phases of a process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process id
     */

    public static List<LifeCyclePhase> getLCPhaseRoadmapAsListByLCProcess(Long lifeCycleProcessId) {
        return findLifeCyclePhase.orderBy("order").where().eq("deleted", false).eq("isRoadmapPhase", true).eq("lifeCycleProcess.id", lifeCycleProcessId)
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
        return findLifeCyclePhase.orderBy("order DESC").where().eq("deleted", false).eq("lifeCycleProcess.id", lifeCycleProcessId).lt("order", order)
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
        return findLifeCyclePhase.orderBy("order ASC").where().eq("deleted", false).eq("lifeCycleProcess.id", lifeCycleProcessId).gt("order", order)
                .setMaxRows(1).findUnique();
    }

    /**
     * Get the last order for a process.
     * 
     * @param lifeCycleProcessId
     *            the life cycle process
     */
    public static Integer getLCPhaseAsLastOrderByLCProcess(Long lifeCycleProcessId) {
        LifeCyclePhase lastPhase = findLifeCyclePhase.orderBy("order DESC").where().eq("deleted", false).eq("lifeCycleProcess.id", lifeCycleProcessId)
                .setMaxRows(1).findUnique();
        if (lastPhase == null) {
            return -1;
        } else {
            return lastPhase.order;
        }

    }

}
