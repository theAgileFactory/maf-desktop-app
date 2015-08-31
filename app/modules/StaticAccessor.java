package modules;

import javax.inject.Inject;
import javax.inject.Singleton;

import framework.security.ISecurityService;
import services.licensesmanagement.ILicensesManagementService;

@Singleton
public class StaticAccessor {
    @Inject
    private static ILicensesManagementService licensesManagementService;
    @Inject
    private static ISecurityService securityService;

    public StaticAccessor() {
    }

    public static ILicensesManagementService getLicensesManagementService() {
        if (licensesManagementService == null) {
            throw new IllegalArgumentException("Service is not injected yet");
        }
        return licensesManagementService;
    }

    public static ISecurityService getSecurityService() {
        if (securityService == null) {
            throw new IllegalArgumentException("Service is not injected yet");
        }
        return securityService;
    }

}
