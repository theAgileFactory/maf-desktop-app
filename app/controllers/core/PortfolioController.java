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
package controllers.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import dao.governance.LifeCyclePlanningDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.StakeholderDao;
import framework.security.ISecurityService;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.custom_attribute.ICustomAttributeManagerService;
import framework.utils.Menu.ClickableMenuItem;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.SideBar;
import framework.utils.Table;
import framework.utils.Utilities;
import models.governance.PlannedLifeCycleMilestoneInstance;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import models.pmo.Stakeholder;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioExists;
import services.tableprovider.ITableProvider;
import utils.form.PortfolioFormData;
import utils.table.PortfolioEntryListView;
import utils.table.PortfolioMilestoneListView;
import utils.table.PortfolioReportListView;
import utils.table.PortfolioStakeholderListView;

/**
 * The controller which displays / allows to edit a portfolio.
 * 
 * @author Johann Kohler
 */
public class PortfolioController extends Controller {

    @Inject
    private ISecurityService securityService;

    @Inject
    private ITableProvider tableProvider;

    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;

    @Inject
    private ICustomAttributeManagerService customAttributeManagerService;

    public static Form<PortfolioFormData> formTemplate = Form.form(PortfolioFormData.class);

    /**
     * Display the overview of a portfolio.
     * 
     * @param id
     *            the portfolio id
     */
    @With(CheckPortfolioExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_VIEW_DYNAMIC_PERMISSION)
    public Result overview(Long id) {

        // get the portfolio
        Portfolio portfolio = PortfolioDao.getPortfolioById(id);

        // get the last red report of the entries
        List<PortfolioReportListView> portfolioReportsListView = new ArrayList<PortfolioReportListView>();
        for (PortfolioEntry portfolioEntry : PortfolioEntryDao.getPERedAsListByPortfolio(id)) {
        	if (!portfolioEntry.archived) {        	
            portfolioReportsListView.add(new PortfolioReportListView(portfolioEntry.lastPortfolioEntryReport));
        	}
        }
        Table<PortfolioReportListView> filledReportTable = this.getTableProvider().get().portfolioReport.templateTable.fill(portfolioReportsListView);

        // get the late milestones
        List<PortfolioMilestoneListView> portfolioMilestoneListView = new ArrayList<PortfolioMilestoneListView>();
        for (PlannedLifeCycleMilestoneInstance plannedMilestoneInstance : LifeCyclePlanningDao
                .getPlannedLCMilestoneInstanceNotApprovedAsListOfPortfolio(id)) {
            portfolioMilestoneListView.add(new PortfolioMilestoneListView(plannedMilestoneInstance));
        }
        Table<PortfolioMilestoneListView> filledMilestoneTable = this.getTableProvider().get().portfolioMilestone.templateTable
                .fill(portfolioMilestoneListView);

        return ok(views.html.core.portfolio.portfolio_overview.render(portfolio, filledReportTable, filledMilestoneTable));
    }

