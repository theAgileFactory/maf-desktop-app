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
import models.governance.LifeCycleProcess;

/**
 * An life cycle process list view is used to display a life cycle process row
 * in a table.
 * 
 * @author Johann Kohler
 */
public class LifeCycleProcessListView {

    public static class TableDefinition {

        public Table<LifeCycleProcessListView> templateTable;

        public TableDefinition() {
            this.templateTable = getTable();
        }

        public Table<LifeCycleProcessListView> getTable() {
            return new Table<LifeCycleProcessListView>() {
                {

                    setIdFieldName("id");

                    addColumn("shortName", "shortName", "object.life_cycle_process.short_name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("shortName", new ObjectFormatter<LifeCycleProcessListView>());

                    addColumn("name", "name", "object.life_cycle_process.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<LifeCycleProcessListView>());

                    addColumn("description", "description", "object.life_cycle_process.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<LifeCycleProcessListView>());

                    addColumn("isActive", "isActive", "object.life_cycle_process.is_active.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isActive", new BooleanFormatter<LifeCycleProcessListView>());

                    addColumn("isRelease", "isRelease", "object.life_cycle_process.is_release.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isRelease", new BooleanFormatter<LifeCycleProcessListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<LifeCycleProcessListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<LifeCycleProcessListView>() {
                        @Override
                        public String convert(LifeCycleProcessListView lifeCycleProcessListView) {
                            return controllers.admin.routes.ConfigurationGovernanceController.manageLifeCycleProcess(lifeCycleProcessListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<LifeCycleProcessListView>() {
                        @Override
                        public String apply(LifeCycleProcessListView lifeCycleProcessListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationGovernanceController.deleteLifeCycleProcess(lifeCycleProcessListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    this.setLineAction(new IColumnFormatter<LifeCycleProcessListView>() {
                        @Override
                        public String apply(LifeCycleProcessListView lifeCycleProcessListView, Object value) {
                            return controllers.admin.routes.ConfigurationGovernanceController.viewLifeCycleProcess(lifeCycleProcessListView.id).url();
                        }
                    });

                    setEmptyMessageKey("object.life_cycle_process.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public LifeCycleProcessListView() {
    }

    public Long id;

    public String shortName;

    public String name;

    public String description;

    public boolean isActive;

    public boolean isRelease;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param lifeCycleProcess
     *            the life cycle process in the DB
     */
    public LifeCycleProcessListView(LifeCycleProcess lifeCycleProcess) {

        this.id = lifeCycleProcess.id;
        this.shortName = lifeCycleProcess.shortName;
        this.name = lifeCycleProcess.name;
        this.description = lifeCycleProcess.description;
        this.isActive = lifeCycleProcess.isActive;
        this.isRelease = lifeCycleProcess.isRelease;

    }
}
