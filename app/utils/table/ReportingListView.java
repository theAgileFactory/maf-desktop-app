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
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.reporting.Reporting;

/**
 * A reporting list view is used to display a reporting row in a table.
 * 
 * @author Johann Kohler
 */
public class ReportingListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<ReportingListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<ReportingListView> getTable() {
            return new Table<ReportingListView>() {
                {
                    setIdFieldName("id");

                    addColumn("name", "name", "object.reporting.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<ReportingListView>());

                    addColumn("description", "description", "object.reporting.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<ReportingListView>());

                    addColumn("isPublic", "isPublic", "object.reporting.is_public.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isPublic", new BooleanFormatter<ReportingListView>());
                    setColumnCssClass("isPublic", IMafConstants.BOOTSTRAP_COLUMN_1);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<ReportingListView>(IMafConstants.CONFIG_URL_FORMAT,
                            new StringFormatFormatter.Hook<ReportingListView>() {
                        @Override
                        public String convert(ReportingListView reportingListView) {
                            return controllers.admin.routes.ReportingController.edit(reportingListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    this.setLineAction(new IColumnFormatter<ReportingListView>() {
                        @Override
                        public String apply(ReportingListView reportingListView, Object value) {
                            return controllers.core.routes.ReportingController.parametrize(reportingListView.id).url();
                        }
                    });

                    setEmptyMessageKey("object.reporting.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public ReportingListView() {
    }

    public Long id;

    public String name;
    public String description;
    public boolean isPublic;
    public boolean isActive;
    public boolean isStandard;

    /**
     * Construct a row with a DB entry.
     * 
     * @param reporting
     *            the reporting in the DB
     */
    public ReportingListView(Reporting reporting) {

        this.id = reporting.id;
        this.name = reporting.getName();
        this.description = reporting.getDescription();
        this.isPublic = reporting.isPublic;
        this.isActive = reporting.isActive;
        this.isStandard = reporting.isStandard;
    }

}
