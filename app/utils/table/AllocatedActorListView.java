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

import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.FilterConfig;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import models.finance.Currency;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocationStatusType;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryPlanningPackage;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Guillaume Petit
 */
public class AllocatedActorListView {

    public static class TableDefinition {

        /**
         * Default constructor.
         *
         */
        public TableDefinition() {
        }

        public static void initFilterConfig(FilterConfig<? extends AllocatedActorListView> filterConfig) {
            filterConfig.addColumnConfiguration("currency", "currency.id", "object.allocated_resource.currency.label",
                    new FilterConfig.AutocompleteFilterComponent(controllers.routes.JsonController.currency().url()), false, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label",
                    new FilterConfig.NumericFieldFilterComponent("0", "="), false, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("forecastDays", "forecastDays", "object.allocated_resource.forecast_days.label",
                    new FilterConfig.NumericFieldFilterComponent("0", "="), false, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("forecastDailyRate", "forecastDailyRate", "object.allocated_resource.forecast_daily_rate.label",
                    new FilterConfig.NumericFieldFilterComponent("0", "="), false, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("days", "days", "object.allocated_resource.days.label", new FilterConfig.NumericFieldFilterComponent("0", "="), true, false,
                    FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("planningPackage", "portfolioEntryPlanningPackage.name", "object.allocated_resource.package.label",
                    new FilterConfig.TextFieldFilterComponent("*"), true, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("followPackageDates", "followPackageDates", "object.allocated_resource.follow_package_dates.label",
                    new FilterConfig.CheckboxFilterComponent(true), false, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("startDate", "startDate", "object.allocated_resource.start_date.label",
                    new FilterConfig.DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("endDate", "endDate", "object.allocated_resource.end_date.label",
                    new FilterConfig.DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, FilterConfig.SortStatusType.ASC);

            filterConfig.addColumnConfiguration("portfolioEntryResourcePlanAllocationStatusType", "portfolioEntryResourcePlanAllocationStatusType", "object.allocated_resource.portfolio_entry_resource_plan_allocation_status_type.label", new FilterConfig.NoneFilterComponent(),
                    true, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("lastStatusTypeUpdateActor", "lastStatusTypeUpdateActor.id", "object.allocated_resource.last_update_status_type_actor.label",
                    new FilterConfig.AutocompleteFilterComponent(controllers.routes.JsonController.manager().url(), new String[]{"lastStatusTypeUpdateActor.lastName", "lastStatusTypeUpdateActor.firstName"}), false, false,
                    FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addColumnConfiguration("lastStatusTypeUpdateTime", "lastStatusTypeUpdateTime", "object.allocated_resource.last_update_status_type_time.label",
                    new FilterConfig.DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), false, false, FilterConfig.SortStatusType.UNSORTED);

            filterConfig.addCustomAttributesColumns("id", PortfolioEntryResourcePlanAllocatedActor.class);
        }
    }

    public static void initTable(Table<? extends AllocatedActorListView> table, II18nMessagesPlugin i18nMessagesPlugin) {

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
        table.setJavaColumnFormatter("planningPackage", (portfolioEntryResourcePlanAllocatedActorListView, value) -> views.html.modelsparts.display_portfolio_entry_planning_package
                .render(portfolioEntryResourcePlanAllocatedActorListView.planningPackage).body());
        table.setColumnValueCssClass("planningPackage", "rowlink-skip");

        table.addColumn("followPackageDates", "followPackageDates", "object.allocated_resource.follow_package_dates.label",
                Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("followPackageDates", new BooleanFormatter<>());

        table.addColumn("startDate", "startDate", "object.allocated_resource.start_date.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("startDate", new DateFormatter<>());

        table.addColumn("endDate", "endDate", "object.allocated_resource.end_date.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("endDate", new DateFormatter<>());

        table.addColumn("portfolioEntryResourcePlanAllocationStatusType", "portfolioEntryResourcePlanAllocationStatusType.status", "object.allocated_resource.portfolio_entry_resource_plan_allocation_status_type.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("portfolioEntryResourcePlanAllocationStatusType", (value, cellValue) -> views.html.modelsparts.display_allocation_status.render(value.portfolioEntryResourcePlanAllocationStatusType).body());

        table.addColumn("lastStatusTypeUpdateActor", "lastStatusTypeUpdateActor", "object.allocated_resource.last_update_status_type_actor.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("lastStatusTypeUpdateActor", (portfolioEntryResourcePlanAllocatedActorListView, value) -> views.html.modelsparts.display_actor.render(portfolioEntryResourcePlanAllocatedActorListView.lastStatusTypeUpdateActor).body());
        table.setColumnValueCssClass("lastStatusTypeUpdateActor", "rowlink-skip");

        table.addColumn("lastStatusTypeUpdateTime", "lastStatusTypeUpdateTime", "object.allocated_resource.last_update_status_type_time.label", Table.ColumnDef.SorterType.NONE);
        table.setJavaColumnFormatter("lastStatusTypeUpdateTime", new DateFormatter<>());

        table.addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntryResourcePlanAllocatedActor.class);

        table.setEmptyMessageKey("object.allocated_resource.actor.table.empty");
    }

    public AllocatedActorListView() {
    }

    /**
     * Construct a list view with a DB entry.
     *
     * @param allocatedActor
     *            the allocated org unit in the DB
     */
    public AllocatedActorListView(PortfolioEntryResourcePlanAllocatedActor allocatedActor) {
        this.id = allocatedActor.id;
        this.portfolioEntry = allocatedActor.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry;
        this.actor = allocatedActor.actor;
        this.startDate = allocatedActor.startDate;
        this.endDate = allocatedActor.endDate;
        this.planningPackage = allocatedActor.portfolioEntryPlanningPackage;
        this.portfolioEntryResourcePlanAllocationStatusType = allocatedActor.portfolioEntryResourcePlanAllocationStatusType;
        this.lastStatusTypeUpdateActor = allocatedActor.lastStatusTypeUpdateActor;
        this.lastStatusTypeUpdateTime = allocatedActor.lastStatusTypeUpdateTime;
        this.followPackageDates = allocatedActor.followPackageDates;

        this.currency = allocatedActor.currency;
        this.days = allocatedActor.days;
        this.dailyRate = allocatedActor.dailyRate;
        this.forecastDays = allocatedActor.forecastDays;
        this.forecastDailyRate = allocatedActor.forecastDailyRate != null ? allocatedActor.forecastDailyRate : allocatedActor.dailyRate;
    }

    public Long id;

    public PortfolioEntry portfolioEntry;

    public Actor actor;

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

}