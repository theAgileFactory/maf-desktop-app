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
import controllers.admin.ConfigurationCustomAttributeController;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.framework_models.common.CustomAttributeDefinition;
import models.framework_models.common.ICustomAttributeValue.AttributeType;

/**
 * An custom attribute list view is used to display a custom attribute row in a
 * table.
 * 
 * @author Johann Kohler
 */
public class CustomAttributeListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<CustomAttributeListView> templateTable;

        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<CustomAttributeListView> getTable() {
            return new Table<CustomAttributeListView>() {
                {
                    setIdFieldName("id");

                    addColumn("changeOrder", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("changeOrder", new IColumnFormatter<CustomAttributeListView>() {
                        @Override
                        public String apply(CustomAttributeListView customAttributeListView, Object value) {
                            return "<a href=\""
                                    + controllers.admin.routes.ConfigurationCustomAttributeController.changeOrder(customAttributeListView.id, false).url()
                                    + "\"><span class=\"fa fa-arrow-down\"></span></a>&nbsp;" + "<a href=\""
                                    + controllers.admin.routes.ConfigurationCustomAttributeController.changeOrder(customAttributeListView.id, true).url()
                                    + "\"><span class=\"fa fa-arrow-up\"></span></a>";
                        }
                    });
                    setColumnCssClass("changeOrder", IMafConstants.BOOTSTRAP_COLUMN_1);

                    addColumn("uuid", "uuid", "object.custom_attribute_definition.uuid.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("uuid", new ObjectFormatter<CustomAttributeListView>());

                    addColumn("attributeType", "attributeType", "object.custom_attribute_definition.attribute_type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("attributeType", new IColumnFormatter<CustomAttributeListView>() {
                        @Override
                        public String apply(CustomAttributeListView customAttributeListView, Object value) {
                            return customAttributeListView.attributeType.getLabel();
                        }
                    });

                    addColumn("name", "name", "object.custom_attribute_definition.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<CustomAttributeListView>());

                    addColumn("description", "description", "object.custom_attribute_definition.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<CustomAttributeListView>());

                    addColumn("isDisplayed", "isDisplayed", "object.custom_attribute_definition.is_displayed.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isDisplayed", new BooleanFormatter<CustomAttributeListView>());

                    addColumn("displayItems", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("displayItems", new IColumnFormatter<CustomAttributeListView>() {
                        @Override
                        public String apply(CustomAttributeListView customAttributeListView, Object value) {
                            if (ConfigurationCustomAttributeController.itemizableAttributeTypes.contains(customAttributeListView.attributeType.name())) {
                                return "<a href=\"" + controllers.admin.routes.ConfigurationCustomAttributeController.items(customAttributeListView.id).url()
                                        + "\"><span class=\"fa fa-list\"></span></a>";
                            } else {
                                return null;
                            }
                        }
                    });
                    setColumnCssClass("displayItems", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("displayItems", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<CustomAttributeListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<CustomAttributeListView>() {
                        @Override
                        public String convert(CustomAttributeListView customAttributeListView) {
                            if (!ConfigurationCustomAttributeController.unauthorizedAttributeTypes.contains(customAttributeListView.attributeType.name())) {
                                return controllers.admin.routes.ConfigurationCustomAttributeController
                                        .manage(customAttributeListView.objectType, customAttributeListView.id).url();
                            } else {
                                return null;
                            }
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<CustomAttributeListView>() {
                        @Override
                        public String apply(CustomAttributeListView customAttributeListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationCustomAttributeController.delete(customAttributeListView.id).url();
                            if (!ConfigurationCustomAttributeController.unauthorizedAttributeTypes.contains(customAttributeListView.attributeType.name())) {
                                return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                            } else {
                                return null;
                            }
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.custom_attribute_definition.table.empty");

                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public CustomAttributeListView() {
    }

    public Long id;
    public String uuid;
    public String objectType;
    public AttributeType attributeType;
    public String name;
    public String description;
    public boolean isDisplayed;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param customAttributeDefinition
     *            the custom attribute definition in the DB
     */
    public CustomAttributeListView(CustomAttributeDefinition customAttributeDefinition) {
        this.id = customAttributeDefinition.id;
        this.uuid = customAttributeDefinition.uuid;
        this.objectType = customAttributeDefinition.objectType;
        this.attributeType = AttributeType.valueOf(customAttributeDefinition.attributeType);
        this.name = customAttributeDefinition.name;
        this.description = customAttributeDefinition.description;
        this.isDisplayed = customAttributeDefinition.isDisplayed;
    }
}
