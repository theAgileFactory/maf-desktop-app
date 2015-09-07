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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.OrderBy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.delivery.ReleaseDAO;
import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryDao;
import framework.security.ISecurityService;
import framework.services.account.AccountManagementException;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.services.system.ISysAdminUtils;
import framework.utils.CustomAttributeFormAndDisplayHandler;
import framework.utils.FilterConfig;
import framework.utils.JqueryGantt;
import framework.utils.Menu.ClickableMenuItem;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.SideBar;
import framework.utils.Table;
import framework.utils.TableExcelRenderer;
import framework.utils.Utilities;
import models.delivery.Release;
import models.delivery.ReleasePortfolioEntry;
import models.delivery.Requirement;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;
import play.Logger;
import play.data.Form;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import scala.concurrent.duration.Duration;
import security.CheckReleaseExists;
import security.dynamic.ReleaseDynamicHelper;
import utils.form.ReleaseFormData;
import utils.gantt.SourceDataValue;
import utils.gantt.SourceItem;
import utils.gantt.SourceValue;
import utils.table.PortfolioEntryListView;
import utils.table.ReleaseListView;
import utils.table.RequirementListView;

/**
 * The controller which displays / allows to edit a release.
 * 
 * @author Johann Kohler
 */
public class ReleaseController extends Controller {
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private IPersonalStoragePlugin personalStoragePlugin;
    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;
    @Inject
    private INotificationManagerPlugin notificationManagerPlugin;
    @Inject
    private ISysAdminUtils sysAdminUtils;
    @Inject
    private ISecurityService securityService;

    private static Logger.ALogger log = Logger.of(ReleaseController.class);

    public static Form<ReleaseFormData> formTemplate = Form.form(ReleaseFormData.class);

    /**
     * Display the list of authorized releases for the sign user.
     * 
     * @param reset
     *            define if the filter must be reseted
     */
    @Restrict({ @Group(IMafConstants.RELEASE_VIEW_ALL_PERMISSION), @Group(IMafConstants.RELEASE_VIEW_AS_MANAGER_PERMISSION) })
    public Result list(Boolean reset) {

        try {

            FilterConfig<ReleaseListView> filterConfig = null;

            /*
             * we try to get the last filter configuration of the sign-in user,
             * if it exists we use it to filter the releases (except if the
             * reset flag is to true)
             */
            String backedUpFilter = getFilterConfigurationFromPreferences();
            if (!reset && !StringUtils.isBlank(backedUpFilter)) {

                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(backedUpFilter);
                filterConfig = ReleaseListView.filterConfig.parseResponse(json);

            } else {

                // get a copy of the default filter config
                filterConfig = ReleaseListView.filterConfig;
                storeFilterConfigFromPreferences(filterConfig.marshall());

            }

            // get the table
            Pair<Table<ReleaseListView>, Pagination<Release>> t = getReleasesTable(filterConfig);

            return ok(views.html.core.release.list.render(t.getLeft(), t.getRight(), filterConfig));

        } catch (Exception e) {

            if (reset.equals(false)) {
                ControllersUtils.logAndReturnUnexpectedError(e, log);
                return redirect(controllers.core.routes.ReleaseController.list(true));
            } else {
                return ControllersUtils.logAndReturnUnexpectedError(e, log);
            }

        }

    }

