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
import models.timesheet.TimesheetActivityType;

/**
 * A timesheet activity type list view is used to display a timesheet activity
 * type row in a table.
 * 
 * @author Johann Kohler
 */
public class TimesheetActivityTypeListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<TimesheetActivityTypeListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<TimesheetActivityTypeListView> getTable() {
            return new Table<TimesheetActivityTypeListView>() {
                {
                    setIdFieldName("id");

                    addColumn("name", "name", "object.timesheet_activity_type.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<TimesheetActivityTypeListView>());

                    addColumn("description", "description", "object.timesheet_activity_type.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<TimesheetActivityTypeListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<TimesheetActivityTypeListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<TimesheetActivityTypeListView>() {
                        @Override
                        public String convert(TimesheetActivityTypeListView timesheetActivityListView) {
                            return controllers.admin.routes.ConfigurationTimesheetActivityController.manageTimesheetActivityType(timesheetActivityListView.id)
                                    .url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<TimesheetActivityTypeListView>() {
                        @Override
                        public String apply(TimesheetActivityTypeListView timesheetActivityListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationTimesheetActivityController
                                    .deleteTimesheetActivityType(timesheetActivityListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.timesheet_activity_type.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public TimesheetActivityTypeListView() {
    }

    public Long id;

    public String name;

    public String description;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param timesheetActivityType
     *            the timesheet activity type in the DB
     */
    public TimesheetActivityTypeListView(TimesheetActivityType timesheetActivityType) {

        this.id = timesheetActivityType.id;
        this.name = timesheetActivityType.getName();
        this.description = timesheetActivityType.getDescription();

    }
}
