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
import framework.utils.ISelectableValueHolder;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * An custom attribute list view is used to display a custom attribute row in a
 * table.
 * 
 * @author Johann Kohler
 */
public class CustomAttributeItemListView {

    public static Table<CustomAttributeItemListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<CustomAttributeItemListView> getTable() {
        return new Table<CustomAttributeItemListView>() {
            {
                setIdFieldName("id");

                addColumn("changeOrder", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("changeOrder", new IColumnFormatter<CustomAttributeItemListView>() {
                    @Override
                    public String apply(CustomAttributeItemListView customAttributeItemListView, Object value) {
                        return "<a href=\""
                                + controllers.admin.routes.ConfigurationCustomAttributeController
                                        .changeItemOrder(customAttributeItemListView.customAttributeId, customAttributeItemListView.id, false).url()
                                + "\"><span class=\"glyphicons glyphicons-down-arrow\"></span></a>&nbsp;" + "<a href=\""
                                + controllers.admin.routes.ConfigurationCustomAttributeController
                                        .changeItemOrder(customAttributeItemListView.customAttributeId, customAttributeItemListView.id, true).url()
                                + "\"><span class=\"glyphicons glyphicons-up-arrow\"></span></a>";
                    }
                });
                setColumnCssClass("changeOrder", IMafConstants.BOOTSTRAP_COLUMN_1);

                addColumn("id", "id", "object.custom_attribute_item.id.label", Table.ColumnDef.SorterType.NONE);

                addColumn("name", "name", "object.custom_attribute_item.name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("name", new ObjectFormatter<CustomAttributeItemListView>());

                addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("editActionLink", new StringFormatFormatter<CustomAttributeItemListView>(IMafConstants.EDIT_URL_FORMAT,
                        new StringFormatFormatter.Hook<CustomAttributeItemListView>() {
                    @Override
                    public String convert(CustomAttributeItemListView customAttributeItemListView) {
                        return controllers.admin.routes.ConfigurationCustomAttributeController
                                .manageItem(customAttributeItemListView.customAttributeId, customAttributeItemListView.id).url();
                    }
                }));
                setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<CustomAttributeItemListView>() {
                    @Override
                    public String apply(CustomAttributeItemListView customAttributeItemListView, Object value) {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = controllers.admin.routes.ConfigurationCustomAttributeController
                                .deleteItem(customAttributeItemListView.customAttributeId, customAttributeItemListView.id).url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    }
                });
                setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                setEmptyMessageKey("object.custom_attribute_definition.table.empty");

            }
        };
    }

    /**
     * Default constructor.
     */
    public CustomAttributeItemListView() {
    }

    public Long customAttributeId;
    public Long id;
    public String name;

    /**
     * Construct an item with a value holder.
     * 
     * @param customAttributeId
     *            the custom attribute definition id
     * @param valueHolder
     *            the value holder
     */
    public CustomAttributeItemListView(Long customAttributeId, ISelectableValueHolder<Long> valueHolder) {

        this.customAttributeId = customAttributeId;
        this.id = valueHolder.getValue();
        this.name = valueHolder.getName();

    }
}
