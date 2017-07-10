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
import controllers.routes;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioEntryDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.*;
import framework.utils.formats.*;
import models.finance.Currency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.pmo.OrgUnit;
import models.pmo.PortfolioEntryPlanningPackage;

/**
 * A portfolio entry resource plan allocated org unit list view is used to
 * display a portfolio entry allocated org unit in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedOrgUnitListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

    	public FilterConfig<PortfolioEntryResourcePlanAllocatedOrgUnitListView> filterConfig;
        public Table<PortfolioEntryResourcePlanAllocatedOrgUnitListView> templateTable;

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

        public FilterConfig<PortfolioEntryResourcePlanAllocatedOrgUnitListView> getFilterConfig() {
            return new FilterConfig<PortfolioEntryResourcePlanAllocatedOrgUnitListView>() {
                {
                    ISelectableValueHolderCollection<Long> orgUnits = OrgUnitDao.getOrgUnitActiveAsVH();
                    addColumnConfiguration("orgUnit", "orgUnit.id", "object.allocated_resource.org_unit.label",
                            new SelectFilterComponent(orgUnits.getValues().iterator().next().getValue(), orgUnits, new String[] {"name"}), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("currency", "currency", "object.allocated_resource.currency.label",
                            new AutocompleteFilterComponent(routes.JsonController.currency().url()), false, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("isConfirmed", "isConfirmed", "object.allocated_resource.is_confirmed.label", new CheckboxFilterComponent(true),
                            true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("days", "days", "object.allocated_resource.days.label", new NumericFieldFilterComponent("0", "="), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("planningPackage", "portfolioEntryPlanningPackage.name", "object.allocated_resource.package.label",
                            new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("portfolioEntryName", "portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.name", "object.allocated_resource.portfolio_entry.label",
                    		new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label",
                            new NumericFieldFilterComponent("0", "="), false, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("forecastDays", "forecastDays", "object.allocated_resource.forecast_days.label",
                            new NumericFieldFilterComponent("0", "="), false, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("forecastDailyRate", "forecastDailyRate", "object.allocated_resource.forecast_daily_rate.label",
                            new NumericFieldFilterComponent("0", "="), false, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("startDate", "startDate", "object.allocated_resource.start_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("endDate", "endDate", "object.allocated_resource.end_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.ASC);

                    addCustomAttributesColumns("id", PortfolioEntryResourcePlanAllocatedOrgUnit.class);
                }
            };
        }
        /**
         * Get the table.
         * 
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */
        public Table<PortfolioEntryResourcePlanAllocatedOrgUnitListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryResourcePlanAllocatedOrgUnitListView>() {
                {
                    setIdFieldName("id");

                    addColumn("orgUnit", "orgUnit", "object.allocated_resource.org_unit.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter(
                            "orgUnit",
                            (portfolioEntryResourcePlanAllocatedOrgUnitListView, value) ->
                                    views.html.modelsparts.display_org_unit.render(portfolioEntryResourcePlanAllocatedOrgUnitListView.orgUnit).body()
                    );
                    setColumnValueCssClass("orgUnit", "rowlink-skip");

                    addColumn("portfolioEntryName", "portfolioEntryName", "object.allocated_resource.portfolio_entry.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryName", new ObjectFormatter<>());

                    addColumn("currency", "currency", "object.allocated_resource.currency.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("currency", new ObjectFormatter<>());

                    addColumn("days", "days", "object.allocated_resource.days.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("days", new NumberFormatter<>());

                    addColumn("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("dailyRate", new NumberFormatter<>());

                    addColumn("forecastDays", "forecastDays", "object.allocated_resource.forecast_days.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("forecastDays", new NumberFormatter<>());

                    addColumn("forecastDailyRate", "forecastDailyRate", "object.allocated_resource.forecast_daily_rate.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("forecastDailyRate", new NumberFormatter<>());

                    addColumn("planningPackage", "planningPackage", "object.allocated_resource.package.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter(
                            "planningPackage",
                            (portfolioEntryResourcePlanAllocatedOrgUnitListView, value) ->
                                    views.html.modelsparts.display_portfolio_entry_planning_package
                                            .render(portfolioEntryResourcePlanAllocatedOrgUnitListView.planningPackage)
                                            .body()
                    );
                    setColumnValueCssClass("planningPackage", "rowlink-skip");

                    addColumn("startDate", "startDate", "object.allocated_resource.start_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("startDate", new DateFormatter<>());

                    addColumn("endDate", "endDate", "object.allocated_resource.end_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("endDate", new DateFormatter<>());

                    addColumn("isConfirmed", "isConfirmed", "object.allocated_resource.is_confirmed.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isConfirmed", new BooleanFormatter<>());

                    addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntryResourcePlanAllocatedOrgUnit.class);

                    addColumn("reallocate", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("reallocate",
                            new StringFormatFormatter<>(
                                    "<a href=\"%s\"><span class=\"fa fa-user\"></span></a>",
                                    allocatedOrgUnitListView -> controllers.core.routes.PortfolioEntryPlanningController
                                            .reallocateOrgUnit(
                                                    allocatedOrgUnitListView.portfolioEntryId,
                                                    allocatedOrgUnitListView.id
                                            ).url()
                            )
                    );
                    setColumnCssClass("reallocate", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("reallocate", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter(
                            "editActionLink",
                            new StringFormatFormatter<>(
                                IMafConstants.EDIT_URL_FORMAT,
                                (StringFormatFormatter.Hook<PortfolioEntryResourcePlanAllocatedOrgUnitListView>) portfolioEntryResourcePlanAllocatedOrgUnitListView ->
                                        controllers.core.routes.PortfolioEntryPlanningController
                                            .manageAllocatedOrgUnit(portfolioEntryResourcePlanAllocatedOrgUnitListView.portfolioEntryId, portfolioEntryResourcePlanAllocatedOrgUnitListView.id)
                                            .url()
                            )
                    );
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", (portfolioEntryResourcePlanAllocatedOrgUnitListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = controllers.core.routes.PortfolioEntryPlanningController
                                .deleteAllocatedOrgUnit(portfolioEntryResourcePlanAllocatedOrgUnitListView.portfolioEntryId,
                                        portfolioEntryResourcePlanAllocatedOrgUnitListView.id)
                                .url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    setEmptyMessageKey("object.allocated_resource.org_unit.table.empty");
                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryResourcePlanAllocatedOrgUnitListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public String portfolioEntryName;

    public OrgUnit orgUnit;

    public Currency currency;

    public BigDecimal days;

    public BigDecimal dailyRate;

    public BigDecimal forecastDays;

    public BigDecimal forecastDailyRate;

    public Date startDate;

    public Date endDate;

    public PortfolioEntryPlanningPackage planningPackage;

    public boolean isConfirmed;

    public Boolean followPackageDates;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param allocatedOrgUnit
     *            the allocated org unit in the DB
     */
    public PortfolioEntryResourcePlanAllocatedOrgUnitListView(PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit) {
        this.id = allocatedOrgUnit.id;
        this.portfolioEntryId = allocatedOrgUnit.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.portfolioEntryName = PortfolioEntryDao.getPEById(this.portfolioEntryId).getName();
        this.orgUnit = allocatedOrgUnit.orgUnit;
        this.startDate = allocatedOrgUnit.startDate;
        this.endDate = allocatedOrgUnit.endDate;
        this.planningPackage = allocatedOrgUnit.portfolioEntryPlanningPackage;
        this.isConfirmed = allocatedOrgUnit.isConfirmed;
        this.followPackageDates = allocatedOrgUnit.followPackageDates;

        this.currency = allocatedOrgUnit.currency;
        this.days = allocatedOrgUnit.days;
        this.dailyRate = allocatedOrgUnit.dailyRate;
        this.forecastDays = allocatedOrgUnit.forecastDays != null ? allocatedOrgUnit.forecastDays : allocatedOrgUnit.days;
        this.forecastDailyRate = allocatedOrgUnit.forecastDailyRate != null ? allocatedOrgUnit.forecastDailyRate : allocatedOrgUnit.dailyRate;

    }

}
