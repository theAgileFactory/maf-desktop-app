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
import dao.pmo.PortfolioEntryDao;
import framework.utils.FilterConfig;
import framework.utils.FilterConfig.SortStatusType;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.pmo.Actor;
import models.pmo.PortfolioEntryPlanningPackage;

/**
 * A portfolio entry resource plan allocated actor list view is used to display
 * a portfolio entry allocated actor in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedActorListView {

    public static FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> filterConfig = getFilterConfig();

    /**
     * Get the filter config.
     */
    public static FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> getFilterConfig() {
        return new FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView>() {
            {

                String[] actorFieldsSort = { "actor.lastName", "actor.firstName" };
                addColumnConfiguration("actor", "actor.id", "object.allocated_resource.actor.label",
                        new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url(), actorFieldsSort), true, false,
                        SortStatusType.UNSORTED);

                addColumnConfiguration("portfolioEntryName", "portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.name",
                        "object.allocated_resource.portfolio_entry.label", new TextFieldFilterComponent("*"), true, false, SortStatusType.NONE);

                addColumnConfiguration("days", "days", "object.allocated_resource.days.label", new NumericFieldFilterComponent("0", "="), true, false,
                        SortStatusType.UNSORTED);

                addColumnConfiguration("forecastDays", "forecastDays", "object.allocated_resource.forecast_days.label",
                        new NumericFieldFilterComponent("0", "="), true, false, SortStatusType.UNSORTED);

                addColumnConfiguration("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label", new NumericFieldFilterComponent("0", "="),
                        true, false, SortStatusType.UNSORTED);

                addColumnConfiguration("planningPackage", "portfolioEntryPlanningPackage.name", "object.allocated_resource.package.label",
                        new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);

                addColumnConfiguration("followPackageDates", "followPackageDates", "object.allocated_resource.follow_package_dates.label",
                        new CheckboxFilterComponent(true), false, false, SortStatusType.UNSORTED);

                addColumnConfiguration("startDate", "startDate", "object.allocated_resource.start_date.label",
                        new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);

                addColumnConfiguration("endDate", "endDate", "object.allocated_resource.end_date.label",
                        new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.ASC);

                addColumnConfiguration("isConfirmed", "isConfirmed", "object.allocated_resource.is_confirmed.label", new CheckboxFilterComponent(true), true,
                        false, SortStatusType.UNSORTED);

                addCustomAttributesColumns("id", PortfolioEntryResourcePlanAllocatedActor.class);

            }
        };
    }

    public static Table<PortfolioEntryResourcePlanAllocatedActorListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<PortfolioEntryResourcePlanAllocatedActorListView> getTable() {
        return new Table<PortfolioEntryResourcePlanAllocatedActorListView>() {
            {
                setIdFieldName("id");

                addColumn("actor", "actor", "object.allocated_resource.actor.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("actor", new IColumnFormatter<PortfolioEntryResourcePlanAllocatedActorListView>() {
                    @Override
                    public String apply(PortfolioEntryResourcePlanAllocatedActorListView portfolioEntryResourcePlanAllocatedActorListView, Object value) {
                        return views.html.modelsparts.display_actor.render(portfolioEntryResourcePlanAllocatedActorListView.actor).body();
                    }
                });
                setColumnValueCssClass("actor", "rowlink-skip");

                addColumn("portfolioEntryName", "portfolioEntryName", "object.allocated_resource.portfolio_entry.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("portfolioEntryName", new ObjectFormatter<PortfolioEntryResourcePlanAllocatedActorListView>());

                addColumn("days", "days", "object.allocated_resource.days.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("days", new NumberFormatter<PortfolioEntryResourcePlanAllocatedActorListView>());

                addColumn("forecastDays", "forecastDays", "object.allocated_resource.forecast_days.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("forecastDays", new NumberFormatter<PortfolioEntryResourcePlanAllocatedActorListView>());

                addColumn("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("dailyRate", new NumberFormatter<PortfolioEntryResourcePlanAllocatedActorListView>());

                addColumn("planningPackage", "planningPackage", "object.allocated_resource.package.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("planningPackage", new IColumnFormatter<PortfolioEntryResourcePlanAllocatedActorListView>() {
                    @Override
                    public String apply(PortfolioEntryResourcePlanAllocatedActorListView portfolioEntryResourcePlanAllocatedActorListView, Object value) {
                        return views.html.modelsparts.display_portfolio_entry_planning_package
                                .render(portfolioEntryResourcePlanAllocatedActorListView.planningPackage).body();
                    }
                });
                setColumnValueCssClass("planningPackage", "rowlink-skip");

                addColumn("followPackageDates", "followPackageDates", "object.allocated_resource.follow_package_dates.label",
                        Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("followPackageDates", new BooleanFormatter<PortfolioEntryResourcePlanAllocatedActorListView>());

                addColumn("startDate", "startDate", "object.allocated_resource.start_date.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("startDate", new DateFormatter<PortfolioEntryResourcePlanAllocatedActorListView>());

                addColumn("endDate", "endDate", "object.allocated_resource.end_date.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("endDate", new DateFormatter<PortfolioEntryResourcePlanAllocatedActorListView>());

                addColumn("isConfirmed", "isConfirmed", "object.allocated_resource.is_confirmed.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isConfirmed", new BooleanFormatter<PortfolioEntryResourcePlanAllocatedActorListView>());

                addCustomAttributeColumns(PortfolioEntryResourcePlanAllocatedActor.class);

                addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryResourcePlanAllocatedActorListView>(
                        IMafConstants.EDIT_URL_FORMAT, new StringFormatFormatter.Hook<PortfolioEntryResourcePlanAllocatedActorListView>() {
                    @Override
                    public String convert(PortfolioEntryResourcePlanAllocatedActorListView portfolioEntryResourcePlanAllocatedActorListView) {
                        return controllers.core.routes.PortfolioEntryPlanningController.manageAllocatedActor(
                                portfolioEntryResourcePlanAllocatedActorListView.portfolioEntryId, portfolioEntryResourcePlanAllocatedActorListView.id).url();
                    }
                }));
                setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("removeActionLink", new IColumnFormatter<PortfolioEntryResourcePlanAllocatedActorListView>() {
                    @Override
                    public String apply(PortfolioEntryResourcePlanAllocatedActorListView portfolioEntryResourcePlanAllocatedActorListView, Object value) {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = controllers.core.routes.PortfolioEntryPlanningController.deleteAllocatedActor(
                                portfolioEntryResourcePlanAllocatedActorListView.portfolioEntryId, portfolioEntryResourcePlanAllocatedActorListView.id).url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    }
                });
                setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                setEmptyMessageKey("object.allocated_resource.actor.table.empty");

            }
        };
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

    public BigDecimal days;

    public BigDecimal forecastDays;

    public BigDecimal dailyRate;

    public Date startDate;

    public Date endDate;

    public PortfolioEntryPlanningPackage planningPackage;

    public boolean isConfirmed;

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
        this.days = allocatedActor.days;
        this.startDate = allocatedActor.startDate;
        this.endDate = allocatedActor.endDate;
        this.planningPackage = allocatedActor.portfolioEntryPlanningPackage;
        this.isConfirmed = allocatedActor.isConfirmed;
        this.followPackageDates = allocatedActor.followPackageDates;
        this.forecastDays = allocatedActor.forecastDays;
        this.dailyRate = allocatedActor.dailyRate;
    }

}
