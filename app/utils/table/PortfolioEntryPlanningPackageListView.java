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
import controllers.core.PortfolioEntryPlanningController;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import dao.timesheet.TimesheetDao;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.FilterConfig;
import framework.utils.FilterConfig.SortStatusType;
import framework.utils.IColumnFormatter;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.pmo.PortfolioEntryPlanningPackage;
import models.pmo.PortfolioEntryPlanningPackageGroup;
import models.pmo.PortfolioEntryPlanningPackageType;

/**
 * A portfolio entry planning package list view is used to display an portfolio
 * entry planning package row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPlanningPackageListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public FilterConfig<PortfolioEntryPlanningPackageListView> filterConfig;
        public Table<PortfolioEntryPlanningPackageListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.filterConfig = getFilterConfig();
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the filter config.
         */
        public FilterConfig<PortfolioEntryPlanningPackageListView> getFilterConfig() {
            return new FilterConfig<PortfolioEntryPlanningPackageListView>() {
                {
                    addColumnConfiguration("refId", "refId", "object.portfolio_entry_planning_package.ref_id.label", new TextFieldFilterComponent("*"), true,
                            false, SortStatusType.UNSORTED);

                    addColumnConfiguration("name", "name", "object.portfolio_entry_planning_package.name.label", new TextFieldFilterComponent("*"), true,
                            false, SortStatusType.UNSORTED);

                    addColumnConfiguration("startDate", "startDate", "object.portfolio_entry_planning_package.start_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("endDate", "endDate", "object.portfolio_entry_planning_package.end_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.ASC);

                    ISelectableValueHolderCollection<Long> groups = PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupActiveAsVH();
                    if (groups.getValues().size() > 0) {
                        addColumnConfiguration("group", "portfolioEntryPlanningPackageGroup.id", "object.portfolio_entry_planning_package.group.label",
                                new SelectFilterComponent(groups.getValues().iterator().next().getValue(), groups, new String[]{"portfolioEntryPlanningPackageGroup.name"}), false, false, SortStatusType.UNSORTED);
                    } else {
                        addColumnConfiguration("group", "portfolioEntryPlanningPackageGroup.id", "object.portfolio_entry_planning_package.group.label",
                                new NoneFilterComponent(), false, false, SortStatusType.NONE);
                    }

                    ISelectableValueHolderCollection<Long> types = PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeActiveAsVH();
                    if (types.getValues().size() > 0) {
                        addColumnConfiguration("type", "portfolioEntryPlanningPackageType.id", "object.portfolio_entry_planning_package.type.label",
                                new SelectFilterComponent(types.getValues().iterator().next().getValue(), types, new String[]{"portfolioEntryPlanningPackageType.name"}), false, false, SortStatusType.NONE);
                    } else {
                        addColumnConfiguration("type", "portfolioEntryPlanningPackageType.id", "object.portfolio_entry_planning_package.type.label",
                                new NoneFilterComponent(), false, false, SortStatusType.NONE);
                    }

                    ISelectableValueHolderCollection<String> status = PortfolioEntryPlanningController.getPackageStatusAsValueHolderCollection();
                    if (status != null && status.getValues().size() > 0) {
                        addColumnConfiguration("status", "status", "object.portfolio_entry_planning_package.status.label",
                                new SelectFilterComponent(status.getValues().iterator().next().getValue(), status), true, false, SortStatusType.NONE);
                    } else {
                        addColumnConfiguration("status", "status", "object.portfolio_entry_planning_package.status.label", new NoneFilterComponent(), true,
                                false, SortStatusType.NONE);
                    }

                    addColumnConfiguration("allocatedResourcesDays", "allocatedResourcesDays",
                            "object.portfolio_entry_planning_package.allocated_resources_days.label", new NoneFilterComponent(), false, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("timesheetsDays", "timesheetsDays", "object.portfolio_entry_planning_package.timesheets_days.label",
                            new NoneFilterComponent(), false, false, SortStatusType.UNSORTED);

                    addCustomAttributesColumns("id", PortfolioEntryPlanningPackage.class);

                }
            };
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryPlanningPackageListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryPlanningPackageListView>() {
                {
                    setIdFieldName("id");

                    addColumn("refId", "refId", "object.portfolio_entry_planning_package.ref_id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("refId", new ObjectFormatter<PortfolioEntryPlanningPackageListView>());

                    addColumn("name", "name", "object.portfolio_entry_planning_package.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<PortfolioEntryPlanningPackageListView>());

                    addColumn("startDate", "startDate", "object.portfolio_entry_planning_package.start_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("startDate", new DateFormatter<PortfolioEntryPlanningPackageListView>());

                    addColumn("endDate", "endDate", "object.portfolio_entry_planning_package.end_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("endDate", new DateFormatter<PortfolioEntryPlanningPackageListView>());

                    addColumn("group", "group", "object.portfolio_entry_planning_package.group.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("group", new IColumnFormatter<PortfolioEntryPlanningPackageListView>() {
                        @Override
                        public String apply(PortfolioEntryPlanningPackageListView portfolioEntryPlanningPackageListView, Object value) {
                            return views.html.framework_views.parts.formats.display_value_holder.render(portfolioEntryPlanningPackageListView.group, true)
                                    .body();
                        }
                    });

                    addColumn("type", "type", "object.portfolio_entry_planning_package.type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("type", new IColumnFormatter<PortfolioEntryPlanningPackageListView>() {
                        @Override
                        public String apply(PortfolioEntryPlanningPackageListView portfolioEntryPlanningPackageListView, Object value) {
                            return views.html.modelsparts.display_portfolio_entry_planning_package_type.render(portfolioEntryPlanningPackageListView.type)
                                    .body();
                        }
                    });

                    addColumn("status", "status", "object.portfolio_entry_planning_package.status.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("status", new ObjectFormatter<>());

                    addColumn("allocatedResourcesDays", "allocatedResourcesDays", "object.portfolio_entry_planning_package.allocated_resources_days.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("allocatedResourcesDays", new NumberFormatter<PortfolioEntryPlanningPackageListView>());

                    addColumn("timesheetsDays", "timesheetsDays", "object.portfolio_entry_planning_package.timesheets_days.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("timesheetsDays", new NumberFormatter<PortfolioEntryPlanningPackageListView>());

                    addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntryPlanningPackage.class);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryPlanningPackageListView>(IMafConstants.EDIT_URL_FORMAT,
                            portfolioEntryPlanningPackageListView -> controllers.core.routes.PortfolioEntryPlanningController
                                    .managePackage(portfolioEntryPlanningPackageListView.portfolioEntryId, portfolioEntryPlanningPackageListView.id).url()));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", (portfolioEntryPlanningPackageListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = controllers.core.routes.PortfolioEntryPlanningController
                                .deletePackage(portfolioEntryPlanningPackageListView.portfolioEntryId, portfolioEntryPlanningPackageListView.id).url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    this.setLineAction((portfolioEntryPlanningPackageListView, value) -> controllers.core.routes.PortfolioEntryPlanningController
                            .viewPackage(portfolioEntryPlanningPackageListView.portfolioEntryId, portfolioEntryPlanningPackageListView.id).url());

                    setEmptyMessageKey("object.portfolio_entry_planning_package.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryPlanningPackageListView() {
    }

    public Long id;
    public Long portfolioEntryId;

    public String refId;
    public String name;
    public Date startDate;
    public Date endDate;
    public PortfolioEntryPlanningPackageGroup group;
    public PortfolioEntryPlanningPackageType type;
    public String status;
    public BigDecimal allocatedResourcesDays;
    public BigDecimal timesheetsDays;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param portfolioEntryPlanningPackage
     *            the portfolio entry planning package in the DB
     * @param messagesPlugin
     *            the i18n service
     * @param preferenceManagerPlugin
     *            the preference manager service
     */
    public PortfolioEntryPlanningPackageListView(PortfolioEntryPlanningPackage portfolioEntryPlanningPackage, II18nMessagesPlugin messagesPlugin,
            IPreferenceManagerPlugin preferenceManagerPlugin) {

        this.id = portfolioEntryPlanningPackage.id;
        this.portfolioEntryId = portfolioEntryPlanningPackage.portfolioEntry.id;

        this.refId = portfolioEntryPlanningPackage.refId;
        this.name = portfolioEntryPlanningPackage.name;
        this.type = portfolioEntryPlanningPackage.portfolioEntryPlanningPackageType;

        this.startDate = portfolioEntryPlanningPackage.startDate;

        this.endDate = portfolioEntryPlanningPackage.endDate;

        this.group = portfolioEntryPlanningPackage.portfolioEntryPlanningPackageGroup;

        this.status = Msg.get("object.portfolio_entry_planning_package.status." + portfolioEntryPlanningPackage.status.name() + ".label");

        this.allocatedResourcesDays = PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsDaysByPlanningPackage(portfolioEntryPlanningPackage)
                .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitAsDaysByPlanningPackage(portfolioEntryPlanningPackage))
                .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedCompetencyAsDaysByPlanningPackage(portfolioEntryPlanningPackage));

        this.timesheetsDays = TimesheetDao.getTimesheetLogAsTotalHoursByPEPlanningPackage(portfolioEntryPlanningPackage)
                .divide(TimesheetDao.getTimesheetReportHoursPerDay(preferenceManagerPlugin), BigDecimal.ROUND_HALF_UP);

    }
}
