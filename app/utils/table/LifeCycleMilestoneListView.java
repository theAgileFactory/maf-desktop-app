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
import java.util.List;

import constants.IMafConstants;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ListOfValuesFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.governance.LifeCycleMilestone;
import models.governance.LifeCycleMilestoneInstanceStatusType;
import models.pmo.Actor;
import models.pmo.OrgUnit;

/**
 * An life cycle milestone list view is used to display a life cycle milestone
 * row in a table.
 * 
 * @author Johann Kohler
 */
public class LifeCycleMilestoneListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<LifeCycleMilestoneListView> templateTable;

        public TableDefinition() {
            this.templateTable = getTable();
        }

        public Table<LifeCycleMilestoneListView> getTable() {
            return new Table<LifeCycleMilestoneListView>() {
                {

                    setIdFieldName("id");

                    addColumn("changeOrder", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("changeOrder", (lifeCycleMilestoneListView, value) -> "<a href=\""
                            + controllers.admin.routes.ConfigurationGovernanceController.changeMilestoneOrder(lifeCycleMilestoneListView.id, false)
                                    .url()
                            + "\"><span class=\"fa fa-arrow-down\"></span></a>&nbsp;"
                            + "<a href=\"" + controllers.admin.routes.ConfigurationGovernanceController
                                    .changeMilestoneOrder(lifeCycleMilestoneListView.id, true).url()
                            + "\"><span class=\"fa fa-arrow-up\"></span></a>");
                    setColumnCssClass("changeOrder", IMafConstants.BOOTSTRAP_COLUMN_1);

                    addColumn("shortName", "shortName", "object.life_cycle_milestone.short_name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("shortName", new ObjectFormatter<>());

                    addColumn("name", "name", "object.life_cycle_milestone.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<>());

                    addColumn("description", "description", "object.life_cycle_milestone.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<>());

                    addColumn("type", "type", "object.life_cycle_milestone.type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("type", (lifeCycleMilestoneListView, value) -> {
                        if (lifeCycleMilestoneListView.type != null) {
                            return lifeCycleMilestoneListView.type.getLabel();
                        } else {
                            return IMafConstants.DEFAULT_VALUE_EMPTY_DATA;
                        }
                    });

                    addColumn("isReviewRequired", "isReviewRequired", "object.life_cycle_milestone.is_review_required.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isReviewRequired", new BooleanFormatter<>());

                    addColumn("defaultStatusType", "defaultStatusType", "object.life_cycle_milestone.default_status_type.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("defaultStatusType", (lifeCycleMilestoneListView, value) -> views.html.framework_views.parts.formats.display_value_holder.render(lifeCycleMilestoneListView.defaultStatusType, true)
                            .body());

                    addColumn("isActive", "isActive", "object.life_cycle_milestone.is_active.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isActive", new BooleanFormatter<>());

                    addColumn("actorApprovers", "actorApprovers", "object.life_cycle_milestone.actorApprovers.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("actorApprovers", new ListOfValuesFormatter<>());

                    addColumn("orgUnitApprovers", "orgUnitApprovers", "object.life_cycle_milestone.orgUnitApprovers.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("orgUnitApprovers", new ListOfValuesFormatter<>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<>(IMafConstants.EDIT_URL_FORMAT,
                            lifeCycleMilestoneListView -> controllers.admin.routes.ConfigurationGovernanceController
                                    .manageMilestone(lifeCycleMilestoneListView.lifeCycleProcessId, lifeCycleMilestoneListView.id).url()));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", (lifeCycleMilestoneListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = controllers.admin.routes.ConfigurationGovernanceController.deleteMilestone(lifeCycleMilestoneListView.id).url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.life_cycle_milestone.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public LifeCycleMilestoneListView() {
    }

    public Long lifeCycleProcessId;
    public Long id;

    public String shortName;

    public String name;

    public String description;

    public int order;

    public boolean isReviewRequired;

    public LifeCycleMilestoneInstanceStatusType defaultStatusType;

    public boolean isActive;

    public LifeCycleMilestone.Type type;

    public List<Actor> actorApprovers;

    public List<OrgUnit> orgUnitApprovers;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param lifeCycleMilestone
     *            the life cycle milestone in the DB
     */
    public LifeCycleMilestoneListView(LifeCycleMilestone lifeCycleMilestone) {

        this.id = lifeCycleMilestone.id;
        this.lifeCycleProcessId = lifeCycleMilestone.lifeCycleProcess.id;

        this.shortName = lifeCycleMilestone.shortName;
        this.name = lifeCycleMilestone.name;
        this.description = lifeCycleMilestone.description;
        this.order = lifeCycleMilestone.order;
        this.isReviewRequired = lifeCycleMilestone.isReviewRequired;
        this.defaultStatusType = lifeCycleMilestone.defaultLifeCycleMilestoneInstanceStatusType;
        this.isActive = lifeCycleMilestone.isActive;
        this.actorApprovers = lifeCycleMilestone.actorApprovers;
        this.orgUnitApprovers = lifeCycleMilestone.orgUnitApprovers;
        this.type = lifeCycleMilestone.type;

    }
}
