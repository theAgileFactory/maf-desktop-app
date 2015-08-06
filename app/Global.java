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
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import models.framework_models.account.SystemPermission;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.modules.swagger.ApiListingCache;
import play.mvc.Action;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import services.job.JobDescriptors.UpdateConsumedLicensesJobDescriptor;
import utils.reporting.JasperUtils;
import akka.actor.Cancellable;

import com.avaje.ebeaninternal.server.lib.ShutdownManager;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.model.ApiListing;

import constants.IMafConstants;
import constants.MafDataType;
import controllers.ControllersUtils;
import controllers.api.ApiController;
import controllers.sso.AuthenticationConfigurationUtils;
import dao.finance.CurrencyDAO;
import framework.commons.FrameworkStart;
import framework.commons.IFrameworkConstants;
import framework.patcher.IPatchLog;
import framework.patcher.PatchManager;
import framework.patcher.PatcherException;
import framework.services.ServiceManager;
import framework.services.actor.ActorSystemPluginException;
import framework.services.actor.IActorSystemPlugin;
import framework.services.api.AbstractApiController;
import framework.services.api.ApiError;
import framework.services.api.ClassSchemaDocumentationConverter;
import framework.services.audit.AuditLoggerUtilities;
import framework.services.configuration.ImplementationDefineObjectServiceFactory;
import framework.services.ext.IExtensionManagerService;
import framework.services.job.IJobDescriptor;
import framework.services.job.IJobsService;
import framework.services.kpi.IKpiService;
import framework.services.plugins.IPluginManagerService;
import framework.services.router.ICustomRouterNotificationService;
import framework.services.router.ICustomRouterService;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Language;
import framework.utils.SysAdminUtils;
import framework.utils.Utilities;

/**
 * Global object.<br/>
 * This object contains all the startup code for the application.
 * 
 * @author Pierre-Yves Cloux
 */
public class Global extends GlobalSettings {
    private static Logger.ALogger log = Logger.of(Global.class);
    private static Cancellable automaticSystemStatus;

