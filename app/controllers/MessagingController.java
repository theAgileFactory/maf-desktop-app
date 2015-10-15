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
import java.util.List;

import javax.inject.Inject;

import models.framework_models.account.Notification;
import models.framework_models.account.Principal;
import models.framework_models.parent.IModelConstants;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import utils.table.MessageListView;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Table;
import framework.utils.Utilities;

/**
 * Messaging controller.
 * 
 * @author Pierre-Yves Cloux
 */
@SubjectPresent
public class MessagingController extends Controller {
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private INotificationManagerPlugin notificationManagerPlugin;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;
    
    private static Logger.ALogger log = Logger.of(MessagingController.class);
    private static Form<NotificationMessage> notificationMessageForm = Form.form(NotificationMessage.class);

    /**
     * The messaging page (list of all message, form to send a message).
     */
    @SubjectPresent
    public Result index() {

        String loggedUser = getUserSessionManagerPlugin().getUserSessionId(ctx());
        Table<Notification> messagesTables =
                MessageListView.templateTable.fill(getNotificationManagerPlugin().getMessagesForUid(loggedUser));

        NotificationMessage notificationMessage = createEmptyNotificationMessage();

        return ok(views.html.messaging.index.render(messagesTables, notificationMessageForm.fill(notificationMessage)));
    }

    /**
     * Send a notification message.
     */
    public Result sendMessage() {

        String loggedUser = getUserSessionManagerPlugin().getUserSessionId(ctx());
        Table<Notification> messagesTables =
                MessageListView.templateTable.fill(getNotificationManagerPlugin().getMessagesForUid(loggedUser));

        try {
            Form<NotificationMessage> boundForm = notificationMessageForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return ok(views.html.messaging.index.render(messagesTables, boundForm));
            }
            NotificationMessage notificationMessage = boundForm.get();
            getNotificationManagerPlugin().sendMessage(getUserSessionManagerPlugin().getUserSessionId(ctx()), notificationMessage.principalUids, notificationMessage.title,
                    notificationMessage.message);
            Utilities.sendSuccessFlashMessage(getI18nMessagesPlugin().get("messaging.send.success", notificationMessage.title));
            return redirect(routes.MessagingController.index());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Creates an empty notification message initialized with the current user
     * id as a sender id.
     */
    private static NotificationMessage createEmptyNotificationMessage() {
        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.principalUids = new ArrayList<String>();
        return notificationMessage;
    }

    /**
     * A class which holds a notification message sent manually by the
     * administrator.<br/>
     * <ul>
     * <li><b>senderUid</b> : the uid of the sender of the notification</li>
     * <li><b>message</b> : the message to be sent as a notification to the
     * specified principals</li>
     * <li><b>principalUids</b> : a list of {@link Principal} uid to which the
     * message must be sent</li>
     * </ul>
     * 
     * @author Pierre-Yves Cloux
     */
    public static class NotificationMessage {

        @Required
        @MaxLength(value = IModelConstants.MEDIUM_STRING)
        public String title;

        @MaxLength(value = IModelConstants.LARGE_STRING, message = "object.message.message.invalid")
        public String message;

        @Required
        public List<String> principalUids;
    }

    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    private INotificationManagerPlugin getNotificationManagerPlugin() {
        return notificationManagerPlugin;
    }

    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    private Configuration getConfiguration() {
        return configuration;
    }
}
