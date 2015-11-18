package modules;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.google.inject.name.Names;

import be.objectify.deadbolt.java.cache.HandlerCache;
import constants.IMafConstants;
import constants.MafDataType;
import controllers.api.ApiAuthenticationBizdockCheck;
import controllers.sso.Authenticator;
import framework.commons.IFrameworkConstants;
import framework.commons.IFrameworkConstants.AuthenticationMode;
import framework.modules.FrameworkModule;
import framework.security.IAuthenticator;
import framework.security.IInstanceAccessSupervisor;
import framework.security.ISecurityService;
import framework.security.ISecurityServiceConfiguration;
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
import framework.services.api.ApiControllerUtilsServiceImpl;
import framework.services.api.IApiControllerUtilsService;
import framework.services.api.server.ApiSignatureServiceImpl;
import framework.services.api.server.IApiSignatureService;
import framework.services.audit.AuditLoggerServiceImpl;
import framework.services.audit.IAuditLoggerService;
import framework.services.configuration.DefaultI18nMessages;
import framework.services.configuration.I18nMessages;
import framework.services.configuration.I18nMessagesPluginImpl;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.configuration.IImplementationDefinedObjectService;
import framework.services.configuration.ITopMenuBarService;
import framework.services.database.IDatabaseDependencyService;
import framework.services.email.EmailServiceImpl;
import framework.services.email.IEmailService;
import framework.services.ext.ExtensionManagerServiceImpl;
import framework.services.ext.IExtensionManagerService;
import framework.services.ext.ILinkGenerationService;
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
import play.Configuration;
import play.Environment;
import play.Logger;
import play.db.ebean.DefaultEbeanConfig;
import play.db.ebean.EbeanConfig;
import security.SecurityServiceImpl;
import services.bizdockapi.BizdockApiClientImpl;
import services.bizdockapi.IBizdockApiClient;
import services.configuration.ImplementationDefinedObjectImpl;
import services.configuration.TopMenuBarService;
import services.database.DatabaseDependencyServiceImpl;
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
        requestStaticInjection(ApiAuthenticationBizdockCheck.class);
        requestStaticInjection(StaticAccessor.class);
        log.info("...Desktop static dependency injected end");

        log.info(">>> Standard dependency injection start...");
        bind(EbeanConfig.class).toProvider(DefaultEbeanConfig.EbeanConfigParser.class).asEagerSingleton();
        bind(IDatabaseDependencyService.class).to(DatabaseDependencyServiceImpl.class).asEagerSingleton();
        bind(IImplementationDefinedObjectService.class).to(ImplementationDefinedObjectImpl.class).asEagerSingleton();

        bind(HandlerCache.class).to(SecurityServiceImpl.class).asEagerSingleton();
        bind(ISecurityService.class).to(SecurityServiceImpl.class);
        bind(ISecurityServiceConfiguration.class).to(SecurityServiceImpl.class);

        bind(IExtensionManagerService.class).to(ExtensionManagerServiceImpl.class).asEagerSingleton();
        bind(ILinkGenerationService.class).to(ExtensionManagerServiceImpl.class);

        bind(II18nMessagesPlugin.class).to(I18nMessagesPluginImpl.class).asEagerSingleton();
        bind(I18nMessages.class).to(DefaultI18nMessages.class).asEagerSingleton();
        
        bind(ITopMenuBarService.class).to(TopMenuBarService.class).asEagerSingleton();

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
        bind(IEmailService.class).to(EmailServiceImpl.class).asEagerSingleton();

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
        bind(IInstanceAccessSupervisor.class).to(LicensesManagementServiceImpl.class);
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
        bind(IAuthenticator.class).to(Authenticator.class).asEagerSingleton();
        bind(IApiControllerUtilsService.class).to(ApiControllerUtilsServiceImpl.class).asEagerSingleton();
        log.info(">>> Standard dependency injection end");
    }

    /**
     * Register the data types to be used in various place of the application.
     */
    private void initDataTypes() {
        MafDataType.add(IMafConstants.Actor, "models.pmo.Actor", true, true,
                ImmutableMap.<String, String> builder().put("employeeId", "object.actor.employee_id.label").put("title", "object.actor.title.label")
                        .put("isActive", "object.actor.is_active.label").put("mail", "object.actor.mail.label").put("actorType", "object.actor.type.label")
                        .put("erpRefId", "object.actor.erp_ref_id.label").build());
        MafDataType.add(IMafConstants.ApplicationBlock, "models.architecture.ApplicationBlock", false, true,
                ImmutableMap.<String, String> builder().put("archived", "object.application_block.archived.label")
                        .put("refId", "object.application_block.ref_id.label").put("name", "object.application_block.name.label").build());
        MafDataType.add(IMafConstants.BudgetBucket, "models.finance.BudgetBucket", false, true,
                ImmutableMap.<String, String> builder().put("refId", "object.budget_bucket.ref_id.label").put("name", "object.budget_bucket.name.label")
                        .put("isApproved", "object.budget_bucket.is_approved.label").put("isActive", "object.budget_bucket.is_active.label").build());
        MafDataType.add(IMafConstants.BudgetBucketLine, "models.finance.BudgetBucketLine", false, false);
        MafDataType.add(IMafConstants.CostCenter, "models.finance.CostCenter", false, false);
        MafDataType.add(IMafConstants.Iteration, "models.delivery.Iteration", false, true,
                ImmutableMap.<String, String> builder().put("storyPoints", "object.iteration.story_points.label").build());
        MafDataType.add(IMafConstants.OrgUnit, "models.pmo.OrgUnit", true, true,
                ImmutableMap.<String, String> builder().put("refId", "object.org_unit.ref_id.label").put("name", "object.org_unit.name.label")
                        .put("isActive", "object.org_unit.is_active.label").put("canSponsor", "object.org_unit.can_sponsor.label")
                        .put("canDeliver", "object.org_unit.can_deliver.label").put("orgUnitType", "object.org_unit.type.label").build());
        MafDataType.add(IMafConstants.PortfolioEntryBudget, "models.finance.PortfolioEntryBudget", false, false);
        MafDataType.add(IMafConstants.PortfolioEntryBudgetLine, "models.finance.PortfolioEntryBudgetLine", true, true,
                ImmutableMap.<String, String> builder().put("refId", "object.portfolio_entry_budget_line.ref_id.label")
                        .put("name", "object.portfolio_entry_budget_line.name.label").put("isOpex", "object.portfolio_entry_budget_line.is_opex.label")
                        .put("currency", "object.portfolio_entry_budget_line.currency.label").put("amount", "object.portfolio_entry_budget_line.amount.label")
                        .build());
        MafDataType.add(IMafConstants.PortfolioEntryEvent, "models.pmo.PortfolioEntryEvent", false, true,
                ImmutableMap.<String, String> builder().put("type", "object.portfolio_entry_event.type.label").build());
        MafDataType.add(IMafConstants.PortfolioEntry, "models.pmo.PortfolioEntry", true, true,
                ImmutableMap.<String, String> builder().put("isConfidential", "object.portfolio_entry.is_condfidential.label")
                        .put("isActive", "object.portfolio_entry.is_active.label").put("name", "object.portfolio_entry.name.label")
                        .put("portfolioEntryType", "object.portfolio_entry.type.label").put("governanceId", "object.portfolio_entry.governance_id.label")
                        .put("erpRefId", "object.portfolio_entry.erp_ref_id.label").build());
        MafDataType.add(IMafConstants.PortfolioEntryPlanningPackage, "models.pmo.PortfolioEntryPlanningPackage", false, true,
                ImmutableMap.<String, String> builder().put("name", "object.portfolio_entry_planning_package.name.label")
                        .put("isImportant", "object.portfolio_entry_planning_package.is_important.label")
                        .put("portfolioEntryPlanningPackageGroup", "object.portfolio_entry_planning_package.group.label")
                        .put("portfolioEntryPlanningPackageType", "object.portfolio_entry_planning_package.type.label")
                        .put("status", "object.portfolio_entry_planning_package.status.label").build());
        MafDataType.add(IMafConstants.PortfolioEntryReport, "models.pmo.PortfolioEntryReport", false, true,
                ImmutableMap.<String, String> builder().put("status", "object.portfolio_entry_report.status.label").build());
        MafDataType.add(IMafConstants.PortfolioEntryResourcePlanAllocatedActor, "models.finance.PortfolioEntryResourcePlanAllocatedActor", false, true,
                ImmutableMap.<String, String> builder().put("days", "object.allocated_resource.days.label")
                        .put("isConfirmed", "object.allocated_resource.is_confirmed.label").build());
        MafDataType.add(IMafConstants.PortfolioEntryResourcePlanAllocatedOrgUnit, "models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit", false, true,
                ImmutableMap.<String, String> builder().put("days", "object.allocated_resource.days.label")
                        .put("isConfirmed", "object.allocated_resource.is_confirmed.label").build());
        MafDataType.add(IMafConstants.PortfolioEntryResourcePlanAllocatedCompetency, "models.finance.PortfolioEntryResourcePlanAllocatedCompetency", false,
                true, ImmutableMap.<String, String> builder().put("days", "object.allocated_resource.days.label")
                        .put("isConfirmed", "object.allocated_resource.is_confirmed.label").build());
        MafDataType.add(IMafConstants.PortfolioEntryRisk, "models.pmo.PortfolioEntryRisk", false, true,
                ImmutableMap.<String, String> builder().put("isActive", "object.portfolio_entry_risk.is_active.label")
                        .put("name", "object.portfolio_entry_risk.name.label").put("riskType", "object.portfolio_entry_risk.type.label").build());
        MafDataType.add(IMafConstants.Portfolio, "models.pmo.Portfolio", true, true,
                ImmutableMap.<String, String> builder().put("name", "object.portfolio.name.label").put("isActive", "object.portfolio.is_active.label")
                        .put("portfolioType", "object.portfolio.type.label").build());
        MafDataType.add(IMafConstants.Stakeholder, "models.pmo.Stakeholder", true, false);
        MafDataType.add(IMafConstants.PurchaseOrderLineItem, "models.finance.PurchaseOrderLineItem", true, false);
        MafDataType.add(IMafConstants.PurchaseOrder, "models.finance.PurchaseOrder", true, false);
        MafDataType.add(IMafConstants.Release, "models.delivery.Release", false, true,
                ImmutableMap.<String, String> builder().put("isActive", "object.release.is_active.label").put("name", "object.release.name.label")
                        .put("capacity", "object.release.capacity.label").build());
        MafDataType.add(IMafConstants.Requirement, "models.delivery.Requirement", false, true);
        MafDataType.add(IMafConstants.TimesheetActivityAllocatedActor, "models.timesheet.TimesheetActivityAllocatedActor", false, true,
                ImmutableMap.<String, String> builder().put("days", "object.allocated_resource.days.label").build());
        MafDataType.add(IMafConstants.WorkOrder, "models.finance.WorkOrder", false, true,
                ImmutableMap.<String, String> builder().put("name", "object.work_order.name.label").put("currency", "object.work_order.currency.label")
                        .put("amount", "object.work_order.amount.label").put("isOpex", "object.work_order.is_opex.label")
                        .put("shared", "object.work_order.shared.label").put("amountReceived", "object.work_order.amount_received.label").build());

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
