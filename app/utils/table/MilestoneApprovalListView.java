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

import dao.governance.LifeCycleMilestoneDao;
import dao.governance.LifeCycleProcessDao;
import dao.pmo.PortfolioEntryEventDao;
import framework.utils.FilterConfig;
import framework.utils.IColumnFormatter;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.FilterConfig.AutocompleteFilterComponent;
import framework.utils.FilterConfig.DateRangeFilterComponent;
import framework.utils.FilterConfig.SelectFilterComponent;
import framework.utils.FilterConfig.SortStatusType;
import framework.utils.FilterConfig.TextFieldFilterComponent;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ObjectFormatter;
import models.governance.LifeCycleMilestoneInstance;
import models.pmo.Actor;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryEvent;

/**
 * An milestone approval list view is used to display a milestone approval row
 * in a table.
 * 
 * @author Johann Kohler
 */
public class MilestoneApprovalListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<MilestoneApprovalListView> templateTable;
        public FilterConfig<MilestoneApprovalListView> filterConfig;

        public TableDefinition() {
        	this.filterConfig = getFilterConfig();
            this.templateTable = getTable();
        }
        public FilterConfig<MilestoneApprovalListView> getFilterConfig() {
            return new FilterConfig<MilestoneApprovalListView>() {
                {
                 	addColumnConfiguration("portfolioEntryGovernanceId", "lifeCycleInstance.portfolioEntry.governanceId", "object.portfolio_entry.governance_id.label",
                 			new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);          	               	

                    addColumnConfiguration("portfolioEntry", "lifeCycleInstance.portfolioEntry.name", "object.life_cycle_milestone_instance.portfolio_entry.label",
                    		 new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);

                 	addColumnConfiguration("portfolioEntryManager", "lifeCycleInstance.portfolioEntry.manager.id", "object.portfolio_entry.manager.label",
                            new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url()), true, false, SortStatusType.UNSORTED);

                    ISelectableValueHolderCollection<Long> lcProcessActiveAsVH = LifeCycleProcessDao.getLCProcessActiveAsVH();
                    addColumnConfiguration("lifeCycleName", "lifeCycleInstance.lifeCycleProcess.id", "object.portfolio_entry.life_cycle_process.label",
                   		 new SelectFilterComponent(null, lcProcessActiveAsVH, new String[]{"lifeCycleInstance.lifeCycleProcess.name"}), true, false, SortStatusType.UNSORTED);

                 	ISelectableValueHolderCollection<Long> lifeCycleMilestones = LifeCycleMilestoneDao.getLCMilestoneActiveAsVH();
                 	addColumnConfiguration("milestoneInstance", "lifeCycleMilestone.id", "object.life_cycle_milestone_instance.milestone.label",
                 			 new SelectFilterComponent(null, lifeCycleMilestones, new String[]{"lifeCycleMilestone.name"}), true, false, SortStatusType.UNSORTED);

                 	addColumnConfiguration("dueDate", "passedDate", "object.life_cycle_milestone_instance.due_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);
                }
            };
        }

        public Table<MilestoneApprovalListView> getTable() {
            return new Table<MilestoneApprovalListView>() {
                {
                    setIdFieldName("id");

                    addColumn("portfolioEntryGovernanceId", "portfolioEntryGovernanceId", "object.portfolio_entry.governance_id.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryGovernanceId", new ObjectFormatter<>());

                    addColumn("portfolioEntry", "portfolioEntry", "object.life_cycle_milestone_instance.portfolio_entry.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntry", (milestoneApprovalListView, value) -> views.html.modelsparts.display_portfolio_entry.render(milestoneApprovalListView.portfolioEntry, true).body());
                    this.setColumnValueCssClass("portfolioEntry", "rowlink-skip");

                    addColumn("portfolioEntryManager", "portfolioEntryManager", "object.portfolio_entry.manager.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryManager", (milestoneApprovalListView, value) -> views.html.modelsparts.display_actor.render(milestoneApprovalListView.portfolioEntryManager).body());
                    this.setColumnValueCssClass("portfolioEntryManager", "rowlink-skip");

                    addColumn("lifeCycleName", "lifeCycleName", "object.portfolio_entry.life_cycle_process.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lifeCycleName", new ObjectFormatter<>());

                    addColumn("milestoneInstance", "milestoneInstance", "object.life_cycle_milestone_instance.milestone.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("milestoneInstance", (milestoneApprovalListView, value) -> views.html.modelsparts.display_milestone.render(milestoneApprovalListView.milestoneInstance.lifeCycleMilestone).body());

                    addColumn("dueDate", "dueDate", "object.life_cycle_milestone_instance.due_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("dueDate", (milestoneApprovalListView, value) -> {
                        DateFormatter<MilestoneApprovalListView> df = new DateFormatter<>();
                        if (milestoneApprovalListView.dueDate != null) {
                            Calendar c = Calendar.getInstance();
                            c.setTime(new Date());
                            c.add(Calendar.DATE, 3);
                            df.setAlert(milestoneApprovalListView.dueDate.before(c.getTime()));
                        }
                        return df.apply(milestoneApprovalListView, value);
                    });

                    this.setLineAction((milestoneApprovalListView, value) -> controllers.core.routes.MilestoneApprovalController.process(milestoneApprovalListView.id).url());

                    setEmptyMessageKey("object.life_cycle_milestone_instance.table.approval.empty");
                }
            };

        }

    }

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
        this.lifeCycleName = portfolioEntry.activeLifeCycleInstance.lifeCycleProcess.getName();

        this.milestoneInstance = milestoneInstance;

        this.dueDate = milestoneInstance.passedDate;

    }
}
