package services.action_log;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.configuration.PropertiesConfiguration;

import constants.MafDataType;
import framework.services.action_log.IActionLogService;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;

/**
 * The action log service for maf objects.
 * 
 * @author Johann Kohler
 *
 */
@Singleton
public class MafActionLogServiceImpl implements IMafActionLogService {

    private static Logger.ALogger log = Logger.of(MafActionLogServiceImpl.class);

    private IActionLogService actionLogService;

    /**
     * Construct the service.
     * 
     * @param lifecycle
     *            the play application lifecycle listener
     * @param configuration
     *            the play application configuration
     * @param actionLogService
     *            the action log service
     */
    @Inject
    public MafActionLogServiceImpl(ApplicationLifecycle lifecycle, Configuration configuration, IActionLogService actionLogService) {
        log.info("SERVICE>>> ActionLogServiceImpl starting...");
        this.actionLogService = actionLogService;
        lifecycle.addStopHook(() -> {
            log.info("SERVICE>>> ActionLogServiceImpl stopping...");
            log.info("SERVICE>>> ActionLogServiceImpl stopped");
            return Promise.pure(null);
        });
        log.info("SERVICE>>> ActionLogServiceImpl started...");
    }

    @Override
    public void logChangeRequirementsOfDeliverable(Long deliverableId, Integer previousNumber, Integer newNumber) {
        PropertiesConfiguration parameters = new PropertiesConfiguration();
        parameters.addProperty("previousNumber", previousNumber);
        parameters.addProperty("newNumber", newNumber);
        this.getActionLogService().log(MafDataType.getDeliverable(), deliverableId, IMafActionLogService.DELIVERABLE_CHANGE_REQUIREMENTS, parameters);
    }

    /**
     * Get the action log service.
     */
    private IActionLogService getActionLogService() {
        return this.actionLogService;
    }

}
