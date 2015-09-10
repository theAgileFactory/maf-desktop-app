package services.configuration;

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
import javax.inject.Inject;
import javax.inject.Singleton;

import constants.IMafConstants;
import controllers.Assets.Asset;
import dao.finance.CurrencyDAO;
import framework.commons.DataType;
import framework.services.configuration.IImplementationDefinedObjectService;
import framework.services.database.IDatabaseDependencyService;
import framework.utils.Menu.ClickableMenuItem;
import framework.utils.Menu.HeaderMenuItem;
import framework.utils.Menu.SeparatorMenuItem;
import framework.utils.TopMenuBar;
import framework.utils.Utilities;
import models.pmo.OrgUnit;
import models.pmo.Portfolio;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;
import play.mvc.Call;

/**
 * Define the objects which can only be defined in the implementation.
 * 
 * @author Pierre-Yves Cloux
 */
@Singleton
public class ImplementationDefinedObjectImpl implements IImplementationDefinedObjectService {
    private static Logger.ALogger log = Logger.of(ImplementationDefinedObjectImpl.class);
    private static final String DEVDOCK_PERSPECTIVE_KEY = "devdock";

    /**
     * An enumeration which contains the top level menu entries.
     * 
     * @author Pierre-Yves Cloux
     */
    public enum TopMenus {
        ROADMAP, COCKPIT, NEW, GOVERNANCE, DELIVERY, TOOLS, ADMIN, SEARCH, SPECIAL, ARCHITECTURE;

        /**
         * Get the name with an index (useful for the help tour).
         * 
         * @param index
         *            the index.
         */
        public String name(int index) {
            return name() + "-" + index;
        }
    }

    /**
     * Default constructor.
     * 
     * @param lifecycle
     *            the play application lifecycle listener
     * @param configuration
     *            the play application configuratio
     * @param databaseDependencyService
     *            the service which ensures that the database is available
     */
    @Inject
    public ImplementationDefinedObjectImpl(ApplicationLifecycle lifecycle, Configuration configuration,
            IDatabaseDependencyService databaseDependencyService) {
        log.info("SERVICE>>> ImplementationDefinedObjectImpl starting...");
        resetTopMenuBar();
        lifecycle.addStopHook(() -> {
            log.info("SERVICE>>> ImplementationDefinedObjectImpl stopping...");
            log.info("SERVICE>>> ImplementationDefinedObjectImpl stopped");
            return Promise.pure(null);
        });
        log.info("SERVICE>>> ImplementationDefinedObjectImpl started");
    }

    /**
     * Return the default currency for the system
     * 
     * @return
     */
    @Override
    public String getDefaultCurrencyCode() {
        return CurrencyDAO.getCurrencyDefaultAsCode();
    }

    @Override
    public Call getRouteForAjaxWaitImage() {
        return controllers.routes.Assets.versioned(new Asset("images/ajax-loader.gif"));
    }

    @Override
    public Call getRouteForDynamicSingleCustomAttributeApi() {
        return controllers.routes.Application.dynamicSingleCustomAttributeApi();
    }

    @Override
    public Call getRouteForDownloadAttachedFile(Long attachmentId) {
        return controllers.routes.Application.downloadFileAttachment(attachmentId);
    }

    @Override
    public Call getRouteForDeleteAttachedFile(Long attachmentId) {
        return controllers.routes.Application.deleteFileAttachment(attachmentId);
    }

    @Override
    public Call getRouteForPluginConfiguratorControllerDoGetCustom(Long pluginConfigurationId, String actionId) {
        return framework.services.plugins.routes.PluginConfiguratorController.doGetForCustomConfiguratorController(pluginConfigurationId, actionId);
    }

    @Override
    public Call getRouteForPluginConfiguratorControllerDoGetRegistration(Long pluginConfigurationId, DataType dataType, Long objectId, String actionId) {
        return framework.services.plugins.routes.PluginConfiguratorController.doGetForRegistrationConfiguratorController(pluginConfigurationId,
                dataType.getDataName(), objectId, actionId);
    }

    @Override
    public Call getRouteForPluginConfiguratorControllerDoPostCustom(Long pluginConfigurationId, String actionId) {
        return framework.services.plugins.routes.PluginConfiguratorController.doPostForCustomConfiguratorController(pluginConfigurationId, actionId);
    }

