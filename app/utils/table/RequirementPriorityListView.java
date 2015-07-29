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

import models.delivery.RequirementPriority;
import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * A requirement priority list view is used to display a requirement priority
 * row in a table.
 * 
 * @author Johann Kohler
 */
public class RequirementPriorityListView {

    public static Table<RequirementPriorityListView> templateTable = new Table<RequirementPriorityListView>() {
        {

            setIdFieldName("id");

            addColumn("id", "id", "object.requirement_priority.id.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("id", new ObjectFormatter<RequirementPriorityListView>());

            addColumn("name", "name", "object.requirement_priority.name.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("name", new ObjectFormatter<RequirementPriorityListView>());

            addColumn("description", "description", "object.requirement_priority.description.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("description", new ObjectFormatter<RequirementPriorityListView>());

            addColumn("isMust", "isMust", "object.requirement_priority.is_must.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("isMust", new BooleanFormatter<RequirementPriorityListView>());

            addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("editActionLink", new StringFormatFormatter<RequirementPriorityListView>(IMafConstants.EDIT_URL_FORMAT,
                    new StringFormatFormatter.Hook<RequirementPriorityListView>() {
                        @Override
                        public String convert(RequirementPriorityListView requirementPriorityListView) {
                            return controllers.admin.routes.ConfigurationRequirementController.manageRequirementPriority(requirementPriorityListView.id)
                                    .url();
                        }
                    }));
            setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<RequirementPriorityListView>() {
                @Override
                public String apply(RequirementPriorityListView requirementPriorityListView, Object value) {
                    String deleteConfirmationMessage =
                            MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                    String url = controllers.admin.routes.ConfigurationRequirementController.deleteRequirementPriority(requirementPriorityListView.id).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            setEmptyMessageKey("object.requirement_priority.table.empty");

        }
    };

    /**
     * Default constructor.
     */
    public RequirementPriorityListView() {
    }

    public Long id;

    public boolean isMust;

    public String name;

    public String description;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param requirementPriority
     *            the requirement severity in the DB
     */
    public RequirementPriorityListView(RequirementPriority requirementPriority) {

        this.id = requirementPriority.id;
        this.isMust = requirementPriority.isMust;
        this.name = requirementPriority.name;
        this.description = requirementPriority.description;

    }
}
