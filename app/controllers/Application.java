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
package controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import dao.datasyndication.DataSyndicationDao;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.plugins.IPluginManagerService;
import framework.services.plugins.IPluginManagerService.IPluginInfo;
import framework.services.plugins.api.IPluginMenuDescriptor;
import framework.services.remote.IAdPanelManagerService;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.services.system.ISysAdminUtils;
import framework.utils.FileAttachmentHelper;
import framework.utils.FilterConfig;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.Table;
import framework.utils.TableExcelRenderer;
import framework.utils.Utilities;
import models.framework_models.account.Notification;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.framework_models.account.Principal;
import models.framework_models.account.Shortcut;
import models.framework_models.common.DynamicSingleItemCustomAttributeValue;
import models.framework_models.common.HelpTarget;
import play.Configuration;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import services.datasyndication.IDataSyndicationService;
import services.echannel.IEchannelService;
import services.echannel.IEchannelService.EchannelException;
import services.echannel.models.InstanceInfo;
import services.tableprovider.ITableProvider;
import utils.table.NotificationListView;
import utils.tour.TourUtils;

/**
 * The Home application controller.<br/>
 * This one deals with the:
 * <ul>
 * <li>The "home" page</li>
 * <li>The IDzone (including the management of the notifications)</li>
 * <li>The {@link DynamicSingleItemCustomAttributeValue} JSON api</li>
 * </ul>
 * 
 * @author Pierre-Yves Cloux
 */
public class Application extends Controller {
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private INotificationManagerPlugin notificationManagerPlugin;
    @Inject
    private IAdPanelManagerService adPanelManagerService;
    @Inject
    private IAttachmentManagerPlugin attachmentManagerPlugin;
    @Inject
    private ISysAdminUtils sysAdminUtils;
    @Inject
    private IPersonalStoragePlugin personalStoragePlugin;
    @Inject
    private IDataSyndicationService dataSyndicationService;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;
    @Inject
    private IEchannelService echannelService;
    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;
    @Inject
    private ITableProvider tableProvider;

    private static Logger.ALogger log = Logger.of(Application.class);

    private static final String ECHANNEL_BIZDOCK_SSO_ACTION = "/callback?client_name=BizDockSSOClient&token=$0&redirect=$1";
    private static final String ECHANNEL_INSTANCE_VIEW_ACTION = "/instance/view?instanceId=$0";

    /**
     * Get the echannel instance view URL with SSO.
     */
    @SubjectPresent
    public Result echannelInstanceViewUrl() {

        String viewInstanceUrl = configuration.getString("maf.echannel.base_url") + ECHANNEL_INSTANCE_VIEW_ACTION;

        String url = null;
        try {
            InstanceInfo instanceInfo = echannelService.getInstanceInfo();
            url = viewInstanceUrl.replace("$0", String.valueOf(instanceInfo.id));
        } catch (EchannelException e) {
            Logger.error("impossible to get the instance info", e);
        }

        return redirect(getEchannelUrl(url));

    }

    /**
     * Get the echannel home URL with SSO.
     */
    @SubjectPresent
    public Result echannelHomeUrl() {

        String homeUrl = configuration.getString("maf.echannel.base_url");

        String url = null;
        try {
            InstanceInfo instanceInfo = echannelService.getInstanceInfo();
            url = homeUrl.replace("$0", String.valueOf(instanceInfo.id));
        } catch (EchannelException e) {
            Logger.error("impossible to get the instance info", e);
        }

        return redirect(getEchannelUrl(url));

    }

