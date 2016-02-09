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

import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.timesheet.TimesheetActivity;
import models.timesheet.TimesheetActivityType;

/**
 * A timesheet activity list view is used to display a timesheet activity row in
 * a table.
 * 
 * @author Johann Kohler
 */
public class TimesheetActivityListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<TimesheetActivityListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<TimesheetActivityListView> getTable() {
            return new Table<TimesheetActivityListView>() {
                {
                    setIdFieldName("id");

                    addColumn("name", "name", "object.timesheet_activity.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<TimesheetActivityListView>());

                    addColumn("description", "description", "object.timesheet_activity.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<TimesheetActivityListView>());

                    addColumn("type", "type", "object.timesheet_activity.type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("type", new IColumnFormatter<TimesheetActivityListView>() {
                        @Override
                        public String apply(TimesheetActivityListView timesheetActivityListView, Object value) {
                            return views.html.framework_views.parts.formats.display_value_holder.render(timesheetActivityListView.type, true).body();
                        }
                    });

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<TimesheetActivityListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<TimesheetActivityListView>() {
                        @Override
                        public String convert(TimesheetActivityListView timesheetActivityListView) {
                            return controllers.admin.routes.ConfigurationTimesheetActivityController.manageTimesheetActivity(timesheetActivityListView.id)
                                    .url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<TimesheetActivityListView>() {
                        @Override
                        public String apply(TimesheetActivityListView timesheetActivityListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationTimesheetActivityController
                                    .deleteTimesheetActivity(timesheetActivityListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.timesheet_activity.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public TimesheetActivityListView() {
    }

    public Long id;

    public String name;

    public String description;

    public TimesheetActivityType type;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param timesheetActivity
     *            the timesheet activity in the DB
     */
    public TimesheetActivityListView(TimesheetActivity timesheetActivity) {

        this.id = timesheetActivity.id;
        this.type = timesheetActivity.timesheetActivityType;
        this.name = timesheetActivity.getName();
        this.description = timesheetActivity.getDescription();

    }
}
