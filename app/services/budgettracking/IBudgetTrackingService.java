package services.budgettracking;

import models.governance.LifeCycleInstancePlanning;

/**
 * The budget tracking service.
 * 
 * @author Johann Kohler
 *
 */
public interface IBudgetTrackingService {

    /**
     * Return true if the budget tracking is active.
     */
    boolean isActive();

    /**
     * Recompute all budget and forecast of portfolio entry from the current
     * resource allocations.
     * 
     * @param planning
     *            the current planning of the portfolio entry
     */
    void recomputeAllBugdetAndForecastFromResource(LifeCycleInstancePlanning planning);

}
