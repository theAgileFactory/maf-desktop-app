package modules;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.name.Names;

import be.objectify.deadbolt.java.cache.HandlerCache;
import constants.IMafConstants;
import constants.MafDataType;
import controllers.api.ApiAuthenticationBizdockCheck;
import framework.commons.IFrameworkConstants;
import framework.commons.IFrameworkConstants.AuthenticationMode;
import framework.modules.FrameworkModule;
import framework.patcher.IPatchLog;
import framework.patcher.PatchManager;
import framework.patcher.PatcherException;
import framework.services.account.AccountManagerPluginImpl;
import framework.services.account.DefaultAuthenticationAccountReaderPlugin;
import framework.services.account.DefaultAuthenticationAccountWriterPlugin;
import framework.services.account.DefaultPreferenceManagementPlugin;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IAuthenticationAccountReaderPlugin;
import framework.services.account.IAuthenticationAccountWriterPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.account.LightAuthenticationAccountReaderPlugin;
import framework.services.account.LightAuthenticationAccountWriterPlugin;
import framework.services.actor.ActorSystemPluginImpl;
import framework.services.actor.IActorSystemPlugin;
import framework.services.api.server.ApiSignatureServiceImpl;
import framework.services.api.server.IApiSignatureService;
import framework.services.audit.AuditLoggerServiceImpl;
import framework.services.audit.IAuditLoggerService;
import framework.services.configuration.I18nMessagesPluginImpl;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.configuration.IImplementationDefinedObjectService;
import framework.services.database.DatabaseDependencyServiceImpl;
import framework.services.database.IDatabaseDependencyService;
import framework.services.ext.ExtensionManagerServiceImpl;
import framework.services.ext.IExtensionManagerService;
import framework.services.job.IJobDescriptor;
import framework.services.job.IJobsService;
import framework.services.job.JobInitialConfig;
import framework.services.job.JobsServiceImpl;
import framework.services.kpi.IKpiService;
import framework.services.kpi.KpiServiceImpl;
import framework.services.notification.DefaultNotificationManagerPlugin;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.plugins.IEventBroadcastingService;
import framework.services.plugins.IPluginManagerService;
import framework.services.plugins.PluginManagerServiceImpl;
import framework.services.remote.AdPanelServiceImpl;
import framework.services.remote.IAdPanelManagerService;
import framework.services.router.CustomRouterServiceImpl;
import framework.services.router.ICustomRouterNotificationService;
import framework.services.router.ICustomRouterService;
import framework.services.session.CookieUserSessionManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.DefaultAttachmentManagerPlugin;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.services.storage.ISharedStorageService;
import framework.services.storage.PersonalStoragePluginImpl;
import framework.services.storage.SharedStorageServiceImpl;
import framework.services.system.ISysAdminUtils;
import framework.services.system.SysAdminUtilsImpl;
import models.CustomBeanPersistController;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.db.ebean.DefaultEbeanConfig;
import play.db.ebean.EbeanConfig;
import security.DefaultHandlerCache;
import services.bizdockapi.BizdockApiClientImpl;
import services.bizdockapi.IBizdockApiClient;
import services.configuration.ImplementationDefinedObjectImpl;
import services.datasyndication.DataSyndicationServiceImpl;
import services.datasyndication.IDataSyndicationService;
import services.echannel.EchannelServiceImpl;
import services.echannel.IEchannelService;
import services.job.JobDescriptors;
import services.licensesmanagement.ILicensesManagementService;
import services.licensesmanagement.LicensesManagementServiceImpl;
import utils.reporting.IReportingUtils;
import utils.reporting.ReportingUtilsImpl;

/**
 * The module which configure the dependency injection for the application
 * 
 * @author Pierre-Yves Cloux
 */
public class ApplicationServicesModule extends FrameworkModule {
    private static Logger.ALogger log = Logger.of(ApplicationServicesModule.class);

    private final Configuration configuration;