    /**
     * Display the details of a portfolio.
     * 
     * @param id
     *            the portfolio id
     * @param portfolioEntryPage
     *            the current page for the portfolio entries table
     * @param stakeholderPage
     *            the current page for the stakeholders table
     * @param viewAllPortfolioEntries
     *            set to true if all portfolio entries (including the inactive)
     *            should be displayed
     */
    @With(CheckPortfolioExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_VIEW_DYNAMIC_PERMISSION)
    public Result view(Long id, Integer portfolioEntryPage, Integer stakeholderPage, Boolean viewAllPortfolioEntries) {

        // get the portfolio
        Portfolio portfolio = PortfolioDao.getPortfolioById(id);

        // construct the corresponding form data (for the custom attributes)
        PortfolioFormData portfolioFormData = new PortfolioFormData(portfolio);

        // get the portfolio entries
        Pagination<PortfolioEntry> portfolioEntryPagination = PortfolioEntryDao.getPEAsPaginationByPortfolio(this.getPreferenceManagerPlugin(), id,
                viewAllPortfolioEntries);
        portfolioEntryPagination.setCurrentPage(portfolioEntryPage);
        portfolioEntryPagination.setPageQueryName("portfolioEntryPage");

        List<PortfolioEntryListView> portfolioEntriesView = new ArrayList<PortfolioEntryListView>();
        for (PortfolioEntry portfolioEntry : portfolioEntryPagination.getListOfObjects()) {
            portfolioEntriesView.add(new PortfolioEntryListView(portfolioEntry));
        }

        Table<PortfolioEntryListView> filledPortfolioEntryTable = this.getTableProvider().get().portfolioEntry.templateTable.fill(portfolioEntriesView,
                PortfolioEntryListView.getHideNonDefaultColumns(true, true));

        // get the stakeholders
        Pagination<Stakeholder> stakeholderPagination = StakeholderDao.getStakeholderAsPaginationByPortfolio(this.getPreferenceManagerPlugin(), id);
        stakeholderPagination.setCurrentPage(stakeholderPage);
        stakeholderPagination.setPageQueryName("stakeholderPage");

        List<PortfolioStakeholderListView> stakeholdersListView = new ArrayList<PortfolioStakeholderListView>();
        for (Stakeholder stakeholder : stakeholderPagination.getListOfObjects()) {
            stakeholdersListView.add(new PortfolioStakeholderListView(stakeholder));
        }

        Set<String> hideColumnsForStakeholder = new HashSet<String>();
        hideColumnsForStakeholder.add("portfolio");
        if (!getSecurityService().dynamic("PORTFOLIO_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumnsForStakeholder.add("editActionLink");
            hideColumnsForStakeholder.add("removeActionLink");
        }

        Table<PortfolioStakeholderListView> filledStakeholderTable = this.getTableProvider().get().portfolioStakeholder.templateTable
                .fill(stakeholdersListView, hideColumnsForStakeholder);

        return ok(views.html.core.portfolio.portfolio_view.render(portfolio, portfolioFormData, filledPortfolioEntryTable, portfolioEntryPagination,
                viewAllPortfolioEntries, filledStakeholderTable, stakeholderPagination));
    }

    /**
     * Form to create a new portfolio (available from the Admin menu).
     */
    @Restrict({ @Group(IMafConstants.PORTFOLIO_EDIT_ALL_PERMISSION) })
    public Result create() {

        // construct the form
        Form<PortfolioFormData> portfolioForm = formTemplate;

        // add the custom attributes default values
        this.getCustomAttributeManagerService().fillWithValues(portfolioForm, Portfolio.class, null);

        return ok(views.html.core.portfolio.portfolio_new.render(portfolioForm, PortfolioDao.getPortfolioTypeActiveAsVH()));
    }

    /**
     * Process the creation of a portfolio.
     */
    @Restrict({ @Group(IMafConstants.PORTFOLIO_EDIT_ALL_PERMISSION) })
    public Result createSubmit() {

        // bind the form
        Form<PortfolioFormData> boundForm = formTemplate.bindFromRequest();

        if (boundForm.hasErrors() || this.getCustomAttributeManagerService().validateValues(boundForm, Portfolio.class)) {
            return ok(views.html.core.portfolio.portfolio_new.render(boundForm, PortfolioDao.getPortfolioTypeActiveAsVH()));
        }

        PortfolioFormData portfolioFormData = boundForm.get();

        Portfolio portfolio = new Portfolio();
        portfolioFormData.fill(portfolio);
        portfolio.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio.new.successful"));

        this.getTableProvider().flushFilterConfig();

        // save the custom attributes
        this.getCustomAttributeManagerService().validateAndSaveValues(boundForm, Portfolio.class, portfolio.id);

        return redirect(controllers.core.routes.PortfolioController.overview(portfolio.id));

    }

    /**
     * Form to edit a portfolio (available from the details of a portfolio).
     * 
     * @param id
     *            the portfolio id
     */
    @With(CheckPortfolioExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_EDIT_DYNAMIC_PERMISSION)
    public Result edit(Long id) {

        // get the portfolio
        Portfolio portfolio = PortfolioDao.getPortfolioById(id);

        // construct the form
        Form<PortfolioFormData> portfolioForm = formTemplate.fill(new PortfolioFormData(portfolio));

        // add the custom attributes values
        this.getCustomAttributeManagerService().fillWithValues(portfolioForm, Portfolio.class, id);

        return ok(views.html.core.portfolio.portfolio_edit.render(portfolio, portfolioForm, PortfolioDao.getPortfolioTypeActiveAsVH()));
    }

    /**
     * Process the update of a portfolio.
     */
    @With(CheckPortfolioExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_EDIT_DYNAMIC_PERMISSION)
    public Result save() {

        // bind the form
        Form<PortfolioFormData> boundForm = formTemplate.bindFromRequest();

        if (boundForm.hasErrors() || this.getCustomAttributeManagerService().validateValues(boundForm, Portfolio.class)) {

            // get the portfolio
            Long id = Long.valueOf(boundForm.data().get("id"));
            Portfolio portfolio = PortfolioDao.getPortfolioById(id);

            return ok(views.html.core.portfolio.portfolio_edit.render(portfolio, boundForm, PortfolioDao.getPortfolioTypeActiveAsVH()));
        }

        PortfolioFormData portfolioFormData = boundForm.get();

        Portfolio updPortfolio = PortfolioDao.getPortfolioById(portfolioFormData.id);
        portfolioFormData.fill(updPortfolio);
        updPortfolio.update();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio.edit.successful"));

        this.getTableProvider().flushFilterConfig();

        // save the custom attributes
        this.getCustomAttributeManagerService().validateAndSaveValues(boundForm, Portfolio.class, updPortfolio.id);

        return redirect(controllers.core.routes.PortfolioController.view(portfolioFormData.id, 0, 0, false));

    }

    /**
     * Delete a portfolio.
     * 
     * @param id
     *            the portfolio id
     */
    @With(CheckPortfolioExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_EDIT_DYNAMIC_PERMISSION)
    public Result delete(Long id) {

        // get the portfolio
        Portfolio portfolio = PortfolioDao.getPortfolioById(id);

        // set the delete flag to true
        portfolio.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio.delete.successful"));

        return redirect(controllers.core.routes.SearchController.index());
    }

    /**
     * Construct the portfolio icons bar depending of the sign-in user
     * permissions.
     * 
     * @param portfolioId
     *            the portfolio id
     * @param currentType
     *            the current menu item type, useful to select the correct item
     */
    public static SideBar getIconsBar(Long portfolioId, MenuItemType currentType) {

        SideBar sideBar = new SideBar();

        sideBar.addMenuItem(new ClickableMenuItem("core.portfolio.sidebar.overview.label", controllers.core.routes.PortfolioController.overview(portfolioId),
                "fa fa-tachometer", currentType.equals(MenuItemType.OVERVIEW)));

        sideBar.addMenuItem(new ClickableMenuItem("core.portfolio.sidebar.view.label",
                controllers.core.routes.PortfolioController.view(portfolioId, 0, 0, false), "fa fa-search-plus", currentType.equals(MenuItemType.VIEW)));

        return sideBar;

    }

    /**
     * The menu item type.
     * 
     * @author Johann Kohler
     * 
     */
    public static enum MenuItemType {
        OVERVIEW, VIEW;
    }

    /**
     * Get the security service.
     */
    private ISecurityService getSecurityService() {
        return securityService;
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return this.preferenceManagerPlugin;
    }

    /**
     * Get the custom attribute manager service.
     */
    private ICustomAttributeManagerService getCustomAttributeManagerService() {
        return this.customAttributeManagerService;
    }
}
