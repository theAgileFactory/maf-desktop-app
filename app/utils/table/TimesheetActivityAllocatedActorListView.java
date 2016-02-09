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

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

import constants.IMafConstants;
import framework.utils.FilterConfig;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.pmo.Actor;
import models.timesheet.TimesheetActivity;
import models.timesheet.TimesheetActivityAllocatedActor;

/**
 * A timesheet activity allocated actor list view is used to display a an
 * allocated actor to a timesheet activity in a table.
 * 
 * @author Johann Kohler
 */
public class TimesheetActivityAllocatedActorListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public FilterConfig<TimesheetActivityAllocatedActorListView> filterConfig;
        public Table<TimesheetActivityAllocatedActorListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.filterConfig = getFilterConfig();
            this.templateTable = getTable();
        }

        /**
         * Get the filter config.
         */
        public FilterConfig<TimesheetActivityAllocatedActorListView> getFilterConfig() {
            return new FilterConfig<TimesheetActivityAllocatedActorListView>() {
                {
                    String[] actorFieldsSort = { "actor.lastName", "actor.firstName" };
                    addColumnConfiguration("actor", "actor.id", "object.allocated_resource.actor.label",
                            new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url(), actorFieldsSort), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("timesheetActivity", "timesheetActivity", "object.allocated_resource.timesheet_activity.label",
                            new NoneFilterComponent(), true, false, SortStatusType.NONE);

                    addColumnConfiguration("days", "days", "object.allocated_resource.days.label", new NumericFieldFilterComponent("0", "="), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("startDate", "startDate", "object.allocated_resource.start_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("endDate", "endDate", "object.allocated_resource.end_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.ASC);

                    addCustomAttributesColumns("id", TimesheetActivityAllocatedActor.class);
                }
            };
        }

        /**
         * Get the table.
         */
        public Table<TimesheetActivityAllocatedActorListView> getTable() {
            return new Table<TimesheetActivityAllocatedActorListView>() {
                {
                    setIdFieldName("id");

                    addColumn("actor", "actor", "object.allocated_resource.actor.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("actor", new IColumnFormatter<TimesheetActivityAllocatedActorListView>() {
                        @Override
                        public String apply(TimesheetActivityAllocatedActorListView allocatedActorListView, Object value) {
                            return views.html.modelsparts.display_actor.render(allocatedActorListView.actor).body();
                        }
                    });
                    setColumnValueCssClass("actor", "rowlink-skip");

                    addColumn("timesheetActivity", "timesheetActivity", "object.allocated_resource.timesheet_activity.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("timesheetActivity", new IColumnFormatter<TimesheetActivityAllocatedActorListView>() {
                        @Override
                        public String apply(TimesheetActivityAllocatedActorListView allocatedActorListView, Object value) {
                            return views.html.framework_views.parts.formats.display_object.render(allocatedActorListView.timesheetActivity.getName(), false)
                                    .body();
                        }
                    });

                    addColumn("days", "days", "object.allocated_resource.days.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("days", new NumberFormatter<TimesheetActivityAllocatedActorListView>());

                    addColumn("startDate", "startDate", "object.allocated_resource.start_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("startDate", new DateFormatter<TimesheetActivityAllocatedActorListView>());

                    addColumn("endDate", "endDate", "object.allocated_resource.end_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("endDate", new DateFormatter<TimesheetActivityAllocatedActorListView>());

                    addCustomAttributeColumns(TimesheetActivityAllocatedActor.class);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<TimesheetActivityAllocatedActorListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<TimesheetActivityAllocatedActorListView>() {
                        @Override
                        public String convert(TimesheetActivityAllocatedActorListView allocatedActorListView) {
                            return controllers.core.routes.ActorController.manageAllocatedActivity(allocatedActorListView.actor.id, allocatedActorListView.id)
                                    .url();
                        }
                    }));
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", new IColumnFormatter<TimesheetActivityAllocatedActorListView>() {
                        @Override
                        public String apply(TimesheetActivityAllocatedActorListView allocatedActorListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.core.routes.ActorController
                                    .deleteAllocatedActivity(allocatedActorListView.actor.id, allocatedActorListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    setEmptyMessageKey("object.allocated_resource.actor.table.empty");

                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public TimesheetActivityAllocatedActorListView() {
    }

    public Long id;

    public TimesheetActivity timesheetActivity;

    public Actor actor;

    public BigDecimal days;

    public Date startDate;

    public Date endDate;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param allocatedActor
     *            the allocated actor in the DB
     */
    public TimesheetActivityAllocatedActorListView(TimesheetActivityAllocatedActor allocatedActor) {
        this.id = allocatedActor.id;
        this.timesheetActivity = allocatedActor.timesheetActivity;
        this.actor = allocatedActor.actor;
        this.days = allocatedActor.days;
        this.startDate = allocatedActor.startDate;
        this.endDate = allocatedActor.endDate;
    }

}
