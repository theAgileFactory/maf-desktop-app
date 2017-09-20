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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import dao.pmo.OrgUnitDao;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.FilterConfig;
import framework.utils.IColumnFormatter;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.NumberFormatter;
import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.PortfolioEntryPlanningPackage;
import models.timesheet.TimesheetLog;
import models.timesheet.TimesheetReport;

/**
 * A timesheet log list view is used to display a timesheet log row in a table.
 * 
 * @author Johann Kohler
 */
public class TimesheetLogListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public FilterConfig<TimesheetLogListView> filterConfig;
        public Table<TimesheetLogListView> templateTable;

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
        public FilterConfig<TimesheetLogListView> getFilterConfig() {
            return new FilterConfig<TimesheetLogListView>() {
                {

                    addColumnConfiguration("actor", "timesheetEntry.timesheetReport.actor.id", "object.timesheet_report.actor.label",
                            new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url()), true, false, SortStatusType.NONE);

                    ISelectableValueHolderCollection<Long> orgUnits = OrgUnitDao.getOrgUnitActiveAsVH();
                    if (orgUnits != null && orgUnits.getValues().size() > 0) {
                        addColumnConfiguration("orgUnit", "timesheetEntry.timesheetReport.orgUnit.id", "object.timesheet_report.org_unit.label",
                                new SelectFilterComponent(orgUnits.getValues().iterator().next().getValue(), orgUnits), true, false, SortStatusType.NONE);
                    } else {
                        addColumnConfiguration("orgUnit", "timesheetEntry.timesheetReport.orgUnit.id", "object.timesheet_report.org_unit.label",
                                new NoneFilterComponent(), true, false, SortStatusType.NONE);
                    }

                    addColumnConfiguration("logDate", "logDate", "object.timesheet_log.log_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.ASC);

                    addColumnConfiguration("hours", "hours", "object.timesheet_log.hours.label", new NumericFieldFilterComponent("0", "="), true, false,
                            SortStatusType.UNSORTED);

                    ISelectableValueHolderCollection<String> statusVH = new DefaultSelectableValueHolderCollection<>();
                    for (TimesheetReport.Status status : TimesheetReport.Status.values()) {
                        statusVH.add(new DefaultSelectableValueHolder<>(status.name(),
                                Msg.get("object.timesheet_report.status." + status.name() + ".label")));
                    }
                    addColumnConfiguration("status", "timesheetEntry.timesheetReport.status", "object.timesheet_report.status.label",
                            new SelectFilterComponent(TimesheetReport.Status.APPROVED.name(), statusVH), true, false, SortStatusType.NONE);

                    addColumnConfiguration("planningPackage", "timesheetEntry.portfolioEntryPlanningPackage.name",
                            "object.timesheet_entry.planning_package.label", new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);

                }
            };
        }

        /**
         * Get the table.
         */
        public Table<TimesheetLogListView> getTable() {
            return new Table<TimesheetLogListView>() {
                {

                    setIdFieldName("id");

                    addColumn("actor", "actor", "object.timesheet_report.actor.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("actor", (timesheetLogListView, value) -> views.html.modelsparts.display_actor.render(timesheetLogListView.actor).body());
                    this.setColumnValueCssClass("actor", "rowlink-skip");

                    addColumn("orgUnit", "orgUnit", "object.timesheet_report.org_unit.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("orgUnit", (timesheetLogListView, value) -> views.html.modelsparts.display_org_unit.render(timesheetLogListView.orgUnit).body());
                    this.setColumnValueCssClass("orgUnit", "rowlink-skip");

                    addColumn("logDate", "logDate", "object.timesheet_log.log_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("logDate", new DateFormatter<>());

                    addSummableColumn("hours", "hours", "object.timesheet_log.hours.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("hours", new NumberFormatter<>());
                    setColumnHeaderCssClass("hours", "text-right");
                    setColumnValueCssClass("hours", "text-right");

                    addColumn("status", "status", "object.timesheet_report.status.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("status", (timesheetLogListView, value) -> "<span class=\"label label-" + timesheetLogListView.statusClass + "\">"
                            + Msg.get("object.timesheet_report.status." + timesheetLogListView.status.name() + ".label") + "</span>");

                    addColumn("planningPackage", "planningPackage", "object.timesheet_entry.planning_package.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("planningPackage", (timesheetLogListView, value) -> views.html.modelsparts.display_portfolio_entry_planning_package.render(timesheetLogListView.planningPackage).body());
                    this.setColumnValueCssClass("planningPackage", "rowlink-skip");

                    this.setLineAction((timesheetLogListView, value) -> {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        String stringDate = df.format(timesheetLogListView.startDate);
                        return controllers.core.routes.ActorController.viewWeeklyTimesheet(timesheetLogListView.actor.id, stringDate).url();
                    });

                    setEmptyMessageKey("object.timesheet_log.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public TimesheetLogListView() {
    }

    public Long id;

    public Double hours;
    public Date logDate;

    public PortfolioEntryPlanningPackage planningPackage;

    public Actor actor;
    public OrgUnit orgUnit;
    public TimesheetReport.Status status;
    public String statusClass;
    public Date startDate;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param timesheetLog
     *            the timesheet log in the DB
     */
    public TimesheetLogListView(TimesheetLog timesheetLog) {

        this.id = timesheetLog.id;

        this.hours = timesheetLog.hours;
        this.logDate = timesheetLog.logDate;

        this.planningPackage = timesheetLog.timesheetEntry.portfolioEntryPlanningPackage;

        this.actor = timesheetLog.timesheetEntry.timesheetReport.actor;
        this.orgUnit = timesheetLog.timesheetEntry.timesheetReport.orgUnit;
        this.status = timesheetLog.timesheetEntry.timesheetReport.status;
        this.statusClass = timesheetLog.timesheetEntry.timesheetReport.getStatusCssClass();
        this.startDate = timesheetLog.timesheetEntry.timesheetReport.startDate;

    }

}
