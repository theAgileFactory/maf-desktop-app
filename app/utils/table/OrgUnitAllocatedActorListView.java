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

import dao.pmo.PortfolioEntryDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.FilterConfig;
import framework.utils.Table;
import framework.utils.formats.ObjectFormatter;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;

/**
 * @author maf
 */
public class OrgUnitAllocatedActorListView extends AllocatedActorListView {

    public static class OrgUnitAllocatedActorTableDefinition extends TableDefinition {

        public FilterConfig<OrgUnitAllocatedActorListView> filterConfig;
        public Table<OrgUnitAllocatedActorListView> templateTable;

        public OrgUnitAllocatedActorTableDefinition(II18nMessagesPlugin ii18nMessagesPlugin) {
            super();
            this.filterConfig = getFilterConfig();
            this.templateTable = getTable(ii18nMessagesPlugin);
        }

        public FilterConfig<OrgUnitAllocatedActorListView> getFilterConfig() {
            return new FilterConfig<OrgUnitAllocatedActorListView>() {
                {
                    String[] actorFieldsSort = { "actor.lastName", "actor.firstName" };
                    addColumnConfiguration("actor", "actor.id", "object.allocated_resource.actor.label",
                            new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url(), actorFieldsSort), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("portfolioEntryName",
                            "portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.name",
                            "object.allocated_resource.portfolio_entry.label", new TextFieldFilterComponent("*"), true, false, SortStatusType.NONE);

                    initFilterConfig(this);

                }
            };
        }

        public Table<OrgUnitAllocatedActorListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<OrgUnitAllocatedActorListView>() {
                {
                    addColumn("actor", "actor", "object.allocated_resource.actor.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("actor", (portfolioEntryResourcePlanAllocatedActorListView, value) -> views.html.modelsparts.display_actor.render(portfolioEntryResourcePlanAllocatedActorListView.actor).body());
                    setColumnValueCssClass("actor", "rowlink-skip");

                    addColumn("portfolioEntryName", "portfolioEntryName", "object.allocated_resource.portfolio_entry.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryName", new ObjectFormatter<>());

                    initTable(this, i18nMessagesPlugin);
                }
            };
        }
    }

    public OrgUnitAllocatedActorListView() {
    }

    public OrgUnitAllocatedActorListView(PortfolioEntryResourcePlanAllocatedActor allocatedActor) {
        super(allocatedActor);
        this.portfolioEntryName = PortfolioEntryDao.getPEById(this.portfolioEntry.id).getName();
    }

    public String portfolioEntryName;

}