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

import java.text.MessageFormat;

import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.governance.LifeCycleMilestone;
import models.governance.LifeCyclePhase;

/**
 * An life cycle phase list view is used to display a life cycle phase row in a
 * table.
 * 
 * @author Johann Kohler
 */
public class LifeCyclePhaseListView {

    public static class TableDefinition {

        public Table<LifeCyclePhaseListView> templateTable;

        public TableDefinition() {
            this.templateTable = getTable();
        }

        public Table<LifeCyclePhaseListView> getTable() {
            return new Table<LifeCyclePhaseListView>() {
                {

                    setIdFieldName("id");

                    addColumn("changeOrder", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("changeOrder", new IColumnFormatter<LifeCyclePhaseListView>() {
                        @Override
                        public String apply(LifeCyclePhaseListView lifeCyclePhaseListView, Object value) {
                            return "<a href=\""
                                    + controllers.admin.routes.ConfigurationGovernanceController.changePhaseOrder(lifeCyclePhaseListView.id, false).url()
                                    + "\"><span class=\"fa fa-arrow-down\"></span></a>&nbsp;" + "<a href=\""
                                    + controllers.admin.routes.ConfigurationGovernanceController.changePhaseOrder(lifeCyclePhaseListView.id, true).url()
                                    + "\"><span class=\"fa fa-arrow-up\"></span></a>";
                        }
                    });
                    setColumnCssClass("changeOrder", IMafConstants.BOOTSTRAP_COLUMN_1);

                    addColumn("name", "name", "object.life_cycle_phase.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<LifeCyclePhaseListView>());

                    addColumn("startMilestone", "startMilestone", "object.life_cycle_phase.start_milestone.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("startMilestone", new IColumnFormatter<LifeCyclePhaseListView>() {
                        @Override
                        public String apply(LifeCyclePhaseListView lifeCyclePhaseListView, Object value) {
                            return views.html.modelsparts.display_milestone.render(lifeCyclePhaseListView.startMilestone).body();
                        }
                    });

                    addColumn("endMilestone", "endMilestone", "object.life_cycle_phase.end_milestone.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("endMilestone", new IColumnFormatter<LifeCyclePhaseListView>() {
                        @Override
                        public String apply(LifeCyclePhaseListView lifeCyclePhaseListView, Object value) {
                            return views.html.modelsparts.display_milestone.render(lifeCyclePhaseListView.endMilestone).body();
                        }
                    });

                    addColumn("gapDaysStart", "gapDaysStart", "object.life_cycle_phase.gap_days_start.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("gapDaysStart", new NumberFormatter<LifeCyclePhaseListView>());

                    addColumn("gapDaysEnd", "gapDaysEnd", "object.life_cycle_phase.gap_days_end.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("gapDaysEnd", new NumberFormatter<LifeCyclePhaseListView>());

                    addColumn("isRoadmapPhase", "isRoadmapPhase", "object.life_cycle_phase.is_roadmap_phase.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isRoadmapPhase", new BooleanFormatter<LifeCyclePhaseListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<LifeCyclePhaseListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<LifeCyclePhaseListView>() {
                        @Override
                        public String convert(LifeCyclePhaseListView lifeCyclePhaseListView) {
                            return controllers.admin.routes.ConfigurationGovernanceController
                                    .managePhase(lifeCyclePhaseListView.lifeCycleProcessId, lifeCyclePhaseListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<LifeCyclePhaseListView>() {
                        @Override
                        public String apply(LifeCyclePhaseListView lifeCyclePhaseListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationGovernanceController.deletePhase(lifeCyclePhaseListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.life_cycle_phase.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public LifeCyclePhaseListView() {
    }

    public Long lifeCycleProcessId;
    public Long id;

    public String name;

    public LifeCycleMilestone startMilestone;

    public LifeCycleMilestone endMilestone;

    public Integer gapDaysStart;

    public Integer gapDaysEnd;

    public boolean isRoadmapPhase;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param lifeCyclePhase
     *            the life cycle phase in the DB
     */
    public LifeCyclePhaseListView(LifeCyclePhase lifeCyclePhase) {

        this.id = lifeCyclePhase.id;
        this.lifeCycleProcessId = lifeCyclePhase.lifeCycleProcess.id;

        this.name = lifeCyclePhase.name;
        this.startMilestone = lifeCyclePhase.startLifeCycleMilestone;
        this.endMilestone = lifeCyclePhase.endLifeCycleMilestone;
        this.gapDaysStart = lifeCyclePhase.gapDaysStart;
        this.gapDaysEnd = lifeCyclePhase.gapDaysEnd;
        this.isRoadmapPhase = lifeCyclePhase.isRoadmapPhase;

    }
}
