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
import models.pmo.OrgUnitType;

/**
 * An org unit type list view is used to display an org unit type row in a
 * table.
 * 
 * @author Johann Kohler
 */
public class OrgUnitTypeListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<OrgUnitTypeListView> templateTable;

        public TableDefinition() {
            this.templateTable = getTable();
        }

        public Table<OrgUnitTypeListView> getTable() {
            return new Table<OrgUnitTypeListView>() {
                {

                    setIdFieldName("id");

                    addColumn("refId", "refId", "object.org_unit_type.ref_id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("refId", new ObjectFormatter<OrgUnitTypeListView>());

                    addColumn("name", "name", "object.org_unit_type.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<OrgUnitTypeListView>());

                    addColumn("description", "description", "object.org_unit_type.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<OrgUnitTypeListView>());

                    addColumn("selectable", "selectable", "object.org_unit_type.selectable.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("selectable", new BooleanFormatter<OrgUnitTypeListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<OrgUnitTypeListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<OrgUnitTypeListView>() {
                        @Override
                        public String convert(OrgUnitTypeListView orgUnitTypeListView) {
                            return controllers.admin.routes.ConfigurationActorAndOrgUnitController.manageOrgUnitType(orgUnitTypeListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<OrgUnitTypeListView>() {
                        @Override
                        public String apply(OrgUnitTypeListView orgUnitTypeListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationActorAndOrgUnitController.deleteOrgUnitType(orgUnitTypeListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.org_unit_type.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public OrgUnitTypeListView() {
    }

    public Long id;

    public String name;

    public String description;

    public String refId;

    public boolean selectable;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param orgUnitType
     *            the org unit type in the DB
     */
    public OrgUnitTypeListView(OrgUnitType orgUnitType) {

        this.id = orgUnitType.id;
        this.name = orgUnitType.name;
        this.description = orgUnitType.description;
        this.refId = orgUnitType.refId;
        this.selectable = orgUnitType.selectable;

    }
}