    /**
     * Get the eChannel URL with SSO if possible.
     * 
     * If the SSO is not possible then returns simply the resource URL.
     * 
     * @param url
     *            the resource eChannel URL
     */
    private String getEchannelUrl(String url) {

        String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
        String token = null;
        try {
            token = echannelService.generateSSOToken(uid);
        } catch (EchannelException e) {
            Logger.info("No possible to get an SSO token for eChannel for the user " + uid + ". Message is: " + e.getMessage());
        }

        if (token == null) {
            return url;
        } else {
            String ssoUrl = configuration.getString("maf.echannel.base_url") + ECHANNEL_BIZDOCK_SSO_ACTION;
            ssoUrl = ssoUrl.replace("$0", token);
            try {
                ssoUrl = ssoUrl.replace("$1", URLEncoder.encode(url, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Logger.warn("impossible to encode the URL " + url);
            }
            return ssoUrl;
        }
    }

    /**
     * Redirect the user to the link of a notification and set it as read.
     * 
     * @param id
     *            the notification id
     */
    @SubjectPresent
    public Result redirectForNotification(Long id) {
        Notification notification = Notification.find.where().eq("deleted", false).eq("id", id).findUnique();
        notification.isRead = true;
        notification.save();
        if (notification.actionLink != null && !notification.actionLink.equals("")) {
            return redirect(notification.actionLink);
        } else {
            return redirect(routes.Application.displayNotifications());
        }
    }

    /**
     * Display the notifications of the current user.
     * 
     * @return
     */
    @SubjectPresent
    public Result displayNotifications() {

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<NotificationListView> filterConfig = this.getTableProvider().get().notification.filterConfig.getCurrent(uid, request());

            Pair<Table<NotificationListView>, Pagination<Notification>> t = getNotificationsTable(filterConfig);

            return ok(views.html.home.notifications_list.render(t.getLeft(), t.getRight(), filterConfig));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Filter the notifications.
     */
    @SubjectPresent
    public Result filterNotifications() {

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<NotificationListView> filterConfig = this.getTableProvider().get().notification.filterConfig.persistCurrentInDefault(uid, request());

            if (filterConfig == null) {
                return ok(views.html.framework_views.parts.table.dynamic_tableview_no_more_compatible.render());
            } else {

                // get the table
                Pair<Table<NotificationListView>, Pagination<Notification>> t = getNotificationsTable(filterConfig);

                return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));

            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Get the notifications table and filter config.
     * 
     * @param filterConfig
     *            the filter config.
     */
    private Pair<Table<NotificationListView>, Pagination<Notification>> getNotificationsTable(FilterConfig<NotificationListView> filterConfig)
            throws AccountManagementException {

        String loggedUser = getUserSessionManagerPlugin().getUserSessionId(ctx());

        ExpressionList<Notification> expressionList = filterConfig
                .updateWithSearchExpression(getNotificationManagerPlugin().getNotificationsForUidAsExpr(loggedUser));
        filterConfig.updateWithSortExpression(expressionList);

        Pagination<Notification> pagination = new Pagination<Notification>(this.getPreferenceManagerPlugin(), expressionList);
        pagination.setCurrentPage(filterConfig.getCurrentPage());

        List<NotificationListView> notificationListViews = new ArrayList<NotificationListView>();
        for (Notification notification : pagination.getListOfObjects()) {
            notificationListViews.add(new NotificationListView(notification));
        }

        Table<NotificationListView> table = this.getTableProvider().get().notification.templateTable.fillForFilterConfig(notificationListViews,
                filterConfig.getColumnsToHide());

        table.addLinkRowAction(Msg.get("notifications.action.delete"), controllers.routes.Application.deleteNotifications().url(),
                Msg.get("notifications.action.delete.confirmation.message"));

        table.setAllIdsUrl(controllers.routes.Application.getAllNotificationIds().url());

        return Pair.of(table, pagination);

    }

    /**
     * Delete the selected notifications.
     */
    @SubjectPresent
    public Result deleteNotifications() {

        List<String> ids = FilterConfig.getIdsFromRequest(request());

        String loggedUser = getUserSessionManagerPlugin().getUserSessionId(ctx());

        for (String idString : ids) {
            Long id = Long.parseLong(idString);
            getNotificationManagerPlugin().deleteNotificationsForUid(loggedUser, id);
        }

        Utilities.sendSuccessFlashMessage(Msg.get("notifications.action.delete.successful.message"));

        return redirect(routes.Application.displayNotifications());
    }

    /**
     * Get all notifications ids according to the current filter configuration.
     */
    @SubjectPresent
    public Result getAllNotificationIds() {

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<NotificationListView> filterConfig = this.getTableProvider().get().notification.filterConfig.getCurrent(uid, request());

            String loggedUser = getUserSessionManagerPlugin().getUserSessionId(ctx());

            ExpressionList<Notification> expressionList = filterConfig
                    .updateWithSearchExpression(getNotificationManagerPlugin().getNotificationsForUidAsExpr(loggedUser));

            List<String> ids = new ArrayList<>();
            for (Notification notification : expressionList.findList()) {
                ids.add(String.valueOf(notification.id));
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.valueToTree(ids);

            return ok(node);

        } catch (Exception e) {
            return internalServerError();
        }
    }

    /**
     * Delete a notification (if this one belongs to the current user).
     * 
     * @param id
     *            the id of a {@link Notification}
     * @return
     */
    @SubjectPresent
    public Result deleteNotification(Long id) {
        String loggedUser = getUserSessionManagerPlugin().getUserSessionId(ctx());
        Notification notification = Notification.find.where().eq("deleted", false).eq("id", id).findUnique();
        if (getNotificationManagerPlugin().deleteNotificationsForUid(loggedUser, id)) {
            if (notification.isMessage) {
                Utilities.sendSuccessFlashMessage(Msg.get("messaging.list.success.deleted"));
                return redirect(routes.MessagingController.index());
            } else {
                Utilities.sendSuccessFlashMessage(Msg.get("notifications.list.success.deleted"));
                return redirect(routes.Application.displayNotifications());
            }
        }
        return badRequest();
    }

    /**
     * Mark a notification as read.
     * 
     * @param id
     *            the id of a {@link Notification}
     * @return
     */
    @SubjectPresent
    public Result markNotificationAsRead(Long id) {
        Notification notification = Notification.find.where().eq("deleted", false).eq("id", id).findUnique();
        notification.isRead = true;
        notification.save();
        if (notification.isMessage) {
            return redirect(routes.MessagingController.index());
        } else {
            return redirect(routes.Application.displayNotifications());
        }
    }

    /**
     * Download a file attachment.
     * 
     * @param attachmentId
     *            the file attachment id
     */
    @SubjectPresent
    public Result downloadFileAttachment(Long attachmentId) {
        return FileAttachmentHelper.downloadFileAttachment(attachmentId, getAttachmentManagerPlugin(), getUserSessionManagerPlugin());
    }

    /**
     * Delete a file attachment.
     * 
     * @param attachmentId
     *            the file attachment id
     * @return
     */
    @SubjectPresent
    public Result deleteFileAttachment(Long attachmentId) {
        return FileAttachmentHelper.deleteFileAttachment(attachmentId, getAttachmentManagerPlugin(), getUserSessionManagerPlugin());
    }

    /**
     * The API which deal with the {@link DynamicSingleItemCustomAttributeValue}
     * (see corresponding documentation).
     */
    public Result dynamicSingleCustomAttributeApi() {
        return DynamicSingleItemCustomAttributeValue.jsonQueryApi();
    }

    /**
     * Provide the help for the current page.
     * 
     * @param route
     *            the current route
     */
    @SubjectPresent
    public Result help(String route) {

        // set the language in the url, if english then set nothing (because
        // this is the default language of the wiki)
        String lang = "";
        if (!Context.current().lang().code().equals("en")) {
            lang = Context.current().lang().code() + ":";
        }

        String wikiBaseUrl = getConfiguration().getString("maf.help.url").replace("%lang%", lang);

        HelpTarget helpTarget = HelpTarget.getByRoute(route);

        if (helpTarget != null) {

            String wikiUrl = wikiBaseUrl.replace("%target%", helpTarget.target);

            return redirect(wikiUrl);

        } else {

            String wikiUrl = wikiBaseUrl.replace("%target%", "start");

            return redirect(wikiUrl);
        }
    }

    /**
     * Provide the HTML fragment that displays the shortcuts in the top menu.
     */
    @SubjectPresent
    public Result viewShortcuts() {

        try {
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            Principal principal = Principal.getPrincipalFromUid(uid);

            List<Shortcut> shortcuts = Shortcut.getByPrincipal(principal.id);

            return ok(views.html.home.shortcuts.render(shortcuts));

        } catch (Exception e) {

            Logger.error(e.getMessage());

        }

        return badRequest();
    }

    /**
     * Add a new shortcut.
     */
    @SubjectPresent
    public Result addShortcut() {

        JsonNode json = request().body().asJson();

        if (json != null) {

            String name = json.findPath("name").textValue();
            String route = json.findPath("route").textValue();

            if (name != null && route != null) {

                try {

                    String message = "";
                    String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
                    Principal principal = Principal.getPrincipalFromUid(uid);

                    // check the shortcut is not already existing
                    if (Shortcut.getByPrincipalAndRoute(principal.id, route) == null) {

                        Shortcut shortcut = new Shortcut();
                        shortcut.name = name;
                        shortcut.route = route;
                        shortcut.principal = principal;
                        shortcut.save();

                        message = Msg.get("shortcut.add.message.success");

                    } else {
                        message = Msg.get("shortcut.add.message.already");
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    JsonFactory factory = mapper.getFactory();
                    JsonParser jp = factory.createParser("{\"message\" : \"" + message + "\"}");
                    JsonNode result = mapper.readTree(jp);

                    return ok(result);

                } catch (Exception e) {
                    Logger.error(e.getMessage());
                }

            } else {
                Logger.error("addShortcut: name or route not found");
            }

        } else {
            Logger.error("addShortcut: no json found");
        }

        return badRequest();

    }

    /**
     * Delete an existing shortcut.
     */
    @SubjectPresent
    public Result deleteShortcut() {

        JsonNode json = request().body().asJson();

        if (json != null) {

            Long id = json.findPath("id").longValue();

            // get the shortcut
            Shortcut shortcut = Shortcut.getById(id);

            if (shortcut != null) {

                try {
                    String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
                    Principal principal = Principal.getPrincipalFromUid(uid);

                    // check the shortcut belongs to the current user
                    if (shortcut.principal.id.equals(principal.id)) {

                        // delete the shortcut
                        shortcut.doDelete();

                        ObjectMapper mapper = new ObjectMapper();
                        JsonFactory factory = mapper.getFactory();
                        JsonParser jp = factory.createParser("{}");
                        JsonNode result = mapper.readTree(jp);

                        return ok(result);

                    } else {

                        Logger.error("deleteShortcut: the user " + principal.id + " try to delete the shortcut " + id
                                + " and this last belongs to another user (" + shortcut.principal.id + ")");

                    }

                } catch (Exception e) {
                    Logger.error(e.getMessage());
                }

            } else {
                Logger.error("deleteShortcut: impossible to find the shortcut for the id " + id);
            }

        } else {
            Logger.error("deleteShortcut: id not found");
        }

        return badRequest();

    }

    /**
     * Call at the end of a tour.
     * 
     * @param tourUid
     *            the tour uid
     */
    public Result endTour(String tourUid) {
        TourUtils.markTourAsRead(this.getPreferenceManagerPlugin(), tourUid);
        return ok();
    }

    /**
     * Export an item of an agreement link as Excel.
     * 
     * @param redirect
     *            the redirect URL
     * @param agreementLinkId
     *            the agreement link id
     * @param agreementItemId
     *            the agreement item id
     */
    @SubjectPresent
    public Result exportDataSyndicationAsExcel(String redirect, Long agreementLinkId, Long agreementItemId) {

        try {

            // get the data
            List<List<Object>> dataSyndication = DataSyndicationDao.getDataSyndicationAsDataByLinkAndItem(agreementLinkId, agreementItemId);

            // create the excel file with a sheet
            XSSFWorkbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("export");

            // add the header
            Row headerRow = sheet.createRow(0);
            int columnIndex = 0;
            CellStyle headerCellStyle = wb.createCellStyle();
            Font f = wb.createFont();
            f.setBoldweight(Font.BOLDWEIGHT_BOLD);
            headerCellStyle.setFont(f);
            for (Object header : dataSyndication.get(0)) {
                Cell cell = headerRow.createCell(columnIndex);
                cell.setCellValue(Msg.get(header.toString()));
                cell.setCellStyle(headerCellStyle);
                columnIndex++;
            }

            // add the data
            int rowIndex = 0;
            for (List<Object> row : dataSyndication) {
                if (rowIndex != 0) {
                    Row dataRow = sheet.createRow(rowIndex);
                    columnIndex = 0;
                    for (Object elem : row) {
                        Cell cell = dataRow.createCell(columnIndex);
                        if (elem == null) {
                            cell.setCellValue("");
                        } else {
                            if (elem instanceof String && getDataSyndicationService().getStringDate(elem.toString()) != null) {
                                cell.setCellValue(getDataSyndicationService().getStringDate(elem.toString()));
                                XSSFCellStyle dateCellStyle = wb.createCellStyle();
                                XSSFDataFormat df = wb.createDataFormat();
                                dateCellStyle.setDataFormat(df.getFormat(TableExcelRenderer.DEFAULT_EXCEL_DATE_FORMAT));
                                cell.setCellStyle(dateCellStyle);
                            } else if (elem instanceof Boolean) {
                                cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
                                cell.setCellValue((Boolean) elem);
                            } else if (elem instanceof Integer || elem instanceof Integer || elem instanceof Double) {
                                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                                cell.setCellValue(Double.valueOf(elem.toString()));
                            } else {
                                cell.setCellValue(Msg.get(elem.toString().replaceAll("\\<[^>]*>", "").trim()));
                            }
                        }
                        columnIndex++;
                    }
                }
                rowIndex++;
            }

            // create the file
            ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
            wb.write(outBuffer);
            wb.close();
            outBuffer.close();
            final byte[] excelFile = outBuffer.toByteArray();

            // get the current user
            final String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());

            // prepare the message
            final String fileName = String.format("syndicatedDataExport_%1$td_%1$tm_%1$ty_%1$tH-%1$tM-%1$tS.xlsx", new Date());
            final String successTitle = Msg.get("excel.export.success.title");
            final String successMessage = Msg.get("excel.export.success.message", fileName);
            final String failureTitle = Msg.get("excel.export.failure.title");
            final String failureMessage = Msg.get("excel.export.failure.message");

            // Execute asynchronously
            getSysAdminUtils().scheduleOnce(false, "Data Excel Export", Duration.create(0, TimeUnit.MILLISECONDS), new Runnable() {
                @Override
                public void run() {

                    try {

                        OutputStream out = getPersonalStoragePlugin().createNewFile(uid, fileName);
                        IOUtils.copy(new ByteArrayInputStream(excelFile), out);
                        getNotificationManagerPlugin().sendNotification(uid, NotificationCategory.getByCode(Code.DOCUMENT), successTitle, successMessage,
                                controllers.my.routes.MyPersonalStorage.index().url());

                    } catch (Exception e) {

                        log.error("Unable to export the excel file", e);
                        getNotificationManagerPlugin().sendNotification(uid, NotificationCategory.getByCode(Code.ISSUE), failureTitle, failureMessage,
                                redirect);

                    }
                }
            });

            Utilities.sendSuccessFlashMessage(Msg.get("data_syndication.display_date.export_as_excel.request.success"));
            return redirect(redirect);

        } catch (Exception e) {
            log.error("Unable to create the excel file", e);
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }

    }

    /**
     * Return the AdPanel content for the specified page.
     * 
     * @param page
     *            a page
     */
    public Promise<Result> getAdPanelContent(String page) {
        return getAdPanelManagerService().getRemotePanel(page);
    }

    /**
     * Returns the data associated with the IDZone component.
     * 
     * @param userSessionManagerService
     *            the user session manager service
     * @param accountManagerService
     *            the account manager service
     * @param notificationManagerService
     *            the notification manager service
     * @param pluginManagerService
     *            the plugin manager service
     * 
     * @return an IDZone instance if the user is logged or null otherwise
     */
    public static IDZoneData getIDZoneData(IUserSessionManagerPlugin userSessionManagerService, IAccountManagerPlugin accountManagerService,
            INotificationManagerPlugin notificationManagerService, IPluginManagerService pluginManagerService) {
        String loggedUser = userSessionManagerService.getUserSessionId(ctx());
        try {
            if (!StringUtils.isBlank(loggedUser)) {
                IUserAccount userAccount = accountManagerService.getUserAccountFromUid(loggedUser);
                if (userAccount != null) {
                    IDZoneData idZoneData = new IDZoneData();
                    idZoneData.isAuthorized = userAccount.isActive();
                    idZoneData.hasNotifications = notificationManagerService.hasNotifications(loggedUser);
                    idZoneData.nbNotReadNotifications = notificationManagerService.nbNotReadNotifications(loggedUser);
                    idZoneData.notifications = notificationManagerService.getNotReadNotificationsForUid(loggedUser);
                    idZoneData.hasMessages = notificationManagerService.hasMessages(loggedUser);
                    idZoneData.nbNotReadMessages = notificationManagerService.nbNotReadMessages(loggedUser);
                    idZoneData.messages = notificationManagerService.getNotReadMessagesForUid(loggedUser);
                    idZoneData.login = userAccount.getFirstName() + " " + userAccount.getLastName();
                    idZoneData.pluginMenuDesriptors = new HashMap<Long, Pair<String, IPluginMenuDescriptor>>();
                    Map<Long, IPluginInfo> pluginInfos = pluginManagerService.getRegisteredPluginDescriptors();
                    for (Long pluginConfigirationId : pluginInfos.keySet()) {
                        if (pluginInfos.get(pluginConfigirationId).getMenuDescriptor() != null) {
                            IPluginInfo pluginInfo = pluginInfos.get(pluginConfigirationId);
                            idZoneData.pluginMenuDesriptors.put(pluginConfigirationId,
                                    Pair.of(pluginInfo.getDescriptor().getIdentifier(), pluginInfo.getMenuDescriptor()));
                        }
                    }
                    idZoneData.logoutUrl = controllers.sso.routes.Authenticator.customLogout().url();
                    return idZoneData;
                }
                return null;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Error while returning the IDzone content", e);
        }
        return null;
    }

    /**
     * A class which holds the data for the IDzone.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class IDZoneData {
        public boolean isAuthorized;
        public boolean hasNotifications;
        public int nbNotReadNotifications;
        public List<Notification> notifications;
        public boolean hasMessages;
        public int nbNotReadMessages;
        public List<Notification> messages;
        public String login;
        public Map<Long, Pair<String, IPluginMenuDescriptor>> pluginMenuDesriptors;
        public String logoutUrl;
    }

    /**
     * Define a shortcut to display.
     * 
     * @author Johann Kohler
     */
    public static class ShortcutView {
        public Long id;
        public String name;
        public String route;

        /**
         * Construct a shortcut view with a DB shortcut entry.
         * 
         * @param shortcut
         *            the DB shortcut entry
         */
        public ShortcutView(Shortcut shortcut) {
            this.id = shortcut.id;
            this.name = shortcut.getName();
            this.route = shortcut.route;
        }
    }

    /**
     * Get the user session manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    /**
     * Get the notification manager service.
     */
    private INotificationManagerPlugin getNotificationManagerPlugin() {
        return notificationManagerPlugin;
    }

    /**
     * Get the ad panel manager service.
     */
    private IAdPanelManagerService getAdPanelManagerService() {
        return adPanelManagerService;
    }

    /**
     * Get the attachment manager service.
     */
    private IAttachmentManagerPlugin getAttachmentManagerPlugin() {
        return attachmentManagerPlugin;
    }

    /**
     * Get the personal storage service.
     */
    private IPersonalStoragePlugin getPersonalStoragePlugin() {
        return personalStoragePlugin;
    }

    /**
     * Get the system admin utils.
     */
    private ISysAdminUtils getSysAdminUtils() {
        return sysAdminUtils;
    }

    /**
     * Get the data syndication service.
     */
    private IDataSyndicationService getDataSyndicationService() {
        return dataSyndicationService;
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    /**
     * Get the Play configuration service.
     */
    private Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return this.preferenceManagerPlugin;
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }
}
