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

import models.architecture.ApplicationBlock;
import framework.commons.IFrameworkConstants;
import framework.utils.FilterConfig;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;

/**
 * An application block list view is used to display an application block row in
 * a table.
 * 
 * @author Johann Kohler
 */
public class ApplicationBlockListView {

    public static FilterConfig<ApplicationBlockListView> filterConfig = getFilterConfig();

    /**
     * Get the filter config.
     */
    public static FilterConfig<ApplicationBlockListView> getFilterConfig() {
        return new FilterConfig<ApplicationBlockListView>() {
            {

                addColumnConfiguration("refId", "refId", "object.application_block.ref_id.label", new TextFieldFilterComponent("*"), true, false,
                        SortStatusType.UNSORTED);

                addColumnConfiguration("name", "name", "object.application_block.name.label", new TextFieldFilterComponent("*"), true, false,
                        SortStatusType.ASC);

                addColumnConfiguration("description", "description", "object.application_block.description.label", new TextFieldFilterComponent("*"), true,
                        false, SortStatusType.NONE);

                addColumnConfiguration("parent", "parent.id", "object.application_block.parent.label", new AutocompleteFilterComponent(
                        controllers.routes.JsonController.applicationBlock().url()), true, false, SortStatusType.NONE);

                addColumnConfiguration("archived", "archived", "object.application_block.archived.label", new CheckboxFilterComponent(false), true, true,
                        SortStatusType.NONE);

                addCustomAttributesColumns("id", ApplicationBlockListView.class);

            }
        };
    }

    public static Table<ApplicationBlockListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<ApplicationBlockListView> getTable() {
        return new Table<ApplicationBlockListView>() {
            {
                setIdFieldName("id");

                addColumn("refId", "refId", "object.application_block.ref_id.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("refId", new ObjectFormatter<ApplicationBlockListView>());

                addColumn("name", "name", "object.application_block.name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("name", new ObjectFormatter<ApplicationBlockListView>());

                addColumn("description", "description", "object.application_block.description.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("description", new ObjectFormatter<ApplicationBlockListView>());

                addColumn("parent", "parent", "object.application_block.parent.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("parent", new IColumnFormatter<ApplicationBlockListView>() {
                    @Override
                    public String apply(ApplicationBlockListView applicationBlockListView, Object value) {
                        if (applicationBlockListView.parent == null) {
                            return IFrameworkConstants.DEFAULT_VALUE_EMPTY_DATA;
                        } else {
                            return views.html.framework_views.parts.formats.display_object.render(applicationBlockListView.parent.getName(), false).body();
                        }

                    }
                });

                addColumn("archived", "archived", "object.application_block.archived.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("archived", new BooleanFormatter<ApplicationBlockListView>());

                addCustomAttributeColumns(ApplicationBlockListView.class);

                this.setLineAction(new IColumnFormatter<ApplicationBlockListView>() {
                    @Override
                    public String apply(ApplicationBlockListView applicationBlockListView, Object value) {
                        return controllers.core.routes.ArchitectureController.index(applicationBlockListView.id).url();
                    }
                });

                setEmptyMessageKey("object.application_block.table.empty");

            }
        };

    }

    /**
     * Default constructor.
     */
    public ApplicationBlockListView() {
    }

    public Long id;

    public boolean archived;

    public String refId;

    public String name;

    public String description;

    public ApplicationBlock parent;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param applicationBlock
     *            the application block in the DB
     */
    public ApplicationBlockListView(ApplicationBlock applicationBlock) {

        this.id = applicationBlock.id;
        this.archived = applicationBlock.archived;
        this.refId = applicationBlock.refId;
        this.name = applicationBlock.name;
        this.description = applicationBlock.description;
        this.parent = applicationBlock.parent;

    }

}
