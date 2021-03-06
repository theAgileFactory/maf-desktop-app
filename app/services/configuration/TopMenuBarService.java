package services.configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

import constants.IMafConstants;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.configuration.AbstractTopMenuBarService;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Menu.ClickableMenuItem;
import framework.utils.Menu.HeaderMenuItem;
import framework.utils.Menu.MenuItem;
import framework.utils.Menu.SeparatorMenuItem;
import framework.utils.Utilities;
import play.Configuration;
import play.inject.ApplicationLifecycle;

/**
 * The top menu bar service.
 * 
 * @author Johann Kohler
 *
 */
@Singleton
public class TopMenuBarService extends AbstractTopMenuBarService {
    private HeaderMenuItem toolsMenuItem;

    /**
     * An enumeration which contains the top level menu entries.
     * 
     * @author Pierre-Yves Cloux
     */
    public enum TopMenus {
        ROADMAP, NEW, GOVERNANCE, DELIVERY, TOOLS, ADMIN, SEARCH, SPECIAL, ARCHITECTURE;

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
     *            the Play lifecycle service
     * @param configuration
     *            the Play configuration service
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param userSessionManagerPlugin
     *            the user session manager service
     */
    @Inject
    public TopMenuBarService(ApplicationLifecycle lifecycle, Configuration configuration, IPreferenceManagerPlugin preferenceManagerPlugin,
            IUserSessionManagerPlugin userSessionManagerPlugin) {
        super(lifecycle, configuration, preferenceManagerPlugin, userSessionManagerPlugin);
    }

    @Override
    public void resetTopMenuBar() {
        clearAllPerspectives();
        defineRoadmapMenu(null);
        defineArchitectureMenu(null);
        defineNewMenu(null);
        defineGovernanceMenu(null);
        defineToolsMenu(null);
        defineAdminMenu(null);
        defineSearchMenu(null);
    }

    @Override
    public void addToolMenuItem(MenuItem menuItem) {
        // Add to the tools menu
        toolsMenuItem.addSubMenuItem(menuItem);
    }

    @Override
    public void removeToolMenuItem(String uuid) {
        // Remove from the tools menu
        toolsMenuItem.removeSubMenuItems(uuid);
    }

