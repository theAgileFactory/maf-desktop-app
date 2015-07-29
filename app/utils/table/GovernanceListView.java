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
import java.util.List;

import models.governance.LifeCycleMilestone;
import models.governance.LifeCycleMilestoneInstance;
import models.governance.PlannedLifeCycleMilestoneInstance;
import constants.IMafConstants;
import dao.governance.LifeCycleMilestoneDao;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ListOfValuesFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * A governance list view is used to display a governance row in a table.
 * 
 * a governance row is a milestone with its last planned date and the list of
 * milestone instances for a portfolio entry
 * 
 * @author Johann Kohler
 */
public class GovernanceListView {

    public static Table<GovernanceListView> templateTable = new Table<GovernanceListView>() {
        {
            setIdFieldName("id");

            addColumn("milestone", "milestone", "object.life_cycle_milestone_instance.milestone.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("milestone", new IColumnFormatter<GovernanceListView>() {
                @Override
                public String apply(GovernanceListView governanceListView, Object value) {
                    return views.html.modelsparts.display_milestone.render(governanceListView.milestone).body();
                }
            });
            setColumnCssClass("milestone", IMafConstants.BOOTSTRAP_COLUMN_4);

            addColumn("lastPlannedDate", "lastPlannedDate", "object.planned_life_cycle_milestone_instance.planned_date.label",
                    Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("lastPlannedDate", new IColumnFormatter<GovernanceListView>() {
                @Override
                public String apply(GovernanceListView governanceListView, Object value) {
                    DateFormatter<GovernanceListView> df = new DateFormatter<GovernanceListView>();
                    if (governanceListView.lastPlannedDate != null) {
                        df.setAlert(!governanceListView.hasMilestoneInstances && governanceListView.lastPlannedDate.before(new Date()));
                    }
                    return df.apply(governanceListView, value);
                }
            });
            setColumnCssClass("lastPlannedDate", IMafConstants.BOOTSTRAP_COLUMN_4);

            addColumn("status", "status", "object.life_cycle_milestone_instance.status.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("status", new ListOfValuesFormatter<GovernanceListView>());
            setColumnCssClass("status", IMafConstants.BOOTSTRAP_COLUMN_3);

            addColumn("requestActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("requestActionLink", new StringFormatFormatter<GovernanceListView>(IMafConstants.REQUEST_URL_FORMAT,
                    new StringFormatFormatter.Hook<GovernanceListView>() {
                        @Override
                        public String convert(GovernanceListView governanceListView) {
                            if (!governanceListView.isPending) {
                                return controllers.core.routes.PortfolioEntryGovernanceController.requestMilestone(governanceListView.portfolioEntryId,
                                        governanceListView.id).url();
                            }
                            return null;
                        }
                    }));
            setColumnCssClass("requestActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("requestActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            this.setLineAction(new IColumnFormatter<GovernanceListView>() {
                @Override
                public String apply(GovernanceListView governanceListView, Object value) {
                    return controllers.core.routes.PortfolioEntryGovernanceController.viewMilestone(governanceListView.portfolioEntryId,
                            governanceListView.id).url();
                }
            });

        }
    };

    /**
     * Default constructor.
     */
    public GovernanceListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public Long planningId;

    public LifeCycleMilestone milestone;

    public Date lastPlannedDate;

    public List<String> status = new ArrayList<String>();

    public Boolean hasMilestoneInstances;
    public Boolean isPending = false;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param plannedMilestone
     *            the planned milestone instance in the DB
     */
    public GovernanceListView(PlannedLifeCycleMilestoneInstance plannedMilestone) {
        this.id = plannedMilestone.lifeCycleMilestone.id;
        this.portfolioEntryId = plannedMilestone.lifeCycleInstancePlanning.lifeCycleInstance.portfolioEntry.id;
        this.milestone = plannedMilestone.lifeCycleMilestone;
        this.lastPlannedDate = plannedMilestone.plannedDate;
        this.planningId = plannedMilestone.lifeCycleInstancePlanning.id;

        // get the milestone instances
        List<LifeCycleMilestoneInstance> milestoneInstances =
                LifeCycleMilestoneDao.getLCMilestoneInstanceAsListByPEAndLCMilestone(this.portfolioEntryId, plannedMilestone.lifeCycleMilestone.id);

        // compute hasMilestoneInstances
        this.hasMilestoneInstances = milestoneInstances != null && !milestoneInstances.isEmpty() ? true : false;

        // compute status
        String format = null;
        String content = "";

        if (hasMilestoneInstances) {

            for (LifeCycleMilestoneInstance milestone : milestoneInstances) {

                switch (milestone.getStatus()) {
                case APPROVED:
                    content = milestone.lifeCycleMilestoneInstanceStatusType.getName();
                    format = IMafConstants.LABEL_SUCCESS_FORMAT;
                    break;
                case PENDING:
                    content = Msg.get("object.life_cycle_milestone_instance.status." + milestone.getStatus() + ".label");
                    format = IMafConstants.LABEL_WARNING_FORMAT;
                    isPending = true;
                    break;
                case REJECTED:
                    content = milestone.lifeCycleMilestoneInstanceStatusType.getName();
                    format = IMafConstants.LABEL_DANGER_FORMAT;
                    break;
                case UNKNOWN:
                    content = Msg.get("object.life_cycle_milestone_instance.status." + milestone.getStatus() + ".label");
                    format = IMafConstants.LABEL_DEFAULT_FORMAT;
                    break;
                }

                if (milestone.passedDate != null) {
                    content += " (" + Utilities.getDateFormat(null).format(milestone.passedDate) + ")";
                }

                status.add("<div style='height: 26px;'>" + String.format(format, content) + "</div>");
            }

        } else {

            content = Msg.get("object.planned_life_cycle_milestone_instance.status." + plannedMilestone.getStatus() + ".label");

            switch (plannedMilestone.getStatus()) {
            case AVAILABLE:
                format = IMafConstants.LABEL_PRIMARY_FORMAT;
                break;
            case NOT_PLANNED:
                format = IMafConstants.LABEL_DEFAULT_FORMAT;
                break;
            }

            status.add(String.format(format, content));
        }

    }

}
