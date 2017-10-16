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

import controllers.routes;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.FilterConfig;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import models.finance.Currency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.finance.PortfolioEntryResourcePlanAllocationStatusType;
import models.pmo.Actor;
import models.pmo.PortfolioEntryPlanningPackage;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Generic class for allocated org unit table (used both in portfolio entry resources page and org unit allocations
 * page
 *
 * @author Guillaume Petit <guillaume.petit@sword-group.com>
 */
public class AllocatedOrgUnitListView {

    public static class TableDefinition {

        /**
         * Default constructor.
         *
         */
        public TableDefinition() {
        }

        public static void initFilterConfig(FilterConfig<? extends AllocatedOrgUnitListView> filterConfig) {

            String[] actorFieldsSort = { "lastStatusTypeUpdateActor.lastName", "lastStatusTypeUpdateActor.firstName" };
            filterConfig.addColumnConfiguration("currency", "currency", "object.allocated_resource.currency.label",
                    new FilterConfig.AutocompleteFilterComponent(routes.JsonController.currency().url()), false, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("portfolioEntryResourcePlanAllocationStatusType", "portfolioEntryResourcePlanAllocationStatusType", "object.allocated_resource.portfolio_entry_resource_plan_allocation_status_type.label", new FilterConfig.NoneFilterComponent(),
                    true, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("lastStatusTypeUpdateActor", "lastStatusTypeUpdateActor.id", "object.allocated_resource.last_update_status_type_actor.label",
                    new FilterConfig.AutocompleteFilterComponent(controllers.routes.JsonController.manager().url(), actorFieldsSort), false, false,
                    FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("lastStatusTypeUpdateTime", "lastStatusTypeUpdateTime", "object.allocated_resource.last_update_status_type_time.label",
                    new FilterConfig.DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), false, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("days", "days", "object.allocated_resource.days.label", new FilterConfig.NumericFieldFilterComponent("0", "="), true, false,
                    FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("planningPackage", "portfolioEntryPlanningPackage.name", "object.allocated_resource.package.label",
                    new FilterConfig.TextFieldFilterComponent("*"), true, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label",
                    new FilterConfig.NumericFieldFilterComponent("0", "="), false, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("forecastDays", "forecastDays", "object.allocated_resource.forecast_days.label",
                    new FilterConfig.NumericFieldFilterComponent("0", "="), false, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("forecastDailyRate", "forecastDailyRate", "object.allocated_resource.forecast_daily_rate.label",
                    new FilterConfig.NumericFieldFilterComponent("0", "="), false, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("startDate", "startDate", "object.allocated_resource.start_date.label",
                    new FilterConfig.DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("endDate", "endDate", "object.allocated_resource.end_date.label",
                    new FilterConfig.DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, FilterConfig.SortStatusType.ASC);

            filterConfig.addCustomAttributesColumns("id", PortfolioEntryResourcePlanAllocatedOrgUnit.class);
        }
    }

    public static void initTable(Table<? extends AllocatedOrgUnitListView> table, II18nMessagesPlugin i18nMessagesPlugin) {
        table.setIdFieldName("id");

        table.addColumn("currency", "currency", "object.allocated_resource.currency.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("currency", new ObjectFormatter<>());

        table.addSummableColumn("days", "days", "object.allocated_resource.days.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("days", new NumberFormatter<>());
        table.setColumnHeaderCssClass("days", "text-right");
        table.setColumnValueCssClass("days", "text-right");

        table.addColumn("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("dailyRate", new NumberFormatter<>());

        table.addSummableColumn("forecastDays", "forecastDays", "object.allocated_resource.forecast_days.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("forecastDays", new NumberFormatter<>());
        table.setColumnHeaderCssClass("forecastDays", "text-right");
        table.setColumnValueCssClass("forecastDays", "text-right");

        table.addColumn("forecastDailyRate", "forecastDailyRate", "object.allocated_resource.forecast_daily_rate.label",
                Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("forecastDailyRate", new NumberFormatter<>());

        table.addColumn("planningPackage", "planningPackage", "object.allocated_resource.package.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter(
                "planningPackage",
                (allocatedOrgUnitListView, value) ->
                        views.html.modelsparts.display_portfolio_entry_planning_package
                                .render(allocatedOrgUnitListView.planningPackage)
                                .body()
        );
        table.setColumnValueCssClass("planningPackage", "rowlink-skip");

        table.addColumn("startDate", "startDate", "object.allocated_resource.start_date.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("startDate", new DateFormatter<>());

        table.addColumn("endDate", "endDate", "object.allocated_resource.end_date.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("endDate", new DateFormatter<>());

        table.addColumn("portfolioEntryResourcePlanAllocationStatusType", "portfolioEntryResourcePlanAllocationStatusType.status", "object.allocated_resource.portfolio_entry_resource_plan_allocation_status_type.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("portfolioEntryResourcePlanAllocationStatusType", (allocatedOrgUnitListView, cellValue) -> views.html.modelsparts.display_allocation_status.render(allocatedOrgUnitListView.portfolioEntryResourcePlanAllocationStatusType).body());

        table.addColumn("lastStatusTypeUpdateActor", "lastStatusTypeUpdateActor", "object.allocated_resource.last_update_status_type_actor.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("lastStatusTypeUpdateActor", (allocatedOrgUnitListView, value) -> views.html.modelsparts.display_actor.render(allocatedOrgUnitListView.lastStatusTypeUpdateActor).body());
        table.setColumnValueCssClass("lastStatusTypeUpdateActor", "rowlink-skip");

        table.addColumn("lastStatusTypeUpdateTime", "lastStatusTypeUpdateTime", "object.allocated_resource.last_update_status_type_time.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("lastStatusTypeUpdateTime", new DateFormatter<>());

        table.addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntryResourcePlanAllocatedOrgUnit.class);

        table.setEmptyMessageKey("object.allocated_resource.org_unit.table.empty");
    }

    public Long id;

    public Long portfolioEntryId;

    public Currency currency;

    public BigDecimal days;

    public BigDecimal dailyRate;

    public BigDecimal forecastDays;

    public BigDecimal forecastDailyRate;

    public Date startDate;

    public Date endDate;

    public PortfolioEntryPlanningPackage planningPackage;

    public PortfolioEntryResourcePlanAllocationStatusType portfolioEntryResourcePlanAllocationStatusType;

    public Actor lastStatusTypeUpdateActor;

    public Date lastStatusTypeUpdateTime;

    public Boolean followPackageDates;

    public AllocatedOrgUnitListView() {
    }

    /**
     * Construct a list view with a DB entry.
     *
     * @param allocatedOrgUnit
     *            the allocated org unit in the DB
     */
    public AllocatedOrgUnitListView(PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit) {
        this.id = allocatedOrgUnit.id;
        this.portfolioEntryId = allocatedOrgUnit.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.startDate = allocatedOrgUnit.startDate;
        this.endDate = allocatedOrgUnit.endDate;
        this.planningPackage = allocatedOrgUnit.portfolioEntryPlanningPackage;
        this.portfolioEntryResourcePlanAllocationStatusType = allocatedOrgUnit.portfolioEntryResourcePlanAllocationStatusType;
        this.lastStatusTypeUpdateActor = allocatedOrgUnit.lastStatusTypeUpdateActor;
        this.lastStatusTypeUpdateTime = allocatedOrgUnit.lastStatusTypeUpdateTime;
        this.followPackageDates = allocatedOrgUnit.followPackageDates;

        this.currency = allocatedOrgUnit.currency;
        this.days = allocatedOrgUnit.days;
        this.dailyRate = allocatedOrgUnit.dailyRate;
        this.forecastDays = allocatedOrgUnit.forecastDays != null ? allocatedOrgUnit.forecastDays : allocatedOrgUnit.days;
        this.forecastDailyRate = allocatedOrgUnit.forecastDailyRate != null ? allocatedOrgUnit.forecastDailyRate : allocatedOrgUnit.dailyRate;
    }

}