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

import models.framework_models.account.SystemLevelRoleType;
import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * An role list view is used to display an role row in a table.
 * 
 * @author Johann Kohler
 */
public class RoleListView {

    public static Table<RoleListView> templateTable = new Table<RoleListView>() {
        {
            setIdFieldName("id");

            addColumn("name", "name", "object.role.name.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("name", new ObjectFormatter<RoleListView>());

            addColumn("description", "description", "object.role.description.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("description", new ObjectFormatter<RoleListView>());

            addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("editActionLink", new StringFormatFormatter<RoleListView>(IMafConstants.EDIT_URL_FORMAT,
                    new StringFormatFormatter.Hook<RoleListView>() {
                        @Override
                        public String convert(RoleListView roleListView) {
                            return controllers.admin.routes.ConfigurationController.manageRole(roleListView.id).url();
                        }
                    }));
            setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<RoleListView>() {
                @Override
                public String apply(RoleListView roleListView, Object value) {
                    String deleteConfirmationMessage =
                            MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                    String url = controllers.admin.routes.ConfigurationController.deleteRole(roleListView.id).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

        }
    };

    /**
     * Default constructor.
     */
    public RoleListView() {
    }

    public Long id;
    public String name;
    public String description;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param roleType
     *            the role type in the DB
     */
    public RoleListView(SystemLevelRoleType roleType) {
        this.id = roleType.id;
        this.name = roleType.getName();
        this.description = roleType.getDescription();
    }
}
