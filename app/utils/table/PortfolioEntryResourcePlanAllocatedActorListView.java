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

import constants.IMafConstants;
import dao.pmo.PortfolioEntryDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.*;
import framework.utils.formats.*;
import models.finance.Currency;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocationStatusType;
import models.pmo.Actor;
import models.pmo.PortfolioEntryPlanningPackage;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

/**
 * A portfolio entry resource plan allocated actor list view is used to display
 * a portfolio entry allocated actor in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedActorListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> filterConfig;
        public Table<PortfolioEntryResourcePlanAllocatedActorListView> templateTable;

        /**
         * Default constructor.
         * 
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */
        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.filterConfig = getFilterConfig();
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the filter config.
         */
        public FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> getFilterConfig() {
            return new FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView>() {
                {

                    String[] actorFieldsSort = { "actor.lastName", "actor.firstName" };
                    addColumnConfiguration("actor", "actor.id", "object.allocated_resource.actor.label",
                            new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url(), actorFieldsSort), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("portfolioEntryName",
                            "portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.name",
                            "object.allocated_resource.portfolio_entry.label", new TextFieldFilterComponent("*"), true, false, SortStatusType.NONE);

                    addColumnConfiguration("currency", "currency.id", "object.allocated_resource.currency.label",
                            new FilterConfig.AutocompleteFilterComponent(controllers.routes.JsonController.currency().url()), false, false, FilterConfig.SortStatusType.UNSORTED);

                    addColumnConfiguration("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label",
                            new FilterConfig.NumericFieldFilterComponent("0", "="), false, false, FilterConfig.SortStatusType.UNSORTED);

                    addColumnConfiguration("forecastDays", "forecastDays", "object.allocated_resource.forecast_days.label",
                            new FilterConfig.NumericFieldFilterComponent("0", "="), false, false, FilterConfig.SortStatusType.UNSORTED);

                    addColumnConfiguration("forecastDailyRate", "forecastDailyRate", "object.allocated_resource.forecast_daily_rate.label",
                            new FilterConfig.NumericFieldFilterComponent("0", "="), false, false, FilterConfig.SortStatusType.UNSORTED);

                    addColumnConfiguration("days", "days", "object.allocated_resource.days.label", new NumericFieldFilterComponent("0", "="), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("planningPackage", "portfolioEntryPlanningPackage.name", "object.allocated_resource.package.label",
                            new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("followPackageDates", "followPackageDates", "object.allocated_resource.follow_package_dates.label",
                            new CheckboxFilterComponent(true), false, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("startDate", "startDate", "object.allocated_resource.start_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("endDate", "endDate", "object.allocated_resource.end_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.ASC);

                    addColumnConfiguration("portfolioEntryResourcePlanAllocationStatusType", "portfolioEntryResourcePlanAllocationStatusType", "object.allocated_resource.portfolio_entry_resource_plan_allocation_status_type.label", new NoneFilterComponent(),
                            true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("lastStatusTypeUpdateActor", "lastStatusTypeUpdateActor.id", "object.allocated_resource.last_update_status_type_actor.label",
                            new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url(), new String[]{"lastStatusTypeUpdateActor.lastName", "lastStatusTypeUpdateActor.firstName"}), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("lastStatusTypeUpdateTime", "lastStatusTypeUpdateTime", "object.allocated_resource.last_update_status_type_time.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);

                    addCustomAttributesColumns("id", PortfolioEntryResourcePlanAllocatedActor.class);
                }
            };
        }

        /**
         * Get the table.
         * 
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */
        public Table<PortfolioEntryResourcePlanAllocatedActorListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryResourcePlanAllocatedActorListView>() {
                {
                    setIdFieldName("id");

                    addColumn("actor", "actor", "object.allocated_resource.actor.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("actor", (portfolioEntryResourcePlanAllocatedActorListView, value) -> views.html.modelsparts.display_actor.render(portfolioEntryResourcePlanAllocatedActorListView.actor).body());
                    setColumnValueCssClass("actor", "rowlink-skip");

                    addColumn("portfolioEntryName", "portfolioEntryName", "object.allocated_resource.portfolio_entry.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryName", new ObjectFormatter<>());

                    addColumn("currency", "currency", "object.allocated_resource.currency.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("currency", new ObjectFormatter<>());

                    addSummableColumn("days", "days", "object.allocated_resource.days.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("days", new NumberFormatter<>());
                    setColumnHeaderCssClass("days", "text-right");
                    setColumnValueCssClass("days", "text-right");

                    addColumn("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("dailyRate", new NumberFormatter<>());

                    addSummableColumn("forecastDays", "forecastDays", "object.allocated_resource.forecast_days.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("forecastDays", new NumberFormatter<>());
                    setColumnHeaderCssClass("forecastDays", "text-right");
                    setColumnValueCssClass("forecastDays", "text-right");

                    addColumn("forecastDailyRate", "forecastDailyRate", "object.allocated_resource.forecast_daily_rate.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("forecastDailyRate", new NumberFormatter<>());

                    addColumn("planningPackage", "planningPackage", "object.allocated_resource.package.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("planningPackage", (portfolioEntryResourcePlanAllocatedActorListView, value) -> views.html.modelsparts.display_portfolio_entry_planning_package
                            .render(portfolioEntryResourcePlanAllocatedActorListView.planningPackage).body());
                    setColumnValueCssClass("planningPackage", "rowlink-skip");

                    addColumn("followPackageDates", "followPackageDates", "object.allocated_resource.follow_package_dates.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("followPackageDates", new BooleanFormatter<>());

                    addColumn("startDate", "startDate", "object.allocated_resource.start_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("startDate", new DateFormatter<>());

                    addColumn("endDate", "endDate", "object.allocated_resource.end_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("endDate", new DateFormatter<>());

                    addColumn("portfolioEntryResourcePlanAllocationStatusType", "portfolioEntryResourcePlanAllocationStatusType.status", "object.allocated_resource.portfolio_entry_resource_plan_allocation_status_type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryResourcePlanAllocationStatusType", (value, cellValue) -> views.html.modelsparts.display_allocation_status.render(value.portfolioEntryResourcePlanAllocationStatusType).body());

                    addColumn("lastStatusTypeUpdateActor", "lastStatusTypeUpdateActor", "object.allocated_resource.last_update_status_type_actor.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lastStatusTypeUpdateActor", (portfolioEntryResourcePlanAllocatedActorListView, value) -> views.html.modelsparts.display_actor.render(portfolioEntryResourcePlanAllocatedActorListView.lastStatusTypeUpdateActor).body());
                    setColumnValueCssClass("lastStatusTypeUpdateActor", "rowlink-skip");

                    addColumn("lastStatusTypeUpdateTime", "lastStatusTypeUpdateTime", "object.allocated_resource.last_update_status_type_time.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lastStatusTypeUpdateTime", new DateFormatter<>());

                    addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntryResourcePlanAllocatedActor.class);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<>(
                            IMafConstants.EDIT_URL_FORMAT, portfolioEntryResourcePlanAllocatedActorListView -> controllers.core.routes.PortfolioEntryPlanningController
                            .manageAllocatedActor(portfolioEntryResourcePlanAllocatedActorListView.portfolioEntryId,
                                    portfolioEntryResourcePlanAllocatedActorListView.id)
                            .url()));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", (portfolioEntryResourcePlanAllocatedActorListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = controllers.core.routes.PortfolioEntryPlanningController
                                .deleteAllocatedActor(portfolioEntryResourcePlanAllocatedActorListView.portfolioEntryId,
                                        portfolioEntryResourcePlanAllocatedActorListView.id)
                                .url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    setEmptyMessageKey("object.allocated_resource.actor.table.empty");

                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryResourcePlanAllocatedActorListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public String portfolioEntryName;

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

    /**
     * Construct a list view with a DB entry.
     * 
     * @param allocatedActor
     *            the allocated actor in the DB
     */
    public PortfolioEntryResourcePlanAllocatedActorListView(PortfolioEntryResourcePlanAllocatedActor allocatedActor) {
        this.id = allocatedActor.id;
        this.portfolioEntryId = allocatedActor.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.portfolioEntryName = PortfolioEntryDao.getPEById(this.portfolioEntryId).getName();
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

}
