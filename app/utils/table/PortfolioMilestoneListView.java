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

import java.util.Date;

import models.governance.LifeCycleMilestone;
import models.governance.PlannedLifeCycleMilestoneInstance;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.DateFormatter;

/**
 * A portfolio milestone list view is used to display a milestone row in a
 * portfolio context.
 * 
 * @author Johann Kohler
 */
public class PortfolioMilestoneListView {

    public static Table<PortfolioMilestoneListView> templateTable = new Table<PortfolioMilestoneListView>() {
        {
            setIdFieldName("id");

            addColumn("portfolioEntryName", "portfolioEntryName", "object.life_cycle_milestone_instance.portfolio_entry.label",
                    Table.ColumnDef.SorterType.NONE);

            addColumn("milestone", "milestone", "object.life_cycle_milestone_instance.milestone.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("milestone", new IColumnFormatter<PortfolioMilestoneListView>() {
                @Override
                public String apply(PortfolioMilestoneListView portfolioMilestoneListView, Object value) {
                    return views.html.modelsparts.display_milestone.render(portfolioMilestoneListView.milestone).body();
                }
            });

            addColumn("lastPlannedDate", "lastPlannedDate", "object.planned_life_cycle_milestone_instance.planned_date.label",
                    Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("lastPlannedDate", new DateFormatter<PortfolioMilestoneListView>());

            this.setLineAction(new IColumnFormatter<PortfolioMilestoneListView>() {
                @Override
                public String apply(PortfolioMilestoneListView governanceListView, Object value) {
                    return controllers.core.routes.PortfolioEntryGovernanceController.index(governanceListView.portfolioEntryId).url();
                }
            });

            setEmptyMessageKey("object.life_cycle_milestone.table.empty");

        }
    };

    /**
     * Default constructor.
     */
    public PortfolioMilestoneListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public String portfolioEntryName;

    public LifeCycleMilestone milestone;

    public Date lastPlannedDate;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param plannedMilestone
     *            the planned milestone instance in the DB
     */
    public PortfolioMilestoneListView(PlannedLifeCycleMilestoneInstance plannedMilestone) {
        this.id = plannedMilestone.lifeCycleMilestone.id;
        this.portfolioEntryId = plannedMilestone.lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntry.id;
        this.portfolioEntryName = plannedMilestone.lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntry.getName();
        this.milestone = plannedMilestone.lifeCycleMilestone;
        this.lastPlannedDate = plannedMilestone.plannedDate;
    }

}