    /**
     * Export the content of the current list of releases as Excel.
     */
    @Restrict({ @Group(IMafConstants.RELEASE_VIEW_ALL_PERMISSION), @Group(IMafConstants.RELEASE_VIEW_AS_MANAGER_PERMISSION) })
    public Promise<Result> exportListAsExcel() {

        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {

                try {

                    // Get the current user
                    final String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());

                    // construct the table

                    JsonNode json = request().body().asJson();
                    FilterConfig<ReleaseListView> filterConfig = ReleaseListView.filterConfig.parseResponse(json);

                    OrderBy<Release> orderBy = filterConfig.getSortExpression();
                    ExpressionList<Release> expressionList = ReleaseDynamicHelper.getReleasesViewAllowedAsQuery(filterConfig.getSearchExpression(), orderBy,
                            getSecurityService());

                    List<ReleaseListView> releaseListView = new ArrayList<ReleaseListView>();
                    for (Release release : expressionList.findList()) {
                        releaseListView.add(new ReleaseListView(release));
                    }

                    Table<ReleaseListView> table = ReleaseListView.templateTable.fillForFilterConfig(releaseListView, filterConfig.getColumnsToHide());

                    final byte[] excelFile = TableExcelRenderer.renderFormatted(table);

                    final String fileName = String.format("releasesExport_%1$td_%1$tm_%1$ty_%1$tH-%1$tM-%1$tS.xlsx", new Date());
                    final String successTitle = Msg.get("excel.export.success.title");
                    final String successMessage = Msg.get("excel.export.success.message", fileName);
                    final String failureTitle = Msg.get("excel.export.failure.title");
                    final String failureMessage = Msg.get("excel.export.failure.message");

                    // Execute asynchronously
                    getSysAdminUtils().scheduleOnce(false, "Releases Excel Export", Duration.create(0, TimeUnit.MILLISECONDS), new Runnable() {
                        @Override
                        public void run() {
                            try {
                                OutputStream out = getPersonalStoragePlugin().createNewFile(uid, fileName);
                                IOUtils.copy(new ByteArrayInputStream(excelFile), out);
                                getNotificationManagerPlugin().sendNotification(uid, NotificationCategory.getByCode(Code.DOCUMENT), successTitle,
                                        successMessage, controllers.my.routes.MyPersonalStorage.index().url());
                            } catch (IOException e) {
                                log.error("Unable to export the excel file", e);
                                getNotificationManagerPlugin().sendNotification(uid, NotificationCategory.getByCode(Code.ISSUE), failureTitle, failureMessage,
                                        controllers.core.routes.ReleaseController.list(false).url());
                            }
                        }
                    });

                    return ok(Json.newObject());

                } catch (Exception e) {
                    return ControllersUtils.logAndReturnUnexpectedError(e, log);
                }
            }
        });

    }

    /**
     * Filter the releases list.
     */
    @Restrict({ @Group(IMafConstants.RELEASE_VIEW_ALL_PERMISSION), @Group(IMafConstants.RELEASE_VIEW_AS_MANAGER_PERMISSION) })
    public Result listFilter() {

        try {

            // get the json
            JsonNode json = request().body().asJson();

            // store the filter config
            storeFilterConfigFromPreferences(json.toString());

            // fill the filter config
            FilterConfig<ReleaseListView> filterConfig = ReleaseListView.filterConfig.parseResponse(json);

            // get the table
            Pair<Table<ReleaseListView>, Pagination<Release>> t = getReleasesTable(filterConfig);

            return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

    }

    /**
     * Display the gantt planning of the current list of releases.
     * 
     * @return
     */
    @Restrict({ @Group(IMafConstants.RELEASE_VIEW_ALL_PERMISSION), @Group(IMafConstants.RELEASE_VIEW_AS_MANAGER_PERMISSION) })
    public Result planning() {

        try {
            FilterConfig<ReleaseListView> filterConfig = null;

            /*
             * we try to get the last filter configuration of the sign-in user,
             * if it exists we use it to filter the releases (except if the
             * reset flag is to true)
             */
            String backedUpFilter = getFilterConfigurationFromPreferences();
            if (!StringUtils.isBlank(backedUpFilter)) {

                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(backedUpFilter);
                filterConfig = ReleaseListView.filterConfig.parseResponse(json);

            } else {

                // get a copy of the default filter config
                filterConfig = ReleaseListView.filterConfig;
                storeFilterConfigFromPreferences(filterConfig.marshall());

            }

            OrderBy<Release> orderBy = filterConfig.getSortExpression();
            ExpressionList<Release> expressionList = ReleaseDynamicHelper.getReleasesViewAllowedAsQuery(filterConfig.getSearchExpression(), orderBy,
                    getSecurityService());

            // initiate the source items (gantt)
            List<SourceItem> items = new ArrayList<SourceItem>();

            // compute the items
            for (Release release : expressionList.findList()) {

                // get the dates
                Date cutOffDate = release.cutOffDate;
                Date endTestsDate = release.endTestsDate;
                Date deploymentDate = release.deploymentDate;

                SourceItem item1 = new SourceItem(release.getName(), "");
                SourceItem item2 = new SourceItem("", "");
                boolean has2Items = false;

                SourceDataValue dataValue = new SourceDataValue(controllers.core.routes.ReleaseController.view(release.id, 0).url(), null, null, null, null);

                if (cutOffDate == null && endTestsDate == null) {

                    item1.values.add(new SourceValue(deploymentDate, deploymentDate, "", "", "diamond diamond-info", dataValue));

                } else if (cutOffDate != null && endTestsDate == null) {

                    item1.values.add(new SourceValue(cutOffDate, JqueryGantt.cleanToDate(cutOffDate, deploymentDate), "",
                            Msg.get("core.release.list.planning.phase.full"), "info", dataValue));

                } else if (cutOffDate == null && endTestsDate != null) {

                    item1.values.add(new SourceValue(endTestsDate, JqueryGantt.cleanToDate(endTestsDate, deploymentDate), "",
                            Msg.get("core.release.list.planning.phase.rollout"), "info", dataValue));

                } else {

                    item1.values.add(new SourceValue(cutOffDate, JqueryGantt.cleanToDate(cutOffDate, endTestsDate), "",
                            Msg.get("core.release.list.planning.phase.execution"), "info", dataValue));

                    // add one day to the from date of the second bar
                    Date from = endTestsDate;
                    Calendar c = Calendar.getInstance();
                    c.setTime(from);
                    c.add(Calendar.DATE, 1);
                    from = c.getTime();
                    item2.values.add(new SourceValue(from, JqueryGantt.cleanToDate(from, deploymentDate), "",
                            Msg.get("core.release.list.planning.phase.rollout"), "info", dataValue));
                    has2Items = true;

                }

                items.add(item1);
                if (has2Items) {
                    items.add(item2);
                }

            }

            String source = "";
            try {
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                source = ow.writeValueAsString(items);
            } catch (JsonProcessingException e) {
                Logger.error(e.getMessage());
            }

            return ok(views.html.core.release.planning.render(source));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

    }

    /**
     * Get the release table and a filter config.
     * 
     * @param filterConfig
     *            the filter config.
     */
    private Pair<Table<ReleaseListView>, Pagination<Release>> getReleasesTable(FilterConfig<ReleaseListView> filterConfig) throws AccountManagementException {

        OrderBy<Release> orderBy = filterConfig.getSortExpression();

        ExpressionList<Release> expressionList = ReleaseDynamicHelper.getReleasesViewAllowedAsQuery(filterConfig.getSearchExpression(), orderBy,
                getSecurityService());

        Pagination<Release> pagination = new Pagination<Release>(expressionList.findList().size(), expressionList);
        pagination.setCurrentPage(filterConfig.getCurrentPage());

        List<ReleaseListView> releaseListView = new ArrayList<ReleaseListView>();
        for (Release release : pagination.getListOfObjects()) {
            releaseListView.add(new ReleaseListView(release));
        }

        Table<ReleaseListView> table = ReleaseListView.templateTable.fillForFilterConfig(releaseListView, filterConfig.getColumnsToHide());

        return Pair.of(table, pagination);

    }

    /**
     * Store the filter configuration in the user preferences.
     * 
     * @param filterConfigAsJson
     *            the filter configuration as a json string
     */
    private void storeFilterConfigFromPreferences(String filterConfigAsJson) {
        getPreferenceManagerPlugin().updatePreferenceValue(IMafConstants.RELEASES_FILTER_STORAGE_PREFERENCE, filterConfigAsJson);
    }

    /**
     * Retrieve the filter configuration from the user preferences.
     */
    private String getFilterConfigurationFromPreferences() {
        return getPreferenceManagerPlugin().getPreferenceValueAsString(IMafConstants.RELEASES_FILTER_STORAGE_PREFERENCE);
    }

    /**
     * Display the details of a release.
     * 
     * @param id
     *            the release id
     * @param page
     *            the current page for initiatives list
     */
    @With(CheckReleaseExists.class)
    @Dynamic(IMafConstants.RELEASE_VIEW_DYNAMIC_PERMISSION)
    public Result view(Long id, Integer page) {

        Release release = ReleaseDAO.getReleaseById(id);

        // get the initiatives
        Pagination<PortfolioEntry> pagination = PortfolioEntryDao.getPEAsPaginationByRelease(id);
        pagination.setCurrentPage(page);

        List<PortfolioEntryListView> portfolioEntryListView = new ArrayList<PortfolioEntryListView>();
        for (PortfolioEntry portfolioEntry : pagination.getListOfObjects()) {
            portfolioEntryListView.add(new PortfolioEntryListView(portfolioEntry, ReleaseDAO.getReleaseByIdAndPE(id, portfolioEntry.id)));
        }

        Set<String> columnsToHide = PortfolioEntryListView.getHideNonDefaultColumns(true, false, false);
        columnsToHide.add("portfolioEntryType");
        columnsToHide.add("isConcept");

        Table<PortfolioEntryListView> table = PortfolioEntryListView.templateTable.fill(portfolioEntryListView, columnsToHide);

        return ok(views.html.core.release.view.render(release, table, pagination));

    }

    /**
     * Display the requirements of a portfolio entry in a release context.
     * 
     * @param id
     *            the release id
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    @With(CheckReleaseExists.class)
    @Dynamic(IMafConstants.RELEASE_VIEW_DYNAMIC_PERMISSION)
    public Result viewInitiative(Long id, Long portfolioEntryId) {

        // get the association
        ReleasePortfolioEntry releasePortfolioEntry = ReleaseDAO.getReleaseByIdAndPE(id, portfolioEntryId);

        List<RequirementListView> requirementListView = new ArrayList<RequirementListView>();
        for (Requirement requirement : ReleaseDAO.getRequirementAsListByIdAndPE(id, portfolioEntryId)) {
            requirementListView.add(new RequirementListView(requirement));
        }

        Set<String> columnsToHide = new HashSet<>();
        columnsToHide.add("editActionLink");
        columnsToHide.add("release");
        columnsToHide.add("category");
        columnsToHide.add("requirementPriority");
        columnsToHide.add("requirementSeverity");
        columnsToHide.add("author");
        columnsToHide.add("storyPoints");

        Table<RequirementListView> table = RequirementListView.templateTable.fill(requirementListView, columnsToHide);

        return ok(views.html.core.release.initiative_view.render(releasePortfolioEntry, table));
    }

    /**
     * Display the form to create a new release.
     */
    @Restrict({ @Group(IMafConstants.RELEASE_EDIT_ALL_PERMISSION) })
    public Result create() {

        Actor actor = ActorDao.getActorByUid(getUserSessionManagerPlugin().getUserSessionId(ctx()));

        Form<ReleaseFormData> filledForm = formTemplate.fill(new ReleaseFormData(actor));

        // add the custom attributes values
        CustomAttributeFormAndDisplayHandler.fillWithValues(filledForm, Release.class, null);

        return ok(views.html.core.release.create.render(filledForm));
    }

    /**
     * Process the form to create a new release.
     */
    @Restrict({ @Group(IMafConstants.RELEASE_EDIT_ALL_PERMISSION) })
    public Result saveCreate() {

        // bind the form
        Form<ReleaseFormData> boundForm = formTemplate.bindFromRequest();

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, Release.class)) {
            return ok(views.html.core.release.create.render(boundForm));
        }

        ReleaseFormData releaseFormData = boundForm.get();

        Release release = new Release();
        releaseFormData.fill(release);
        release.save();

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, Release.class, release.id);

        Utilities.sendSuccessFlashMessage(Msg.get("core.release.create.successful"));

        return redirect(controllers.core.routes.ReleaseController.view(release.id, 0));

    }

    /**
     * Display the form to edit a release.
     * 
     * @param id
     *            the release id
     */
    @With(CheckReleaseExists.class)
    @Dynamic(IMafConstants.RELEASE_EDIT_DYNAMIC_PERMISSION)
    public Result edit(Long id) {

        // get the release
        Release release = ReleaseDAO.getReleaseById(id);

        Form<ReleaseFormData> releaseForm = formTemplate.fill(new ReleaseFormData(release));

        // add the custom attributes values
        CustomAttributeFormAndDisplayHandler.fillWithValues(releaseForm, Release.class, id);

        return ok(views.html.core.release.edit.render(release, releaseForm));

    }

    /**
     * Process the form to edit a release.
     */
    @With(CheckReleaseExists.class)
    @Dynamic(IMafConstants.RELEASE_EDIT_DYNAMIC_PERMISSION)
    public Result saveEdit() {

        // bind the form
        Form<ReleaseFormData> boundForm = formTemplate.bindFromRequest();

        // get the release
        Long id = Long.valueOf(request().body().asFormUrlEncoded().get("id")[0]);
        Release release = ReleaseDAO.getReleaseById(id);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, Release.class)) {
            return ok(views.html.core.release.edit.render(release, boundForm));
        }

        ReleaseFormData releaseFormData = boundForm.get();

        releaseFormData.fill(release);
        release.update();

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, Release.class, release.id);

        Utilities.sendSuccessFlashMessage(Msg.get("core.release.edit.successful"));

        return redirect(controllers.core.routes.ReleaseController.view(release.id, 0));

    }

    /**
     * Display the details of a release.
     * 
     * @param id
     *            the release id
     */
    @With(CheckReleaseExists.class)
    @Dynamic(IMafConstants.RELEASE_EDIT_DYNAMIC_PERMISSION)
    public Result delete(Long id) {

        // get the release
        Release release = ReleaseDAO.getReleaseById(id);

        // set the delete flag to true
        release.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.release.delete.successful"));

        return redirect(controllers.core.routes.ReleaseController.list(false));
    }

    /**
     * Construct the side bar.
     * 
     * @param currentType
     *            the current menu item type, useful to select the correct item
     */
    public static SideBar getSideBar(MenuItemType currentType) {

        SideBar sideBar = new SideBar();

        sideBar.addMenuItem(new ClickableMenuItem("core.release.list.title", controllers.core.routes.ReleaseController.list(false),
                "glyphicons glyphicons-git-branch", currentType.equals(MenuItemType.LIST)));

        sideBar.addMenuItem(new ClickableMenuItem("core.release.list.planning.title", controllers.core.routes.ReleaseController.planning(),
                "glyphicons glyphicons-calendar", currentType.equals(MenuItemType.PLANNING)));

        return sideBar;

    }

    /**
     * The menu item type for a release.
     * 
     * @author Johann Kohler
     * 
     */
    public static enum MenuItemType {
        LIST, PLANNING;
    }

    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    private IPersonalStoragePlugin getPersonalStoragePlugin() {
        return personalStoragePlugin;
    }

    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return preferenceManagerPlugin;
    }

    private INotificationManagerPlugin getNotificationManagerPlugin() {
        return notificationManagerPlugin;
    }

    private ISysAdminUtils getSysAdminUtils() {
        return sysAdminUtils;
    }

    private ISecurityService getSecurityService() {
        return securityService;
    }

}
