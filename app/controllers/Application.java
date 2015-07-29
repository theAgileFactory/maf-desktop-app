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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.framework_models.account.Notification;
import models.framework_models.account.Principal;
import models.framework_models.account.Shortcut;
import models.framework_models.common.DynamicSingleItemCustomAttributeValue;
import models.framework_models.common.HelpTarget;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import play.Logger;
import play.Play;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Result;
import security.dynamic.PortfolioEntryDynamicHelper;
import utils.table.NotificationListView;
import utils.tour.TourUtils;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.OrderBy;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import constants.IMafConstants;
import dao.pmo.ActorDao;
import framework.services.ServiceManager;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.plugins.IPluginManagerService;
import framework.services.plugins.IPluginManagerService.IPluginInfo;
import framework.services.plugins.api.IPluginMenuDescriptor;
import framework.services.remote.IAdPanelManagerService;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.FileAttachmentHelper;
import framework.utils.FilterConfig;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.Table;
import framework.utils.Utilities;

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
    private static Logger.ALogger log = Logger.of(Application.class);

    /**
     * Redirect the user to the link of a notification and set it as read.
     * 
     * @param id
     *            the notification id
     */
    @SubjectPresent
    public static Result redirectForNotification(Long id) {
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
    public static Result displayNotifications() {

        try {

            FilterConfig<NotificationListView> filterConfig = NotificationListView.filterConfig;

            Pair<Table<NotificationListView>, Pagination<Notification>> t = getNotificationsTable(filterConfig);

            return ok(views.html.home.notifications_list.render(Msg.get("notifications.list.title"), t.getLeft(), t.getRight(), filterConfig));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Filter the notifications.
     */
    @SubjectPresent
    public static Result filterNotifications() {

        try {

            // get the json
            JsonNode json = request().body().asJson();

            // fill the filter config
            FilterConfig<NotificationListView> filterConfig = NotificationListView.filterConfig.parseResponse(json);

            // get the table
            Pair<Table<NotificationListView>, Pagination<Notification>> t = getNotificationsTable(filterConfig);

            return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Get the notifications table and filter config.
     * 
     * @param filterConfig
     *            the filter config.
     */
    private static Pair<Table<NotificationListView>, Pagination<Notification>> getNotificationsTable(FilterConfig<NotificationListView> filterConfig)
            throws AccountManagementException {

        String loggedUser = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class).getUserSessionId(ctx());

        ExpressionList<Notification> expressionList = filterConfig.updateWithSearchExpression(ServiceManager.getService(INotificationManagerPlugin.NAME,
                INotificationManagerPlugin.class).getNotificationsForUidAsExpr(loggedUser));
        filterConfig.updateWithSortExpression(expressionList);

        Pagination<Notification> pagination = new Pagination<Notification>(expressionList);
        pagination.setCurrentPage(filterConfig.getCurrentPage());

        List<NotificationListView> notificationListViews = new ArrayList<NotificationListView>();
        for (Notification notification : pagination.getListOfObjects()) {
            notificationListViews.add(new NotificationListView(notification));
        }

        Table<NotificationListView> table = NotificationListView.templateTable.fillForFilterConfig(notificationListViews, filterConfig.getColumnsToHide());

        table.addLinkRowAction(Msg.get("notifications.action.delete"), controllers.routes.Application.deleteNotifications().url(),
                Msg.get("notifications.action.delete.confirmation.message"));

        table.setAllIdsUrl(controllers.routes.Application.getAllNotificationIds().url());

        return Pair.of(table, pagination);

    }

    /**
     * Delete the selected notifications.
     */
    @SubjectPresent
    public static Result deleteNotifications() {

        List<String> ids = FilterConfig.getIdsFromRequest(request());

        String loggedUser = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class).getUserSessionId(ctx());

        for (String idString : ids) {
            Long id = Long.parseLong(idString);
            ServiceManager.getService(INotificationManagerPlugin.NAME, INotificationManagerPlugin.class).deleteNotificationsForUid(loggedUser, id);
        }

        Utilities.sendSuccessFlashMessage(Msg.get("notifications.action.delete.successful.message"));

        return redirect(routes.Application.displayNotifications());
    }

    /**
     * Get all notifications ids according to the current filter configuration.
     */
    @SubjectPresent
    public static Result getAllNotificationIds() {

        try {

            // get the json
            JsonNode json = request().body().asJson();

            // fill the filter config
            FilterConfig<NotificationListView> filterConfig = NotificationListView.filterConfig.parseResponse(json);

            String loggedUser = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class).getUserSessionId(ctx());

            ExpressionList<Notification> expressionList = filterConfig.updateWithSearchExpression(ServiceManager.getService(INotificationManagerPlugin.NAME,
                    INotificationManagerPlugin.class).getNotificationsForUidAsExpr(loggedUser));

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
    public static Result deleteNotification(Long id) {
        String loggedUser = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class).getUserSessionId(ctx());
        Notification notification = Notification.find.where().eq("deleted", false).eq("id", id).findUnique();
        if (ServiceManager.getService(INotificationManagerPlugin.NAME, INotificationManagerPlugin.class).deleteNotificationsForUid(loggedUser, id)) {
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
    public static Result markNotificationAsRead(Long id) {
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
     * Display the home page.
     * 
     * @return a list of {@link ThirdPartySystem}
     */
    @SubjectPresent(forceBeforeAuthCheck = true)
    public static Result index() {

        // check if the user has an actor
        boolean hasActor = false;
        try {
            IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            String uid = userSessionManagerPlugin.getUserSessionId(ctx());
            Actor actor = ActorDao.getActorByUid(uid);
            if (actor != null && actor.id != null) {
                hasActor = true;
            }
        } catch (Exception e) {
            Logger.debug("the user hasn't an actor");
        }

        // get the not read notifications
        Set<String> hideColumnsForNotifications = new HashSet<String>();
        hideColumnsForNotifications.add("deleteActionLink");
        hideColumnsForNotifications.add("isRead");
        String loggedUser = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class).getUserSessionId(ctx());

        List<NotificationListView> notificationListViews = new ArrayList<NotificationListView>();
        for (Notification notification : ServiceManager.getService(INotificationManagerPlugin.NAME, INotificationManagerPlugin.class)
                .getNotReadNotificationsForUid(loggedUser)) {
            notificationListViews.add(new NotificationListView(notification));
        }

        Table<NotificationListView> notificationsTable = NotificationListView.templateTable.fill(notificationListViews, hideColumnsForNotifications);

        // get last created portfolio entries
        List<PortfolioEntry> portfolioEntries;
        OrderBy<PortfolioEntry> orderBy = new OrderBy<PortfolioEntry>();
        orderBy.desc("creationDate");
        orderBy.desc("id");
        try {
            portfolioEntries = PortfolioEntryDynamicHelper.getPortfolioEntriesViewAllowedAsQuery(orderBy).setMaxRows(5).findList();
        } catch (AccountManagementException e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

        // get the profile info
        IUserAccount account = null;
        try {
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            IUserSessionManagerPlugin userSessionPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            account = accountManagerPlugin.getUserAccountFromUid(userSessionPlugin.getUserSessionId(ctx()));
        } catch (AccountManagementException e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

        return ok(views.html.home.index.render(Msg.get("main.application.title.header"), notificationsTable, portfolioEntries, account, hasActor));
    }

    /**
     * Download a file attachment.
     * 
     * @param attachmentId
     *            the file attachment id
     */
    @SubjectPresent
    public static Result downloadFileAttachment(Long attachmentId) {
        return FileAttachmentHelper.downloadFileAttachment(attachmentId);
    }

    /**
     * Delete a file attachment.
     * 
     * @param attachmentId
     *            the file attachment id
     * @return
     */
    @SubjectPresent
    public static Result deleteFileAttachment(Long attachmentId) {
        return FileAttachmentHelper.deleteFileAttachment(attachmentId);
    }

    /**
     * Returns a json flow for the IDzone component. Here is a sample structure:
     * 
     * <pre>
     * {
     *    "authorized" : true,
     *    "hasNotifications" : true,
     *    "login" : "Pierre-Yves Cloux",
     *    "applist" : {
     *        "1":["Continuous build","http://site/jenkins", "jenk1"],
     *        "2":["Project management","http://site/redmine","redm1"],
     *        "0" : ["Governance","http://site/","governance"]},
     *    "logouturl" : "http://site/logout"
     * }
     * </pre>
     * 
     * <br/>
     * The structure of the applist is:
     * <p>
     * "pluginConfigurationId", ["Name of the application",
     * "URL to the application", "pluginDefinitionIdentifier"]
     * </p>
     */
    @SubjectPresent(forceBeforeAuthCheck = true)
    public static Result idzone() {
        IDZoneData idZoneData = getIDZoneData();
        ObjectNode result = Json.newObject();
        try {
            if (idZoneData != null) {
                result.put("authorized", idZoneData.isAuthorized);
                result.put("hasNotifications", idZoneData.hasNotifications);
                result.put("login", idZoneData.login);
                ObjectNode applist = Json.newObject();

                // The MAF governance module
                ArrayNode array = Json.newObject().arrayNode();
                array.add(Msg.get("main.application.title.header"));
                array.add(routes.Redirector.governance().url());
                array.add(IMafConstants.MAF_GOVERNANCE_MODULE_ID);
                applist.set(String.valueOf(IMafConstants.MAF_GOVERNANCE_MODULE_CONFIGURATION), array);

                // Add the plugin menus
                for (Long pluginConfigurationId : idZoneData.pluginMenuDesriptors.keySet()) {
                    IPluginMenuDescriptor pluginMenuDescriptor = idZoneData.pluginMenuDesriptors.get(pluginConfigurationId).getRight();
                    array = Json.newObject().arrayNode();
                    array.add(Msg.get(pluginMenuDescriptor.getLabel()));
                    array.add(pluginMenuDescriptor.getPath());
                    array.add(idZoneData.pluginMenuDesriptors.get(pluginConfigurationId).getLeft());
                    applist.set(String.valueOf(pluginConfigurationId), array);
                }
                result.set("applist", applist);

                result.put("logouturl", idZoneData.logoutUrl);
            } else {
                result.put("authorized", false);
            }
        } catch (Exception e) {
            result = Json.newObject();
            result.put("authorized", false);
            log.error("Error while returning the IDzone content", e);
        }
        return ok(result);
    }

    /**
     * The API which deal with the {@link DynamicSingleItemCustomAttributeValue}
     * (see corresponding documentation).
     */
    public static Result dynamicSingleCustomAttributeApi() {
        return DynamicSingleItemCustomAttributeValue.jsonQueryApi();
    }

    /**
     * Provide the help for the current page.
     * 
     * @param route
     *            the current route
     */
    @SubjectPresent
    public static Result help(String route) {

        // set the language in the url, if english then set nothing (because
        // this is the default language of the wiki)
        String lang = "";
        if (!Context.current().lang().code().equals("en")) {
            lang = Context.current().lang().code() + ":";
        }

        String wikiBaseUrl = Play.application().configuration().getString("maf.help.url").replace("%lang%", lang);

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
    public static Result viewShortcuts() {

        try {

            IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            String uid = userSessionManagerPlugin.getUserSessionId(ctx());
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
    public static Result addShortcut() {

        JsonNode json = request().body().asJson();

        if (json != null) {

            String name = json.findPath("name").textValue();
            String route = json.findPath("route").textValue();

            if (name != null && route != null) {

                try {

                    String message = "";

                    IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME,
                            IUserSessionManagerPlugin.class);
                    String uid = userSessionManagerPlugin.getUserSessionId(ctx());
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
    public static Result deleteShortcut() {

        JsonNode json = request().body().asJson();

        if (json != null) {

            Long id = json.findPath("id").longValue();

            // get the shortcut
            Shortcut shortcut = Shortcut.getById(id);

            if (shortcut != null) {

                try {

                    IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME,
                            IUserSessionManagerPlugin.class);
                    String uid = userSessionManagerPlugin.getUserSessionId(ctx());
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
                Logger.error("deleteShortcut: impossible to findRelease the shortcut for the id " + id);
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
    public static Result endTour(String tourUid) {
        TourUtils.markTourAsRead(tourUid);
        return ok();
    }

    /**
     * Return the AdPanel content for the specified page.
     * 
     * @param page
     *            a page
     */
    public static Promise<Result> getAdPanelContent(String page) {
        IAdPanelManagerService adPanelManagerService = ServiceManager.getService(IAdPanelManagerService.NAME, IAdPanelManagerService.class);
        return adPanelManagerService.getRemotePanel(page);
    }

    /**
     * Returns the data associated with the IDZone component.
     * 
     * @return an IDZone instance if the user is logged or null otherwise
     */
    public static IDZoneData getIDZoneData() {
        String loggedUser = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class).getUserSessionId(ctx());
        try {
            if (!StringUtils.isBlank(loggedUser)) {
                IUserAccount userAccount = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class).getUserAccountFromUid(loggedUser);
                if (userAccount != null) {
                    IDZoneData idZoneData = new IDZoneData();
                    idZoneData.isAuthorized = userAccount.isActive();
                    idZoneData.hasNotifications = ServiceManager.getService(INotificationManagerPlugin.NAME, INotificationManagerPlugin.class)
                            .hasNotifications(loggedUser);
                    idZoneData.nbNotReadNotifications = ServiceManager.getService(INotificationManagerPlugin.NAME, INotificationManagerPlugin.class)
                            .nbNotReadNotifications(loggedUser);
                    idZoneData.notifications = ServiceManager.getService(INotificationManagerPlugin.NAME, INotificationManagerPlugin.class)
                            .getNotReadNotificationsForUid(loggedUser);
                    idZoneData.hasMessages = ServiceManager.getService(INotificationManagerPlugin.NAME, INotificationManagerPlugin.class).hasMessages(
                            loggedUser);
                    idZoneData.nbNotReadMessages = ServiceManager.getService(INotificationManagerPlugin.NAME, INotificationManagerPlugin.class)
                            .nbNotReadMessages(loggedUser);
                    idZoneData.messages = ServiceManager.getService(INotificationManagerPlugin.NAME, INotificationManagerPlugin.class)
                            .getNotReadMessagesForUid(loggedUser);
                    idZoneData.login = userAccount.getFirstName() + " " + userAccount.getLastName();

                    IPluginManagerService pluginManagerService = ServiceManager.getService(IPluginManagerService.NAME, IPluginManagerService.class);
                    idZoneData.pluginMenuDesriptors = new HashMap<Long, Pair<String, IPluginMenuDescriptor>>();
                    Map<Long, IPluginInfo> pluginInfos = pluginManagerService.getRegisteredPluginDescriptors();
                    for (Long pluginConfigirationId : pluginInfos.keySet()) {
                        if (pluginInfos.get(pluginConfigirationId).getStaticDescriptor().getMenuDescriptor() != null) {
                            IPluginInfo pluginInfo = pluginInfos.get(pluginConfigirationId);
                            idZoneData.pluginMenuDesriptors.put(pluginConfigirationId, Pair.of(
                                    pluginInfo.getStaticDescriptor().getPluginDefinitionIdentifier(), pluginInfo.getStaticDescriptor().getMenuDescriptor()));
                        }
                    }

                    idZoneData.logoutUrl = controllers.sso.routes.Authenticator.logout().url();
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
}