    /**
     * Define the search menu.
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineSearchMenu(String perspectiveKey) {
        ClickableMenuItem searchMenuItem = new ClickableMenuItem(TopMenus.SEARCH.name(), "topmenubar.search.menu.label",
                controllers.core.routes.SearchController.index(), "fa fa-search", false);
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
        HeaderMenuItem adminMenuItem = new HeaderMenuItem(TopMenus.ADMIN.name(), "topmenubar.admin.menu.label", "fa fa-cog", false);
        adminMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(
                IMafConstants.ADMIN_USER_ADMINISTRATION_PERMISSION,
                IMafConstants.ADMIN_AUDIT_LOG_PERMISSION,
                IMafConstants.REPORTING_ADMINISTRATION_PERMISSION,
                IMafConstants.ADMIN_CONFIGURATION_PERMISSION,
                IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION,
                IMafConstants.ADMIN_KPI_MANAGER_PERMISSION,
                IMafConstants.ADMIN_CUSTOM_ATTRIBUTE_PERMISSION,
                IMafConstants.ADMIN_TRANSLATION_KEY_EDIT_PERMISSION,
                IMafConstants.ADMIN_ATTACHMENTS_MANAGEMENT_PERMISSION
        ));
        if (perspectiveKey == null) {
            addMenuItemToMainPerspective(adminMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(adminMenuItem);
        }

        // User management
        ClickableMenuItem userManagerMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(1), "topmenubar.admin.usermanager.menu.label",
                controllers.admin.routes.UserManager.displayUserSearchForm(), "fa fa-user", false);
        adminMenuItem.addSubMenuItem(userManagerMenuItem);
        userManagerMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_USER_ADMINISTRATION_PERMISSION));

        // Configuration
        ClickableMenuItem systemPreferenceMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(2), "topmenubar.admin.configuration.menu.label",
                controllers.admin.routes.ConfigurationController.index(), "fa fa-gavel", false);
        adminMenuItem.addSubMenuItem(systemPreferenceMenuItem);
        systemPreferenceMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_CONFIGURATION_PERMISSION,
                IMafConstants.ADMIN_CUSTOM_ATTRIBUTE_PERMISSION, IMafConstants.ADMIN_TRANSLATION_KEY_EDIT_PERMISSION));

        // Integration
        ClickableMenuItem pluginManagerMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(3), "topmenubar.admin.integration.menu.label",
                controllers.admin.routes.PluginManagerController.index(), "fa fa-cloud", false);
        adminMenuItem.addSubMenuItem(pluginManagerMenuItem);
        pluginManagerMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION,
                IMafConstants.API_MANAGER_PERMISSION, IMafConstants.PARTNER_SYNDICATION_PERMISSION));

        // KPI manager
        ClickableMenuItem kpiManagerMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(4), "topmenubar.admin.kpimanager.menu.label",
                controllers.admin.routes.KpiManagerController.index(), "fa fa-line-chart", false);
        adminMenuItem.addSubMenuItem(kpiManagerMenuItem);
        kpiManagerMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_KPI_MANAGER_PERMISSION));

        // Reporting
        ClickableMenuItem reportingMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(5), "topmenubar.admin.reporting.menu.label",
                controllers.admin.routes.ReportingController.index(), "fa fa-pie-chart", false);
        adminMenuItem.addSubMenuItem(reportingMenuItem);
        reportingMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.REPORTING_ADMINISTRATION_PERMISSION));

        // Audit log
        ClickableMenuItem auditLogMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(6), "topmenubar.admin.audit.menu.label",
                controllers.admin.routes.AuditableController.listAuditable(), "fa fa-signal", false);
        auditLogMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_AUDIT_LOG_PERMISSION));
        adminMenuItem.addSubMenuItem(auditLogMenuItem);

        // Attachments
        ClickableMenuItem attachmentsMenuItem = new ClickableMenuItem(TopMenus.ADMIN.name(7), "topmenubar.admin.attachments.menu.label", controllers.admin.routes.AttachmentsController.index(), "fa fa-file", false);
        attachmentsMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_ATTACHMENTS_MANAGEMENT_PERMISSION, IMafConstants.ADMIN_ATTACHMENTS_MANAGEMENT_PERMISSION_NO_CONFIDENTIAL));
        adminMenuItem.addSubMenuItem(attachmentsMenuItem);
    }

    /**
     * Define the tools menu.
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineToolsMenu(String perspectiveKey) {
        toolsMenuItem = new HeaderMenuItem(TopMenus.TOOLS.name(), "topmenubar.tools.menu.label", "fa fa-wrench", false);
        toolsMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.TIMESHEET_ENTRY_PERMISSION, IMafConstants.REPORTING_VIEW_ALL_PERMISSION,
                IMafConstants.REPORTING_VIEW_AS_VIEWER_PERMISSION));
        if (perspectiveKey == null) {
            addMenuItemToMainPerspective(toolsMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(toolsMenuItem);
        }

        ClickableMenuItem timesheetMenuItem = new ClickableMenuItem(TopMenus.TOOLS.name(1), "topmenubar.tools.timesheet.menu.label",
                controllers.core.routes.TimesheetController.weeklyFill(""), "fa fa-clock-o", false);
        timesheetMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.TIMESHEET_ENTRY_PERMISSION));
        toolsMenuItem.addSubMenuItem(timesheetMenuItem);

        ClickableMenuItem viewReportingMenuItem = new ClickableMenuItem(TopMenus.TOOLS.name(2), "topmenubar.tools.reporting.menu.label",
                controllers.core.routes.ReportingController.index(), "fa fa-pie-chart", false);
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
                controllers.core.routes.ArchitectureController.index(null), "fa fa-globe", false);
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
        HeaderMenuItem governanceMenuItem = new HeaderMenuItem(TopMenus.GOVERNANCE.name(), "topmenubar.governance.menu.label", "fa fa-university", false);
        governanceMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.MILESTONE_OVERVIEW_PERMISSION,
                IMafConstants.MILESTONE_APPROVAL_PERMISSION, IMafConstants.MILESTONE_DECIDE_PERMISSION,
                IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_ALL_PERMISSION, IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_AS_PORTFOLIO_MANAGER_PERMISSION));
        if (perspectiveKey == null) {
            addMenuItemToMainPerspective(governanceMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(governanceMenuItem);
        }

        ClickableMenuItem milestonePlanningMenuItem = new ClickableMenuItem(TopMenus.GOVERNANCE.name(1),
                "topmenubar.governance.milestone_planning.menu.label", controllers.core.routes.MilestoneApprovalController.overview(), "fa fa-calendar",
                false);
        milestonePlanningMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.MILESTONE_OVERVIEW_PERMISSION));
        governanceMenuItem.addSubMenuItem(milestonePlanningMenuItem);

        ClickableMenuItem provideApprovalMenuItem = new ClickableMenuItem(TopMenus.GOVERNANCE.name(2), "topmenubar.governance.provide_approval.menu.label",
                controllers.core.routes.MilestoneApprovalController.list(), "fa fa-thumbs-up", false);
        provideApprovalMenuItem
                .setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.MILESTONE_APPROVAL_PERMISSION, IMafConstants.MILESTONE_DECIDE_PERMISSION));
        governanceMenuItem.addSubMenuItem(provideApprovalMenuItem);

        ClickableMenuItem reviewRequestMenuItem = new ClickableMenuItem(TopMenus.GOVERNANCE.name(3),
                "topmenubar.governance.review_milestone_request.menu.label",
                controllers.core.routes.ProcessTransitionRequestController.reviewMilestoneRequestList(0), "fa fa-exchange", false);
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
        HeaderMenuItem newMenuItem = new HeaderMenuItem(TopMenus.NEW.name(), "topmenubar.new.menu.label", "fa fa-gift", false);
        newMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.PORTFOLIO_ENTRY_SUBMISSION_PERMISSION,
                IMafConstants.RELEASE_SUBMISSION_PERMISSION, IMafConstants.ACTOR_EDIT_ALL_PERMISSION, IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION,
                IMafConstants.PORTFOLIO_EDIT_ALL_PERMISSION, IMafConstants.BUDGET_BUCKET_EDIT_ALL_PERMISSION));
        if (perspectiveKey == null) {
            addMenuItemToMainPerspective(newMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(newMenuItem);
        }

        ClickableMenuItem newInitiativeMenuItem = new ClickableMenuItem(TopMenus.NEW.name(1), "topmenubar.new.initiative.menu.label",
                controllers.core.routes.PortfolioEntryController.create(false), "fa fa-sticky-note", false);
        newInitiativeMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.PORTFOLIO_ENTRY_SUBMISSION_PERMISSION));
        newMenuItem.addSubMenuItem(newInitiativeMenuItem);

        ClickableMenuItem newReleaseMenuItem = new ClickableMenuItem(TopMenus.NEW.name(2), "topmenubar.new.release.menu.label",
                controllers.core.routes.PortfolioEntryController.create(true), "fa fa-code-fork", false);
        newReleaseMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.RELEASE_SUBMISSION_PERMISSION));
        newMenuItem.addSubMenuItem(newReleaseMenuItem);

        SeparatorMenuItem newSeparatorMenuItem = new SeparatorMenuItem();
        newSeparatorMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ACTOR_EDIT_ALL_PERMISSION,
                IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION, IMafConstants.PORTFOLIO_EDIT_ALL_PERMISSION, IMafConstants.BUDGET_BUCKET_EDIT_ALL_PERMISSION));
        newMenuItem.addSubMenuItem(newSeparatorMenuItem);

        ClickableMenuItem newActorMenuItem = new ClickableMenuItem(TopMenus.NEW.name(3), "topmenubar.new.actor.menu.label",
                controllers.core.routes.ActorController.create(), "fa fa-user", false);
        newActorMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ACTOR_EDIT_ALL_PERMISSION));
        newMenuItem.addSubMenuItem(newActorMenuItem);

        ClickableMenuItem newOrgUnitMenuItem = new ClickableMenuItem(TopMenus.NEW.name(4), "topmenubar.new.org_unit.menu.label",
                controllers.core.routes.OrgUnitController.create(), "fa fa-building", false);
        newOrgUnitMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ORG_UNIT_EDIT_ALL_PERMISSION));
        newMenuItem.addSubMenuItem(newOrgUnitMenuItem);

        ClickableMenuItem newPortfolioMenuItem = new ClickableMenuItem(TopMenus.NEW.name(5), "topmenubar.new.portfolio.menu.label",
                controllers.core.routes.PortfolioController.create(), "fa fa-folder", false);
        newPortfolioMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.PORTFOLIO_EDIT_ALL_PERMISSION));
        newMenuItem.addSubMenuItem(newPortfolioMenuItem);

        ClickableMenuItem newBudgetBucketMenuItem = new ClickableMenuItem(TopMenus.NEW.name(6), "topmenubar.new.budget_bucket.menu.label",
                controllers.core.routes.BudgetBucketController.create(), "fa fa-calculator", false);
        newBudgetBucketMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.BUDGET_BUCKET_EDIT_ALL_PERMISSION));
        newMenuItem.addSubMenuItem(newBudgetBucketMenuItem);

    }

    /**
     * Define the roadmap menu (item).
     * 
     * @param perspectiveKey
     *            the perspective key, let null for the main
     */
    private void defineRoadmapMenu(String perspectiveKey) {
        ClickableMenuItem roadmapMenuItem = new ClickableMenuItem(TopMenus.ROADMAP.name(), "topmenubar.roadmap.menu.label",
                controllers.core.routes.RoadmapController.index(), "fa fa-road", false);
        roadmapMenuItem.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ROADMAP_DISPLAY_PERMISSION));
        if (perspectiveKey == null) {
            addMenuItemToMainPerspective(roadmapMenuItem);
        } else {
            getPerspective(perspectiveKey).addMenuItem(roadmapMenuItem);
        }
    }
}
