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

import constants.IMafConstants;
import controllers.admin.routes;
import framework.commons.DataType;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.framework_models.common.CustomAttributeGroup;

import java.text.MessageFormat;

/**
 * Table view for {@link models.framework_models.common.CustomAttributeGroup}
 *
 * @author Guillaume Petit
 */
public class CustomAttributeGroupListView {

    public static class TableDefinition {

        public Table<CustomAttributeGroupListView> templateTable;

        public TableDefinition() {
            this.templateTable = getTable();
        }

        private Table<CustomAttributeGroupListView> getTable() {
            return new Table<CustomAttributeGroupListView>() {
                {
                    setIdFieldName("id");

                    addColumn("changeOrder", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("changeOrder", (listView, value) ->
                        "<a href=\""
                        + routes.ConfigurationCustomAttributeController.changeGroupOrder(listView.id, false).url()
                        + "\"><span class=\"fa fa-arrow-down\"></span></a>&nbsp;<a href=\""
                        + routes.ConfigurationCustomAttributeController.changeGroupOrder(listView.id, true).url()
                        + "\"><span class=\"fa fa-arrow-up\"></span></a>"
                    );
                    setColumnCssClass("changeOrder", IMafConstants.BOOTSTRAP_COLUMN_1);

                    addColumn("name", "name", "object.custom_attribute_group.name.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<>());

                    addColumn("label", "label", "object.custom_attribute_group.label.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("label", new ObjectFormatter<>());

                    addColumn("editActionLink", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<>(
                            IMafConstants.EDIT_URL_FORMAT,
                            view -> routes.ConfigurationCustomAttributeController.manageGroup(view.dataTypeName, view.id).url())
                    );
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("deleteActionLink", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", (view, value) ->
                            {
                                String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                                String url = routes.ConfigurationCustomAttributeController.deleteGroup(view.id).url();
                                return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                            }
                    );
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.custom_attribute_group.table.empty");
                }
            };
        }

    }

    public CustomAttributeGroupListView() {
    }

    public Long id;
    public String name;
    public String label;
    public String dataTypeName;

    public CustomAttributeGroupListView(CustomAttributeGroup group) {
        this.id = group.id;
        this.name = group.getName();
        this.label = group.label;
        DataType dataType = DataType.getDataTypeFromClassName(group.objectType);
        this.dataTypeName = dataType != null ? dataType.getDataName() : null;
    }

}
