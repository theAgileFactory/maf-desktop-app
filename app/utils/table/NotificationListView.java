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

import java.util.Date;

import constants.IMafConstants;
import framework.utils.FilterConfig;
import framework.utils.FilterConfig.SortStatusType;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.Table.ColumnDef.SorterType;
import framework.utils.Utilities;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.framework_models.account.Notification;
import models.framework_models.account.NotificationCategory;

/**
 * A notification list view is used to display a notification row in a table.
 * 
 * @author Johann Kohler
 */
public class NotificationListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public FilterConfig<NotificationListView> filterConfig;
        public Table<NotificationListView> templateTable;

        public TableDefinition() {
            this.filterConfig = getFilterConfig();
            this.templateTable = getTable();
        }

        /**
         * Get the filter config.
         */
        public FilterConfig<NotificationListView> getFilterConfig() {
            return new FilterConfig<NotificationListView>() {
                {

                    addColumnConfiguration("isRead", "isRead", "object.notification.is_read.label", new CheckboxFilterComponent(false), false, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("creationDate", "creationDate", "object.notification.date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.DESC);

                    addColumnConfiguration("title", "title", "object.notification.title.label", new TextFieldFilterComponent("*"), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("message", "message", "object.notification.message.label", new NoneFilterComponent(), true, false,
                            SortStatusType.NONE);
                }
            };
        }

        public Table<NotificationListView> getTable() {
            return new Table<NotificationListView>() {
                {

                    this.setIdFieldName("id");

                    this.addColumn("isRead", "isRead", "object.notification.is_read.label", SorterType.NONE);
                    setJavaColumnFormatter("isRead", new BooleanFormatter<NotificationListView>());
                    setColumnCssClass("isRead", IMafConstants.BOOTSTRAP_COLUMN_1);

                    this.addColumn("creationDate", "creationDate", "object.notification.date.label", SorterType.NONE);
                    setJavaColumnFormatter("creationDate", new IColumnFormatter<NotificationListView>() {
                        @Override
                        public String apply(NotificationListView notification, Object value) {
                            DateFormatter<NotificationListView> df = new DateFormatter<NotificationListView>();
                            return strongify(notification, df.apply(notification, value));
                        }
                    });
                    setColumnCssClass("creationDate", IMafConstants.BOOTSTRAP_COLUMN_1);

                    this.addColumn("title", "title", "object.notification.title.label", SorterType.NONE);
                    setJavaColumnFormatter("title", new IColumnFormatter<NotificationListView>() {
                        @Override
                        public String apply(NotificationListView notification, Object value) {
                            String r = "";
                            if (notification.notificationCategory != null) {
                                r += "<span class='" + notification.notificationCategory.bootstrapGlyphicon + "'></span> ";
                            }
                            r += notification.title;
                            return strongify(notification, r);
                        }
                    });
                    setColumnCssClass("title", IMafConstants.BOOTSTRAP_COLUMN_3);

                    this.addColumn("message", "message", "object.notification.message.label", SorterType.NONE);
                    setJavaColumnFormatter("message", new ObjectFormatter<NotificationListView>());
                    setColumnCssClass("message", IMafConstants.BOOTSTRAP_COLUMN_6);

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new StringFormatFormatter<NotificationListView>(IMafConstants.DELETE_URL_FORMAT,
                            new StringFormatFormatter.Hook<NotificationListView>() {
                        @Override
                        public String convert(NotificationListView notification) {
                            return controllers.routes.Application.deleteNotification(notification.id).url();
                        }
                    }));
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    this.setLineAction(new IColumnFormatter<NotificationListView>() {
                        @Override
                        public String apply(NotificationListView notification, Object value) {
                            return controllers.routes.Application.redirectForNotification(notification.id).url();
                        }
                    });

                    setEmptyMessageKey("object.notification.table.empty");

                }
            };

        }

    }

    /**
     * Strongify a string if needed.
     * 
     * @param notification
     *            the concerned notification
     * @param in
     *            the string input
     */
    public static String strongify(NotificationListView notification, String in) {
        if (!notification.isRead) {
            return "<strong>" + in + "</strong>";
        }
        return in;
    }

    public Long id;

    public boolean isRead;

    public Date creationDate;

    public String title;

    public String message;

    public NotificationCategory notificationCategory;

    /**
     * Default constructor.
     * 
     * @param notification
     *            the notification in the DB
     */
    public NotificationListView(Notification notification) {
        this.id = notification.id;
        this.isRead = notification.isRead;
        this.creationDate = notification.creationDate;
        this.title = notification.getTitle();
        this.message = notification.message;
        this.notificationCategory = notification.notificationCategory;
    }

}
