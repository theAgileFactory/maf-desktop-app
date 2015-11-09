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
package utils.table;

import java.text.MessageFormat;
import java.util.Date;

import constants.IMafConstants;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Table.ColumnDef.SorterType;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ObjectFormatter;
import models.framework_models.account.Notification;
import models.framework_models.account.Principal;

/**
 * A message list view is used to display a message row in a table.
 * 
 * @author Johann Kohler
 */
public class MessageListView {

    public static Table<MessageListView> templateTable = new Table<MessageListView>() {
        {

            this.setIdFieldName("id");

            this.addColumn("creationDate", "creationDate", "object.message.date.label", SorterType.DATE_SORTER);
            setJavaColumnFormatter("creationDate", new IColumnFormatter<MessageListView>() {
                @Override
                public String apply(MessageListView messageListView, Object value) {
                    DateFormatter<MessageListView> df = new DateFormatter<MessageListView>();
                    return strongify(messageListView, df.apply(messageListView, value));
                }
            });
            setColumnCssClass("creationDate", IMafConstants.BOOTSTRAP_COLUMN_2);

            this.addColumn("senderPrincipal", "senderPrincipal", "object.message.sender.label", SorterType.STRING_SORTER);
            setJavaColumnFormatter("senderPrincipal", new IColumnFormatter<MessageListView>() {
                @Override
                public String apply(MessageListView messageListView, Object value) {
                    try {
                        IUserAccount userAccount = messageListView.accountManagerPlugin.getUserAccountFromUid(messageListView.senderPrincipal.uid);
                        return strongify(messageListView, userAccount.getFirstName() + " " + userAccount.getLastName());
                    } catch (AccountManagementException e) {
                        return strongify(messageListView, messageListView.senderPrincipal.uid);
                    }

                }
            });
            setColumnCssClass("senderPrincipal", IMafConstants.BOOTSTRAP_COLUMN_2);

            this.addColumn("title", "title", "object.message.title.label", SorterType.STRING_SORTER);
            setJavaColumnFormatter("title", new IColumnFormatter<MessageListView>() {
                @Override
                public String apply(MessageListView messageListView, Object value) {
                    return strongify(messageListView, messageListView.title);
                }
            });
            setColumnCssClass("title", IMafConstants.BOOTSTRAP_COLUMN_2);

            this.addColumn("message", "message", "object.message.message.label", SorterType.NONE);
            setJavaColumnFormatter("message", new ObjectFormatter<MessageListView>());
            setColumnCssClass("message", IMafConstants.BOOTSTRAP_COLUMN_4);

            addColumn("maskAsReadLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("maskAsReadLink", new IColumnFormatter<MessageListView>() {
                @Override
                public String apply(MessageListView messageListView, Object value) {
                    if (!messageListView.isRead) {
                        String message = "<a href=\"%s\"><span class=\"glyphicons glyphicons-check\"></span></a>";
                        String url = controllers.routes.Application.markNotificationAsRead(messageListView.id).url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, message).body();
                    } else {
                        return "";
                    }
                }
            });
            setColumnCssClass("maskAsReadLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("maskAsReadLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<MessageListView>() {
                @Override
                public String apply(MessageListView messageListView, Object value) {
                    String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                            Msg.get("default.delete.confirmation.message"));
                    String url = controllers.routes.Application.deleteNotification(messageListView.id).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            setEmptyMessageKey("object.message.table.empty");

        }
    };

    /**
     * Strongify a string if needed.
     * 
     * @param messageListView
     *            the concerned row
     * @param in
     *            the string input
     */
    public static String strongify(MessageListView messageListView, String in) {
        if (!messageListView.isRead) {
            return "<strong>" + in + "</strong>";
        }
        return in;
    }

    public Long id;
    public boolean isRead;
    public Date creationDate;
    public Principal senderPrincipal;
    public String title;
    public String message;

    public IAccountManagerPlugin accountManagerPlugin;

    /**
     * Default constructor.
     * 
     * @param accountManagerPlugin
     *            the account manager service
     * @param notification
     *            the notification
     */
    public MessageListView(IAccountManagerPlugin accountManagerPlugin, Notification notification) {
        this.id = notification.id;
        this.isRead = notification.isRead;
        this.creationDate = notification.creationDate;
        this.senderPrincipal = notification.senderPrincipal;
        this.title = notification.title;
        this.message = notification.message;

        this.accountManagerPlugin = accountManagerPlugin;
    }

}
