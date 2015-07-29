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

import models.delivery.RequirementStatus;
import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * A requirement status list view is used to display a requirement status row in
 * a table.
 * 
 * @author Johann Kohler
 */
public class RequirementStatusListView {

    public static Table<RequirementStatusListView> templateTable = new Table<RequirementStatusListView>() {
        {

            setIdFieldName("id");

            addColumn("id", "id", "object.requirement_status.id.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("id", new ObjectFormatter<RequirementStatusListView>());

            addColumn("name", "name", "object.requirement_status.name.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("name", new ObjectFormatter<RequirementStatusListView>());

            addColumn("description", "description", "object.requirement_status.description.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("description", new ObjectFormatter<RequirementStatusListView>());

            addColumn("type", "type", "object.requirement_status.type.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("type", new ObjectFormatter<RequirementStatusListView>());

            addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("editActionLink", new StringFormatFormatter<RequirementStatusListView>(IMafConstants.EDIT_URL_FORMAT,
                    new StringFormatFormatter.Hook<RequirementStatusListView>() {
                        @Override
                        public String convert(RequirementStatusListView requirementStatusListView) {
                            return controllers.admin.routes.ConfigurationRequirementController.manageRequirementStatus(requirementStatusListView.id).url();
                        }
                    }));
            setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<RequirementStatusListView>() {
                @Override
                public String apply(RequirementStatusListView requirementStatusListView, Object value) {
                    String deleteConfirmationMessage =
                            MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                    String url = controllers.admin.routes.ConfigurationRequirementController.deleteRequirementStatus(requirementStatusListView.id).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            setEmptyMessageKey("object.requirement_status.table.empty");

        }
    };

    /**
     * Default constructor.
     */
    public RequirementStatusListView() {
    }

    public Long id;

    public String type;

    public String name;

    public String description;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param requirementStatus
     *            the requirement status in the DB
     */
    public RequirementStatusListView(RequirementStatus requirementStatus) {

        this.id = requirementStatus.id;
        this.type = Msg.get("object.requirement_status.type." + requirementStatus.type.name() + ".label");
        this.name = requirementStatus.name;
        this.description = requirementStatus.description;

    }
}
