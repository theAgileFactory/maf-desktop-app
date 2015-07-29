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
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.architecture.ArchitectureDao;
import framework.services.ServiceManager;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.taftree.TafTreeHelper;
import framework.utils.CustomAttributeFormAndDisplayHandler;
import framework.utils.FilterConfig;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.SysAdminUtils;
import framework.utils.Table;
import framework.utils.TableExcelRenderer;
import models.architecture.ApplicationBlock;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import play.Logger;
import play.data.Form;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import utils.form.ApplicationBlockFormData;
import utils.table.ApplicationBlockListView;

/**
 * The architecture controller (DevDock).
 * 
 * 
 * @author Johann Kohler
 */
public class ArchitectureController extends Controller {

    private static Logger.ALogger log = Logger.of(ArchitectureController.class);

    public static Form<ApplicationBlockFormData> applicationBlockFormTemplate = Form.form(ApplicationBlockFormData.class);

    /**
     * Display the application blocks as a tree view. The render includes a call
     * to viewApplicationBlockFragment.
     * 
     * @param applicationBockId
     *            the application block id to display, null means we display the
     *            root blocks.
     */
    @Restrict({ @Group(IMafConstants.ARCHITECTURE_PERMISSION) })
    public static Result index(Long applicationBockId) {
        return ok(views.html.core.architecture.index.render(applicationBockId));
    }

    /**
     * Display the children of an application block with "boxes" view.
     * 
     * @param applicationBockId
     *            the application block id to display, null means we display the
     *            root blocks.
     */
    @Restrict({ @Group(IMafConstants.ARCHITECTURE_PERMISSION) })
    public static Result viewApplicationBlockFragment(Long applicationBockId) {

        ApplicationBlock applicationBlock = applicationBockId != null ? ArchitectureDao.getApplicationBlockById(applicationBockId) : null;
        List<ApplicationBlock> applicationBlocks = applicationBlock != null ? ArchitectureDao.getApplicationBlockActiveAsListByParent(applicationBlock.id)
                : ArchitectureDao.getApplicationBlockActiveRootsAsList();

        return ok(views.html.core.architecture.application_block_view_fragment.render("applicationBlockTree", applicationBlock, applicationBlocks));
    }

    /**
     * Display the application blocks as a list.
     * 
     * @param reset
     *            define if the filter should be reseted
     */
    @Restrict({ @Group(IMafConstants.ARCHITECTURE_PERMISSION) })
    public static Result applicationBlocks(Boolean reset) {

        try {

            FilterConfig<ApplicationBlockListView> filterConfig = null;

            String backedUpFilter = getFilterConfigurationFromPreferences();
            if (!reset && !StringUtils.isBlank(backedUpFilter)) {

                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(backedUpFilter);
                filterConfig = ApplicationBlockListView.filterConfig.parseResponse(json);

            } else {

                filterConfig = ApplicationBlockListView.filterConfig;
                storeFilterConfigFromPreferences(filterConfig.marshall());

            }

            Pair<Table<ApplicationBlockListView>, Pagination<ApplicationBlock>> table = getApplicationBlocksTable(filterConfig);

            return ok(views.html.core.architecture.application_block_list.render(table.getLeft(), table.getRight(), filterConfig));

        } catch (Exception e) {

            if (reset.equals(false)) {
                ControllersUtils.logAndReturnUnexpectedError(e, log);
                return redirect(controllers.core.routes.ArchitectureController.applicationBlocks(true));
            } else {
                return ControllersUtils.logAndReturnUnexpectedError(e, log);
            }

        }
    }

