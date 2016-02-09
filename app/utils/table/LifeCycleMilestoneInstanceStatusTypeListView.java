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
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.governance.LifeCycleMilestoneInstanceStatusType;

/**
 * An life cycle milestone instance status type list view is used to display a
 * status type row in a table.
 * 
 * @author Johann Kohler
 */
public class LifeCycleMilestoneInstanceStatusTypeListView {

    public static class TableDefinition {

        public Table<LifeCycleMilestoneInstanceStatusTypeListView> templateTable;

        public TableDefinition() {
            this.templateTable = getTable();
        }

        public Table<LifeCycleMilestoneInstanceStatusTypeListView> getTable() {
            return new Table<LifeCycleMilestoneInstanceStatusTypeListView>() {
                {

                    setIdFieldName("id");

                    addColumn("name", "name", "object.life_cycle_milestone_instance_status_type.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<LifeCycleMilestoneInstanceStatusTypeListView>());

                    addColumn("description", "description", "object.life_cycle_milestone_instance_status_type.description.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<LifeCycleMilestoneInstanceStatusTypeListView>());

                    addColumn("selectable", "selectable", "object.life_cycle_milestone_instance_status_type.selectable.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("selectable", new BooleanFormatter<LifeCycleMilestoneInstanceStatusTypeListView>());

                    addColumn("isApproved", "isApproved", "object.life_cycle_milestone_instance_status_type.is_approved.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isApproved", new BooleanFormatter<LifeCycleMilestoneInstanceStatusTypeListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<LifeCycleMilestoneInstanceStatusTypeListView>(
                            IMafConstants.EDIT_URL_FORMAT, new StringFormatFormatter.Hook<LifeCycleMilestoneInstanceStatusTypeListView>() {
                        @Override
                        public String convert(LifeCycleMilestoneInstanceStatusTypeListView statusTypeListView) {
                            return controllers.admin.routes.ConfigurationGovernanceController.manageStatusType(statusTypeListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<LifeCycleMilestoneInstanceStatusTypeListView>() {
                        @Override
                        public String apply(LifeCycleMilestoneInstanceStatusTypeListView statusTypeListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationGovernanceController.deleteStatusType(statusTypeListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.life_cycle_milestone_instance_status_type.table.empty");

                }
            };

        }
    }

    /**
     * Default constructor.
     */
    public LifeCycleMilestoneInstanceStatusTypeListView() {
    }

    public Long id;

    public String name;

    public String description;

    public boolean selectable;

    public boolean isApproved;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param statusType
     *            the status type in the DB
     */
    public LifeCycleMilestoneInstanceStatusTypeListView(LifeCycleMilestoneInstanceStatusType statusType) {

        this.id = statusType.id;
        this.name = statusType.name;
        this.description = statusType.description;
        this.selectable = statusType.selectable;
        this.isApproved = statusType.isApproved;

    }
}
