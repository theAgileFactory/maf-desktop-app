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
import models.delivery.RequirementSeverity;

/**
 * A requirement severity list view is used to display a requirement severity
 * row in a table.
 * 
 * @author Johann Kohler
 */
public class RequirementSeverityListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<RequirementSeverityListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<RequirementSeverityListView> getTable() {
            return new Table<RequirementSeverityListView>() {
                {

                    setIdFieldName("id");

                    addColumn("id", "id", "object.requirement_severity.id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("id", new ObjectFormatter<RequirementSeverityListView>());

                    addColumn("name", "name", "object.requirement_severity.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<RequirementSeverityListView>());

                    addColumn("description", "description", "object.requirement_severity.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<RequirementSeverityListView>());

                    addColumn("isBlocker", "isBlocker", "object.requirement_severity.is_blocker.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isBlocker", new BooleanFormatter<RequirementSeverityListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<RequirementSeverityListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<RequirementSeverityListView>() {
                        @Override
                        public String convert(RequirementSeverityListView requirementSeverityListView) {
                            return controllers.admin.routes.ConfigurationRequirementController.manageRequirementSeverity(requirementSeverityListView.id)
                                    .url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<RequirementSeverityListView>() {
                        @Override
                        public String apply(RequirementSeverityListView requirementSeverityListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationRequirementController.deleteRequirementSeverity(requirementSeverityListView.id)
                                    .url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.requirement_severity.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public RequirementSeverityListView() {
    }

    public Long id;

    public boolean isBlocker;

    public String name;

    public String description;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param requirementSeverity
     *            the requirement severity in the DB
     */
    public RequirementSeverityListView(RequirementSeverity requirementSeverity) {

        this.id = requirementSeverity.id;
        this.isBlocker = requirementSeverity.isBlocker;
        this.name = requirementSeverity.name;
        this.description = requirementSeverity.description;

    }
}
