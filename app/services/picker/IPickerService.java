package services.picker;

import framework.utils.PickerHandler;

/**
 * The service that provides the pickers.
 * 
 * @author Johann Kohler
 */
public interface IPickerService {

    /**
     * Initialize the pickers.
     */
    void init();

    /**
     * Get the picker for principals.
     * 
     * Data: all active and displayed principals<br/>
     * Search: by first name / last name
     */
    PickerHandler<String> getPrincipal();

    /**
     * Get the picker for actors.
     * 
     * Data: all active actors<br/>
     * Search: by ref id / first name / last name
     */
    PickerHandler<Long> getActor();

    /**
     * Get the picker for actors without uid.
     * 
     * Data: all active actors without uid (null or empty)<br/>
     * Search: by ref id / first name / last name
     */
    PickerHandler<Long> getActorWithoutUid();

    /**
     * Get the picker for actors of an org unit.
     * 
     * Data: all active actors of an org unit<br/>
     * Search: no<br/>
     * Parameter: orgUnitId
     */
    PickerHandler<Long> getActorByOrgUnit();

    /**
     * Get the picker for actors with a given competency.
     * 
     * Data: all active actors with a given competency<br/>
     * Search: no<br/>
     * Parameter: competencyId
     */
    PickerHandler<Long> getActorByCompetency();

    /**
     * Get the picker for budget buckets.
     * 
     * Data: all budget buckets<br/>
     * Search: by ref id / name
     */
    PickerHandler<Long> getBudgetBucket();

    /**
     * Get the picker for cost centers.
     * 
     * Data: all cost centers<br/>
     * Search: by ref id / name
     */
    PickerHandler<Long> getCostCenter();

    /**
     * Get the picker for deliverables of a portfolio entry.
     * 
     * Data: all deliverables of a portfolio entry (as OWNER)<br/>
     * Search: no<br/>
     * Parameters: portfolioEntryId
     */
    PickerHandler<Long> getDeliverableByPortfolioEntry();

    /**
     * Get the picker for org units.
     * 
     * Data: all active org units<br/>
     * Search: by ref id / name
     */
    PickerHandler<Long> getOrgUnit();

    /**
     * Get the picker for delivery units.
     * 
     * Data: all active delivery units<br/>
     * Search: by ref id / name
     */
    PickerHandler<Long> getDeliveryUnit();

    /**
     * Get the picker for sponsoring units.
     * 
     * Data: all active sponsoring units<br/>
     * Search: by ref id / name
     */
    PickerHandler<Long> getSponsoringUnit();

    /**
     * Get the picker for planning packages (of a portfolio entry).
     * 
     * Data: all planning packages (of a portfolio entry)<br/>
     * Search: by name<br/>
     * Parameter: portfolioEntryId
     */
    PickerHandler<Long> getPlanningPackage();

    /**
     * Get the picker for portfolio entry types.
     * 
     * Data: all active portfolio entry types<br/>
     * Search: no
     */
    PickerHandler<Long> getPortfolioEntryType();

    /**
     * Get the picker for portfolios.
     * 
     * Data: all active portfolios<br/>
     * Search: no
     */
    PickerHandler<Long> getPortfolio();

    /**
     * Get the picker for portfolio types.
     * 
     * Data: all active portfolio types<br/>
     * Search: no
     */
    PickerHandler<Long> getPortfolioType();

}
