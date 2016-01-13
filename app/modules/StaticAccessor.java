package modules;

import javax.inject.Inject;
import javax.inject.Singleton;

import framework.security.ISecurityService;
import services.licensesmanagement.ILicensesManagementService;

/**
 * The static accessor provides a static access to the services.
 * 
 * Ultimately, it will disappear.
 * 
 * @author Johann Kohler
 *
 */
@Singleton
public class StaticAccessor {
    @Inject
    private static ILicensesManagementService licensesManagementService;
    @Inject
    private static ISecurityService securityService;

    /**
     * Default constructor.
     */
    public StaticAccessor() {
    }

    /**
     * Get the licenses management service.
     */
    public static ILicensesManagementService getLicensesManagementService() {
        if (licensesManagementService == null) {
            throw new IllegalArgumentException("Service is not injected yet");
        }
        return licensesManagementService;
    }

    /**
     * Get the security service.
     */
    public static ISecurityService getSecurityService() {
        if (securityService == null) {
            throw new IllegalArgumentException("Service is not injected yet");
        }
        return securityService;
    }

}
