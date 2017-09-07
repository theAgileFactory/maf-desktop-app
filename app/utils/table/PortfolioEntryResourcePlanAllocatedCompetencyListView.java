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
import controllers.routes;
import dao.pmo.PortfolioEntryDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.FilterConfig;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.finance.Currency;
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import models.finance.PortfolioEntryResourcePlanAllocationStatusType;
import models.pmo.Actor;
import models.pmo.Competency;
import models.pmo.PortfolioEntryPlanningPackage;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

/**
 * A portfolio entry resource plan allocated competency list view is used to
 * display a portfolio entry allocated competency in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedCompetencyListView {

    /**
     * The definition of the table.
     *
     * @author Johann Kohler
     */
    public static class TableDefinition {

    	public FilterConfig<PortfolioEntryResourcePlanAllocatedCompetencyListView> filterConfig;
        public Table<PortfolioEntryResourcePlanAllocatedCompetencyListView> templateTable;

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

        public FilterConfig<PortfolioEntryResourcePlanAllocatedCompetencyListView> getFilterConfig() {
            return new FilterConfig<PortfolioEntryResourcePlanAllocatedCompetencyListView>() {
                {
                    String[] actorFieldsSort = { "lastStatusTypeUpdateActor.lastName", "lastStatusTypeUpdateActor.firstName" };

                    addColumnConfiguration("competency", "competency", "object.allocated_resource.competency.label",
                            new AutocompleteFilterComponent(routes.JsonController.sponsoringUnit().url()), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("currency", "currency", "object.allocated_resource.currency.label",
                            new AutocompleteFilterComponent(routes.JsonController.currency().url()), false, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("portfolioEntryResourcePlanAllocationStatusType", "portfolioEntryResourcePlanAllocationStatusType", "object.allocated_resource.portfolio_entry_resource_plan_allocation_status_type.label", new NoneFilterComponent(),
                            true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("lastStatusTypeUpdateActor", "lastStatusTypeUpdateActor.id", "object.allocated_resource.last_update_status_type_actor.label",
                            new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url(), actorFieldsSort), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("lastStatusTypeUpdateTime", "lastStatusTypeUpdateTime", "object.allocated_resource.last_update_status_type_time.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.ASC);

                    addColumnConfiguration("days", "days", "object.allocated_resource.days.label", new NumericFieldFilterComponent("0", "="), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("planningPackage", "portfolioEntryPlanningPackage.name", "object.allocated_resource.package.label",
                            new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("portfolioEntryName", "portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.name", "object.allocated_resource.portfolio_entry.label",
                    		new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label",
                            new NumericFieldFilterComponent("0", "="), false, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("startDate", "startDate", "object.allocated_resource.start_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("endDate", "endDate", "object.allocated_resource.end_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);

                    addCustomAttributesColumns("id", PortfolioEntryResourcePlanAllocatedCompetency.class);
                }
            };
        }
        /**
         * Get the table.
         *
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */
        public Table<PortfolioEntryResourcePlanAllocatedCompetencyListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryResourcePlanAllocatedCompetencyListView>() {
                {
                    setIdFieldName("id");

                    addColumn("competency", "competency", "object.allocated_resource.competency.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter(
                            "competency",
                            (portfolioEntryResourcePlanAllocatedCompetencyListView, value) ->
                                    views.html.modelsparts.display_competency.render(portfolioEntryResourcePlanAllocatedCompetencyListView.competency).body()
                    );
                    setColumnValueCssClass("competency", "rowlink-skip");

                    addColumn("portfolioEntryName", "portfolioEntryName", "object.allocated_resource.portfolio_entry.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryName", new ObjectFormatter<>());

                    addColumn("currency", "currency", "object.allocated_resource.currency.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("currency", new ObjectFormatter<>());

                    addSummableColumn("days", "days", "object.allocated_resource.days.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("days", new NumberFormatter<>());
                    setColumnHeaderCssClass("days", "text-right");
                    setColumnValueCssClass("days", "text-right");

                    addColumn("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("dailyRate", new NumberFormatter<>());

                    addColumn("planningPackage", "planningPackage", "object.allocated_resource.package.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter(
                            "planningPackage",
                            (portfolioEntryResourcePlanAllocatedCompetencyListView, value) ->
                                    views.html.modelsparts.display_portfolio_entry_planning_package
                                            .render(portfolioEntryResourcePlanAllocatedCompetencyListView.planningPackage)
                                            .body()
                    );
                    setColumnValueCssClass("planningPackage", "rowlink-skip");

                    addColumn("startDate", "startDate", "object.allocated_resource.start_date.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("startDate", new DateFormatter<>());

                    addColumn("endDate", "endDate", "object.allocated_resource.end_date.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("endDate", new DateFormatter<>());

                    addColumn("portfolioEntryResourcePlanAllocationStatusType", "portfolioEntryResourcePlanAllocationStatusType.status", "object.allocated_resource.portfolio_entry_resource_plan_allocation_status_type.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryResourcePlanAllocationStatusType", (value, cellValue) -> views.html.modelsparts.display_allocation_status.render(value.portfolioEntryResourcePlanAllocationStatusType).body());

                    addColumn("lastStatusTypeUpdateActor", "lastStatusTypeUpdateActor", "object.allocated_resource.last_update_status_type_actor.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lastStatusTypeUpdateActor", (portfolioEntryResourcePlanAllocatedActorListView, value) -> views.html.modelsparts.display_actor.render(portfolioEntryResourcePlanAllocatedActorListView.lastStatusTypeUpdateActor).body());
                    setColumnValueCssClass("lastStatusTypeUpdateActor", "rowlink-skip");

                    addColumn("lastStatusTypeUpdateTime", "lastStatusTypeUpdateTime", "object.allocated_resource.last_update_status_type_time.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lastStatusTypeUpdateTime", new DateFormatter<>());

                    addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntryResourcePlanAllocatedCompetency.class);

                    addColumn("reallocate", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("reallocate",
                            new StringFormatFormatter<>(
                                    "<a href=\"%s\"><span class=\"fa fa-user\"></span></a>",
                                    allocatedCompetencyListView -> controllers.core.routes.PortfolioEntryPlanningController
                                            .reallocateCompetency(
                                                    allocatedCompetencyListView.portfolioEntryId,
                                                    allocatedCompetencyListView.id
                                            ).url()
                            )
                    );
                    setColumnCssClass("reallocate", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("reallocate", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("editActionLink", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter(
                            "editActionLink",
                            new StringFormatFormatter<>(
                                IMafConstants.EDIT_URL_FORMAT,
                                (StringFormatFormatter.Hook<PortfolioEntryResourcePlanAllocatedCompetencyListView>) portfolioEntryResourcePlanAllocatedCompetencyListView ->
                                        controllers.core.routes.PortfolioEntryPlanningController
                                            .manageAllocatedCompetency(portfolioEntryResourcePlanAllocatedCompetencyListView.portfolioEntryId, portfolioEntryResourcePlanAllocatedCompetencyListView.id)
                                            .url()
                            )
                    );
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("removeActionLink", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", (portfolioEntryResourcePlanAllocatedCompetencyListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = controllers.core.routes.PortfolioEntryPlanningController
                                .deleteAllocatedCompetency(portfolioEntryResourcePlanAllocatedCompetencyListView.portfolioEntryId,
                                        portfolioEntryResourcePlanAllocatedCompetencyListView.id)
                                .url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    setEmptyMessageKey("object.allocated_resource.competency.table.empty");
                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryResourcePlanAllocatedCompetencyListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public String portfolioEntryName;

    public Competency competency;

    public Currency currency;

    public BigDecimal days;

    public BigDecimal dailyRate;

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
     * @param allocatedCompetency
     *            the allocated competency in the DB
     */
    public PortfolioEntryResourcePlanAllocatedCompetencyListView(PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency) {
        this.id = allocatedCompetency.id;
        this.portfolioEntryId = allocatedCompetency.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.portfolioEntryName = PortfolioEntryDao.getPEById(this.portfolioEntryId).getName();
        this.competency = allocatedCompetency.competency;
        this.startDate = allocatedCompetency.startDate;
        this.endDate = allocatedCompetency.endDate;
        this.planningPackage = allocatedCompetency.portfolioEntryPlanningPackage;
        this.portfolioEntryResourcePlanAllocationStatusType = allocatedCompetency.portfolioEntryResourcePlanAllocationStatusType;
        this.lastStatusTypeUpdateActor = allocatedCompetency.lastStatusTypeUpdateActor;
        this.lastStatusTypeUpdateTime = allocatedCompetency.lastStatusTypeUpdateTime;
        this.followPackageDates = allocatedCompetency.followPackageDates;

        this.currency = allocatedCompetency.currency;
        this.days = allocatedCompetency.days;
        this.dailyRate = allocatedCompetency.dailyRate;

    }

}