    @Override
    public void beforeStart(Application app) {
        initSwaggerConfiguration();
        super.beforeStart(app);
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

    @Override
    public void onStart(Application app) {
        dumpSystemConfiguration();

        // Initialize the framework
        FrameworkStart.start();
        initDataTypes();
        dumpSystemStatus("applicationStart");

        // Flush the scheduler state
        SysAdminUtils.flushAllSchedulerStates();

        IFrameworkConstants.AuthenticationMode authenticationMode = Utilities.getAuthenticationMode();
        log.warn("AUTHENTICATION MODE [" + authenticationMode + "]");

        // Initialize the service manager (passing the authentication mode
        // profile)
        ServiceManager.init(authenticationMode.name());
        dumpSystemStatus("initServiceManager");

        // Initialize the authentication mode
        AuthenticationConfigurationUtils.init(authenticationMode);
        dumpSystemStatus("initAuthenticationMode");

        // Initialize the custom routing service
        initCustomRouterService();
        dumpSystemStatus("customRouterService");

        // Initialize the permissions system
        initPermissions();
        dumpSystemStatus("initPermissions");

        // Initialize the menu
        initTopMenuBar();
        dumpSystemStatus("initTopMenuBar");

        // Initialize the audit logger
        initAuditLog();
        dumpSystemStatus("initAuditLog");

        // Load actors
        initActorSystem();
        dumpSystemStatus("initActorSystem");

        // Load the jasper report definition
        initReportingEngine();
        dumpSystemStatus("initReportingEngine");

        // Initialize the KPI
        initKpiService();
        dumpSystemStatus("initKpiService");

        // Initialize the jobs
        initJobs();
        dumpSystemStatus("initJobs");

        // Initialize the automated system status
        initAutomatedSystemStatus();

        // Set the basepath of the API and clean the cache
        ConfigFactory.config().setBasePath(
                Utilities.getPreferenceElseConfigurationValue(IFrameworkConstants.SWAGGER_API_BASEPATH_PREFERENCE, "swagger.api.basepath"));
        scala.Option<scala.collection.immutable.Map<String, ApiListing>> x = scala.Option.apply(null);
        ApiListingCache.cache_$eq(x);
    }

    @Override
    public void onStop(Application application) {
        log.info(">>>>>>>>>>>>>>>> Shutting down the BizDock desktop application");

        // Shutdown the automatic system status
        if (automaticSystemStatus != null) {
            try {
                automaticSystemStatus.cancel();
            } catch (Exception e) {
                log.error("Unable to stop the automatic system status", e);
            }
        }

        // Shutdown the service manager
        if (ServiceManager.isServiceRegistered(IPluginManagerService.NAME)) {
            ServiceManager.getService(IPluginManagerService.NAME, IPluginManagerService.class).shutdown();
        }

        // Shutdown the actor system
        ServiceManager.getService(IActorSystemPlugin.NAME, IActorSystemPlugin.class).shutdown();
        int waitTime = play.Play.application().configuration().getInt("action.wait.time");
        log.info("Waiting " + waitTime + " ms while the actor system is stopping...");
        Utilities.wait(waitTime);
        log.info("... hopefully the actor system is down !");

        // Cancel the KPI
        ServiceManager.getService(IKpiService.NAME, IKpiService.class).cancel();
        // Cancel the jobs
        ServiceManager.getService(IJobsService.NAME, IJobsService.class).cancel();
        // Shutdown the service manager
        ServiceManager.shutdown();
        log.info("Waiting " + " ms while the service manager is stopping...");
        Utilities.wait(waitTime);
        log.info("... hopefully the service manager is down !");

        // Free the jasper report definition
        JasperUtils.shutdown();
        // Shutdown Ebean
        ShutdownManager.shutdown();
        // Shutdown connections (not sure it does anything but let's try)
        shutdownDbResources();
        log.info(">>>>>>>>>>>>>>>> BizDock closed");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Action onRequest(Request request, Method actionMethod) {

        final Language language = new Language(request.getQueryString("lang"));

        if (language.isValid()) {

            Logger.debug("change language to: " + language.getCode());

            return new Action.Simple() {

                @Override
                public Promise<Result> call(Context ctx) throws Throwable {
                    ctx.changeLang(language.getCode());
                    // Update the CAS language cookie which is relying on Spring
                    // framework (not really solid yet works)
                    Utilities.setSsoLanguage(ctx, language.getCode());
                    return delegate.call(ctx);
                }

            };

        }

        return super.onRequest(request, actionMethod);
    }

    /**
     * Http code 500.
     * 
     * @param request
     *            the request
     * @param t
     *            the exception
     */
    @Override
    public Promise<Result> onError(final RequestHeader request, final Throwable t) {

        return Promise.promise(new Function0<Result>() {

            public Result apply() throws Throwable {
                return (Result) ControllersUtils.logAndReturnUnexpectedError((Exception) t, Logger.of(Global.class));
            }
        });

    }

    /**
     * Http code 404.
     * 
     * @param request
     *            the request
     */
    @Override
    public Promise<Result> onHandlerNotFound(final RequestHeader request) {
        try {
            ICustomRouterNotificationService customRouterNotificationService = ServiceManager.getService(ICustomRouterNotificationService.NAME,
                    ICustomRouterNotificationService.class);
            Promise<Result> result = customRouterNotificationService.notify(Controller.ctx());
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            log.warn("Error while calling the custom router", e);
        }
        return Promise.promise(new Function0<Result>() {
            public Result apply() throws Throwable {
                if (request.path().startsWith(AbstractApiController.STANDARD_API_ROOT_URI)) {
                    return ApiController.getJsonErrorResponse(new ApiError(404, "Not found"));
                } else {
                    return play.mvc.Results.notFound(views.html.error.not_found.render(request.uri()));
                }
            }
        });

    }

    /**
     * Http code 400.
     * 
     * @param request
     *            the request
     * @param error
     *            the error mesage
     */
    @Override
    public Promise<Result> onBadRequest(final RequestHeader request, final String error) {

        return Promise.promise(new Function0<Result>() {

            public Result apply() throws Throwable {
                if (request.path().startsWith(AbstractApiController.STANDARD_API_ROOT_URI)) {
                    return ApiController.getJsonErrorResponse(new ApiError(400, error));
                } else {
                    return play.mvc.Results.badRequest(views.html.error.bad_request.render());
                }
            }

        });

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
     * Init the swagger documentation system.
     */
    private void initSwaggerConfiguration() {
        log.info(">>>>>>>>>>>>>>>> Initialize the API documentation system");
        ModelConverters.addConverter(new ClassSchemaDocumentationConverter(), true);
        log.info(">>>>>>>>>>>>>>>> Initialize the API documentation system (end)");
    }

    /**
     * Initialize the KPI management service.
     */
    private void initKpiService() {
        log.info(">>>>>>>>>>>>>>>> Initialize the KPI service");
        ServiceManager.getService(IKpiService.NAME, IKpiService.class).setDefaultCurrencyCode(CurrencyDAO.getCurrencyDefaultAsCode());
        ServiceManager.getService(IKpiService.NAME, IKpiService.class).init();
        log.info(">>>>>>>>>>>>>>>> Initialize the KPI service (end)");
    }

    /**
     * Initialize the jobs service: start all jobs.
     */
    private void initJobs() {

        List<IJobDescriptor> jobs = new ArrayList<>();

        // subscribe the jobs
        jobs.add(new UpdateConsumedLicensesJobDescriptor());

        ServiceManager.getService(IJobsService.NAME, IJobsService.class).start(jobs);

        ServiceManager.getService(IJobsService.NAME, IJobsService.class).trigger("UpdateConsumedLicenses");
    }

    /**
     * Initialize the custom routing service.
     */
    private void initCustomRouterService() {
        log.info(">>>>>>>>>>>>>>>> Initialize the custom router service");
        ICustomRouterService customRouterService = ServiceManager.getService(ICustomRouterService.NAME, ICustomRouterService.class);
        // Add extension router
        IExtensionManagerService extensionManagerService = ServiceManager.getService(IExtensionManagerService.NAME, IExtensionManagerService.class);
        customRouterService.addListener(IExtensionManagerService.PATH_PREFIX, extensionManagerService);
        log.info(">>>>>>>>>>>>>>>> Initialize the custom router service (end)");
    }

    /**
     * Starts the actors used by the BizDock system.
     * 
     * @throws ActorSystemPluginException
     */
    private void initActorSystem() {
        try {
            log.info(">>>>>>>>>>>>>>>> Initialize the actor system");
            ServiceManager.getService(IActorSystemPlugin.NAME, IActorSystemPlugin.class).startup();
            log.info(">>>>>>>>>>>>>>>> Initialize the actor system (end)");
        } catch (ActorSystemPluginException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize the audit logger (must be done after the service has been
     * instantiated otherwise Ebean do not start).
     */
    private void initAuditLog() {
        log.info(">>>>>>>>>>>>>>>> Initialize the audit log system");
        AuditLoggerUtilities.init(ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class));
        log.info(">>>>>>>>>>>>>>>> Initialize the audit log system (end)");
    }

    /**
     * Check permissions (=check the consistency between the code and the
     * database content).
     */
    private void initPermissions() {
        log.info(">>>>>>>>>>>>>>>> Check permissions consistency");
        if (!SystemPermission.checkPermissions(IMafConstants.class)) {
            log.error("WARNING: permissions in code are not consistent with permissions in database");
        }
        log.info(">>>>>>>>>>>>>>>> Check permissions consistency (end)");
    }

    /**
     * Initialize the top menu bar.
     */
    private void initTopMenuBar() {
        log.info(">>>>>>>>>>>>>>>> Initialize the top menu bar");
        ImplementationDefineObjectServiceFactory.getInstance().resetTopMenuBar();
        log.info(">>>>>>>>>>>>>>>> Initialize the top menu bar (end)");
    }

    /**
     * Initialize the reporting engine.
     */
    private void initReportingEngine() {
        log.info(">>>>>>>>>>>>>>>> Initialize Reporting engine");
        JasperUtils.loadDefinitions();
        log.info(">>>>>>>>>>>>>>>> Initialize Reporting engine (end)");
    }

    /**
     * Initialize the automated system status.
     */
    private void initAutomatedSystemStatus() {
        if (play.Configuration.root().getBoolean("maf.sysadmin.dump.vmstatus.active")) {
            int frequency = play.Configuration.root().getInt("maf.sysadmin.dump.vmstatus.frequency");
            log.info(">>>>>>>>>>>>>>>> Activate automated system status, frequency " + frequency);
            automaticSystemStatus = SysAdminUtils.scheduleRecurring(true, "AUTOMATED STATUS", Duration.create(frequency, TimeUnit.SECONDS),
                    Duration.create(frequency, TimeUnit.SECONDS), new Runnable() {
                        @Override
                        public void run() {
                            // Do nothing, the system will anyway
                            // display the
                            // status
                        }
                    });
            log.info(">>>>>>>>>>>>>>>> Activate automated system status (end)");
        }
    }

    /**
     * Shutdown database connections.
     */
    @SuppressWarnings("deprecation")
    private void shutdownDbResources() {
        log.info(">>>>>>>>>>>>>>>> Shutting down the database resources...");
        try {
            // Unregister the JDBC drivers
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                DriverManager.deregisterDriver(driver);
            }
            // Kill the JDBC cleanup thread
            AbandonedConnectionCleanupThread.shutdown();
            // Kill the remaining Timer threads
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                if (t.getName().startsWith("Timer-")) {
                    t.stop();
                }
            }
        } catch (Exception e) {
            log.debug("Exception while shutting down the database connections", e);
        }
        log.info(">>>>>>>>>>>>>>>> database resources closed");
    }

    /**
     * Dump the VM memory and thread configuration.<br/>
     */
    private void dumpSystemConfiguration() {
        SysAdminUtils.dumpSystemConfiguration();
    }

    /**
     * Dump the VM memory and thread status.<br/>
     * This is meant for debugging at startup
     * 
     * @param eventName
     *            the name of the event to be logged
     */
    private void dumpSystemStatus(String eventName) {
        SysAdminUtils.dumpSystemStatus(eventName);
    }

}