    @Override
    public Call getRouteForPluginConfiguratorControllerDoPostRegistration(Long pluginConfigurationId, DataType dataType, Long objectId, String actionId) {
        return framework.services.plugins.routes.PluginConfiguratorController.doPostForRegistrationConfiguratorController(pluginConfigurationId,
                dataType.getDataName(), objectId, actionId);
    }

    @Override
    public Call getRouteForAdPanelContent(String page) {
        return controllers.routes.Application.getAdPanelContent(page);
    }

    @Override
    public Call getRouteForSwitchingTopMenuBarPerspective(String key) {
        return framework.utils.routes.TopMenuBarController.switchPerspective(key);
    }

    @Override
    public void resetTopMenuBar() {
        TopMenuBar.getInstance().clear();

        defineRoadmapMenu(null);
        defineCockpitMenu(null);
        defineArchitectureMenu(null);
        defineNewMenu(null);
        defineGovernanceMenu(null);
        defineDeliveryMenu(null);
        defineToolsMenu(null);
        defineAdminMenu(null);
        defineSearchMenu(null);
    }

    @Override
    public String renderObject(Object object) {
        if (object instanceof OrgUnit) {
            return views.html.modelsparts.display_org_unit.render((OrgUnit) object).body();
        }
        if (object instanceof Portfolio) {
            return views.html.modelsparts.display_portfolio.render((Portfolio) object).body();
        }
        return object.toString();
    }

