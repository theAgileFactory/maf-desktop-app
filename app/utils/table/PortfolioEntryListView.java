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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import controllers.core.CockpitController;
import dao.governance.LifeCycleMilestoneDao;
import dao.governance.LifeCycleProcessDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.PortfolioEntryReportDao;
import framework.utils.FilterConfig;
import framework.utils.IColumnFormatter;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ListOfValuesFormatter;
import framework.utils.formats.ObjectFormatter;
import models.governance.LifeCycleMilestoneInstance;
import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryDependency;
import models.pmo.PortfolioEntryReport;
import models.pmo.PortfolioEntryType;
import models.pmo.Stakeholder;

/**
 * A portfolio entry list view is used to display an portfolio entry row in a
 * table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryListView {

    public static FilterConfig<PortfolioEntryListView> filterConfig = getFilterConfig();

    /**
     * Get the filter config.
     */
    public static FilterConfig<PortfolioEntryListView> getFilterConfig() {
        return new FilterConfig<PortfolioEntryListView>() {
            {

                addColumnConfiguration("governanceId", "governanceId", "object.portfolio_entry.governance_id.label", new TextFieldFilterComponent("*"), true,
                        false, SortStatusType.ASC);

                addColumnConfiguration("creationDate", "creationDate", "object.portfolio_entry.creation_date.label",
                        new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), false, false, SortStatusType.NONE);

                addColumnConfiguration("isPublic", "isPublic", "object.portfolio_entry.is_public.label", new CheckboxFilterComponent(true), false, false,
                        SortStatusType.NONE);

                addColumnConfiguration("name", "name", "object.portfolio_entry.name.label", new TextFieldFilterComponent("*"), true, false,
                        SortStatusType.UNSORTED);

                ISelectableValueHolderCollection<Long> portfolioEntryTypes = PortfolioEntryDao.getPETypeActiveAsVH();
                if (portfolioEntryTypes != null && portfolioEntryTypes.getValues().size() > 0) {
                    addColumnConfiguration("portfolioEntryType", "portfolioEntryType.id", "object.portfolio_entry.type.label",
                            new SelectFilterComponent(portfolioEntryTypes.getValues().iterator().next().getValue(), portfolioEntryTypes), true, false,
                            SortStatusType.UNSORTED);
                } else {
                    addColumnConfiguration("portfolioEntryType", "portfolioEntryType.id", "object.portfolio_entry.type.label", new NoneFilterComponent(),
                            false, false, SortStatusType.NONE);
                }

                addColumnConfiguration("manager", "manager.id", "object.portfolio_entry.manager.label",
                        new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url()), false, false, SortStatusType.NONE);

                ISelectableValueHolderCollection<Long> sponsoringUnits = OrgUnitDao.getOrgUnitActiveCanSponsorAsVH();
                if (sponsoringUnits != null && sponsoringUnits.getValues().size() > 0) {
                    addColumnConfiguration("sponsoringUnit", "sponsoringUnit.id", "object.portfolio_entry.sponsoring_unit.label",
                            new SelectFilterComponent(sponsoringUnits.getValues().iterator().next().getValue(), sponsoringUnits), false, false,
                            SortStatusType.NONE);
                } else {
                    addColumnConfiguration("sponsoringUnit", "sponsoringUnit.id", "object.portfolio_entry.sponsoring_unit.label", new NoneFilterComponent(),
                            false, false, SortStatusType.NONE);
                }

                ISelectableValueHolderCollection<Long> deliveryUnits = OrgUnitDao.getOrgUnitActiveCanDeliverAsVH();
                if (deliveryUnits != null && deliveryUnits.getValues().size() > 0) {
                    addColumnConfiguration("deliveryUnits", "deliveryUnits.id", "object.portfolio_entry.delivery_units.label",
                            new SelectFilterComponent(deliveryUnits.getValues().iterator().next().getValue(), deliveryUnits), false, false,
                            SortStatusType.NONE);
                } else {
                    addColumnConfiguration("deliveryUnits", "deliveryUnits.id", "object.portfolio_entry.delivery_units.label", new NoneFilterComponent(),
                            false, false, SortStatusType.NONE);
                }

                ISelectableValueHolderCollection<Long> portfolios = PortfolioDao.getPortfolioActiveAsVH();
                if (portfolios != null && portfolios.getValues().size() > 0) {
                    addColumnConfiguration("portfolios", "portfolios.id", "object.portfolio_entry.portfolios.label",
                            new SelectFilterComponent(portfolios.getValues().iterator().next().getValue(), portfolios), false, false, SortStatusType.NONE);
                } else {
                    addColumnConfiguration("portfolios", "portfolios.id", "object.portfolio_entry.portfolios.label", new NoneFilterComponent(), false, false,
                            SortStatusType.NONE);
                }

                addColumnConfiguration("stakeholders", "stakeholders.actor.id", "object.portfolio_entry.stakeholders.label",
                        new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url()), false, false, SortStatusType.NONE);

                addColumnConfiguration("dependencies", "dependencies.id", "object.portfolio_entry.dependencies.label", new NoneFilterComponent(), false,
                        false, SortStatusType.NONE);

                ISelectableValueHolderCollection<Long> lifeCycleProcesses = LifeCycleProcessDao.getLCProcessActiveAsVH();
                if (lifeCycleProcesses != null && lifeCycleProcesses.getValues().size() > 0) {
                    addColumnConfiguration("lifeCycleProcess", "activeLifeCycleInstance.lifeCycleProcess.id",
                            "object.portfolio_entry.life_cycle_process.label",
                            new SelectFilterComponent(lifeCycleProcesses.getValues().iterator().next().getValue(), lifeCycleProcesses), false, false,
                            SortStatusType.NONE);
                } else {
                    addColumnConfiguration("lifeCycleProcess", "activeLifeCycleInstance.lifeCycleProcess.id",
                            "object.portfolio_entry.life_cycle_process.label", new NoneFilterComponent(), false, false, SortStatusType.NONE);
                }

                ISelectableValueHolderCollection<Long> portfolioEntryReportStatusTypes = PortfolioEntryReportDao.getPEReportStatusTypeActiveAsVH();
                if (portfolioEntryReportStatusTypes != null && portfolioEntryReportStatusTypes.getValues().size() > 0) {
                    addColumnConfiguration("portfolioEntryStatus", "lastPortfolioEntryReport.portfolioEntryReportStatusType.id",
                            "object.portfolio_entry.status.label",
                            new SelectFilterComponent(portfolioEntryReportStatusTypes.getValues().iterator().next().getValue(),
                                    portfolioEntryReportStatusTypes),
                            true, false, SortStatusType.NONE);
                } else {
                    addColumnConfiguration("portfolioEntryStatus", "lastPortfolioEntryReport.portfolioEntryReportStatusType.id",
                            "object.portfolio_entry.status.label", new NoneFilterComponent(), false, false, SortStatusType.NONE);
                }

                ISelectableValueHolderCollection<Long> lifeCycleMilestones = LifeCycleMilestoneDao.getLCMilestoneActiveAsVH();
                if (lifeCycleMilestones != null && lifeCycleMilestones.getValues().size() > 0) {
                    addColumnConfiguration("lastMilestone", "lastApprovedLifeCycleMilestoneInstance.lifeCycleMilestone.id",
                            "object.portfolio_entry.last_milestone.label",
                            new SelectFilterComponent(lifeCycleMilestones.getValues().iterator().next().getValue(), lifeCycleMilestones), true, false,
                            SortStatusType.NONE);
                } else {
                    addColumnConfiguration("lastMilestone", "lastApprovedLifeCycleMilestoneInstance.lifeCycleMilestone.id",
                            "object.portfolio_entry.last_milestone.label", new NoneFilterComponent(), false, false, SortStatusType.NONE);
                }

                addColumnConfiguration("archived", "archived", "object.portfolio_entry.archived.label", new CheckboxFilterComponent(false), false, true,
                        SortStatusType.NONE);

                addColumnConfiguration("isConcept", "activeLifeCycleInstance.isConcept", "object.portfolio_entry.is_concept.label",
                        new CheckboxFilterComponent(false), false, false, SortStatusType.NONE);

                addKpis("id", PortfolioEntry.class);

                addCustomAttributesColumns("id", PortfolioEntry.class);
            }
        };
    }

    public static Table<PortfolioEntryListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<PortfolioEntryListView> getTable() {
        return new Table<PortfolioEntryListView>() {
            {

                setIdFieldName("id");

                addColumn("governanceId", "governanceId", "object.portfolio_entry.governance_id.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("governanceId", new ObjectFormatter<PortfolioEntryListView>());

                addColumn("creationDate", "creationDate", "object.portfolio_entry.creation_date.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("creationDate", new DateFormatter<PortfolioEntryListView>());

                addColumn("name", "name", "object.portfolio_entry.name.label", Table.ColumnDef.SorterType.NONE);

                addColumn("portfolioEntryType", "portfolioEntryType", "object.portfolio_entry.type.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("portfolioEntryType", new IColumnFormatter<PortfolioEntryListView>() {
                    @Override
                    public String apply(PortfolioEntryListView portfolioEntryListView, Object value) {
                        return views.html.framework_views.parts.formats.display_value_holder.render(portfolioEntryListView.portfolioEntryType, true).body();
                    }
                });

                addColumn("manager", "manager", "object.portfolio_entry.manager.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("manager", new IColumnFormatter<PortfolioEntryListView>() {
                    @Override
                    public String apply(PortfolioEntryListView portfolioEntryListView, Object value) {
                        return views.html.modelsparts.display_actor.render(portfolioEntryListView.manager).body();
                    }
                });
                this.setColumnValueCssClass("manager", "rowlink-skip");

                addColumn("sponsoringUnit", "sponsoringUnit", "object.portfolio_entry.sponsoring_unit.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("sponsoringUnit", new IColumnFormatter<PortfolioEntryListView>() {
                    @Override
                    public String apply(PortfolioEntryListView portfolioEntryListView, Object value) {
                        return views.html.modelsparts.display_org_unit.render(portfolioEntryListView.sponsoringUnit).body();
                    }
                });
                this.setColumnValueCssClass("sponsoringUnit", "rowlink-skip");

                addColumn("deliveryUnits", "deliveryUnits", "object.portfolio_entry.delivery_units.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("deliveryUnits", new ListOfValuesFormatter<PortfolioEntryListView>());
                this.setColumnValueCssClass("deliveryUnits", "rowlink-skip");

                addColumn("portfolios", "portfolios", "object.portfolio_entry.portfolios.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("portfolios", new ListOfValuesFormatter<PortfolioEntryListView>());
                this.setColumnValueCssClass("portfolios", "rowlink-skip");

                addColumn("stakeholders", "stakeholders", "object.portfolio_entry.stakeholders.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("stakeholders", new ListOfValuesFormatter<PortfolioEntryListView>());
                this.setColumnValueCssClass("stakeholders", "rowlink-skip");

                addColumn("dependencies", "dependencies", "object.portfolio_entry.dependencies.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("dependencies", new ListOfValuesFormatter<PortfolioEntryListView>());
                this.setColumnValueCssClass("dependencies", "rowlink-skip");

                addColumn("lifeCycleProcess", "lifeCycleProcess", "object.portfolio_entry.life_cycle_process.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("lifeCycleProcess", new ObjectFormatter<PortfolioEntryListView>());

                addColumn("portfolioEntryStatus", "portfolioEntryStatus", "object.portfolio_entry.status.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("portfolioEntryStatus", new IColumnFormatter<PortfolioEntryListView>() {
                    @Override
                    public String apply(PortfolioEntryListView portfolioEntryView, Object value) {
                        return views.html.modelsparts.display_portfolio_entry_report.render(portfolioEntryView.portfolioEntryStatus).body();
                    }
                });
                this.setColumnValueCssClass("portfolioEntryStatus", "rowlink-skip");

                addColumn("archived", "archived", "object.portfolio_entry.archived.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("archived", new BooleanFormatter<PortfolioEntryListView>());

                addColumn("isConcept", "isConcept", "object.portfolio_entry.is_concept.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isConcept", new BooleanFormatter<PortfolioEntryListView>());

                addColumn("lastMilestone", "lastMilestone", "object.portfolio_entry.last_milestone.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("lastMilestone", new IColumnFormatter<PortfolioEntryListView>() {
                    @Override
                    public String apply(PortfolioEntryListView portfolioEntryView, Object value) {
                        return views.html.modelsparts.display_milestone_instance.render(portfolioEntryView.lastMilestone).body();
                    }
                });
                this.setColumnValueCssClass("lastMilestone", "rowlink-skip");

                addColumn("stakeholderTypes", "stakeholderTypes", "object.portfolio_entry.stakeholder_types.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("stakeholderTypes", new ListOfValuesFormatter<PortfolioEntryListView>());

                addColumn("isPublic", "isPublic", "object.portfolio_entry.is_public.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isPublic", new BooleanFormatter<PortfolioEntryListView>());

                addKpis(PortfolioEntry.class);

                addCustomAttributeColumns(PortfolioEntry.class);

                this.setLineAction(new IColumnFormatter<PortfolioEntryListView>() {
                    @Override
                    public String apply(PortfolioEntryListView portfolioEntryListView, Object value) {
                        return controllers.core.routes.PortfolioEntryController.overview(portfolioEntryListView.id).url();
                    }
                });

                setEmptyMessageKey("object.portfolio_entry.table.empty");
            }
        };
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
        Set<String> columns = new HashSet<String>();

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
        columns.add("dependencies");
        columns.add("lifeCycleProcess");
        columns.add("archived");

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
    public OrgUnit sponsoringUnit;
    public List<OrgUnit> deliveryUnits;
    public List<Portfolio> portfolios;
    public String lifeCycleProcess;
    public PortfolioEntryReport portfolioEntryStatus;
    public LifeCycleMilestoneInstance lastMilestone;
    public boolean isConcept;
    public boolean archived;
    public List<Actor> stakeholders;
    public List<PortfolioEntry> dependencies;

    // contextual attributes
    public List<String> stakeholderTypes = new ArrayList<String>();

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
        this.sponsoringUnit = portfolioEntry.sponsoringUnit;
        this.deliveryUnits = portfolioEntry.deliveryUnits;
        this.portfolios = portfolioEntry.portfolios;
        this.lifeCycleProcess = portfolioEntry.activeLifeCycleInstance != null ? portfolioEntry.activeLifeCycleInstance.lifeCycleProcess.getName() : null;
        this.portfolioEntryStatus = portfolioEntry.lastPortfolioEntryReport;
        this.lastMilestone = portfolioEntry.lastApprovedLifeCycleMilestoneInstance;
        this.isConcept = portfolioEntry.activeLifeCycleInstance != null ? portfolioEntry.activeLifeCycleInstance.isConcept : true;
        this.archived = portfolioEntry.archived;

        this.stakeholders = new ArrayList<>();
        Set<Long> actorIds = new HashSet<>();
        for (Stakeholder stakehoder : portfolioEntry.stakeholders) {
            if (!stakehoder.actor.deleted && !actorIds.contains(stakehoder.actor.id)) {
                actorIds.add(stakehoder.actor.id);
                this.stakeholders.add(stakehoder.actor);
            }
        }

        this.dependencies = new ArrayList<>();
        Set<Long> dependencyIds = new HashSet<>();
        for (PortfolioEntryDependency portfolioEntryDependency : portfolioEntry.getDestinationDependencies()) {
            PortfolioEntry dependency = portfolioEntryDependency.getSourcePortfolioEntry();
            if (dependency != null && !dependencyIds.contains(dependency.id)) {
                dependencyIds.add(dependency.id);
                this.dependencies.add(dependency);
            }
        }
        for (PortfolioEntryDependency portfolioEntryDependency : portfolioEntry.getSourceDependencies()) {
            PortfolioEntry dependency = portfolioEntryDependency.getDestinationPortfolioEntry();
            if (dependency != null && !dependencyIds.contains(dependency.id)) {
                dependencyIds.add(dependency.id);
                this.dependencies.add(dependency);
            }
        }

    }

    /**
     * Constructor used in the case of the stakeholderTypes column must be
     * displayed.
     * 
     * note: this constructor is called when displaying the portfolio entries
     * for which the current user is a direct stakeholder (see
     * {@link CockpitController}), in order to display his roles on the
     * portfolio entry
     * 
     * @param portfolioEntry
     *            the portfolio entry
     * @param stakeholders
     *            the stakeholders
     */
    public PortfolioEntryListView(PortfolioEntry portfolioEntry, List<Stakeholder> stakeholders) {

        this(portfolioEntry);
        for (Stakeholder stakeholder : stakeholders) {
            this.stakeholderTypes.add(stakeholder.stakeholderType.getName());
        }

    }

}