    /**
     * Filter the application blocks.
     */
    @Restrict({ @Group(IMafConstants.ARCHITECTURE_PERMISSION) })
    public static Result applicationBlocksFilter() {

        try {

            // get the json
            JsonNode json = request().body().asJson();

            // store the filter config
            storeFilterConfigFromPreferences(json.toString());

            // fill the filter config
            FilterConfig<ApplicationBlockListView> filterConfig = ApplicationBlockListView.filterConfig.parseResponse(json);

            // get the table
            Pair<Table<ApplicationBlockListView>, Pagination<ApplicationBlock>> t = getApplicationBlocksTable(filterConfig);

            return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));

        } catch (Exception e) {

            return ControllersUtils.logAndReturnUnexpectedError(e, log);

        }

    }

    /**
     * Store the filter configuration in the user preferences.
     * 
     * @param filterConfigAsJson
     *            the filter configuration as a json string
     */
    private static void storeFilterConfigFromPreferences(String filterConfigAsJson) {
        IPreferenceManagerPlugin preferenceManagerPlugin = ServiceManager.getService(IPreferenceManagerPlugin.NAME, IPreferenceManagerPlugin.class);
        preferenceManagerPlugin.updatePreferenceValue(IMafConstants.APPLICATION_BLOCK_FILTER_STORAGE_PREFERENCE, filterConfigAsJson);
    }

    /**
     * Retrieve the filter configuration from the user preferences.
     */
    private static String getFilterConfigurationFromPreferences() {
        IPreferenceManagerPlugin preferenceManagerPlugin = ServiceManager.getService(IPreferenceManagerPlugin.NAME, IPreferenceManagerPlugin.class);
        return preferenceManagerPlugin.getPreferenceValueAsString(IMafConstants.APPLICATION_BLOCK_FILTER_STORAGE_PREFERENCE);
    }

    /**
     * Export the content of the current list of application blocks as Excel.
     */
    @Restrict({ @Group(IMafConstants.ARCHITECTURE_PERMISSION) })
    public static Promise<Result> applicationBlocksAsExcel() {

        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {

                try {

                    // Get the current user
                    IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME,
                            IUserSessionManagerPlugin.class);
                    final String uid = userSessionManagerPlugin.getUserSessionId(ctx());

                    // construct the table
                    JsonNode json = request().body().asJson();
                    FilterConfig<ApplicationBlockListView> filterConfig = ApplicationBlockListView.filterConfig.parseResponse(json);

                    ExpressionList<ApplicationBlock> expressionList = filterConfig.updateWithSearchExpression(ArchitectureDao.getApplicationBlockAsExpr());
                    filterConfig.updateWithSortExpression(expressionList);

                    List<ApplicationBlockListView> applicationBlockListView = new ArrayList<ApplicationBlockListView>();
                    for (ApplicationBlock applicationBlock : expressionList.findList()) {
                        applicationBlockListView.add(new ApplicationBlockListView(applicationBlock));
                    }

                    Table<ApplicationBlockListView> table = ApplicationBlockListView.templateTable.fillForFilterConfig(applicationBlockListView,
                            filterConfig.getColumnsToHide());

                    final byte[] excelFile = TableExcelRenderer.renderFormatted(table);

                    final String fileName = String.format("applicationBlocksExport_%1$td_%1$tm_%1$ty_%1$tH-%1$tM-%1$tS.xlsx", new Date());
                    final String successTitle = Msg.get("excel.export.success.title");
                    final String successMessage = Msg.get("excel.export.success.message", fileName, "application blocks");
                    final String failureTitle = Msg.get("excel.export.failure.title");
                    final String failureMessage = Msg.get("excel.export.failure.message", "application blocks");

                    // Execute asynchronously
                    SysAdminUtils.scheduleOnce(false, "Application blocks Excel Export", Duration.create(0, TimeUnit.MILLISECONDS), new Runnable() {
                        @Override
                        public void run() {
                            IPersonalStoragePlugin personalStorage = ServiceManager.getService(IPersonalStoragePlugin.NAME, IPersonalStoragePlugin.class);
                            INotificationManagerPlugin notificationManagerPlugin = ServiceManager.getService(INotificationManagerPlugin.NAME,
                                    INotificationManagerPlugin.class);
                            try {
                                OutputStream out = personalStorage.createNewFile(uid, fileName);
                                IOUtils.copy(new ByteArrayInputStream(excelFile), out);
                                notificationManagerPlugin.sendNotification(uid, NotificationCategory.getByCode(Code.DOCUMENT), successTitle, successMessage,
                                        controllers.my.routes.MyPersonalStorage.index().url());
                            } catch (IOException e) {
                                log.error("Unable to export the excel file", e);
                                notificationManagerPlugin.sendNotification(uid, NotificationCategory.getByCode(Code.ISSUE), failureTitle, failureMessage,
                                        controllers.core.routes.ArchitectureController.index(null).url());
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
     * Get the application blocks table with filtering capabilities.
     * 
     * @param filterConfig
     *            the filter config.
     */
    private static Pair<Table<ApplicationBlockListView>, Pagination<ApplicationBlock>> getApplicationBlocksTable(
            FilterConfig<ApplicationBlockListView> filterConfig) {

        ExpressionList<ApplicationBlock> expressionList = filterConfig.updateWithSearchExpression(ArchitectureDao.getApplicationBlockAsExpr());
        filterConfig.updateWithSortExpression(expressionList);

        Pagination<ApplicationBlock> pagination = new Pagination<ApplicationBlock>(expressionList);
        pagination.setCurrentPage(filterConfig.getCurrentPage());

        List<ApplicationBlockListView> listView = new ArrayList<ApplicationBlockListView>();
        for (ApplicationBlock applicationBlock : pagination.getListOfObjects()) {
            listView.add(new ApplicationBlockListView(applicationBlock));
        }

        Table<ApplicationBlockListView> table = ApplicationBlockListView.templateTable.fillForFilterConfig(listView, filterConfig.getColumnsToHide());

        return Pair.of(table, pagination);

    }

    /**
     * Form to create/edit an application block.
     * 
     * @param parentId
     *            only for create case: the application block parent id, null if
     *            root node
     * @param order
     *            only for create case: the application block order
     * @param id
     *            the application block id, null for create case
     */
    @Restrict({ @Group(IMafConstants.APPLICATION_BLOCK_EDIT_ALL_PERMISSION) })
    public static Result manageApplicationBlockFragment(Long parentId, Integer order, Long id) {

        Form<ApplicationBlockFormData> applicationBlockForm = null;
        ApplicationBlock parent = null;

        // edit case
        if (id != null) {

            ApplicationBlock applicationBlock = ArchitectureDao.getApplicationBlockById(id);

            parent = applicationBlock.parent;

            applicationBlockForm = applicationBlockFormTemplate.fill(new ApplicationBlockFormData(applicationBlock));

            // add the custom attributes values
            CustomAttributeFormAndDisplayHandler.fillWithValues(applicationBlockForm, ApplicationBlock.class, id);

        } else { // create case

            applicationBlockForm = applicationBlockFormTemplate.fill(new ApplicationBlockFormData(parentId, order));

            if (parentId != null) {
                parent = ArchitectureDao.getApplicationBlockById(parentId);
            }

            // add the custom attributes default values
            CustomAttributeFormAndDisplayHandler.fillWithValues(applicationBlockForm, ApplicationBlock.class, null);
        }

        return ok(views.html.core.architecture.application_block_manage_fragment.render(parent, applicationBlockForm));
    }

    /**
     * Process the form to create/edit an application block.
     */
    @Restrict({ @Group(IMafConstants.APPLICATION_BLOCK_EDIT_ALL_PERMISSION) })
    public static Result manageApplicationBlockProcessFragment() {

        // bind the form
        Form<ApplicationBlockFormData> boundForm = applicationBlockFormTemplate.bindFromRequest();

        // get the parent
        ApplicationBlock parent = null;
        String parentIdString = boundForm.data().get("parentId");
        if (parentIdString != null && !parentIdString.equals("")) {
            parent = ArchitectureDao.getApplicationBlockById(Long.valueOf(parentIdString));
        }

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, ApplicationBlock.class)) {
            return ok(views.html.core.architecture.application_block_manage_fragment.render(parent, boundForm));
        }

        ApplicationBlockFormData applicationBlockFormData = boundForm.get();

        ApplicationBlock applicationBlock = null;

        String action = null;

        // create case
        if (applicationBlockFormData.id == null) {

            action = "add";

            applicationBlock = new ApplicationBlock();
            applicationBlockFormData.fill(applicationBlock);
            applicationBlock.save();

        } else { // edit case

            action = "edit";

            applicationBlock = ArchitectureDao.getApplicationBlockById(applicationBlockFormData.id);
            applicationBlockFormData.fill(applicationBlock);
            applicationBlock.update();

            if (applicationBlock.archived) {
                archiveChildrenRec(applicationBlock);
            }

        }

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, ApplicationBlock.class, applicationBlock.id);

        return ok(views.html.framework_views.parts.taftree.taf_tree_manual_manage_node.render("applicationBlockTree", action, applicationBlock));
    }

    /**
     * Archive recursively the children of an application block.
     * 
     * @param applicationBlock
     *            the application block to archive the children
     */
    private static void archiveChildrenRec(ApplicationBlock applicationBlock) {
        if (applicationBlock.getChildren() != null) {
            for (Object object : applicationBlock.getChildren()) {
                ApplicationBlock child = (ApplicationBlock) object;
                child.archived = true;
                child.update();
                archiveChildrenRec(child);
            }
        }
    }

    /**
     * Manage an application block in the tree.
     */
    @Restrict({ @Group(IMafConstants.APPLICATION_BLOCK_EDIT_ALL_PERMISSION) })
    public static Result manageApplicationBlockTree() {

        try {

            Long id = TafTreeHelper.getId(request());
            ApplicationBlock applicationBlock = null;
            if (id == null) {
                applicationBlock = new ApplicationBlock();
            } else {
                applicationBlock = ArchitectureDao.getApplicationBlockById(id);
            }

            TafTreeHelper.fill(request(), applicationBlock);
            applicationBlock.save();

            return ok(TafTreeHelper.get(applicationBlock));

        } catch (IllegalArgumentException e) {
            return badRequest();
        }

    }

    /**
     * Load the children of an application block in the tree.
     */
    @Restrict({ @Group(IMafConstants.ARCHITECTURE_PERMISSION) })
    public static Result loadChildrenApplicationBlockTree() {

        try {

            Long id = TafTreeHelper.getId(request());
            List<ApplicationBlock> applicationBlocks = null;
            if (id == null) {
                applicationBlocks = ArchitectureDao.getApplicationBlockActiveRootsAsList();
            } else {
                applicationBlocks = ArchitectureDao.getApplicationBlockActiveAsListByParent(id);
            }

            return ok(TafTreeHelper.gets(applicationBlocks));

        } catch (IllegalArgumentException e) {
            return badRequest();
        }
    }
}
