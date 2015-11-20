package services.action_log;

/**
 * The action log service interface for maf objects.
 * 
 * @author Johann Kohler
 *
 */
public interface IMafActionLogService {

    public static final String DELIVERABLE_CHANGE_REQUIREMENTS = "CHANGE_REQUIREMENTS";

    /**
     * Log the change of requirements in a deliverable.
     * 
     * @param deliverableId
     *            the concerned deliverable id
     * @param previousNumber
     *            the number of requirements before the change
     * @param newNumber
     *            the number of requirements after the change
     */
    void logChangeRequirementsOfDeliverable(Long deliverableId, Integer previousNumber, Integer newNumber);

}
