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

import dao.governance.LifeCycleMilestoneDao;
import dao.governance.LifeCyclePlanningDao;
import dao.governance.LifeCycleProcessDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.PortfolioEntryReportDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.kpi.IKpiService;
import framework.utils.*;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ListOfValuesFormatter;
import framework.utils.formats.ObjectFormatter;
import models.governance.LifeCycleMilestoneInstance;
import models.governance.PlannedLifeCycleMilestoneInstance;
import models.pmo.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A portfolio entry list view is used to display an portfolio entry row in a
 * table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public PostQueryFilterConfig<PortfolioEntryListView, PortfolioEntry> filterConfig;
        public Table<PortfolioEntryListView> templateTable;

        /**
         * Default constructor.
         * 
         * @param kpiService
         *            the KPI service.
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */
        public TableDefinition(IKpiService kpiService, II18nMessagesPlugin i18nMessagesPlugin) {
            this.filterConfig = getFilterConfig(kpiService);
            this.templateTable = getTable(kpiService, i18nMessagesPlugin);
        }

        /**
         * Get the filter config.
         * 
         * @param kpiService
         *            the KPI service
         */
        public PostQueryFilterConfig<PortfolioEntryListView, PortfolioEntry> getFilterConfig(IKpiService kpiService) {
            return new PostQueryFilterConfig<PortfolioEntryListView, PortfolioEntry>() {
                {
                    addColumnConfiguration("governanceId", "governanceId", "object.portfolio_entry.governance_id.label", new TextFieldFilterComponent("*"),
                            true, false, SortStatusType.ASC);

                    addColumnConfiguration("creationDate", "creationDate", "object.portfolio_entry.creation_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), false, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("isPublic", "isPublic", "object.portfolio_entry.is_public.label", new CheckboxFilterComponent(true), false, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("name", "name", "object.portfolio_entry.name.label", new TextFieldFilterComponent("*"), true, false,
                            SortStatusType.UNSORTED);

                    ISelectableValueHolderCollection<Long> portfolioEntryTypes = PortfolioEntryDao.getPETypeActiveAsVH();
                    if (portfolioEntryTypes.getValues().size() > 0) {
                        addColumnConfiguration("portfolioEntryType", "portfolioEntryType.id", "object.portfolio_entry.type.label",
                                new SelectFilterComponent(portfolioEntryTypes.getValues().iterator().next().getValue(), portfolioEntryTypes, new String[]{"portfolioEntryType.name"}), true, false,
                                SortStatusType.UNSORTED);
                    } else {
                        addColumnConfiguration("portfolioEntryType", "portfolioEntryType.id", "object.portfolio_entry.type.label", new NoneFilterComponent(),
                                false, false, SortStatusType.NONE);
                    }

                    addColumnConfiguration("manager", "manager.id", "object.portfolio_entry.manager.label",
                            new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url(), new String[] { "manager.firstName", "manager.lastName" }), false, false, SortStatusType.UNSORTED);

                    ISelectableValueHolderCollection<Long> orgUnits = OrgUnitDao.getOrgUnitActiveAsVH();
                    if (orgUnits.getValues().size() > 0) {
                        addColumnConfiguration("managerOrgUnit", "manager.orgUnit.id", "object.portfolio_entry.manager_org_unit.label",
                                new SelectFilterComponent(orgUnits.getValues().iterator().next().getValue(), orgUnits, new String[] {"manager.orgUnit.name"}), false, false,
                                SortStatusType.UNSORTED);
                    } else {
                        addColumnConfiguration("managerOrgUnit", "manager.orgUnit.id", "object.portfolio_entry.manager_org_unit.label",
                                new NoneFilterComponent(), false, false, SortStatusType.NONE);
                    }

                    ISelectableValueHolderCollection<Long> sponsoringUnits = OrgUnitDao.getOrgUnitActiveCanSponsorAsVH();
                    if (sponsoringUnits.getValues().size() > 0) {
                        addColumnConfiguration("sponsoringUnit", "sponsoringUnit.id", "object.portfolio_entry.sponsoring_unit.label",
                                new SelectFilterComponent(sponsoringUnits.getValues().iterator().next().getValue(), sponsoringUnits, new String[] {"sponsoringUnit.name"}), false, false,
                                SortStatusType.UNSORTED);
                    } else {
                        addColumnConfiguration("sponsoringUnit", "sponsoringUnit.id", "object.portfolio_entry.sponsoring_unit.label",
                                new NoneFilterComponent(), false, false, SortStatusType.NONE);
                    }

                    ISelectableValueHolderCollection<Long> deliveryUnits = OrgUnitDao.getOrgUnitActiveCanDeliverAsVH();
                    if (deliveryUnits.getValues().size() > 0) {
                        addColumnConfiguration("deliveryUnits", "deliveryUnits.id", "object.portfolio_entry.delivery_units.label",
                                new SelectFilterComponent(deliveryUnits.getValues().iterator().next().getValue(), deliveryUnits, new String[] {"deliveryUnits.name"}), false, false,
                                SortStatusType.NONE);
                    } else {
                        addColumnConfiguration("deliveryUnits", "deliveryUnits.id", "object.portfolio_entry.delivery_units.label", new NoneFilterComponent(),
                                false, false, SortStatusType.NONE);
                    }

                    ISelectableValueHolderCollection<Long> portfolios = PortfolioDao.getPortfolioActiveAsVH();
                    if (portfolios.getValues().size() > 0) {
                        addColumnConfiguration("portfolios", "portfolios.id", "object.portfolio_entry.portfolios.label",
                                new SelectFilterComponent(portfolios.getValues().iterator().next().getValue(), portfolios, new String[] {"portfolios.name"}), false, false,
                                SortStatusType.NONE);
                    } else {
                        addColumnConfiguration("portfolios", "portfolios.id", "object.portfolio_entry.portfolios.label", new NoneFilterComponent(), false,
                                false, SortStatusType.NONE);
                    }

                    addColumnConfiguration("stakeholders", "stakeholders.actor.id", "object.portfolio_entry.stakeholders.label",
                            new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url(), new String[] {"stakeholders.actor.firstName", "stakeholders.actor.lastName"}), false, false, SortStatusType.NONE);

                    addColumnConfiguration("dependencies", "dependencies.id", "object.portfolio_entry.dependencies.label", new NoneFilterComponent(), false,
                            false, SortStatusType.NONE);

                    ISelectableValueHolderCollection<Long> lifeCycleProcesses = LifeCycleProcessDao.getLCProcessActiveAsVH();
                    if (lifeCycleProcesses.getValues().size() > 0) {
                        addColumnConfiguration("lifeCycleProcess", "activeLifeCycleInstance.lifeCycleProcess.id",
                                "object.portfolio_entry.life_cycle_process.label",
                                new SelectFilterComponent(lifeCycleProcesses.getValues().iterator().next().getValue(), lifeCycleProcesses, new String[] {"activeLifeCycleInstance.lifeCycleProcess.name"}), false, false,
                                SortStatusType.UNSORTED);
                    } else {
                        addColumnConfiguration("lifeCycleProcess", "activeLifeCycleInstance.lifeCycleProcess.id",
                                "object.portfolio_entry.life_cycle_process.label", new NoneFilterComponent(), false, false, SortStatusType.UNSORTED);
                    }

                    ISelectableValueHolderCollection<Long> portfolioEntryReportStatusTypes = PortfolioEntryReportDao.getPEReportStatusTypeActiveAsVH();
                    if (portfolioEntryReportStatusTypes.getValues().size() > 0) {
                        addColumnConfiguration(
                                "portfolioEntryStatus",
                                "lastPortfolioEntryReport.portfolioEntryReportStatusType.id",
                                "object.portfolio_entry.status.label",
                                new SelectFilterComponent(portfolioEntryReportStatusTypes.getValues().iterator().next().getValue(), portfolioEntryReportStatusTypes, new String[]{"lastPortfolioEntryReport.portfolioEntryReportStatusType.name"}),
                                true,
                                false,
                                SortStatusType.UNSORTED
                        );
                    } else {
                        addColumnConfiguration("portfolioEntryStatus", "lastPortfolioEntryReport.portfolioEntryReportStatusType.id",
                                "object.portfolio_entry.status.label", new NoneFilterComponent(), false, false, SortStatusType.NONE);
                    }

                    addColumnConfiguration("lastPEReportDate", "lastPortfolioEntryReport.publicationDate", "object.portfolio_entry_report.report_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), false, false, SortStatusType.UNSORTED);

                    ISelectableValueHolderCollection<Long> lifeCycleMilestones = LifeCycleMilestoneDao.getLCMilestoneActiveAsVH();
                    if (lifeCycleMilestones != null && lifeCycleMilestones.getValues().size() > 0) {
                        addColumnConfiguration("lastMilestone", "lastApprovedLifeCycleMilestoneInstance.lifeCycleMilestone.id",
                                "object.portfolio_entry.last_milestone.label",
                                new SelectFilterComponent(lifeCycleMilestones.getValues().iterator().next().getValue(), lifeCycleMilestones, new String[] {"lastApprovedLifeCycleMilestoneInstance.lifeCycleMilestone.shortName"}), true, false,
                                SortStatusType.UNSORTED);
                        addColumnConfiguration("lastMilestoneDate", "lastApprovedLifeCycleMilestoneInstance.passedDate", "object.portfolio_entry.last_milestone_date.label",
                                new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()),
                                false, false, SortStatusType.UNSORTED);
                        addColumnConfiguration("nextMilestone", "activeLifeCycleInstance.lifeCycleInstancePlannings.plannedLifeCycleMilestoneInstance.lifeCycleMilestone.id",
                                "object.portfolio_entry.next_milestone.label",
                                new PostQueryFilterConfig.PostQueryFilterComponent<>(
                                        new SelectFilterComponent(
                                                lifeCycleMilestones.getValues().iterator().next().getValue(),
                                                lifeCycleMilestones,
                                                new String[]{"lifeCycleMilestone.shortName"}),
                                        new PostQueryFilter<PortfolioEntry>() {
                                            @Override
                                            public boolean test(PortfolioEntry portfolioEntry) {
                                                PlannedLifeCycleMilestoneInstance nextMilestone = portfolioEntry.getNextMilestone();
                                                return  filterValue instanceof List &&
                                                        nextMilestone != null &&
                                                        ((List<?>) filterValue).contains(nextMilestone.lifeCycleMilestone.id.toString());
                                            }
                                        },
                                        (p1, p2) -> {
                                            PlannedLifeCycleMilestoneInstance nextMilestone1 = p1.getNextMilestone();
                                            PlannedLifeCycleMilestoneInstance nextMilestone2 = p2.getNextMilestone();
                                            if (nextMilestone1 == null) {
                                                return (nextMilestone2 == null) ? 0 : 1;
                                            } else if (nextMilestone2 == null) {
                                                return -1;
                                            } else {
                                                return nextMilestone1.lifeCycleMilestone.getName().compareTo(nextMilestone2.lifeCycleMilestone.getName());
                                            }
                                        }),
                                    false,
                                    false,
                                    SortStatusType.UNSORTED);
                        addColumnConfiguration("nextMilestoneDate", "activeLifeCycleInstance.lifeCycleInstancePlannings.plannedLifeCycleMilestoneInstance.lifeCycleMilestone.plannedDate", "object.portfolio_entry.next_milestone_date.label",
                                new PostQueryFilterComponent<>(
                                        new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()),
                                        new PostQueryFilter<PortfolioEntry>() {
                                            @Override
                                            public boolean test(PortfolioEntry portfolioEntry) {
                                                PlannedLifeCycleMilestoneInstance nextMilestone = portfolioEntry.getNextMilestone();
                                                return filterValue instanceof Date[] &&
                                                        nextMilestone != null &&
                                                        nextMilestone.plannedDate != null &&
                                                        nextMilestone.plannedDate.after(((Date[])filterValue)[0]) &&
                                                        nextMilestone.plannedDate.before(((Date[])filterValue)[1]);
                                            }
                                        },
                                        (p1, p2) -> {
                                            PlannedLifeCycleMilestoneInstance nextMilestone1 = p1.getNextMilestone();
                                            PlannedLifeCycleMilestoneInstance nextMilestone2 = p2.getNextMilestone();
                                            if (nextMilestone1 == null || nextMilestone1.plannedDate == null) {
                                                return (nextMilestone2 == null || nextMilestone2.plannedDate == null) ? 0 : 1;
                                            } else if (nextMilestone2 == null || nextMilestone2.plannedDate == null) {
                                                return -1;
                                            } else {
                                                return nextMilestone1.plannedDate.compareTo(nextMilestone2.plannedDate);
                                            }
                                        }
                                ),
                                false, false, SortStatusType.UNSORTED);
                    } else {
                        addColumnConfiguration("lastMilestone", "lastApprovedLifeCycleMilestoneInstance.lifeCycleMilestone.id",
                                "object.portfolio_entry.last_milestone.label", new NoneFilterComponent(), false, false, SortStatusType.NONE);
                        addColumnConfiguration("lastMilestoneDate", "lastApprovedLifeCycleMilestoneInstance.passedDate", "object.portfolio_entry.last_milestone_date.label",
                                new NoneFilterComponent(), false, false, SortStatusType.NONE);
                        addColumnConfiguration("nextMilestone", "lifeCycleMilestone.id",
                                "object.portfolio_entry.next_milestone.label", new NoneFilterComponent(), false, false, SortStatusType.NONE);
                        addColumnConfiguration("nextMilestoneDate", "plannedDate", "object.portfolio_entry.next_milestone_date.label",
                                new NoneFilterComponent(), false, false, SortStatusType.NONE);
                    }

                    addColumnConfiguration("plannedLifeCycleMilestoneInstances", "plannedLifeCycleMilestoneInstances.id", "object.planned_life_cycle_milestone_instance.label", new NoneFilterComponent(), false,
                            false, SortStatusType.NONE);

                    addColumnConfiguration("lifeCycleMilestoneInstances", "lifeCycleMilestoneInstances.id", "object.life_cycle_milestone_instance.label", new NoneFilterComponent(), false,
                            false, SortStatusType.NONE);

                    addColumnConfiguration("archived", "archived", "object.portfolio_entry.archived.label", new CheckboxFilterComponent(false), false, true,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("isConcept", "activeLifeCycleInstance.isConcept", "object.portfolio_entry.is_concept.label",
                            new CheckboxFilterComponent(false), false, false, SortStatusType.UNSORTED);

                    addKpis(kpiService, "id", PortfolioEntry.class);

                    addCustomAttributesColumns("id", PortfolioEntry.class);

                    addColumnConfiguration("startDate", "startDate" ,"object.portfolio_entry.start_date.label",
                    		new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()),
                    		false, false, SortStatusType.UNSORTED);
                    addColumnConfiguration("endDate", "endDate", "object.portfolio_entry.end_date.label",
                    		new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()),
                    		false, false, SortStatusType.UNSORTED);
                }
            };
        }

        /**
         * Get the table.
         * 
         * @param kpiService
         *            the KPI service
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */
        public Table<PortfolioEntryListView> getTable(IKpiService kpiService, II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryListView>() {
                {

                    setIdFieldName("id");

                    addColumn("governanceId", "governanceId", "object.portfolio_entry.governance_id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("governanceId", new ObjectFormatter<>());

                    addColumn("creationDate", "creationDate", "object.portfolio_entry.creation_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("creationDate", new DateFormatter<>());

                    addColumn("name", "name", "object.portfolio_entry.name.label", Table.ColumnDef.SorterType.NONE);

                    addColumn("portfolioEntryType", "portfolioEntryType", "object.portfolio_entry.type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryType", (portfolioEntryListView, value) -> views.html.framework_views.parts.formats.display_value_holder.render(portfolioEntryListView.portfolioEntryType, true)
                            .body());

                    addColumn("manager", "manager", "object.portfolio_entry.manager.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("manager", (portfolioEntryListView, value) -> views.html.modelsparts.display_actor.render(portfolioEntryListView.manager).body());
                    this.setColumnValueCssClass("manager", "rowlink-skip");

                    addColumn("managerOrgUnit", "managerOrgUnit", "object.portfolio_entry.manager_org_unit.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("managerOrgUnit", (portfolioEntryListView, value) -> views.html.modelsparts.display_org_unit.render(portfolioEntryListView.managerOrgUnit).body());
                    this.setColumnValueCssClass("managerOrgUnit", "rowlink-skip");

                    addColumn("sponsoringUnit", "sponsoringUnit", "object.portfolio_entry.sponsoring_unit.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("sponsoringUnit", (portfolioEntryListView, value) -> views.html.modelsparts.display_org_unit.render(portfolioEntryListView.sponsoringUnit).body());
                    this.setColumnValueCssClass("sponsoringUnit", "rowlink-skip");

                    addColumn("deliveryUnits", "deliveryUnits", "object.portfolio_entry.delivery_units.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deliveryUnits", new ListOfValuesFormatter<>());
                    this.setColumnValueCssClass("deliveryUnits", "rowlink-skip");

                    addColumn("portfolios", "portfolios", "object.portfolio_entry.portfolios.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolios", new ListOfValuesFormatter<>());
                    this.setColumnValueCssClass("portfolios", "rowlink-skip");

                    addColumn("stakeholders", "stakeholders", "object.portfolio_entry.stakeholders.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("stakeholders", new ListOfValuesFormatter<>());
                    this.setColumnValueCssClass("stakeholders", "rowlink-skip");

                    addColumn("dependencies", "dependencies", "object.portfolio_entry.dependencies.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("dependencies", new ListOfValuesFormatter<>());
                    this.setColumnValueCssClass("dependencies", "rowlink-skip");

                    addColumn("lifeCycleProcess", "lifeCycleProcess", "object.portfolio_entry.life_cycle_process.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lifeCycleProcess", new ObjectFormatter<>());

                    addColumn("portfolioEntryStatus", "portfolioEntryStatus", "object.portfolio_entry.status.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryStatus", (portfolioEntryView, value) -> views.html.modelsparts.display_portfolio_entry_report.render(portfolioEntryView.portfolioEntryStatus).body());
                    this.setColumnValueCssClass("portfolioEntryStatus", "rowlink-skip");

                    addColumn("lastPEReportDate", "lastPEReportDate", "object.portfolio_entry_report.report_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lastPEReportDate", new DateFormatter<>());

                    addColumn("archived", "archived", "object.portfolio_entry.archived.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("archived", new BooleanFormatter<>());

                    addColumn("isConcept", "isConcept", "object.portfolio_entry.is_concept.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isConcept", new BooleanFormatter<>());

                    addColumn("plannedLifeCycleMilestoneInstances", "plannedLifeCycleMilestoneInstances", "object.planned_life_cycle_milestone_instance.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("plannedLifeCycleMilestoneInstances", new ListOfValuesFormatter<>());
                    this.setColumnValueCssClass("plannedLifeCycleMilestoneInstances", "rowlink-skip");

                    addColumn("lifeCycleMilestoneInstances", "lifeCycleMilestoneInstances", "object.life_cycle_milestone_instance.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lifeCycleMilestoneInstances", new ListOfValuesFormatter<>());
                    this.setColumnValueCssClass("lifeCycleMilestoneInstances", "rowlink-skip");

                    addColumn("lastMilestone", "lastMilestone", "object.portfolio_entry.last_milestone.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lastMilestone", (portfolioEntryView, value) -> views.html.modelsparts.display_milestone_instance.render(portfolioEntryView.lastMilestone, false).body());
                    this.setColumnValueCssClass("lastMilestone", "rowlink-skip");
                    addColumn("lastMilestoneDate", "lastMilestoneDate", "object.portfolio_entry.last_milestone_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lastMilestoneDate", new DateFormatter<>());

                    addColumn("nextMilestone", "nextMilestone", "object.portfolio_entry.next_milestone.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("nextMilestone", (portfolioEntryView, value) -> views.html.modelsparts.display_milestone.render(portfolioEntryView.nextMilestone == null ? null : portfolioEntryView.nextMilestone.lifeCycleMilestone).body());
                    this.setColumnValueCssClass("nextMilestone", "rowlink-skip");
                    addColumn("nextMilestoneDate", "nextMilestoneDate", "object.portfolio_entry.next_milestone_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("nextMilestoneDate", new DateFormatter<>());

                    addColumn("stakeholderTypes", "stakeholderTypes", "object.portfolio_entry.stakeholder_types.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("stakeholderTypes", new ListOfValuesFormatter<>());

                    addColumn("isPublic", "isPublic", "object.portfolio_entry.is_public.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isPublic", new BooleanFormatter<>());
                    addColumn("startDate", "startDate", "object.portfolio_entry.start_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("startDate", new DateFormatter<>());
                    addColumn("endDate", "endDate", "object.portfolio_entry.end_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("endDate", new DateFormatter<>());

                    addKpis(kpiService, PortfolioEntry.class);

                    addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntry.class);

                    this.setLineAction((portfolioEntryListView, value) -> controllers.core.routes.PortfolioEntryController.overview(portfolioEntryListView.id).url());

                    setEmptyMessageKey("object.portfolio_entry.table.empty");
                }
            };
        }

    }

    /**
     * Returns a table with the columns to be hidden (by default).
     * 
     * @param hideStakeholderTypesColumn
     *            set to true if the stakeholder types column must be hidden
     * @param hideManagerColumn
     *            set to true if the manager column must be hidden
     * 
     * @return a set of columns name
     */
    public static Set<String> getHideNonDefaultColumns(boolean hideStakeholderTypesColumn, boolean hideManagerColumn) {
        Set<String> columns = new HashSet<>();

        if (hideStakeholderTypesColumn) {
            columns.add("stakeholderTypes");
        }

        if (hideManagerColumn) {
            columns.add("manager");
        }

        columns.add("creationDate");
        columns.add("isPublic");
        columns.add("sponsoringUnit");
        columns.add("deliveryUnits");
        columns.add("portfolios");
        columns.add("stakeholders");
        columns.add("lastPEReportDate");
        columns.add("dependencies");
        columns.add("lifeCycleProcess");
        columns.add("archived");
        columns.add("lastMilestoneDate");
        columns.add("nextMilestone");
        columns.add("nextMilestoneDate");
        columns.add("startDate");
        columns.add("endDate");
        columns.add("plannedLifeCycleMilestoneInstances");

        return columns;
    }

    /**
     * Default constructor.
     */
    public PortfolioEntryListView() {
    }

    public Long id;
    public String governanceId;
    public Date creationDate;
    public boolean isPublic;
    public String name;
    public PortfolioEntryType portfolioEntryType;
    public Actor manager;
    public OrgUnit managerOrgUnit;
    public OrgUnit sponsoringUnit;
    public List<OrgUnit> deliveryUnits;
    public List<Portfolio> portfolios;
    public String lifeCycleProcess;
    public PortfolioEntryReport portfolioEntryStatus;
    public Date lastPEReportDate;
    public LifeCycleMilestoneInstance lastMilestone;
    public PlannedLifeCycleMilestoneInstance nextMilestone;
    public Date lastMilestoneDate;
    public Date nextMilestoneDate;
    public boolean isConcept;
    public boolean archived;
    public List<Actor> stakeholders;
    public List<PortfolioEntry> dependencies;
    public Date startDate;
    public Date endDate;
    public List<PlannedLifeCycleMilestoneInstance> plannedLifeCycleMilestoneInstances;
    public List<LifeCycleMilestoneInstance> lifeCycleMilestoneInstances;

    // contextual attributes
    public List<String> stakeholderTypes = new ArrayList<>();

    /**
     * Construct a portfolio entry list view with a DB entry.
     * 
     * @param portfolioEntry
     *            the portfolio entry in the DB
     */
    public PortfolioEntryListView(PortfolioEntry portfolioEntry) {

        this.id = portfolioEntry.id;
        this.governanceId = portfolioEntry.governanceId;
        this.creationDate = portfolioEntry.creationDate;
        this.isPublic = portfolioEntry.isPublic;
        this.name = portfolioEntry.name;
        this.portfolioEntryType = portfolioEntry.portfolioEntryType;
        this.manager = portfolioEntry.manager;
        this.managerOrgUnit = portfolioEntry.manager.orgUnit;
        this.sponsoringUnit = portfolioEntry.sponsoringUnit;
        this.deliveryUnits = portfolioEntry.deliveryUnits;
        this.deliveryUnits.sort(Comparator.comparing(du -> du.name));
        this.portfolios = portfolioEntry.portfolios;
        this.portfolios.sort(Comparator.comparing(p -> p.name));
        this.lifeCycleProcess = portfolioEntry.activeLifeCycleInstance != null ? portfolioEntry.activeLifeCycleInstance.lifeCycleProcess.getName() : null;
        this.portfolioEntryStatus = portfolioEntry.lastPortfolioEntryReport;
        this.lastPEReportDate = portfolioEntry.lastPortfolioEntryReport != null ? portfolioEntry.lastPortfolioEntryReport.publicationDate : null;
        this.lastMilestone = portfolioEntry.lastApprovedLifeCycleMilestoneInstance;
        this.nextMilestone = portfolioEntry.getNextMilestone();
        this.startDate = portfolioEntry.startDate;
        this.endDate = portfolioEntry.endDate;
        this.isConcept = portfolioEntry.activeLifeCycleInstance == null || portfolioEntry.activeLifeCycleInstance.isConcept;
        this.archived = portfolioEntry.archived;
        if (  this.lastMilestone!= null && this.lastMilestone.passedDate != null)
        {
            this.lastMilestoneDate = this.lastMilestone.passedDate;
        }
        if (  this.nextMilestone!= null && this.nextMilestone.plannedDate != null)
        {
            this.nextMilestoneDate = this.nextMilestone.plannedDate;
        }

        this.stakeholders = new ArrayList<>();
        Set<Long> actorIds = new HashSet<>();
        portfolioEntry.stakeholders
                .stream()
                .filter(stakeholder -> !stakeholder.actor.deleted && !actorIds.contains(stakeholder.actor.id))
                .forEach(stakeholder -> {
                    actorIds.add(stakeholder.actor.id);
                    this.stakeholders.add(stakeholder.actor);
                });
        this.stakeholders.sort(Comparator.comparing(Actor::getName));

        this.dependencies = new ArrayList<>();
        Set<Long> dependencyIds = new HashSet<>();
        for (PortfolioEntryDependency portfolioEntryDependency : portfolioEntry.destinationDependencies) {
            PortfolioEntry dependency = portfolioEntryDependency.getSourcePortfolioEntry();
            if (dependency != null && !dependencyIds.contains(dependency.id)) {
                dependencyIds.add(dependency.id);
                this.dependencies.add(dependency);
            }
        }
        for (PortfolioEntryDependency portfolioEntryDependency : portfolioEntry.destinationDependencies) {
            PortfolioEntry dependency = portfolioEntryDependency.getDestinationPortfolioEntry();
            if (dependency != null && !dependencyIds.contains(dependency.id)) {
                dependencyIds.add(dependency.id);
                this.dependencies.add(dependency);
            }
        }
        this.dependencies.sort(Comparator.comparing(d -> d.name));

        this.plannedLifeCycleMilestoneInstances = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceLastAsListByPE(this.id);
        this.lifeCycleMilestoneInstances = portfolioEntry.activeLifeCycleInstance.lifeCycleMilestoneInstances;

    }

    /**
     * Constructor used in the case of the stakeholderTypes column must be
     * displayed.
     * 
     * note: this constructor is called when displaying the portfolio entries
     * for which the current user is a direct stakeholder, in order to display
     * his roles on the portfolio entry
     * 
     * @param portfolioEntry
     *            the portfolio entry
     * @param stakeholders
     *            the stakeholders
     */
    public PortfolioEntryListView(PortfolioEntry portfolioEntry, List<Stakeholder> stakeholders) {

        this(portfolioEntry);
        this.stakeholderTypes.addAll(stakeholders.stream().map(stakeholder -> stakeholder.stakeholderType.getName()).collect(Collectors.toList()));

    }

}
