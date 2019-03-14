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

import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ObjectFormatter;
import models.governance.LifeCycleMilestoneInstanceApprover;
import models.governance.LifeCycleMilestoneInstanceApprover.Status;
import models.pmo.Actor;
import models.pmo.OrgUnit;

/**
 * A milestone approver list view is used to display a milestone approver row in
 * a table.
 * 
 * @author Johann Kohler
 */
public class MilestoneApproverListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<MilestoneApproverListView> templateTable;

        public TableDefinition() {
            this.templateTable = getTable();
        }

        public Table<MilestoneApproverListView> getTable() {
            return new Table<MilestoneApproverListView>() {
                {
                    setIdFieldName("id");

                    addColumn("actor", "actor", "object.life_cycle_milestone_instance_approver.actor.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("actor", (milestoneApproverListView, value) -> views.html.modelsparts.display_actor.render(milestoneApproverListView.actor).body());

                    addColumn("orgUnit", "orgUnit", "object.life_cycle_milestone_instance_approver.org_unit.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("orgUnit", (milestoneApproverListView, value) -> views.html.modelsparts.display_org_unit.render(milestoneApproverListView.orgUnit).body());

                    addColumn("status", "status", "object.life_cycle_milestone_instance_approver.status.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("status", (milestoneApproverListView, value) -> {
                        Status status = (Status) value;
                        switch (status) {
                        case APPROVED:
                            return "<span class=\"label label-success\">" + Msg.get("object.life_cycle_milestone_instance_approver.status.APPROVED.label")
                                    + "</span>";
                        case PENDING:
                            return "<span class=\"label label-warning\">" + Msg.get("object.life_cycle_milestone_instance_approver.status.PENDING.label")
                                    + "</span>";
                        case REJECTED:
                            return "<span class=\"label label-danger\">" + Msg.get("object.life_cycle_milestone_instance_approver.status.REJECTED.label")
                                    + "</span>";
                        case NOT_VOTED:
                            return "<span class=\"label label-primary\">"
                                    + Msg.get("object.life_cycle_milestone_instance_approver.status.NOT_VOTED.label") + "</span>";
                        }
                        return "";
                    });

                    addColumn("approvalDate", "approvalDate", "object.life_cycle_milestone_instance_approver.approval_date.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("approvalDate", new DateFormatter<>());

                    addColumn("comments", "comments", "object.life_cycle_milestone_instance_approver.comments.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("comments", new ObjectFormatter<>());

                    setEmptyMessageKey("object.life_cycle_milestone_instance_approver.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public MilestoneApproverListView() {
    }

    public Long id;
    public Actor actor;
    public OrgUnit orgUnit;
    public Boolean hasApproved;
    public Date approvalDate;
    public String comments;
    public Status status;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param lifeCycleMilestoneInstanceApprover
     *            the approver in the DB
     */
    public MilestoneApproverListView(LifeCycleMilestoneInstanceApprover lifeCycleMilestoneInstanceApprover) {
        this.id = lifeCycleMilestoneInstanceApprover.actor != null ? lifeCycleMilestoneInstanceApprover.actor.id : lifeCycleMilestoneInstanceApprover.orgUnit.id;
        this.actor = lifeCycleMilestoneInstanceApprover.actor;
        this.orgUnit = lifeCycleMilestoneInstanceApprover.actor == null ? lifeCycleMilestoneInstanceApprover.orgUnit : lifeCycleMilestoneInstanceApprover.actor.orgUnit;
        this.hasApproved = lifeCycleMilestoneInstanceApprover.hasApproved;
        this.approvalDate = lifeCycleMilestoneInstanceApprover.approvalDate;
        this.comments = lifeCycleMilestoneInstanceApprover.comments;
        this.status = lifeCycleMilestoneInstanceApprover.getStatus();
    }
}