    public ApplicationServicesModule(Environment environment, Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * The method deals with the initializations required for the application to
     * run
     */
    protected void beforeInjection() {
        super.beforeInjection();
        initDataTypes();
    }

    @Override
    protected void configure() {
        beforeInjection();
        super.configure();
        log.info(">>> Desktop static dependency injected start...");
        requestStaticInjection(CustomBeanPersistController.class);
        requestStaticInjection(ApiAuthenticationBizdockCheck.class);
        requestStaticInjection(StaticAccessor.class);
        log.info("...Desktop static dependency injected end");

        log.info(">>> Standard dependency injection start...");
        bind(EbeanConfig.class).toProvider(DefaultEbeanConfig.EbeanConfigParser.class).asEagerSingleton();
        bind(IDatabaseDependencyService.class).to(DatabaseDependencyServiceImpl.class).asEagerSingleton();
        bind(IImplementationDefinedObjectService.class).to(ImplementationDefinedObjectImpl.class).asEagerSingleton();
        bind(HandlerCache.class).to(DefaultHandlerCache.class).asEagerSingleton();
        bind(IExtensionManagerService.class).to(ExtensionManagerServiceImpl.class).asEagerSingleton();
        bind(II18nMessagesPlugin.class).to(I18nMessagesPluginImpl.class).asEagerSingleton();
        bind(IUserSessionManagerPlugin.class).to(CookieUserSessionManagerPlugin.class).asEagerSingleton();
        bind(IPreferenceManagerPlugin.class).to(DefaultPreferenceManagementPlugin.class).asEagerSingleton();
        bind(IPersonalStoragePlugin.class).to(PersonalStoragePluginImpl.class).asEagerSingleton();
        bind(ISharedStorageService.class).to(SharedStorageServiceImpl.class).asEagerSingleton();
        bind(IAdPanelManagerService.class).to(AdPanelServiceImpl.class).asEagerSingleton();
        bind(IAttachmentManagerPlugin.class).to(DefaultAttachmentManagerPlugin.class).asEagerSingleton();
        bind(INotificationManagerPlugin.class).to(DefaultNotificationManagerPlugin.class).asEagerSingleton();
        bind(IPluginManagerService.class).to(PluginManagerServiceImpl.class).asEagerSingleton();
        bind(IEventBroadcastingService.class).to(PluginManagerServiceImpl.class);
        bind(IActorSystemPlugin.class).to(ActorSystemPluginImpl.class).asEagerSingleton();
        bind(IKpiService.class).to(KpiServiceImpl.class).asEagerSingleton();
        bind(IAuditLoggerService.class).to(AuditLoggerServiceImpl.class).asEagerSingleton();
        bind(IReportingUtils.class).to(ReportingUtilsImpl.class).asEagerSingleton();
        bind(ISysAdminUtils.class).to(SysAdminUtilsImpl.class).asEagerSingleton();
        bind(ICustomRouterService.class).to(CustomRouterServiceImpl.class).asEagerSingleton();
        bind(ICustomRouterNotificationService.class).to(CustomRouterServiceImpl.class).asEagerSingleton();
        bind(IApiSignatureService.class).to(ApiSignatureServiceImpl.class).asEagerSingleton();
        bind(IBizdockApiClient.class).to(BizdockApiClientImpl.class).asEagerSingleton();

        // Initialize with a defined list of jobs
        List<IJobDescriptor> jobs = new ArrayList<>();
        JobDescriptors.UpdateConsumedLicensesJobDescriptor updateConsumedLicensesJobDescriptor = new JobDescriptors.UpdateConsumedLicensesJobDescriptor();
        bind(JobDescriptors.UpdateConsumedLicensesJobDescriptor.class).toInstance(updateConsumedLicensesJobDescriptor);
        jobs.add(updateConsumedLicensesJobDescriptor);
        JobDescriptors.SendNotificationEventsJobDescriptor sendNotificationEventsJobDescriptor = new JobDescriptors.SendNotificationEventsJobDescriptor();
        bind(JobDescriptors.SendNotificationEventsJobDescriptor.class).toInstance(sendNotificationEventsJobDescriptor);
        jobs.add(sendNotificationEventsJobDescriptor);
        JobDescriptors.ActivatePendingInstanceAgreementJobDescriptor activatePendingInstanceAgreementJobDescriptor = new JobDescriptors.ActivatePendingInstanceAgreementJobDescriptor();
        bind(JobDescriptors.ActivatePendingInstanceAgreementJobDescriptor.class).toInstance(activatePendingInstanceAgreementJobDescriptor);
        jobs.add(activatePendingInstanceAgreementJobDescriptor);
        JobDescriptors.SynchronizeDataSyndicationJobDescriptor synchronizeDataSyndicationJobDescriptor = new JobDescriptors.SynchronizeDataSyndicationJobDescriptor();
        bind(JobDescriptors.SynchronizeDataSyndicationJobDescriptor.class).toInstance(synchronizeDataSyndicationJobDescriptor);
        jobs.add(synchronizeDataSyndicationJobDescriptor);
        bind(JobInitialConfig.class).annotatedWith(Names.named("JobConfig")).toInstance(new JobInitialConfig(jobs));
        bind(IJobsService.class).to(JobsServiceImpl.class).asEagerSingleton();

        // Echannel services
        bind(IEchannelService.class).to(EchannelServiceImpl.class).asEagerSingleton();
        bind(ILicensesManagementService.class).to(LicensesManagementServiceImpl.class).asEagerSingleton();
        bind(IDataSyndicationService.class).to(DataSyndicationServiceImpl.class).asEagerSingleton();

        // Configure the authentication system
        IFrameworkConstants.AuthenticationMode authenticationMode = getConfiguredAuthenticationMode();
        bind(IFrameworkConstants.AuthenticationMode.class).annotatedWith(Names.named("AuthenticatonMode")).toInstance(authenticationMode);
        log.warn("AUTHENTICATION MODE [" + authenticationMode + "]");
        Boolean ldapMasterMode = getConfiguration().getBoolean("maf.ic_ldap_master");
        switch (authenticationMode) {
        case CAS_MASTER:
            bind(IAuthenticationAccountReaderPlugin.class).to(DefaultAuthenticationAccountReaderPlugin.class).asEagerSingleton();
            bind(IAuthenticationAccountWriterPlugin.class).to(DefaultAuthenticationAccountWriterPlugin.class).asEagerSingleton();
            bind(String.class).annotatedWith(Names.named("UserAccountClassName")).toInstance("framework.services.account.DefaultUserAccount");
            bind(Boolean.class).annotatedWith(Names.named("AuthenticationRepositoryMasterMode")).toInstance(ldapMasterMode);
            bind(IAccountManagerPlugin.class).to(AccountManagerPluginImpl.class).asEagerSingleton();
            break;
        case CAS_SLAVE:
            bind(IAuthenticationAccountReaderPlugin.class).to(LightAuthenticationAccountReaderPlugin.class).asEagerSingleton();
            bind(IAuthenticationAccountWriterPlugin.class).to(LightAuthenticationAccountWriterPlugin.class).asEagerSingleton();
            bind(String.class).annotatedWith(Names.named("UserAccountClassName")).toInstance("framework.services.account.LightUserAccount");
            bind(Boolean.class).annotatedWith(Names.named("AuthenticationRepositoryMasterMode")).toInstance(false);
            bind(IAccountManagerPlugin.class).to(AccountManagerPluginImpl.class).asEagerSingleton();
            break;
        case FEDERATED:
            bind(IAuthenticationAccountReaderPlugin.class).to(LightAuthenticationAccountReaderPlugin.class).asEagerSingleton();
            bind(IAuthenticationAccountWriterPlugin.class).to(LightAuthenticationAccountWriterPlugin.class).asEagerSingleton();
            bind(String.class).annotatedWith(Names.named("UserAccountClassName")).toInstance("framework.services.account.LightUserAccount");
            bind(Boolean.class).annotatedWith(Names.named("AuthenticationRepositoryMasterMode")).toInstance(false);
            bind(IAccountManagerPlugin.class).to(AccountManagerPluginImpl.class).asEagerSingleton();
            break;
        case STANDALONE:
            bind(IAuthenticationAccountReaderPlugin.class).to(LightAuthenticationAccountReaderPlugin.class).asEagerSingleton();
            bind(IAuthenticationAccountWriterPlugin.class).to(LightAuthenticationAccountWriterPlugin.class).asEagerSingleton();
            bind(String.class).annotatedWith(Names.named("UserAccountClassName")).toInstance("framework.services.account.LightUserAccount");
            bind(Boolean.class).annotatedWith(Names.named("AuthenticationRepositoryMasterMode")).toInstance(ldapMasterMode);
            bind(IAccountManagerPlugin.class).to(AccountManagerPluginImpl.class).asEagerSingleton();
            break;
        }
        log.info(">>> Standard dependency injection end");
    }

    /**
     * Register the data types to be used in various place of the application
     */
    private void initDataTypes() {
        MafDataType.add(IMafConstants.Actor, "models.pmo.Actor", true, true);
        MafDataType.add(IMafConstants.BudgetBucket, "models.finance.BudgetBucket", false, true);
        MafDataType.add(IMafConstants.BudgetBucketLine, "models.finance.BudgetBucketLine", false, false);
        MafDataType.add(IMafConstants.CostCenter, "models.finance.CostCenter", false, false);
        MafDataType.add(IMafConstants.Iteration, "models.delivery.Iteration", false, true);
        MafDataType.add(IMafConstants.OrgUnit, "models.pmo.OrgUnit", true, true);
        MafDataType.add(IMafConstants.PortfolioEntryBudget, "models.finance.PortfolioEntryBudget", false, false);
        MafDataType.add(IMafConstants.PortfolioEntryBudgetLine, "models.finance.PortfolioEntryBudgetLine", true, true);
        MafDataType.add(IMafConstants.PortfolioEntryEvent, "models.pmo.PortfolioEntryEvent", false, true);
        MafDataType.add(IMafConstants.PortfolioEntry, "models.pmo.PortfolioEntry", true, true);
        MafDataType.add(IMafConstants.PortfolioEntryPlanningPackage, "models.pmo.PortfolioEntryPlanningPackage", false, true);
        MafDataType.add(IMafConstants.PortfolioEntryReport, "models.pmo.PortfolioEntryReport", false, true);
        MafDataType.add(IMafConstants.PortfolioEntryResourcePlanAllocatedActor, "models.finance.PortfolioEntryResourcePlanAllocatedActor", false, true);
        MafDataType.add(IMafConstants.PortfolioEntryResourcePlanAllocatedOrgUnit, "models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit", false, true);
        MafDataType.add(IMafConstants.PortfolioEntryResourcePlanAllocatedCompetency, "models.finance.PortfolioEntryResourcePlanAllocatedCompetency", false,
                true);
        MafDataType.add(IMafConstants.PortfolioEntryRisk, "models.pmo.PortfolioEntryRisk", false, true);
        MafDataType.add(IMafConstants.Portfolio, "models.pmo.Portfolio", true, true);
        MafDataType.add(IMafConstants.Stakeholder, "models.pmo.Stakeholder", true, false);
        MafDataType.add(IMafConstants.PurchaseOrderLineItem, "models.finance.PurchaseOrderLineItem", true, false);
        MafDataType.add(IMafConstants.PurchaseOrder, "models.finance.PurchaseOrder", true, false);
        MafDataType.add(IMafConstants.Release, "models.delivery.Release", false, true);
        MafDataType.add(IMafConstants.Requirement, "models.delivery.Requirement", false, true);
        MafDataType.add(IMafConstants.TimesheetActivityAllocatedActor, "models.timesheet.TimesheetActivityAllocatedActor", false, true);
        MafDataType.add(IMafConstants.WorkOrder, "models.finance.WorkOrder", false, true);
    }

    /**
     * Execute the patches.
     */
    private void runPatchBeforeStart() {
        try {
            PatchManager patchManager = new PatchManager("com.agifac.maf.desktop.patcher", "before_start_status.log", new IPatchLog() {
                @Override
                public void warn(String message) {
                    log.warn("PATCH - " + message);
                }

                @Override
                public void info(String message) {
                    log.info("PATCH - " + message);
                }
            });
            patchManager.execute();
        } catch (PatcherException e) {
            // Halt the execution of the application startup
            throw new RuntimeException(e);
        }
    }

    private Configuration getConfiguration() {
        return configuration;
    }

    /**
     * The authentication mode is either:
     * <ul>
     * <li>read from the database (if a record exists)</li>
     * <li>read from the configuration file (default option)</li>
     * </ul>
     * 
     * @return
     */
    public AuthenticationMode getConfiguredAuthenticationMode() {
        Connection connection = null;
        try {
            String driver = getConfiguration().getString("db.default.driver");
            String url = getConfiguration().getString("db.default.url");
            String username = getConfiguration().getString("db.default.username");
            String password = getConfiguration().getString("db.default.password");
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
            String sql = "select scav.value from string_custom_attribute_value as scav "
                    + "join custom_attribute_definition as cad on scav.custom_attribute_definition_id=cad.id "
                    + "join preference as pref on pref.uuid=cad.uuid " + "where pref.uuid='AUTHENTICATION_MODE_PREFERENCE'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.first()) {
                return AuthenticationMode.valueOf(rs.getString("value"));
            }
            return AuthenticationMode.valueOf(getConfiguration().getString("maf.authentication.mode"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to read the authentication mode from the database", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
            }
        }
    }
}
