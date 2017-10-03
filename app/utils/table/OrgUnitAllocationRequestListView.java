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
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;

/**
 * An org unit allocation request list view is used to
 * display a portfolio entry allocated org unit in a table.
 * 
 * @author Guillaume Petit<guillaume.petit@sword-group.com>
 */
public class OrgUnitAllocationRequestListView extends AllocatedOrgUnitListView {

    /**
     * The definition of the table.
     *
     * @author Johann Kohler
     */
    public static class OrgUnitAllocationRequestTableDefinition extends TableDefinition {

        public FilterConfig<OrgUnitAllocationRequestListView> filterConfig;
        public Table<OrgUnitAllocationRequestListView> templateTable;

        public OrgUnitAllocationRequestTableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            super(i18nMessagesPlugin);
            this.templateTable = getTable(i18nMessagesPlugin);
            this.filterConfig = getFilterConfig();
        }

        public FilterConfig<OrgUnitAllocationRequestListView> getFilterConfig() {

            return new FilterConfig<OrgUnitAllocationRequestListView>() {
                {
                    initFilterConfig(this);

                    addColumnConfiguration("portfolioEntryName", "portfolioEntryResourcePlan.lifeCycleInstancePlannings.lifeCycleInstance.portfolioEntry.name", "object.allocated_resource.portfolio_entry.label",
                            new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);
                }
            };


        }

        /**
         * Get the table.
         *
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */
        public Table<OrgUnitAllocationRequestListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<OrgUnitAllocationRequestListView>() {
                {
                    addColumn("portfolioEntryName", "portfolioEntryName", "object.allocated_resource.portfolio_entry.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryName", new ObjectFormatter<>());

                    initTable(this, i18nMessagesPlugin);
                }
            };

        }


    }

    /**
     * Default constructor.
     */
    public OrgUnitAllocationRequestListView() {
        super();
    }

    public String portfolioEntryName;

    /**
     * Construct a list view with a DB entry.
     *
     * @param allocatedOrgUnit
     *            the allocated org unit in the DB
     */
    public OrgUnitAllocationRequestListView(PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit) {
        super(allocatedOrgUnit);
        this.portfolioEntryName = PortfolioEntryDao.getPEById(this.portfolioEntryId).getName();
    }

}
