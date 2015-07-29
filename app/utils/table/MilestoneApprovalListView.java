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

import java.util.Calendar;
import java.util.Date;

import models.governance.LifeCycleMilestoneInstance;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ObjectFormatter;

/**
 * An milestone approval list view is used to display a milestone approval row
 * in a table.
 * 
 * @author Johann Kohler
 */
public class MilestoneApprovalListView {

    public static Table<MilestoneApprovalListView> templateTable = new Table<MilestoneApprovalListView>() {
        {
            setIdFieldName("id");

            addColumn("portfolioEntryGovernanceId", "portfolioEntryGovernanceId", "object.portfolio_entry.governance_id.label",
                    Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("portfolioEntryGovernanceId", new ObjectFormatter<MilestoneApprovalListView>());

            addColumn("portfolioEntry", "portfolioEntry", "object.life_cycle_milestone_instance.portfolio_entry.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("portfolioEntry", new IColumnFormatter<MilestoneApprovalListView>() {
                @Override
                public String apply(MilestoneApprovalListView milestoneApprovalListView, Object value) {
                    return views.html.modelsparts.display_portfolio_entry.render(milestoneApprovalListView.portfolioEntry, true).body();
                }
            });
            this.setColumnValueCssClass("portfolioEntry", "rowlink-skip");

            addColumn("portfolioEntryManager", "portfolioEntryManager", "object.portfolio_entry.manager.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("portfolioEntryManager", new IColumnFormatter<MilestoneApprovalListView>() {
                @Override
                public String apply(MilestoneApprovalListView milestoneApprovalListView, Object value) {
                    return views.html.modelsparts.display_actor.render(milestoneApprovalListView.portfolioEntryManager).body();
                }
            });
            this.setColumnValueCssClass("portfolioEntryManager", "rowlink-skip");

            addColumn("lifeCycleName", "lifeCycleName", "object.portfolio_entry.life_cycle_process.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("lifeCycleName", new ObjectFormatter<MilestoneApprovalListView>());

            addColumn("milestoneInstance", "milestoneInstance", "object.life_cycle_milestone_instance.milestone.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("milestoneInstance", new IColumnFormatter<MilestoneApprovalListView>() {
                @Override
                public String apply(MilestoneApprovalListView milestoneApprovalListView, Object value) {
                    return views.html.modelsparts.display_milestone.render(milestoneApprovalListView.milestoneInstance.lifeCycleMilestone).body();
                }
            });

            addColumn("dueDate", "dueDate", "object.life_cycle_milestone_instance.due_date.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("dueDate", new IColumnFormatter<MilestoneApprovalListView>() {
                @Override
                public String apply(MilestoneApprovalListView milestoneApprovalListView, Object value) {
                    DateFormatter<MilestoneApprovalListView> df = new DateFormatter<MilestoneApprovalListView>();
                    if (milestoneApprovalListView.dueDate != null) {
                        Calendar c = Calendar.getInstance();
                        c.setTime(new Date());
                        c.add(Calendar.DATE, 3);
                        df.setAlert(milestoneApprovalListView.dueDate.before(c.getTime()));
                    }
                    return df.apply(milestoneApprovalListView, value);
                }
            });

            this.setLineAction(new IColumnFormatter<MilestoneApprovalListView>() {
                @Override
                public String apply(MilestoneApprovalListView milestoneApprovalListView, Object value) {
                    return controllers.core.routes.MilestoneApprovalController.process(milestoneApprovalListView.id).url();
                }
            });

            setEmptyMessageKey("object.life_cycle_milestone_instance.table.approval.empty");
        }
    };

    public Long id;

    public String portfolioEntryGovernanceId;
    public PortfolioEntry portfolioEntry;
    public Actor portfolioEntryManager;
    public String lifeCycleName;
    public LifeCycleMilestoneInstance milestoneInstance;
    public Date dueDate;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param milestoneInstance
     *            the milestone instance in the DB
     */
    public MilestoneApprovalListView(LifeCycleMilestoneInstance milestoneInstance) {

        this.id = milestoneInstance.id;

        PortfolioEntry portfolioEntry = milestoneInstance.lifeCycleInstance.portfolioEntry;
        this.portfolioEntryGovernanceId = portfolioEntry.governanceId;
        this.portfolioEntry = portfolioEntry;
        this.portfolioEntryManager = portfolioEntry.manager;
        this.lifeCycleName = portfolioEntry.activeLifeCycleInstance.lifeCycleProcess.getShortName();

        this.milestoneInstance = milestoneInstance;

        this.dueDate = milestoneInstance.passedDate;

    }
}
