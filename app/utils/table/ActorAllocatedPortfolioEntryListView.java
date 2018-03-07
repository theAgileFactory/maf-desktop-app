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

import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioEntryDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.FilterConfig;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Table;
import framework.utils.formats.ListOfValuesFormatter;
import framework.utils.formats.ObjectFormatter;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;

/**
 * @author Guillaume Petit
 */
public class ActorAllocatedPortfolioEntryListView extends  AllocatedActorListView {

    public static class ActorAllocatedPortfolioEntryTableDefinition extends TableDefinition {

        public FilterConfig<ActorAllocatedPortfolioEntryListView> filterConfig;
        public Table<ActorAllocatedPortfolioEntryListView> templateTable;

        public ActorAllocatedPortfolioEntryTableDefinition(II18nMessagesPlugin ii18nMessagesPlugin) {
            super();
            this.filterConfig = getFilterConfig();
            this.templateTable = getTable(ii18nMessagesPlugin);
        }

        public FilterConfig<ActorAllocatedPortfolioEntryListView> getFilterConfig() {
            return new FilterConfig<ActorAllocatedPortfolioEntryListView>() {
                {
                    addColumnConfiguration("portfolioEntryName",
                            "portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.name",
                            "object.allocated_resource.portfolio_entry.label", new TextFieldFilterComponent("*"), true, false, SortStatusType.NONE);

                    ISelectableValueHolderCollection<Long> orgUnits = OrgUnitDao.getOrgUnitActiveAsVH();
                    if (orgUnits.getValues().size() > 0) {
                        addColumnConfiguration("orgUnit", "actor.orgUnit.id", "object.allocated_resource.org_unit.label",
                                new SelectFilterComponent(orgUnits.getValues().iterator().next().getValue(), orgUnits, new String[] {"actor.orgUnit.name"}), false, false,
                                SortStatusType.UNSORTED);
                    } else {
                        addColumnConfiguration("orgUnit", "actor.orgUnit.id", "object.allocated_resource.org_unit.label",
                                new NoneFilterComponent(), false, false, SortStatusType.NONE);
                    }

                    initFilterConfig(this);

                }
            };
        }

        public Table<ActorAllocatedPortfolioEntryListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<ActorAllocatedPortfolioEntryListView>() {
                {
                    addColumn("portfolioEntryName", "portfolioEntryName", "object.allocated_resource.portfolio_entry.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryName", (view, value) -> views.html.modelsparts.display_portfolio_entry.render(view.portfolioEntry, true).body());

                    addColumn("orgUnit", "orgUnit", "object.allocated_resource.org_unit.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("orgUnit", (view, value) -> views.html.modelsparts.display_org_unit.render(view.actor.orgUnit).body());
                    this.setColumnValueCssClass("orgUnit", "rowlink-skip");

                    initTable(this, i18nMessagesPlugin);
                }
            };
        }
    }

    public ActorAllocatedPortfolioEntryListView() {
    }

    public ActorAllocatedPortfolioEntryListView(PortfolioEntryResourcePlanAllocatedActor allocatedActor) {
        super(allocatedActor);
        this.portfolioEntryName = PortfolioEntryDao.getPEById(this.portfolioEntry.id).getName();
    }

    public String portfolioEntryName;
}