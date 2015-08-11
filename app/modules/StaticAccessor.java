package modules;

import javax.inject.Inject;
import javax.inject.Singleton;

import services.licensesmanagement.ILicensesManagementService;

@Singleton
public class StaticAccessor {
    @Inject
    private static ILicensesManagementService licensesManagementService;
    
    public StaticAccessor() {
    }

    public static ILicensesManagementService getLicensesManagementService() {
        if (licensesManagementService == null) {
            throw new IllegalArgumentException("Service is not injected yet");
        }
        return licensesManagementService;
    }

}
