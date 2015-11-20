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
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ListOfValuesFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.governance.LifeCycleMilestone;
import models.governance.LifeCycleMilestoneInstanceStatusType;
import models.pmo.Actor;

/**
 * An life cycle milestone list view is used to display a life cycle milestone
 * row in a table.
 * 
 * @author Johann Kohler
 */
public class LifeCycleMilestoneListView {

    public static Table<LifeCycleMilestoneListView> templateTable = new Table<LifeCycleMilestoneListView>() {
        {

            setIdFieldName("id");

            addColumn("changeOrder", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("changeOrder", new IColumnFormatter<LifeCycleMilestoneListView>() {
                @Override
                public String apply(LifeCycleMilestoneListView lifeCycleMilestoneListView, Object value) {
                    return "<a href=\""
                            + controllers.admin.routes.ConfigurationGovernanceController.changeMilestoneOrder(lifeCycleMilestoneListView.id, false).url()
                            + "\"><span class=\"glyphicons glyphicons-down-arrow\"></span></a>&nbsp;" + "<a href=\""
                            + controllers.admin.routes.ConfigurationGovernanceController.changeMilestoneOrder(lifeCycleMilestoneListView.id, true).url()
                            + "\"><span class=\"glyphicons glyphicons-up-arrow\"></span></a>";
                }
            });
            setColumnCssClass("changeOrder", IMafConstants.BOOTSTRAP_COLUMN_1);

            addColumn("shortName", "shortName", "object.life_cycle_milestone.short_name.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("shortName", new ObjectFormatter<LifeCycleMilestoneListView>());

            addColumn("name", "name", "object.life_cycle_milestone.name.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("name", new ObjectFormatter<LifeCycleMilestoneListView>());

            addColumn("description", "description", "object.life_cycle_milestone.description.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("description", new ObjectFormatter<LifeCycleMilestoneListView>());

            addColumn("type", "type", "object.life_cycle_milestone.type.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("type", new IColumnFormatter<LifeCycleMilestoneListView>() {
                @Override
                public String apply(LifeCycleMilestoneListView lifeCycleMilestoneListView, Object value) {
                    if (lifeCycleMilestoneListView.type != null) {
                        return lifeCycleMilestoneListView.type.getLabel();
                    } else {
                        return IMafConstants.DEFAULT_VALUE_EMPTY_DATA;
                    }
                }
            });

            addColumn("isReviewRequired", "isReviewRequired", "object.life_cycle_milestone.is_review_required.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("isReviewRequired", new BooleanFormatter<LifeCycleMilestoneListView>());

            addColumn("defaultStatusType", "defaultStatusType", "object.life_cycle_milestone.default_status_type.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("defaultStatusType", new IColumnFormatter<LifeCycleMilestoneListView>() {
                @Override
                public String apply(LifeCycleMilestoneListView lifeCycleMilestoneListView, Object value) {
                    return views.html.framework_views.parts.formats.display_value_holder.render(lifeCycleMilestoneListView.defaultStatusType, true).body();
                }
            });

            addColumn("isActive", "isActive", "object.life_cycle_milestone.is_active.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("isActive", new BooleanFormatter<LifeCycleMilestoneListView>());

            addColumn("approvers", "approvers", "object.life_cycle_milestone.approvers.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("approvers", new ListOfValuesFormatter<LifeCycleMilestoneListView>());

            addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("editActionLink", new StringFormatFormatter<LifeCycleMilestoneListView>(IMafConstants.EDIT_URL_FORMAT,
                    new StringFormatFormatter.Hook<LifeCycleMilestoneListView>() {
                @Override
                public String convert(LifeCycleMilestoneListView lifeCycleMilestoneListView) {
                    return controllers.admin.routes.ConfigurationGovernanceController
                            .manageMilestone(lifeCycleMilestoneListView.lifeCycleProcessId, lifeCycleMilestoneListView.id).url();
                }
            }));
            setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<LifeCycleMilestoneListView>() {
                @Override
                public String apply(LifeCycleMilestoneListView lifeCycleMilestoneListView, Object value) {
                    String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                            Msg.get("default.delete.confirmation.message"));
                    String url = controllers.admin.routes.ConfigurationGovernanceController.deleteMilestone(lifeCycleMilestoneListView.id).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            setEmptyMessageKey("object.life_cycle_milestone.table.empty");

        }
    };

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

    public List<Actor> approvers;

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
        this.approvers = lifeCycleMilestone.approvers;
        this.type = lifeCycleMilestone.type;

    }
}
