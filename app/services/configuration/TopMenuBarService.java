package services.configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

import constants.IMafConstants;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.configuration.AbstractTopMenuBarService;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Menu.ClickableMenuItem;
import framework.utils.Menu.HeaderMenuItem;
import framework.utils.Menu.SeparatorMenuItem;
import framework.utils.Utilities;
import play.Configuration;
import play.inject.ApplicationLifecycle;
import services.configuration.ImplementationDefinedObjectImpl.TopMenus;

@Singleton
public class TopMenuBarService extends AbstractTopMenuBarService {

    @Inject
    public TopMenuBarService(ApplicationLifecycle lifecycle, Configuration configuration, IPreferenceManagerPlugin preferenceManagerPlugin,
            IUserSessionManagerPlugin userSessionManagerPlugin) {
        super(lifecycle, configuration, preferenceManagerPlugin, userSessionManagerPlugin);
    }

    @Override
    public void resetTopMenuBar() {
        clearAllPerspectives();
        defineRoadmapMenu(null);
        defineCockpitMenu(null);
        defineArchitectureMenu(null);
        defineNewMenu(null);
        defineGovernanceMenu(null);
        defineToolsMenu(null);
        defineAdminMenu(null);
        defineSearchMenu(null);
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
            addMenuItemToMainPerspective(searchMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(searchMenuItem);
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
                IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION, IMafConstants.ADMIN_KPI_MANAGER_PERMISSION, IMafConstants.ADMIN_CUSTOM_ATTRIBUTE_PERMISSION,
                IMafConstants.ADMIN_TRANSLATION_KEY_EDIT_PERMISSION));
        if (perspectiveKey == null) {
            addMenuItemToMainPerspective(adminMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(adminMenuItem);
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
        systemPreferenceMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_CONFIGURATION_PERMISSION,
                IMafConstants.ADMIN_CUSTOM_ATTRIBUTE_PERMISSION, IMafConstants.ADMIN_TRANSLATION_KEY_EDIT_PERMISSION));

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
            addMenuItemToMainPerspective(toolsMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(toolsMenuItem);
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
            addMenuItemToMainPerspective(architectureMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(architectureMenuItem);
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
            addMenuItemToMainPerspective(governanceMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(governanceMenuItem);
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
                IMafConstants.BUDGET_BUCKET_EDIT_ALL_PERMISSION));
        if (perspectiveKey == null) {
            addMenuItemToMainPerspective(newMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(newMenuItem);
        }

        ClickableMenuItem newInitiativeMenuItem = new ClickableMenuItem(TopMenus.NEW.name(1), "topmenubar.new.initiative.menu.label",
                controllers.core.routes.PortfolioEntryController.create(), "glyphicons glyphicons-wallet", false);
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
            addMenuItemToMainPerspective(cockpitMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(cockpitMenuItem);
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
                controllers.core.routes.RoadmapController.index(), "glyphicons glyphicons-road", false);
        roadmapMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ROADMAP_DISPLAY_PERMISSION));
        if (perspectiveKey == null) {
            addMenuItemToMainPerspective(roadmapMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(roadmapMenuItem);
        }
    }
}