    /**
     * Define the search menu.
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineSearchMenu(String perspectiveKey) {
        ClickableMenuItem searchMenuItem = new ClickableMenuItem(TopMenus.SEARCH.name(), "topmenubar.search.menu.label",
                controllers.core.routes.SearchController.index(), "glyphicons glyphicons-search", false);
        searchMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.SEARCH_PERMISSION));
        if (perspectiveKey == null) {
            TopMenuBar.getInstance().addMenuItem(searchMenuItem);
        } else {
            TopMenuBar.getInstance().get(perspectiveKey).addMenuItem(searchMenuItem);
        }
    }

    /**
     * Define the admin menu.
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineAdminMenu(String perspectiveKey) {
        // Admin user management features
        HeaderMenuItem adminMenuItem = new HeaderMenuItem(TopMenus.ADMIN.name(), "topmenubar.admin.menu.label", "glyphicons glyphicons-king", false);
        adminMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_USER_ADMINISTRATION_PERMISSION,
                IMafConstants.ADMIN_AUDIT_LOG_PERMISSION, IMafConstants.REPORTING_ADMINISTRATION_PERMISSION, IMafConstants.ADMIN_CONFIGURATION_PERMISSION,
                IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION, IMafConstants.ADMIN_KPI_MANAGER_PERMISSION, IMafConstants.ADMIN_CUSTOM_ATTRIBUTE_PERMISSION));
        if (perspectiveKey == null) {
            TopMenuBar.getInstance().addMenuItem(adminMenuItem);
        } else {
            TopMenuBar.getInstance().get(perspectiveKey).addMenuItem(adminMenuItem);
        }

        // User management
        ClickableMenuItem userManagerMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(1), "topmenubar.admin.usermanager.menu.label",
                controllers.admin.routes.UserManager.displayUserSearchForm(), "glyphicons glyphicons-user", false);
        adminMenuItem.addSubMenuItem(userManagerMenuItem);
        userManagerMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_USER_ADMINISTRATION_PERMISSION));

        // Configuration
        ClickableMenuItem systemPreferenceMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(2), "topmenubar.admin.configuration.menu.label",
                controllers.admin.routes.ConfigurationController.index(), "glyphicons glyphicons-classic-hammer", false);
        adminMenuItem.addSubMenuItem(systemPreferenceMenuItem);
        systemPreferenceMenuItem.setAuthorizedPermissions(
                Utilities.getListOfArray(IMafConstants.ADMIN_CONFIGURATION_PERMISSION, IMafConstants.ADMIN_CUSTOM_ATTRIBUTE_PERMISSION));

        // Integration
        ClickableMenuItem pluginManagerMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(3), "topmenubar.admin.integration.menu.label",
                controllers.admin.routes.PluginManagerController.index(), "glyphicons glyphicons-cloud", false);
        adminMenuItem.addSubMenuItem(pluginManagerMenuItem);
        pluginManagerMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION,
                IMafConstants.API_MANAGER_PERMISSION, IMafConstants.PARTNER_SYNDICATION_PERMISSION));

        // KPI manager
        ClickableMenuItem kpiManagerMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(4), "topmenubar.admin.kpimanager.menu.label",
                controllers.admin.routes.KpiManagerController.index(), "glyphicons glyphicons-stats", false);
        adminMenuItem.addSubMenuItem(kpiManagerMenuItem);
        kpiManagerMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_KPI_MANAGER_PERMISSION));

        // Reporting
        ClickableMenuItem reportingMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(5), "topmenubar.admin.reporting.menu.label",
                controllers.admin.routes.ReportingController.index(), "glyphicons glyphicons-pie-chart", false);
        adminMenuItem.addSubMenuItem(reportingMenuItem);
        reportingMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.REPORTING_ADMINISTRATION_PERMISSION));

        // Audit log
        ClickableMenuItem auditLogMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(6), "topmenubar.admin.audit.menu.label",
                controllers.admin.routes.AuditableController.listAuditable(), "glyphicons glyphicons-signal", false);
        auditLogMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_AUDIT_LOG_PERMISSION));
        adminMenuItem.addSubMenuItem(auditLogMenuItem);
    }

    /**
     * Define the tools menu.
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineToolsMenu(String perspectiveKey) {
        HeaderMenuItem toolsMenuItem = new HeaderMenuItem(TopMenus.TOOLS.name(), "topmenubar.tools.menu.label", "glyphicons glyphicons-settings", false);
        toolsMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.TIMESHEET_ENTRY_PERMISSION, IMafConstants.REPORTING_VIEW_ALL_PERMISSION,
                IMafConstants.REPORTING_VIEW_AS_VIEWER_PERMISSION));
        if (perspectiveKey == null) {
            TopMenuBar.getInstance().addMenuItem(toolsMenuItem);
        } else {
            TopMenuBar.getInstance().get(perspectiveKey).addMenuItem(toolsMenuItem);
        }

        ClickableMenuItem timesheetMenuItem = new ClickableMenuItem(TopMenus.TOOLS.name(1), "topmenubar.tools.timesheet.menu.label",
                controllers.core.routes.TimesheetController.weeklyFill(""), "glyphicons glyphicons-clock", false);
        timesheetMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.TIMESHEET_ENTRY_PERMISSION));
        toolsMenuItem.addSubMenuItem(timesheetMenuItem);

        ClickableMenuItem viewReportingMenuItem = new ClickableMenuItem(TopMenus.TOOLS.name(2), "topmenubar.tools.reporting.menu.label",
                controllers.core.routes.ReportingController.index(), "glyphicons glyphicons-pie-chart", false);
        viewReportingMenuItem.setAuthorizedPermissions(
                Utilities.getListOfArray(IMafConstants.REPORTING_VIEW_ALL_PERMISSION, IMafConstants.REPORTING_VIEW_AS_VIEWER_PERMISSION));
        toolsMenuItem.addSubMenuItem(viewReportingMenuItem);
    }

    /**
     * Define the delivery menu.
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineDeliveryMenu(String perspectiveKey) {
        HeaderMenuItem deliveryMenuItem = new HeaderMenuItem(TopMenus.DELIVERY.name(), "topmenubar.delivery.menu.label", "glyphicons glyphicons-cargo",
                false);
        deliveryMenuItem.setAuthorizedPermissions(
                Utilities.getListOfArray(IMafConstants.RELEASE_VIEW_ALL_PERMISSION, IMafConstants.RELEASE_VIEW_AS_MANAGER_PERMISSION));
        if (perspectiveKey == null) {
            TopMenuBar.getInstance().addMenuItem(deliveryMenuItem);
        } else {
            TopMenuBar.getInstance().get(perspectiveKey).addMenuItem(deliveryMenuItem);
        }

        ClickableMenuItem viewReleasesMenuItem = new ClickableMenuItem(TopMenus.DELIVERY.name(1), "topmenubar.delivery.releases.menu.label",
                controllers.core.routes.ReleaseController.list(false), "glyphicons glyphicons-git-branch", false);
        viewReleasesMenuItem.setAuthorizedPermissions(
                Utilities.getListOfArray(IMafConstants.RELEASE_VIEW_ALL_PERMISSION, IMafConstants.RELEASE_VIEW_AS_MANAGER_PERMISSION));
        deliveryMenuItem.addSubMenuItem(viewReleasesMenuItem);
    }

    /**
     * Define the architecture menu (item).
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineArchitectureMenu(String perspectiveKey) {
        ClickableMenuItem architectureMenuItem = new ClickableMenuItem(TopMenus.ARCHITECTURE.name(), "topmenubar.architecture.menu.label",
                controllers.core.routes.ArchitectureController.index(null), "glyphicons glyphicons-globe-af", false);
        architectureMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ARCHITECTURE_PERMISSION));
        if (perspectiveKey == null) {
            TopMenuBar.getInstance().addMenuItem(architectureMenuItem);
        } else {
            TopMenuBar.getInstance().get(perspectiveKey).addMenuItem(architectureMenuItem);
        }
    }

    /**
     * Define the governance menu.
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineGovernanceMenu(String perspectiveKey) {
        HeaderMenuItem governanceMenuItem = new HeaderMenuItem(TopMenus.GOVERNANCE.name(), "topmenubar.governance.menu.label",
                "glyphicons glyphicons-cluster", false);
        governanceMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.MILESTONE_OVERVIEW_PERMISSION,
                IMafConstants.MILESTONE_APPROVAL_PERMISSION, IMafConstants.MILESTONE_DECIDE_PERMISSION,
                IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_ALL_PERMISSION, IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_AS_PORTFOLIO_MANAGER_PERMISSION));
        if (perspectiveKey == null) {
            TopMenuBar.getInstance().addMenuItem(governanceMenuItem);
        } else {
            TopMenuBar.getInstance().get(perspectiveKey).addMenuItem(governanceMenuItem);
        }

        ClickableMenuItem milestonePlanningMenuItem = new ClickableMenuItem(TopMenus.GOVERNANCE.name(1),
                "topmenubar.governance.milestone_planning.menu.label", controllers.core.routes.MilestoneApprovalController.overview(),
                "glyphicons glyphicons-calendar", false);
        milestonePlanningMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.MILESTONE_OVERVIEW_PERMISSION));
        governanceMenuItem.addSubMenuItem(milestonePlanningMenuItem);

        ClickableMenuItem provideApprovalMenuItem = new ClickableMenuItem(TopMenus.GOVERNANCE.name(2), "topmenubar.governance.provide_approval.menu.label",
                controllers.core.routes.MilestoneApprovalController.list(0), "glyphicons glyphicons-thumbs-up", false);
        provideApprovalMenuItem
                .setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.MILESTONE_APPROVAL_PERMISSION, IMafConstants.MILESTONE_DECIDE_PERMISSION));
        governanceMenuItem.addSubMenuItem(provideApprovalMenuItem);

        ClickableMenuItem reviewRequestMenuItem = new ClickableMenuItem(TopMenus.GOVERNANCE.name(3),
                "topmenubar.governance.review_milestone_request.menu.label",
                controllers.core.routes.ProcessTransitionRequestController.reviewMilestoneRequestList(0), "glyphicons glyphicons-transfer", false);
        reviewRequestMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_ALL_PERMISSION,
                IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_AS_PORTFOLIO_MANAGER_PERMISSION));
        governanceMenuItem.addSubMenuItem(reviewRequestMenuItem);
    }

    /**
     * Define the new menu for BizDock.
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineNewMenu(String perspectiveKey) {
        HeaderMenuItem newMenuItem = new HeaderMenuItem(TopMenus.NEW.name(), "topmenubar.new.menu.label", "glyphicons glyphicons-gift", false);
        newMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.PORTFOLIO_ENTRY_SUBMISSION_PERMISSION,
                IMafConstants.ACTOR_EDIT_ALL_PERMISSION, IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION, IMafConstants.PORTFOLIO_EDIT_ALL_PERMISSION,
                IMafConstants.BUDGET_BUCKET_EDIT_ALL_PERMISSION, IMafConstants.RELEASE_EDIT_ALL_PERMISSION));
        if (perspectiveKey == null) {
            TopMenuBar.getInstance().addMenuItem(newMenuItem);
        } else {
            TopMenuBar.getInstance().get(perspectiveKey).addMenuItem(newMenuItem);
        }

        ClickableMenuItem newInitiativeMenuItem = new ClickableMenuItem(TopMenus.NEW.name(1), "topmenubar.new.initiative.menu.label",
                controllers.core.routes.PortfolioEntryController.createStep1(), "glyphicons glyphicons-wallet", false);
        newInitiativeMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.PORTFOLIO_ENTRY_SUBMISSION_PERMISSION));
        newMenuItem.addSubMenuItem(newInitiativeMenuItem);

        SeparatorMenuItem newSeparatorMenuItem = new SeparatorMenuItem();
        newSeparatorMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ACTOR_EDIT_ALL_PERMISSION,
                IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION, IMafConstants.PORTFOLIO_EDIT_ALL_PERMISSION, IMafConstants.BUDGET_BUCKET_EDIT_ALL_PERMISSION));
        newMenuItem.addSubMenuItem(newSeparatorMenuItem);

        ClickableMenuItem newActorMenuItem = new ClickableMenuItem(TopMenus.NEW.name(2), "topmenubar.new.actor.menu.label",
                controllers.core.routes.ActorController.create(), "glyphicons glyphicons-parents", false);
        newActorMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ACTOR_EDIT_ALL_PERMISSION));
        newMenuItem.addSubMenuItem(newActorMenuItem);

        ClickableMenuItem newOrgUnitMenuItem = new ClickableMenuItem(TopMenus.NEW.name(3), "topmenubar.new.org_unit.menu.label",
                controllers.core.routes.OrgUnitController.create(), "glyphicons glyphicons-building", false);
        newOrgUnitMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION));
        newMenuItem.addSubMenuItem(newOrgUnitMenuItem);

        ClickableMenuItem newPortfolioMenuItem = new ClickableMenuItem(TopMenus.NEW.name(4), "topmenubar.new.portfolio.menu.label",
                controllers.core.routes.PortfolioController.create(), "glyphicons glyphicons-sort", false);
        newPortfolioMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.PORTFOLIO_EDIT_ALL_PERMISSION));
        newMenuItem.addSubMenuItem(newPortfolioMenuItem);

        ClickableMenuItem newBudgetBucketMenuItem = new ClickableMenuItem(TopMenus.NEW.name(5), "topmenubar.new.budget_bucket.menu.label",
                controllers.core.routes.BudgetBucketController.create(), "glyphicons glyphicons-calculator", false);
        newBudgetBucketMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.BUDGET_BUCKET_EDIT_ALL_PERMISSION));
        newMenuItem.addSubMenuItem(newBudgetBucketMenuItem);

        ClickableMenuItem newReleaseMenuItem = new ClickableMenuItem(TopMenus.NEW.name(6), "topmenubar.new.release.menu.label",
                controllers.core.routes.ReleaseController.create(), "glyphicons glyphicons-git-branch", false);
        newReleaseMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.RELEASE_EDIT_ALL_PERMISSION));
        newMenuItem.addSubMenuItem(newReleaseMenuItem);

    }

    /**
     * Define the cockpit menu (item).
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineCockpitMenu(String perspectiveKey) {
        ClickableMenuItem cockpitMenuItem = new ClickableMenuItem(TopMenus.COCKPIT.name(), "topmenubar.cockpit.menu.label",
                controllers.core.routes.CockpitController.initiatives(0, 0, false), "glyphicons glyphicons-dashboard", false);
        cockpitMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.COCKPIT_DISPLAY_PERMISSION));
        if (perspectiveKey == null) {
            TopMenuBar.getInstance().addMenuItem(cockpitMenuItem);
        } else {
            TopMenuBar.getInstance().get(perspectiveKey).addMenuItem(cockpitMenuItem);
        }
    }

    /**
     * Define the roadmap menu (item).
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineRoadmapMenu(String perspectiveKey) {
        ClickableMenuItem roadmapMenuItem = new ClickableMenuItem(TopMenus.ROADMAP.name(), "topmenubar.roadmap.menu.label",
                controllers.core.routes.RoadmapController.index(false), "glyphicons glyphicons-road", false);
        roadmapMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ROADMAP_DISPLAY_PERMISSION));
        if (perspectiveKey == null) {
            TopMenuBar.getInstance().addMenuItem(roadmapMenuItem);
        } else {
            TopMenuBar.getInstance().get(perspectiveKey).addMenuItem(roadmapMenuItem);
        }
    }
}
