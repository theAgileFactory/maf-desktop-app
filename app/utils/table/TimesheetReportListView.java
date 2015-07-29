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

import java.text.SimpleDateFormat;

import models.pmo.Actor;
import models.timesheet.TimesheetReport;
import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * A timesheet report list view is used to display a timesheet report row in a
 * table.
 * 
 * @author Johann Kohler
 */
public class TimesheetReportListView {

    public static Table<TimesheetReportListView> templateTable = new Table<TimesheetReportListView>() {
        {
            setIdFieldName("id");

            addColumn("actor", "actor", "object.timesheet_report.actor.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("actor", new IColumnFormatter<TimesheetReportListView>() {
                @Override
                public String apply(TimesheetReportListView timesheetReportListView, Object value) {
                    return views.html.modelsparts.display_actor.render(timesheetReportListView.actor).body();
                }
            });
            setColumnCssClass("actor", IMafConstants.BOOTSTRAP_COLUMN_3);
            this.setColumnValueCssClass("actor", "rowlink-skip");

            addColumn("period", "period", "object.timesheet_report.period.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("period", new ObjectFormatter<TimesheetReportListView>());
            setColumnCssClass("period", IMafConstants.BOOTSTRAP_COLUMN_3);

            addColumn("hours", "hours", "object.timesheet_report.hours.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("hours", new NumberFormatter<TimesheetReportListView>());
            setColumnCssClass("hours", IMafConstants.BOOTSTRAP_COLUMN_3);

            addColumn("status", "status", "object.timesheet_report.status.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("status", new ObjectFormatter<TimesheetReportListView>());
            setColumnCssClass("status", IMafConstants.BOOTSTRAP_COLUMN_2);

            addColumn("approveActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("approveActionLink", new IColumnFormatter<TimesheetReportListView>() {
                @Override
                public String apply(TimesheetReportListView timesheetReportListView, Object value) {
                    return "<form action='" + controllers.core.routes.TimesheetController.processTimesheet().url() + "' method='POST'>"
                            + "<input type='hidden' name='comments' value='' />" + "<input type='hidden' name='id' value='" + timesheetReportListView.id
                            + "' />" + "<button type='submit' class='btn btn-default btn-xs' name='action' value='APPROVE'>"
                            + "<span class='glyphicons glyphicons-thumbs-up'></span> " + Msg.get("button.approve") + "</button>" + "</form>";
                }
            });
            setColumnCssClass("approveActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("approveActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            addColumn("reminderActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("reminderActionLink", new StringFormatFormatter<TimesheetReportListView>(
                    "<a href=\"%s\" data-toggle='tooltip' class='timesheet-reminder' title=\"" + Msg.get("object.timesheet_report.reminder.help") + "\">"
                            + "<span class=\"glyphicons glyphicons-bell\"></span></a><script>$('.timesheet-reminder').tooltip();</script>",
                    new StringFormatFormatter.Hook<TimesheetReportListView>() {
                        @Override
                        public String convert(TimesheetReportListView timesheetReportListView) {
                            return controllers.core.routes.TimesheetController.sendReminderTimesheet(timesheetReportListView.id).url();
                        }
                    }));
            setColumnCssClass("reminderActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("reminderActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            this.setLineAction(new IColumnFormatter<TimesheetReportListView>() {
                @Override
                public String apply(TimesheetReportListView timesheetReportListView, Object value) {
                    return controllers.core.routes.ActorController.viewWeeklyTimesheet(timesheetReportListView.actor.id, timesheetReportListView.stringDate)
                            .url();
                }
            });

            setEmptyMessageKey("object.timesheet_report.table.empty");

        }
    };

    /**
     * Default constructor.
     */
    public TimesheetReportListView() {
    }

    public Long id;

    public Actor actor;

    public String stringDate;

    public String period;

    public Double hours;

    public String status;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param timesheetReport
     *            the timesheet report in the DB
     */
    public TimesheetReportListView(TimesheetReport timesheetReport) {

        this.id = timesheetReport.id;

        this.actor = timesheetReport.actor;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.stringDate = sdf.format(timesheetReport.startDate);

        this.period =
                Msg.get("object.timesheet_report.period.value", Utilities.getDateFormat(null).format(timesheetReport.startDate), Utilities
                        .getDateFormat(null).format(timesheetReport.getEndDate()));

        this.hours = timesheetReport.getTotal();

        this.status =
                "<span class='label label-" + timesheetReport.getStatusCssClass() + "'>"
                        + Msg.get("object.timesheet_report.status." + timesheetReport.status.name() + ".label") + "</span>";

    }
}
